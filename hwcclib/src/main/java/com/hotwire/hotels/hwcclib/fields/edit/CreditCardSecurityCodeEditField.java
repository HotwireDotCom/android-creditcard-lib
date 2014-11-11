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
 * EditText field for credit card security code entry.
 */
public class CreditCardSecurityCodeEditField extends EditText {
    public static final String TAG = CreditCardSecurityCodeEditField.class.getSimpleName();

    private final Context mContext;
    private AnimatedScaleDrawable mAnimatedScaleDrawable;

    public CreditCardSecurityCodeEditField(Context context) {
        this(context, null);
    }

    public CreditCardSecurityCodeEditField(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     * Overridden constructor from EditText that will also initialize the field.
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
     * Method to initialize the field when created.
     */
    private void init() {
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
     * Sets the new card type for security code on the field, if a valid resourceId.
     *
     * @param newSecurityCodeResId resourceId for the new security code card image for the field.
     */
    private void setCardTypeForField(int newSecurityCodeResId) {
        if (newSecurityCodeResId != CreditCardUtilities.NO_RES_ID) {
            Drawable newFieldDrawable = mContext.getResources().getDrawable(newSecurityCodeResId);
            mAnimatedScaleDrawable.startDrawableTransition(newFieldDrawable);
        }
    }

    /**
     * Clears the error state of the field. Removes the error, and changes the text color back to default.
     */
    public void clearErrors() {
        setError(null);
        setTextColor(mContext.getResources().getColor(R.color.field_text_color_default));
    }

    /**
     * Sets the error state on the field with no message.
     */
    public void setErrorState() {
        setErrorState(CreditCardUtilities.NO_RES_ID);
    }

    /**
     * Sets the error state on the field with the message provided as a resourceId. If context is null or the
     * errorMessageResId isn't valid.
     *
     * @param errorMessageResId resourceId of the message to be displayed in the error state.
     */
    public void setErrorState(int errorMessageResId) {
        if (mContext != null && errorMessageResId != CreditCardUtilities.NO_RES_ID) {
            setErrorState(mContext.getString(errorMessageResId));
        }
        else {
            setErrorState(null);
        }
    }

    /**
     * Sets the error state on the field with the errorMessage provided as a string. If context is null or the string
     * is null/empty, still attempt to change text color to error if context is non-null.
     *
     * @param errorMessage error message string to be displayed in the error state.
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
     * Normally called from the controller when a card type has changed. Updates the security code card type image,
     * and applies the proper InputFilter for that card type.
     *
     * @param cardIssuer which card issues (eg. Visa, MasterCard, etc).
     * @param shouldAnimate boolean that determines if the animation between card types should take place.
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
     *
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
     * Initializes the AnimatedScaleDrawable on the field with provided drawable. Sets the default animation time.
     *
     * @param drawable Drawable to initialize AnimatedScaleDrawable with.
     */
    private void initializeAnimatedScaleDrawable(Drawable drawable) {
        mAnimatedScaleDrawable = new AnimatedScaleDrawable(drawable);
        int duration = mContext.getResources().getInteger(R.integer.credit_card_field_animation_duration_ms);
        mAnimatedScaleDrawable.setDuration(duration);
    }
}

