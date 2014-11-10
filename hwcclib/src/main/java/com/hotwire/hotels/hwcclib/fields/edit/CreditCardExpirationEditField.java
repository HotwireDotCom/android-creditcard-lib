package com.hotwire.hotels.hwcclib.fields.edit;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;

import java.util.Date;

/**
 * EditText field for expiration date entry.
 */
public class CreditCardExpirationEditField extends EditText {
    public static String TAG = CreditCardExpirationEditField.class.getSimpleName();

    private final Context mContext;

    public CreditCardExpirationEditField(Context context) {
        this(context, null);
    }

    public CreditCardExpirationEditField(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    /**
     * Overridden constructor from EditText that will also initialize the field.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CreditCardExpirationEditField(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    /**
     * Method to initialize the field when created.
     */
    private void init() {
        setHint(R.string.expiration_field_hint_text);
        setHintTextColor(mContext.getResources().getColor(R.color.field_text_color_hint_default));
        setGravity(Gravity.BOTTOM);
        setSingleLine(true);
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setKeyListener(null);
    }

    /******************************
     * BEGIN CUSTOM METHODS
     ******************************/

    /**
     * Sets a non-null expiration date in the proper format on the field.
     *
     * @param expirationDate date to be formatted and set on the field.
     */
    public void setExpirationDate(Date expirationDate) {
        if (expirationDate != null) {
            String dateFormat = getResources().getString(R.string.expiration_field_date_format);
            String dateString = CreditCardUtilities.getFormattedDate(dateFormat, expirationDate);

            setText(dateString);
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
}
