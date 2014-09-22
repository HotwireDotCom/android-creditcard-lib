/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardExpirationEditField extends EditText {
    public static String TAG = CreditCardExpirationEditField.class.getSimpleName();

    public static final int NO_RES_ID = -1;

    private final Context mContext;
    /**
     *
     * @param context
     */
    public CreditCardExpirationEditField(Context context) {
        this(context, null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public CreditCardExpirationEditField(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CreditCardExpirationEditField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    /**
     *
     */
    private void init() {
        setHint(R.string.expiration_field_hint_text);
        setHintTextColor(mContext.getResources().getColor(R.color.field_text_color_hint_default));
        setGravity(Gravity.BOTTOM);
        setSingleLine(true);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setKeyListener(null);
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/

    /**
     *
     * @param expirationDate
     */
    public void setExpirationDate(Date expirationDate) {
        if (expirationDate != null) {
            String dateFormat = getResources().getString(R.string.expiration_field_date_format);
            String dateString = CreditCardUtilities.getFormattedDate(dateFormat, expirationDate);

            setText(dateString);
        }
    }

    /**
     *
     */
    public void clearErrors() {
        setError(null);
        setTextColor(mContext.getResources().getColor(R.color.field_text_color_default));
    }

    /**
     *
     */
    public void setErrorState() {
        setErrorState(NO_RES_ID);
    }

    /**
     *
     * @param errorMessageResId
     */
    public void setErrorState(int errorMessageResId) {
        if (mContext != null && errorMessageResId != NO_RES_ID) {
            setErrorState(mContext.getString(errorMessageResId));
        }
        else {
            setErrorState(null);
        }
    }

    /**
     *
     * @param errorMessage
     */
    public void setErrorState(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            setError(errorMessage);
        }

        if (mContext != null) {
            setTextColor(mContext.getResources().getColor(R.color.field_text_color_error));
        }
    }
}
