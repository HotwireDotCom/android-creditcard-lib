/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.R;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardSecurityCodeEditField extends EditText {

    /**
     *
     * @param context
     */
    public CreditCardSecurityCodeEditField(Context context) {
        this(context, null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public CreditCardSecurityCodeEditField(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CreditCardSecurityCodeEditField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     *
     */
    private void init() {
        // can this be a style?
        setHint(R.string.security_code_field_hint_text);
        setGravity(Gravity.BOTTOM);
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        setSingleLine(true); // must set single line before transformation method
        setTransformationMethod(new PasswordTransformationMethod());
        setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_security_code_disabled),
                null,
                null,
                null);
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/
}

