/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.animation.drawable.AnimatedScaleDrawable;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardNumberEditField extends EditText implements TextWatcher {
    public static final String TAG = CreditCardNumberEditField.class.getSimpleName();

    public static final int NO_RES_ID = -1;

    private final Context mContext;
    private AnimatedScaleDrawable mAnimatedScaleDrawable;



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
        mContext = context;
        init();
    }

    /**
     *
     */
    private void init() {
        // can this be a style?
        setHint(R.string.credit_card_field_hint_text);
        setHintTextColor(mContext.getResources().getColor(R.color.field_text_color_hint_default));
        setGravity(Gravity.BOTTOM);
        setSingleLine(true);
        // for the Credit card field we do not want to have suggestions from keyboards
        // this makes the inputfilter very hard to deal with
        setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_NUMBER);

        initializeAnimatedScaleDrawable(mContext.getResources().getDrawable(R.drawable.ic_credit_card_generic));
        setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.compound_drawable_padding_default));
        setCompoundDrawablesWithIntrinsicBounds(mAnimatedScaleDrawable,
                                                null,
                                                null,
                                                null);
        addTextChangedListener(this);
    }

    /**
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // no op
    }

    /**
     *
     * @param text
     * @param start
     * @param lengthBefore
     * @param lengthAfter
     */
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    /**
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            if (Character.isWhitespace(s.charAt(s.length() - 1))) {
                String goodString = new String(s.toString().trim());
                goodString = CreditCardUtilities.getCleanString(goodString);
                Log.d(TAG, "Replacing: " + s + " with: " + goodString);
                s.replace(0, s.length(), goodString, 0, goodString.length());
            }
        }
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/

    /**
     *
     * @return
     */
    public String getRawCreditCardNumber() {
        String currentText = getText().toString();
        return CreditCardUtilities.getCleanString(currentText);
    }

    /**
     *
     * @param newCardTypeResId
     */
    public void setCardTypeForField(int newCardTypeResId) {
        if (newCardTypeResId != NO_RES_ID) {
            Drawable newFieldDrawable = mContext.getResources().getDrawable(newCardTypeResId);
            mAnimatedScaleDrawable.startDrawableTransition(newFieldDrawable);
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
        if (errorMessageResId != NO_RES_ID) {
            setError(mContext.getString(errorMessageResId));
        }
        setTextColor(mContext.getResources().getColor(R.color.field_text_color_error));
    }

    /**
     *
     * @param cardIssuer
     */
    public void updateCardType(CreditCardUtilities.CardIssuer cardIssuer) {
        InputFilter creditCardNumFilter = new CreditCardInputFilter(cardIssuer.getOffset(),
                cardIssuer.getModulo(),
                cardIssuer.getFormattedLength());
        setFilters(new InputFilter[]{creditCardNumFilter});
        setCardTypeForField(cardIssuer.getIconResourceId());
    }

    /**
     *
     * @param drawable
     */
    private void initializeAnimatedScaleDrawable(Drawable drawable) {
        mAnimatedScaleDrawable = new AnimatedScaleDrawable(drawable);
        int duration = mContext.getResources().getInteger(R.integer.credit_card_field_animation_duration_ms);
        mAnimatedScaleDrawable.setDuration(duration);
    }
}
