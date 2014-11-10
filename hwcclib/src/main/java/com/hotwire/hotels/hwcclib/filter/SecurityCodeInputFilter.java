package com.hotwire.hotels.hwcclib.filter;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * An input filter that restricts length and limits input to only numeric characters.
 */
public class SecurityCodeInputFilter extends InputFilter.LengthFilter {
    public static final String TAG = SecurityCodeInputFilter.class.getSimpleName();

    /**
     * Constructor that takes in max length for a field.
     *
     * @param maxLength maximum length for a field.
     */
    public SecurityCodeInputFilter(int maxLength) {
        super(maxLength);
    }

    /**
     * Overridden method that limits length of the field and limits the input to only numeric characters.
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

        if (sequence == null) {
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
                builder.append(source.charAt(i));
            }
            return builder.toString();
        }

        return sequence;
    }
}
