/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardNumberEditField extends EditText {

    private static final int COMPOUND_DRAWABLE_LEFT = 0;

    /**
     *
     * @param context
     */
    public CreditCardNumberEditField(Context context) {
        this(context, null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public CreditCardNumberEditField(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CreditCardNumberEditField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     *
     */
    private void init() {
        // can this be a style?
        setHint(R.string.credit_card_field_hint_text);
        setGravity(Gravity.BOTTOM);
        setSingleLine(true);
        // for the Credit card field we do not want to have suggestions from keyboards
        // this makes the inputfilter very hard to deal with
        setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.compound_drawable_padding_default));
        setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_credit_card_generic),
                null,
                null,
                null);

        // CODE BELOW THIS LINE NEEDS TO BE DELETED, THIS IS ONLY FOR INITIAL TESTING
        int cardOffset = getResources().getInteger(R.integer.credit_card_offset_visa);
        int cardModulo = getResources().getInteger(R.integer.credit_card_modulo_visa);
        int cardFieldLen = getResources().getInteger(R.integer.credit_card_len_visa_formatted);

        InputFilter filter = new CreditCardInputFilter(cardOffset, cardModulo, cardFieldLen);
        setFilters(new InputFilter[] {filter});
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/
}
