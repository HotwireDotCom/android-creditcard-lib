package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.animation.drawable.AnimatedScaleDrawable;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;

/**
 * EditText field for credit card number entry.
 */
public class CreditCardNumberEditField extends EditText {
    public static final String TAG = CreditCardNumberEditField.class.getSimpleName();

    private final Context mContext;
    private AnimatedScaleDrawable mAnimatedScaleDrawable;

    public CreditCardNumberEditField(Context context) {
        this(context, null);
    }

    public CreditCardNumberEditField(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     * Overridden constructor from EditText that will also initialize the field.
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
     * Method to initialize the field when created.
     */
    private void init() {
        setHint(R.string.credit_card_field_hint_text);
        setHintTextColor(mContext.getResources().getColor(R.color.field_text_color_hint_default));
        setGravity(Gravity.BOTTOM);
        setSingleLine(true);
        /* for the Credit card field we do not want to have suggestions from keyboards
         * this makes the InputFilter difficult to deal with. Also attempt to restrict the keyboard to
         * only be the number pad for credit card entry.
         */
        setRawInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                     InputType.TYPE_CLASS_NUMBER |
                     InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        initializeAnimatedScaleDrawable(mContext.getResources().getDrawable(R.drawable.ic_credit_card_generic));
        setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.compound_drawable_padding_default));
        setCompoundDrawablesWithIntrinsicBounds(mAnimatedScaleDrawable,
                                                null,
                                                null,
                                                null);
        addTextChangedListener(new CreditCardNumberTextWatcher());
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/

    /**
     * Gets a clean string representing a credit card number.
     *
     * @return an un-formatted clean string of the credit card number.
     */
    public String getRawCreditCardNumber() {
        String currentText = getText().toString();
        return CreditCardUtilities.getCleanString(currentText);
    }

    /**
     * Sets the new credit card type on the field, if a valid resourceId.
     *
     * @param newCardTypeResId resourceId for the new card image for the field.
     */
    private void setCardTypeForField(int newCardTypeResId) {
        if (newCardTypeResId != CreditCardUtilities.NO_RES_ID) {
            Drawable newFieldDrawable = mContext.getResources().getDrawable(newCardTypeResId);
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
     * Normally called from the controller when a card type has changed. Updates the card type image, and applies
     * the proper InputFilter for that card type.
     *
     * @param cardIssuer which card issues (eg. Visa, MasterCard, etc).
     * @param shouldAnimate boolean that determines if the animation between card types should take place.
     */
    public void updateCardType(CreditCardUtilities.CardIssuer cardIssuer, boolean shouldAnimate) {
        InputFilter creditCardNumFilter = new CreditCardInputFilter(cardIssuer.getOffset(),
                cardIssuer.getModulo(),
                cardIssuer.getFormattedLength());
        setFilters(new InputFilter[]{creditCardNumFilter});

        int resCardResId = cardIssuer.getIconResourceId();
        if (shouldAnimate) {
            setCardTypeForField(resCardResId);
        }
        else {
            setCardTypeImageResource(resCardResId);
        }
    }

    /**
     * This is intended to be used by the controller's restoreInstanceState method in order
     * to restore the image associated with the current card type.
     *
     * @param newCardTypeResId resourceId for the card image on the field.
     */
    private void setCardTypeImageResource(int newCardTypeResId) {
        mAnimatedScaleDrawable.setDrawable(mContext.getResources().getDrawable(newCardTypeResId));
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

    /**
     * Text watcher that listens to text input and forces the characters to be re-formatted by the input filter.
     */
    static class CreditCardNumberTextWatcher implements TextWatcher {
        int mStart = 0;

        /**
         * Store cursor start location to be used in afterTextChanged
         *
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // store the cursor start location
            mStart = start;
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
            // no op
        }

        /**
         * This will force each character to go through the input filter to be re-formatted when it is being
         * edited.
         *
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) {
            if (mStart > 0) {
                String goodString = new String(s.toString().trim());
                goodString = CreditCardUtilities.getCleanString(goodString);
                s.replace(0, s.length(), goodString, 0, goodString.length());
            }
        }
    }
}
