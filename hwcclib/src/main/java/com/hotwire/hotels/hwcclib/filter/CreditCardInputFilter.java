/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.filter;

import android.text.InputFilter;
import android.text.Spanned;

import java.security.InvalidParameterException;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardInputFilter implements InputFilter {

    private static final String INVALID_PARAMETER_ERROR = "Provided Credit Card Mask is either null or empty";
    private String mCreditCardNumberMask;

    /**
     *
     *
     * @param creditCardNumberMask
     */
    public CreditCardInputFilter(String creditCardNumberMask) {
        if (creditCardNumberMask != null && !creditCardNumberMask.isEmpty()) {
            mCreditCardNumberMask = creditCardNumberMask;
        }
        else {
            throw new InvalidParameterException(INVALID_PARAMETER_ERROR);
        }
    }

    /**
     *
     *
     * @param source
     * @param start
     * @param end
     * @param dest
     * @param dstart
     * @param dend
     * @return
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        return null;
    }
}
