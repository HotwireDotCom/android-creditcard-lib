/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.animation.drawable.AnimatedScaleDrawable;
import com.hotwire.hotels.hwcclib.filter.SecurityCodeInputFilter;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardSecurityCodeEditField extends EditText {
    public static final String TAG = CreditCardSecurityCodeEditField.class.getSimpleName();

    public static final int NO_RES_ID = -1;

    private final Context mContext;
    private AnimatedScaleDrawable mAnimatedScaleDrawable;

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
        mContext = context;
        init();
    }

    /**
     *
     */
    private void init() {
        // can this be a style?
        setHint(R.string.security_code_field_hint_text);
        setHintTextColor(mContext.getResources().getColor(R.color.field_text_color_hint_default));
        setGravity(Gravity.BOTTOM);
        /*
         * This InputType combination will give us the number keypad and will allow, in conjunction with the
         * transformation method, the user to input a password but be shown the digit currently being entered before
         * masking like a password
         */
        setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                        InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        setSingleLine(true); // must set single line before transformation method
        setTransformationMethod(PasswordTransformationMethod.getInstance());

        initializeAnimatedScaleDrawable(mContext.getResources().getDrawable(R.drawable.ic_security_code_disabled));
        setCompoundDrawablesWithIntrinsicBounds(mAnimatedScaleDrawable,
                                                null,
                                                null,
                                                null);
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/

    /**
     *
     * @param newSecurityCodeResId
     */
    private void setCardTypeForField(int newSecurityCodeResId) {
        if (newSecurityCodeResId != NO_RES_ID) {
            Drawable newFieldDrawable = mContext.getResources().getDrawable(newSecurityCodeResId);
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

    /**
     *
     * @param cardIssuer
     * @param shouldAnimate
     */
    public void updateCardType(CreditCardUtilities.CardIssuer cardIssuer, boolean shouldAnimate) {
        int secResId = cardIssuer.getSecurityIconResourceId();
        if (shouldAnimate) {
            setCardTypeForField(secResId);
        }
        else {
            setSecurityResourceImage(secResId);
        }

        InputFilter secCodeFilter = new SecurityCodeInputFilter(cardIssuer.getSecurityLength());
        setFilters(new InputFilter[]{secCodeFilter});
    }

    /**
     * This is intended to be used only by the controller's restoreInstanceState method in order
     * to restore the appropriate security code image.
     * @param secCodeResId
     */
    private void setSecurityResourceImage(int secCodeResId) {
        mAnimatedScaleDrawable.setDrawable(mContext.getResources().getDrawable(secCodeResId));
        setCompoundDrawablesWithIntrinsicBounds(mAnimatedScaleDrawable,
                                                null,
                                                null,
                                                null);
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

