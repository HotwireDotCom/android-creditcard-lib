/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hotwire.hotels.hwcclib.CreditCardController;
import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardExpirationEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardNumberEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardSecurityCodeEditField;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardModule extends LinearLayout {

    private static final int DEFAULT_CHILD_WEIGHT = 1;
    private static final int MAX_CHILDREN = 2;

    private LinearLayout mHorizontalLayout;
    private CreditCardNumberEditField mCreditCardNumber;
    private CreditCardExpirationEditField mCreditCardExpiration;
    private CreditCardSecurityCodeEditField mCreditCardSecurityCode;
    private CreditCardController mCreditCardController;

    /**
     *
     * @param context
     */
    public CreditCardModule(Context context) {
        this(context, null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public CreditCardModule(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CreditCardModule(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     *
     * @param creditCardModelCompleteListener
     */
    public void setCreditCardModelCompleteListener(CreditCardController.CreditCardModelCompleteListener
                                                           creditCardModelCompleteListener) {
        mCreditCardController.setCreditCardModelCompleteListener(creditCardModelCompleteListener);
    }

    /**
     *
     * @return
     */
    public boolean isComplete() {
        return mCreditCardController.isComplete();
    }

    /**
     *
     * @param context
     */
    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        mHorizontalLayout = new LinearLayout(context);
        mHorizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        mCreditCardNumber = new CreditCardNumberEditField(context);
        mCreditCardExpiration = new CreditCardExpirationEditField(context);
        mCreditCardSecurityCode = new CreditCardSecurityCodeEditField(context);

        mCreditCardController = new CreditCardController(context, mCreditCardNumber,
                mCreditCardExpiration, mCreditCardSecurityCode);

        addView(mCreditCardNumber, getDefaultLayoutParams());
        mHorizontalLayout.addView(mCreditCardExpiration, getWeightedLayoutParams());
        mHorizontalLayout.addView(mCreditCardSecurityCode, getWeightedLayoutParams());
        addView(mHorizontalLayout);
    }

    /**
     * Override the addView method to ensure no additional children are being added to the parent module
     *
     * @param child child view
     * @param index index
     */
    @Override
    public void addView(View child, int index) {
        if (getChildCount() > MAX_CHILDREN) {
            throw new IllegalStateException(getResources().getString(R.string.error_max_children));
        }
        super.addView(child, index);
    }

    /**
     * Override the addView method to ensure no additional children are being added to the parent module
     *
     * @param child child view
     * @param width width of child
     * @param height height of child
     */
    @Override
    public void addView(View child, int width, int height) {
        if (getChildCount() > MAX_CHILDREN) {
            throw new IllegalStateException(getResources().getString(R.string.error_max_children));
        }
        super.addView(child, width, height);
    }

    /**
     * Override the addView method to ensure no additional children are being added to the parent module
     *
     * @param child child view
     * @param params child layout params
     */
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > MAX_CHILDREN) {
            throw new IllegalStateException(getResources().getString(R.string.error_max_children));
        }
        super.addView(child, params);
    }

    /**
     * Override the addView method to ensure no additional children are being added to the parent module
     *
     * @param child child view
     * @param index index
     * @param params child layout params
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > MAX_CHILDREN) {
            throw new IllegalStateException(getResources().getString(R.string.error_max_children));
        }
        super.addView(child, index, params);
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/
    private LayoutParams getDefaultLayoutParams() {
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        return params;
    }

    private LinearLayout.LayoutParams getWeightedLayoutParams() {
        LinearLayout.LayoutParams params = new LayoutParams(0 /* 0dp */,
                                                            LayoutParams.WRAP_CONTENT,
                                                            DEFAULT_CHILD_WEIGHT);

        return params;
    }

    public CreditCardController getCreditCardController() {
        return mCreditCardController;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        mCreditCardController.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreSavedInstanceState(Bundle savedInstanceState) {
        mCreditCardController.onRestoreSavedInstanceState(savedInstanceState);
    }

    /******************************
     * Unit Test Helper Methods
     ******************************/

    CreditCardNumberEditField getCreditCardNumberEditField() {
        return mCreditCardNumber;
    }

    CreditCardExpirationEditField getCreditCardExpirationEditField() {
        return mCreditCardExpiration;
    }

    CreditCardSecurityCodeEditField getCreditCardSecurityCodeEditField() {
        return mCreditCardSecurityCode;
    }

    CreditCardUtilities.CardIssuer getCardIssuer() {
        return mCreditCardController.getCardIssuer();
    }
}
