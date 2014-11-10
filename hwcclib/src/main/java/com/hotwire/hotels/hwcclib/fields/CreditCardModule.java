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
 * Module that can be added to an xml layout file that will have a working credit card library.
 *
 * In a layout.xml file adding the following lines are all that is needed to get started
 *
 *     <com.hotwire.hotels.hwcclib.fields.CreditCardModule
 *          android:id="@+id/credit_card_module"
 *          android:layout_width="match_parent"
 *          android:layout_height="wrap_content"/>
 *
 */
public class CreditCardModule extends LinearLayout {

    private static final int DEFAULT_CHILD_WEIGHT = 1;

    // This module can only support a total of children
    private static final int MAX_CHILDREN = 2;

    // Layout that holds the expiration and security code fields
    private LinearLayout mHorizontalLayout;
    // Credit card number entry field
    private CreditCardNumberEditField mCreditCardNumber;
    // Expiration date entry field
    private CreditCardExpirationEditField mCreditCardExpiration;
    // Security code entry field
    private CreditCardSecurityCodeEditField mCreditCardSecurityCode;
    // The controller to wire up all the fields, and houses all of the logic
    private CreditCardController mCreditCardController;

    public CreditCardModule(Context context) {
        this(context, null);
    }

    public CreditCardModule(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Overridden constructor from LinearLayout that will also initialize the module, layout the views, and
     * initializes the CreditCardController.
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
     * Method to initialize the module. Lays out views and initializes and stores a reference
     * to the CreditCardController.
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

    /**
     * A pass-through method to the controller to add a listener for completeness of the credit card model.
     *
     * @param creditCardModelCompleteListener listener.
     */
    public void setCreditCardModelCompleteListener(CreditCardController.CreditCardModelCompleteListener
                                                           creditCardModelCompleteListener) {
        mCreditCardController.setCreditCardModelCompleteListener(creditCardModelCompleteListener);
    }

    /**
     * A pass-through method to the controller to check for completeness of a credit card.
     *
     * @return true if the all credit card related data is complete and valid (credit card number, expiration date,
     *         and security code). false otherwise.
     */
    public boolean isComplete() {
        return mCreditCardController.isComplete();
    }

    /**
     * Getter for the CreditCardController.
     *
     * @return reference to the CreditCardController.
     */
    public CreditCardController getCreditCardController() {
        return mCreditCardController;
    }

    /**
     * A pass-through method to the controller to save the current state. This is mainly used when the module
     * is added to a layout, and only a reference to the module is used.
     *
     * @param savedInstanceState
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        mCreditCardController.onSaveInstanceState(savedInstanceState);
    }

    /**
     * A pass-through method to the controller to restore the saved state. This is mainly used when the module
     * is added to a layout, and only a reference to the module is used.
     *
     * @param savedInstanceState
     */
    public void onRestoreSavedInstanceState(Bundle savedInstanceState) {
        mCreditCardController.onRestoreSavedInstanceState(savedInstanceState);
    }

    /**
     * Getting for the credit card number field.
     *
     * @return CreditCardNumberEditField reference.
     */
    public CreditCardNumberEditField getCreditCardNumberEditField() {
        return mCreditCardNumber;
    }

    /**
     * Getter for the credit card expiration field.
     *
     * @return CreditCardExpirationEditField reference.
     */
    public CreditCardExpirationEditField getCreditCardExpirationEditField() {
        return mCreditCardExpiration;
    }

    /**
     * Getter for the credit card security code field.
     *
     * @return CreditCardSecurityCode reference.
     */
    public CreditCardSecurityCodeEditField getCreditCardSecurityCodeEditField() {
        return mCreditCardSecurityCode;
    }

    /**
     * A pass-through method to the controller to get the current card issuer.
     *
     * @return the current CardIssuer.
     */
    public CreditCardUtilities.CardIssuer getCardIssuer() {
        return mCreditCardController.getCardIssuer();
    }

    /**
     * Method to get the default layout parameters for a LinearLayout
     *
     * @return LinearLayout.LayoutParams with default settings
     */
    private LayoutParams getDefaultLayoutParams() {
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        return params;
    }

    /**
     * Method to get the layout parameters for a LinearLayout with child weight
     *
     * @return LinearLayout.LayoutParams with child weight
     */
    private LinearLayout.LayoutParams getWeightedLayoutParams() {
        LinearLayout.LayoutParams params = new LayoutParams(0 /* 0dp */,
                                                            LayoutParams.WRAP_CONTENT,
                                                            DEFAULT_CHILD_WEIGHT);

        return params;
    }
}
