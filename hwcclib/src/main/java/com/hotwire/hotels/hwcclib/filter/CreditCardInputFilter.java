/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.filter;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardInputFilter extends InputFilter.LengthFilter {
    public static final String TAG = "CreditCardInputFilter";

    private static final int INVALID_THRESHOLD = 0;
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_MODULO = 1;

    private int mOffset;
    private int mMod;

    /**
     *
     * @param offset
     * @param mod
     * @param creditCardLength
     */
    public CreditCardInputFilter(int offset, int mod, int creditCardLength) {
        super(creditCardLength);

        mOffset = offset >= INVALID_THRESHOLD ? offset : DEFAULT_OFFSET;
        mMod = mod > INVALID_THRESHOLD ? mod : DEFAULT_MODULO;
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
        CharSequence sequence = super.filter(source, start, end, dest, dstart, dend);

        // if sequence is null, we have not hit the filter to limit length, apply credit card mask logic
        if (sequence == null) {
            Log.v(TAG, "offset: " + mOffset + " | source: " + source + " | start: " + start + " | end: " + end +
                                              " | dest: " + dest + " | dstart: " + dstart + " | dend: " + dend);
            StringBuilder builder = new StringBuilder();
            // used for keeping track of how many spaces were added when a number is pasted to the field
            int insertedFormatSpaces = 0;

            // Determining the length here will take into consideration, when the string has been modified by the
            // text watcher in the CreditCardNumberEditField to backspace the formatting white space characters
            // and allow for the inputfilter to reformat the string properly
            int length = dest.length();
            if (source.length() ==
                    dest.toString().replaceAll(CreditCardUtilities.REGEX_WHITESPACE,
                                               CreditCardUtilities.REGEX_WHITESPACE).length()) {
                length = 0;
            }
            Log.v(TAG, "length: " + length);
            for (int i = start; i < end; i++) {
                int check = length + i + mOffset + insertedFormatSpaces;
                int modCheck = check % mMod;
                Log.v(TAG, "i: " + i + " | added: " + insertedFormatSpaces + " | check: " + check);

                // invalid characters do not move the cursor forward. spaces when not input by the input filter
                // do not move the cursor forward
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
                else if (modCheck == 0) {
                    insertedFormatSpaces++;
                    builder.append(" ");
                }
                builder.append(source.charAt(i));
            }

            return builder.toString();
        }

        // otherwise return the length filter
        return sequence;
    }

    /***********************************************************************************
     * BEGIN DO NOT DELETE JUST IN CASE WE GO WITH MASK IMPLEMENTATION VS OFFSET/MODULO
     ***********************************************************************************/

//    private char[] mCreditCardNumberMask;
//    private int mCreditCardLength;
//
//    /**
//     *
//     *
//     * @param creditCardNumberMask
//     */
//    public CreditCardInputFilter(String creditCardNumberMask, int creditCardLength) {
//        super(creditCardLength);
//        if (creditCardNumberMask != null && !creditCardNumberMask.isEmpty()) {
//            mCreditCardNumberMask = creditCardNumberMask.toCharArray();
//        }
//        else {
//            throw new InvalidParameterException(INVALID_PARAMETER_ERROR);
//        }
//    }
//
//    @Override
//    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//        CharSequence sequence = super.filter(source, start, end, dest, dstart, dend);
//        // if sequence is null that means we are within our limits
//        Log.d(TAG, "Source: " + source + " | start: " + start + "   | end: " + end + "   | range: " +
//             (end - start) + "   | length: " + source.length());
//        Log.d(TAG, "  dest: " + dest + "   | dstart: " + dstart + " | dend: " + dend + " | range: " +
//             (dend - dstart) + " | length: " + dest.length());
//
//        // if sequence is null, we have not hit the filter to limit length, apply credit card mask logic
//        if (sequence == null) {
//            StringBuilder builder = new StringBuilder();
//            if (end - start > 0) {
//                for (int i = start; i < end; i++) {
//                    if (!isValidChar(source.charAt(i))) {
//                        return "";
//                    } else if (Character.isWhitespace(mCreditCardNumberMask[dest.length() + i])) {
//                        builder.append(" ");
//                    }
//                    builder.append(source.charAt(i));
//                }
//                return builder.toString();
//            }
//        }
//
//        // otherwise return the length filter
//        return sequence;
//    }

    /***********************************************************************************
     * END DO NOT DELETE JUST IN CASE WE GO WITH MASK IMPLEMENTATION VS OFFSET/MODULO
     ***********************************************************************************/
}
