package com.hotwire.hotels.hwcclib.filter;

import android.text.InputFilter;
import android.text.Spanned;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;

/**
 * An input filter that formats the credit card number based on detected credit card type as the user
 * enters their information.
 *
 * VISA, MASTER CARD, DISCOVER format
 *     Uses an offset of: 1
 *     Uses a modulo of:  5
 *     Formatted length: 19
 *     |#### #### #### ####|
 *     |    ^    ^    ^    |
 *
 *     The length at each of the carats are 4, 9, 14 respectively. By offsetting the length by 1, the new lengths
 *     become 5, 10, 15 respectively. The length can now be modulo'd by 5 to determine where the formatting space should
 *     be inserted.
 *
 * AMERICAN EXPRESS format
 *     Uses an offset of: 3
 *     Uses a modulo of:  7
 *     Formatted length: 19
 *    |#### ###### #####|
 *    |    ^      ^     |
 *
 *    The length at each of the carats are 4 and 11 respectively. By offsetting the length by 3, the new lengths become
 *    7 and 14 respectively. The length can now be modulo'd by 7 to determine where the formatting space should be
 *    inserted.
 *
 * By extending the InputFilter.LengthFilter the length will be limited automatically, and the CreditCardInputFilter
 * can focus on formatting.
 */
public class CreditCardInputFilter extends InputFilter.LengthFilter {
    public static final String TAG = CreditCardInputFilter.class.getSimpleName();

    private static final int INVALID_THRESHOLD = 0;
    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_MODULO = 1;


    private int mOffset;
    private int mMod;

    /**
     * Constructor for a CreditCardInputFilter that takes an offset, a modulo, and the formatted credit card length.
     *
     * @param offset offset to add to the current length/position in the string.
     * @param mod value to modulo against the current length + offset determining if a formatting space is necessary.
     * @param creditCardLength total formatted credit card length.
     */
    public CreditCardInputFilter(int offset, int mod, int creditCardLength) {
        super(creditCardLength);

        mOffset = offset >= INVALID_THRESHOLD ? offset : DEFAULT_OFFSET;
        mMod = mod > INVALID_THRESHOLD ? mod : DEFAULT_MODULO;
    }

    /**
     * Overridden method to filter the input while the user is entering information
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

        // If sequence is null, we have not hit the filter to limit length, apply credit card mask logic
        if (sequence == null) {
            StringBuilder builder = new StringBuilder();
            // used for keeping track of how many spaces were added when a number is pasted to the field
            int insertedFormatSpaces = 0;

            // Determining the length here will take into consideration, when the string has been modified by the
            // text watcher in the CreditCardNumberEditField to backspace the formatting white space characters
            // and allow for the inputfilter to reformat the string properly
            int length = dest.length();
            String cleanDest = CreditCardUtilities.getCleanString(dest.toString());
            if (source.length() == cleanDest.length()) {
                length = 0;
            }
            for (int i = start; i < end; i++) {
                int check = length + i + mOffset + insertedFormatSpaces;
                int modCheck = check % mMod;

                // Invalid characters, including spaces when not input by the input filter, do not move the
                // cursor forward
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
                else if (modCheck == 0) {
                    insertedFormatSpaces++;
                    builder.append(" ");
                }
                builder.append(source.charAt(i));
            }

            return builder.toString();
        }

        // otherwise return the length filter's value
        return sequence;
    }
}
