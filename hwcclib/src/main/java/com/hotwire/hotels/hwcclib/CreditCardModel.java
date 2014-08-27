package com.hotwire.hotels.hwcclib;

/**
 * Created by a-elpark on 8/22/14.
 */
public class CreditCardModel {

    private String mCreditCardNumber;
    private String mExpirationDate;
    private String mSecurityCode;

    public CreditCardModel(String creditCardNumber, String expirationDate, String securityCode) {
        mCreditCardNumber = creditCardNumber;
        mExpirationDate = expirationDate;
        mSecurityCode = securityCode;
    }

    public String getCreditCardNumber() {
        return mCreditCardNumber;
    }

    public String getExpirationDate() {
        return mExpirationDate;
    }

    public String getSecurityCode() {
        return mSecurityCode;
    }
}
