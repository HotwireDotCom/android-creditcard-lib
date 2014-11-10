package com.hotwire.hotels.hwcclib;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides utility methods to
 * check for valid supported credit cards using Luhn mod 10.
 * determine the card issuer with the first 4 characters entered. and
 * return the security code length for supported cards.
 *
 */
public final class CreditCardUtilities {
    public static final String TAG = CreditCardUtilities.class.getSimpleName();

    public static final int NO_RES_ID = -1;

    // VISA, MASTER CARD, DISCOVER values
    public static final int CARD_FORMATTED_LENGTH_19 = 19; // credit card formatted length
    public static final int OFFSET_1 = 1;                  // offset (see CreditCardInputFilter)
    public static final int MODULO_5 = 5;                  // modulo (see CreditCardInputFilter)
    public static final int SECURITY_LENGTH_3 = 3;         // security code length (see SecurityCodeInputFilter)

    // AMERICAN EXPRESS values
    public static final int CARD_FORMATTED_LENGTH_17 = 17; // credit card formatted length
    public static final int OFFSET_3 = 3;                  // offset (see CreditCardInputFilter)
    public static final int MODULO_7 = 7;                  // modulo (see CreditCardInputFilter)
    public static final int SECURITY_LENGTH_4 = 4;         // security code length (see SecurityCodeInputFilter)



    public static final int MIN_LENGTH_FOR_TYPE = 4;

    // Regex for VISA
    public static final String VISA_CARD_REGEX = "^4[0-9]{15}?";
    public static final String VISA_CARD_TYPE_REGEX = "^4[0-9]{3}?";

    // Regex for MASTER CARD
    public static final String MASTERCARD_CARD_REGEX = "^5[1-5][0-9]{14}$";
    public static final String MASTERCARD_CARD_TYPE_REGEX = "^5[1-5][0-9]{2}$";

    // Regex for AMERICAN EXPRESS
    public static final String AMERICANEXPRESS_CARD_REGEX = "^3[47][0-9]{13}$";
    public static final String AMERICANEXPRESS_CARD_TYPE_REGEX = "^3[47][0-9]{2}$";

    // Regex for DISCOVER
    public static final String DISCOVER_CARD_REGEX = "^6(?:011|5[0-9]{2})[0-9]{12}$";
    public static final String DISCOVER_CARD_TYPE_REGEX = "^6(?:011|5[0-9]{2})$";

    public static final String EMPTY_STRING = "";
    public static final String REGEX_WHITESPACE = "\\s";

    private CreditCardUtilities() {
        // Intentionally left empty, static utility class
    }
    /**
     * An enum containing all the supported cards and their corresponding rules.
     * The rules are laid out in the following order
     *
     * Regex for validating the card
     * Partial Regex for determining the card issuer based on the first 4 characters entered
     * Security code length
     * Offset used during credit card number formatting (see the CreditCardInputFilter).
     * Modulo value used to determine where to put spaces in a credit card number (see CreditCardInputFilter).
     * The id of the drawable for the credit card type
     * The id of the drawable used to show where a security code is located on a card
     */
    public static enum CardIssuer {

        VISA(VISA_CARD_REGEX,
                VISA_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_visa,
                R.drawable.ic_security_code_3),
        MASTERCARD(MASTERCARD_CARD_REGEX,
                MASTERCARD_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_mastercard,
                R.drawable.ic_security_code_3),
        AMERICANEXPRESS(AMERICANEXPRESS_CARD_REGEX,
                AMERICANEXPRESS_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_17,
                SECURITY_LENGTH_4,
                OFFSET_3,
                MODULO_7,
                R.drawable.ic_credit_card_americanexpress,
                R.drawable.ic_security_code_4),
        DISCOVER(DISCOVER_CARD_REGEX,
                DISCOVER_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_discover,
                R.drawable.ic_security_code_3),
        INVALID(EMPTY_STRING,
                EMPTY_STRING,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_generic,
                R.drawable.ic_security_code_disabled);

        private String mRegex;
        private String mRegexType;
        private int mFormattedLength;
        private int mSecurityLength;
        private int mOffset;
        private int mModulo;
        private int mIconResourceId;
        private int mSecCodeResourceId;

        private CardIssuer(String regex,
                           String regexType,
                           int formattedLength,
                           int securityLength,
                           int offset,
                           int modulo,
                           int iconResourceId,
                           int secCodeResourceId) {
            this.mRegex = regex;
            this.mRegexType = regexType;
            this.mFormattedLength = formattedLength;
            this.mSecurityLength = securityLength;
            this.mOffset = offset;
            this.mModulo = modulo;
            this.mIconResourceId = iconResourceId;
            this.mSecCodeResourceId = secCodeResourceId;
        }

