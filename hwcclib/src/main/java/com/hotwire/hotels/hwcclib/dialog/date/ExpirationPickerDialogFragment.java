/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.dialog.date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.hotwire.hotels.hwcclib.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ahobbs on 8/12/14.
 */
public class ExpirationPickerDialogFragment extends DialogFragment {
    public static final String TAG = "DatePickerDialogFragment";

    private static final String TITLE_KEY = "com.hotwire.hotels.hwcclib.dialog.date.title_key";
    private static final String DATE_KEY = "com.hotwire.hotels.hwcclib.dialog.date.date_key";
    private static final String MONTH_PICKER_KEY = "com.hotwire.hotels.hwcclib.dialog.date.month_picker_key";
    private static final String YEAR_PICKER_KEY = "com.hotwire.hotels.hwcclib.dialog.date.year_picker_key";

    private ExpirationPickerListener mExpirationPickerListener;
    private NumberPicker mNumberPickerMonth;
    private NumberPicker mNumberPickerYear;

    private String[] mDisplayMonths;
    private String[] mDisplayYears;

    private int mTitleResource;
    private int mMonthPickerValue;
    private int mYearPickerValue;

    /**
     *
     */
    public ExpirationPickerDialogFragment() {
        // intentionally empty
    }

    /**
     *
     * @param titleResource
     * @param currentExpiration
     * @return
     */
    public static ExpirationPickerDialogFragment newInstance(int titleResource, Date currentExpiration) {
        ExpirationPickerDialogFragment dialogFragment = new ExpirationPickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleResource);
        args.putLong(DATE_KEY, currentExpiration.getTime());
        dialogFragment.setArguments(args);

