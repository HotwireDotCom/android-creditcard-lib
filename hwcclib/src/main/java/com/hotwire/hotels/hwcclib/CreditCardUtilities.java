/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */

package com.hotwire.hotels.hwcclib;

/**
 * Created by ankpal on 8/12/14.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ankpal
 * This class provides utility methods to
 * check for valid supported credit cards using Luhn mod 10.
 * determine the card issuer with the first 4 characters entered. and
 * return the security code length for supported cards.
 *
 */
public final class CreditCardUtilities {


    public static final int CARD_FORMATTED_LENGTH_19 = 19;
    public static final int CARD_FORMATTED_LENGTH_17 = 17;
    public static final int SECURITY_LENGTH_3 = 3;
    public static final int SECURITY_LENGTH_4 = 4;
    public static final int OFFSET_1 = 1;
    public static final int OFFSET_3 = 3;
    public static final int MODULO_5 = 5;
    public static final int MODULO_7 = 7;
    public static final int MIN_LENGTH_FOR_TYPE = 4;

    public static final String VISA_CARD_REGEX = "^4[0-9]{15}?";
    public static final String VISA_CARD_TYPE_REGEX = "^4[0-9]{3}?";

    public static final String MASTERCARD_CARD_REGEX = "^5[1-5][0-9]{14}$";
    public static final String MASTERCARD_CARD_TYPE_REGEX = "^5[1-5][0-9]{2}$";

    public static final String AMERICANEXPRESS_CARD_REGEX = "^3[47][0-9]{13}$";
    public static final String AMERICANEXPRESS_CARD_TYPE_REGEX = "^3[47][0-9]{2}$";

    public static final String DISCOVER_CARD_REGEX = "^6(?:011|5[0-9]{2})[0-9]{12}$";
    public static final String DISCOVER_CARD_TYPE_REGEX = "^6(?:011|5[0-9]{2})$";

    public static final String EMPTY_STRING = "";
    public static final String REGEX_WHITESPACE = "\\s";

    private CreditCardUtilities() {

    }
    /**
     * An enum containing all the supported cards and it's corresponding rules.
     * The rules are laid out in the following order
     * mRegex for the card
     * partial mRegex for determining the card issuer based on the first 4 characters entered
     * the security code length
     */
    public static enum CardIssuer {

        VISA(VISA_CARD_REGEX,
                VISA_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_visa),
        MASTERCARD(MASTERCARD_CARD_REGEX,
                MASTERCARD_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_mastercard),
        AMERICANEXPRESS(AMERICANEXPRESS_CARD_REGEX,
                AMERICANEXPRESS_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_17,
                SECURITY_LENGTH_4,
                OFFSET_3,
                MODULO_7,
                R.drawable.ic_credit_card_americanexpress),
        DISCOVER(DISCOVER_CARD_REGEX,
                DISCOVER_CARD_TYPE_REGEX,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_discover),
        INVALID(EMPTY_STRING,
                EMPTY_STRING,
                CARD_FORMATTED_LENGTH_19,
                SECURITY_LENGTH_3,
                OFFSET_1,
                MODULO_5,
                R.drawable.ic_credit_card_generic);

        private String mRegex;
        private String mRegexType;
        private int mFormattedLength;
        private int mSecurityLength;
        private int mOffset;
        private int mModulo;
        private int mIconResourceId;
        private int mSecCodeResourceId;

        private CardIssuer(String regex, String regexType, int formattedLength, int securityLength, int offset, int modulo,
                           int iconResourceId) {
            this.mRegex = regex;
            this.mRegexType = regexType;
            this.mFormattedLength = formattedLength;
            this.mSecurityLength = securityLength;
            this.mOffset = offset;
            this.mModulo = modulo;
            this.mIconResourceId = iconResourceId;
        }

        public String getRegex() {
            return mRegex;
        }

        public String getRegexType() {
            return mRegexType;
        }

        public int getFormattedLength() {
            return mFormattedLength;
        }

        public int getSecurityLength() {
            return mSecurityLength;
        }

        public int getOffset() {
            return mOffset;
        }

        public int getModulo() {
            return mModulo;
        }

        public int getIconResourceId() {
            return mIconResourceId;
        }
    }


    /**
     *
     * @param inputNumber - a sanitized string representation of the credit card number
     * @return - the enum value that represents CardIssuer
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
     *
     * @param inputNumber - a sanitized string representation of the credit card number
     * @return true if the credit card is supported and passes the Luhn algorithm
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

    /**hfhf
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
     * Strips a string of all white space and replaces with an empty string
     *
     * @param original
     * @return
     */
    public static String getCleanString(String original) {
        if (original == null || original.isEmpty()) {
            return EMPTY_STRING;
        }
        String sanitized = original.trim();

        return sanitized.replaceAll(REGEX_WHITESPACE, EMPTY_STRING);
    }
}