        /**
         * @return regex for validating a credit card
         */
        public String getRegex() {
            return mRegex;
        }

        /**
         * @return regex for determining a credit card type
         */
        public String getRegexType() {
            return mRegexType;
        }

        /**
         * @return the length of a card type including formatting
         */
        public int getFormattedLength() {
            return mFormattedLength;
        }

        /**
         * @return the security code length required by a card type
         */
        public int getSecurityLength() {
            return mSecurityLength;
        }

        /**
         * @return the offset to use when formatting the credit card number
         */
        public int getOffset() {
            return mOffset;
        }

        /**
         * @return the modulo to use when formatting the credit card number
         */
        public int getModulo() {
            return mModulo;
        }

        /**
         * @return the resource id for the credit card image
         */
        public int getIconResourceId() {
            return mIconResourceId;
        }

        /**
         * @return the resource id for the security code image
         */
        public int getSecurityIconResourceId() {
            return mSecCodeResourceId;
        }
    }


    /**
     * Determine the CardIssuer based on an un-formatted credit card number string.
     *
     * @param inputNumber - a sanitized string representing the credit card number.
     * @return - the enum value that represents CardIssuer.
     */
    public static CardIssuer getCardIssuer(String inputNumber) {
        if (inputNumber.length() < MIN_LENGTH_FOR_TYPE) {
            return CardIssuer.INVALID;
        }

        for (CardIssuer cardIssuer: CardIssuer.values()) {
            Pattern pattern = Pattern.compile(cardIssuer.getRegexType());
            Matcher matcher = pattern.matcher(inputNumber.substring(0, MIN_LENGTH_FOR_TYPE));

            if (matcher.matches()) {
                return cardIssuer;
            }
        }

        return CardIssuer.INVALID;
    }

    /**
     * Determine if a valid credit card number based on an un-formatted credit card number string.
     *
     * @param inputNumber - a sanitized string representation of the credit card number.
     * @return true if the credit card is supported, valid, and passes the Luhn algorithm.
     */
    public static boolean isValidCreditCard(String inputNumber) {
        CardIssuer cardIssuer = getCardIssuer(inputNumber);

        if (cardIssuer == CardIssuer.INVALID) {
            return false;
        }

        Pattern pattern = Pattern.compile(cardIssuer.getRegex());
        Matcher matcher = pattern.matcher(inputNumber);

        return matcher.matches() && isValidUsingLuhn(inputNumber);
    }

    /**
     * Determine if an un-formatted credit card number can pass the luhn algorithm.
     *
     * @param inputNumber - a sanitized string representation of the credit card number
     * @return true if inputNumber passes the Luhn algorithm
     * @throws NumberFormatException
     */
    public static boolean isValidUsingLuhn(String inputNumber) throws NumberFormatException {
        int sum = 0, digit = 0;

        boolean doubled = false;
        for (int i = inputNumber.length() - 1; i >= 0; i--) {
            digit = Integer.parseInt(inputNumber.substring(i, i + 1));
            if (doubled) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubled = !doubled;
        }
        return (sum % 10) == 0;
    }

    /**
     * Strips a string of all white space and replaces with an empty string.
     *
     * @param original string that can have any number of spaces.
     * @return a clean string stripped of white space.
     */
    public static String getCleanString(String original) {
        if (original == null || original.isEmpty()) {
            return EMPTY_STRING;
        }

        return original.replaceAll(REGEX_WHITESPACE, EMPTY_STRING);
    }

    /**
     * Formats a provided Date to the provided date format.
     *
     * @param dateFormat format to return the date in.
     * @param date date to be for matted.
     * @return a formatted date, or an empty string.
     */
    public static String getFormattedDate(String dateFormat, Date date) {
        String dateString = "";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            dateString = simpleDateFormat.format(date);
        }
        catch (NullPointerException npe) {
            // either the dateformat, or the date are null, log and swallow the exception and proceed with an
            // empty string.
            CreditCardLogger.e(TAG, "Null date, or null date format", npe);
        }
        catch (IllegalArgumentException e) {
            // either the date could not be formatted, log and swallow the exception and proceed with an empty string.
            CreditCardLogger.e(TAG, "Invalid date, or invalid date format", e);
        }
        return dateString;
    }
}