        return dialogFragment;
    }

    /**
     *
     * @return
     */
    public static ExpirationPickerDialogFragment newInstance() {
        return newInstance(R.string.expiration_picker_default_title, new Date());
    }

    /**
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Initialize NumberPicker backing data
        if (mDisplayMonths == null) {
            mDisplayMonths = getResources().getStringArray(R.array.expiration_picker_display_months);
        }

        if (mDisplayYears == null) {
            mDisplayYears =
                    generatePickerYears(getResources().getInteger(R.integer.expiration_picker_max_years));
        }

        // if savedInstance is non-null, restore the state from the bundle
        if (savedInstanceState != null) {
            mTitleResource = savedInstanceState.getInt(TITLE_KEY);
            mMonthPickerValue = savedInstanceState.getInt(MONTH_PICKER_KEY);
            mYearPickerValue = savedInstanceState.getInt(YEAR_PICKER_KEY);
        }
        else {
            mTitleResource = getArguments().getInt(TITLE_KEY);
            long dateMs = getArguments().getLong(DATE_KEY);
            mMonthPickerValue = getValueFromDate(dateMs, Calendar.MONTH);
            mYearPickerValue = getValueFromDate(dateMs, Calendar.YEAR);
        }

        setCancelable(false);

        // Inflate the view to be used in the AlertDialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.expiration_picker_layout, null);

        // Setup each of the NumberPickers to display the correct values
        mNumberPickerMonth = getNumberPickerView(v, R.id.expiration_picker_month, mMonthPickerValue, mDisplayMonths);
        mNumberPickerYear = getNumberPickerView(v, R.id.expiration_picker_year, mYearPickerValue, mDisplayYears);

        // Setup value changed listeners to store the values scrolled to by each NumberPicker
        mNumberPickerMonth.setOnValueChangedListener(getMonthOnValueChangeListener());
        mNumberPickerYear.setOnValueChangedListener(getYearOnValueChangeListener());

        // Create the Dialog builder, and build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitleResource)
               .setView(v)
               .setPositiveButton(R.string.expiration_picker_button_positive, getPositiveClickListener())
               .setNeutralButton(R.string.expiration_picker_button_neutral, getNeutralClickListener());

        return builder.create();
    }

    /**
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // store the state of the dialog
        outState.putInt(TITLE_KEY, mTitleResource);
        outState.putInt(MONTH_PICKER_KEY, mNumberPickerMonth.getValue());
        outState.putInt(YEAR_PICKER_KEY, mNumberPickerYear.getValue());
    }

    /**
     * This is a hack for a known bug involving the dismissal of a dialog on a screen orientation change
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        if (mExpirationPickerListener != null) {
            mExpirationPickerListener.onDestroy();
        }
        super.onDestroyView();
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/

    public void setDatePickerListener(ExpirationPickerListener listener) {
        if (listener != null) {
            mExpirationPickerListener = listener;
        }
    }

    /**
     *
     * @param v
     * @param numberPickerResourceId
     * @param currentValue
     * @param displayValues
     * @return
     */
    private NumberPicker getNumberPickerView(View v,
                                             int numberPickerResourceId,
                                             int currentValue,
                                             String[] displayValues) {
        NumberPicker numberPicker = (NumberPicker) v.findViewById(numberPickerResourceId);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayValues.length - 1);
        numberPicker.setDisplayedValues(displayValues);
        numberPicker.setValue(currentValue);
        // prevent the display values from being editable
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setWrapSelectorWheel(false);

        return numberPicker;
    }

    /**
     *
     * @param selectedDate
     */

    private void callPositiveListener(Date selectedDate) {
        if (mExpirationPickerListener != null) {
            mExpirationPickerListener.onExpirationDateSelected(selectedDate);
        }
    }

    /**
     *
     */
    private void callNegativeListener() {
        if (mExpirationPickerListener != null) {
            mExpirationPickerListener.onDialogPickerCanceled();
        }
    }

    /**
     *
     * @return
     */
    private Date buildDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, mMonthPickerValue);
        int year = Integer.valueOf(mDisplayYears[mYearPickerValue]);
        cal.set(Calendar.YEAR, year);

        return cal.getTime();
    }

    /**
     *
     * @param dateMs
     * @param calendarValue
     * @return
     */
    private int getValueFromDate(long dateMs, int calendarValue) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMs);
        switch(calendarValue) {
            case Calendar.MONTH:
                // this should reflect the same values that are used for the picker
                return cal.get(calendarValue);
            case Calendar.YEAR:
                String calYear = String.valueOf(cal.get(calendarValue));
                for (int i = 0; i < mDisplayYears.length; i++) {
                    if (calYear.equals(mDisplayYears[i])) {
                        return i;
                    }
                }
                break;
            default:
                break;
        }

        return 0;
    }

    /**
     *
     * @return
     */
    private DialogInterface.OnClickListener getPositiveClickListener() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Date date = buildDate();
                callPositiveListener(date);
                dialog.dismiss();
            }
        };

        return listener;
    }

    /**
     *
     * @return
     */
    private DialogInterface.OnClickListener getNeutralClickListener() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callNegativeListener();
                dialog.dismiss();
            }
        };

        return listener;
    }

    /**
     *
     * @return
     */
    private NumberPicker.OnValueChangeListener getMonthOnValueChangeListener() {
        return new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker view, int oldVal, int newVal) {
                mMonthPickerValue = view.getValue();
            }
        };
    }

    /**
     *
     * @return
     */
    private NumberPicker.OnValueChangeListener getYearOnValueChangeListener() {
        return new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker view, int oldVal, int newVal) {
                mYearPickerValue = view.getValue();
            }
        };
    }

    /**
     *
     * @param maxYearsOffset
     * @return
     */
    private String[] generatePickerYears(int maxYearsOffset) {
        Calendar cal = Calendar.getInstance();
        List<String> listOfYears = new ArrayList<String>();

        for (int i = 0; i < maxYearsOffset; i++) {
            listOfYears.add(String.valueOf(cal.get(Calendar.YEAR) + i));
        }
        return listOfYears.toArray(new String[]{});
    }
}
