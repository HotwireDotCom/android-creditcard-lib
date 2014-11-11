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
 * DialogFragment for selecting the expiration date for a Credit Card.
 */
public class ExpirationPickerDialogFragment extends DialogFragment {
    public static final String TAG = ExpirationPickerDialogFragment.class.getSimpleName();

    /**************
     * BUNDLE KEYS
     **************/
    private static final String TITLE_KEY = ExpirationPickerDialogFragment.class.getCanonicalName() + ".title_key";
    private static final String DATE_KEY = ExpirationPickerDialogFragment.class.getCanonicalName() + ".date_key";
    private static final String MONTH_PICKER_KEY = ExpirationPickerDialogFragment.class.getCanonicalName() + ".month_picker_key";
    private static final String YEAR_PICKER_KEY = ExpirationPickerDialogFragment.class.getCanonicalName() + ".year_picker_key";

    /* Value to override expiration date day with.
     * Expiration dates are not checked against the day, only month and year.
     */
    private static final int FIRST_OF_THE_MONTH = 1;

    // Listener to call back into when a selection is made
    private ExpirationPickerListener mExpirationPickerListener;

    // Pickers for month and year
    private NumberPicker mNumberPickerMonth;
    private NumberPicker mNumberPickerYear;

    // Display values for months and years
    private String[] mDisplayMonths;
    private String[] mDisplayYears;

    // Title of the dialog fragment
    private int mTitleResource;

    // Stored selected values from the dialog
    private int mMonthPickerValue;
    private int mYearPickerValue;

    public ExpirationPickerDialogFragment() {
        // intentionally empty per Android documentation
    }

    /**
     * Create a dialog fragment with the provided title resource id, and initial expiration date.
     *
     * @param titleResource resourceId of the title to be displayed with the dialog.
     * @param currentExpiration expiration date to set the number pickers to when instantiated.
     * @return an ExpirationPickerDialogFragment.
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
     * Create a dialog fragment with a default title, and today's date as the expiration.
     *
     * @return an ExpirationPickerDialogFragment.
     */
    public static ExpirationPickerDialogFragment newInstance() {
        return newInstance(R.string.expiration_picker_default_title, new Date());
    }

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
     * Store the title resource used, the currently selected value for month, and the currently selected value for
     * year.
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
     * This is a hack for a known bug involving the dismissal of a dialog on a screen orientation change.
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

    /**
     * Sets the listener to call back to, when a selection is made, or cancel is pressed.
     *
     * @param listener listener to call when there is user interaction.
     */
    public void setDatePickerListener(ExpirationPickerListener listener) {
        if (listener != null) {
            mExpirationPickerListener = listener;
        }
    }

    /**
     * On a positive click of the dialog, call this method. Build a date from the selected NumberPicker values,
     * make a call to the ExpirationPickerListener, and dismiss the dialog.
     *
     * @param dialog the current dialog.
     */
    public void positiveClick(DialogInterface dialog) {
        Date date = buildDate();
        callPositiveListener(date);
        dialog.dismiss();
    }

    /**
     * On a neutral click of the dialog, call this method. Make a call to the ExpirationPickerListener, and dismiss
     * the dialog.
     *
     * @param dialog the current dialog.
     */
    public void neutralClick(DialogInterface dialog) {
        callNegativeListener();
        dialog.dismiss();
    }

    /**
     * Creates and initializes a NumberPicker view.
     *
     * @param v current dialog fragment view.
     * @param numberPickerResourceId resource id of the.
     * @param currentValue number picker's current value.
     * @param displayValues display values to show in the number picker.
     * @return a NumberPicker with proper slection and display value.
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
     * Calls the listener when a date is selected if non-null.
     *
     * @param selectedDate date that was selected from the picker.
     */

    private void callPositiveListener(Date selectedDate) {
        if (mExpirationPickerListener != null) {
            mExpirationPickerListener.onExpirationDateSelected(selectedDate);
        }
    }

    /**
     * Calls the listener when cancel is selected if non-null.
     */
    private void callNegativeListener() {
        if (mExpirationPickerListener != null) {
            mExpirationPickerListener.onDialogPickerCanceled();
        }
    }

    /**
     * Builds a date object from the values of the NumberPickers. Defaults the date to the first day of the month.
     *
     * @return a Date object representing the expiration date for the credit card.
     */
    private Date buildDate() {
        Calendar cal = Calendar.getInstance();
        // override the date to be the first of the month, this is not checked when evaluating expiration
        cal.set(Calendar.DATE, FIRST_OF_THE_MONTH);
        cal.set(Calendar.MONTH, mMonthPickerValue);
        int year = Integer.valueOf(mDisplayYears[mYearPickerValue]);
        cal.set(Calendar.YEAR, year);

        return cal.getTime();
    }

    /**
     * Based on calendarValue (Month or year), get the value from the provided date in milliseconds.
     *
     * @param dateMs a date in milliseconds.
     * @param calendarValue calendar value to extract from the date in milliseconds.
     * @return the Month or Year value from dateMs or 0 if calendarValue is not MONTH or YEAR.
     */
    private int getValueFromDate(long dateMs, int calendarValue) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMs);
        // override the date to be the first of the month, this is not checked when evaluating expiration
        cal.set(Calendar.DATE, FIRST_OF_THE_MONTH);
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
     * Method to setup positive click listener for the dialog.
     *
     * @return an OnClickListener for the positive click.
     */
    private DialogInterface.OnClickListener getPositiveClickListener() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positiveClick(dialog);
            }
        };

        return listener;
    }

    /**
     * Method to setup neutral click listener for the dialog.
     *
     * @return an OnClickListener for the neutral click.
     */
    private DialogInterface.OnClickListener getNeutralClickListener() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                neutralClick(dialog);
            }
        };

        return listener;
    }

    /**
     * Method to setup the value change listener for the month NumberPicker. Updates mMonthPickerValue on
     * each change of the picker.
     *
     * @return an OnValueChangeListener for the month NumberPicker.
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
     * Method to setup the value change listener for the year NumberPicker. Updates mYearPickerValue on
     * each change of the picker.
     *
     * @return an OnValueChangeListener for the year NumberPicker.
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
     * Creates the display values for the year NumberPicker. Using the maxYearsOffset, create a String[] of values
     * between today's year to today's year + maxYearsOffset.
     *
     * @param maxYearsOffset number of years into the future to generate display values for.
     * @return a String[] of display values for the year NumberPicker.
     */
    private String[] generatePickerYears(int maxYearsOffset) {
        Calendar cal = Calendar.getInstance();
        List<String> listOfYears = new ArrayList<String>();

        for (int i = 0; i < maxYearsOffset; i++) {
            listOfYears.add(String.valueOf(cal.get(Calendar.YEAR) + i));
        }
        return listOfYears.toArray(new String[]{});
    }

    /*****************
     * Helper Methods
     *****************/

    public NumberPicker getNumberPickerYear() {
        return mNumberPickerYear;
    }

    public NumberPicker getNumberPickerMonth() {
        return mNumberPickerMonth;
    }
}
