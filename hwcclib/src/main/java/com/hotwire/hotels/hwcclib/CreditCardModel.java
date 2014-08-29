package com.hotwire.hotels.hwcclib;

import java.util.Date;

/**
 * Created by a-elpark on 8/22/14.
 */
public class CreditCardModel {

    private String mCreditCardNumber;
    private Date mExpirationDate;
    private String mSecurityCode;

    public CreditCardModel(String creditCardNumber, Date expirationDate, String securityCode) {
        mCreditCardNumber = creditCardNumber;
        mExpirationDate = expirationDate;
        mSecurityCode = securityCode;
    }

    /**
     *
     * @return
     */
    public String getCreditCardNumber() {
        return mCreditCardNumber;
    }

    /**
     *
     * @return
     */
    public Date getExpirationDate() {
        return mExpirationDate;
    }

    /**
     *
     * @return
     */
    public String getSecurityCode() {
        return mSecurityCode;
    }
}
