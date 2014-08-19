/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.R;

import java.util.Date;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardExpirationEditField extends EditText {

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
        init();
    }

    /**
     *
     */
    private void init() {
        setHint(R.string.expiration_field_hint_text);
        setSingleLine(true);
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
            String dateString = DateFormat.format(getResources().getString(R.string.expiration_field_date_format),
                                                  expirationDate).toString();
            setText(dateString);
        }
    }
}
