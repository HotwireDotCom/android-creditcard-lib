package com.hotwire.hotels.hwcclib.filter;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by ahobbs on 9/4/14.
 */
public class SecurityCodeInputFilter extends InputFilter.LengthFilter {
    public static final String TAG = SecurityCodeInputFilter.class.getSimpleName();

    /**
     *
     * @param maxLength
     */
    public SecurityCodeInputFilter(int maxLength) {
        super(maxLength);
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
