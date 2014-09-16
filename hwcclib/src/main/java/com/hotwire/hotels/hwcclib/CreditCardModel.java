package com.hotwire.hotels.hwcclib;

import java.util.Date;

/**
 * Created by elpark on 8/22/14.
 */
public class CreditCardModel {

    private String mCreditCardNumber;
    private Date mExpirationDate;
    private String mSecurityCode;
    private CreditCardUtilities.CardIssuer mCardIssuer;

    public CreditCardModel(String creditCardNumber,
                           Date expirationDate,
                           String securityCode,
                           CreditCardUtilities.CardIssuer cardIssuer) {
        mCreditCardNumber = creditCardNumber;
        mExpirationDate = expirationDate;
        mSecurityCode = securityCode;
        mCardIssuer = cardIssuer;
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
     * @param creditCardNumber
     */
    public void setCreditCardNumber(String creditCardNumber) {
        mCreditCardNumber = creditCardNumber;
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
     * @param expirationDate
     */
    public void setExpirationDate(Date expirationDate) {
        mExpirationDate = expirationDate;
    }

    /**
     *
     * @return
     */
    public String getSecurityCode() {
        return mSecurityCode;
    }

    /**
     *
     * @param securityCode
     */
    public void setSecurityCode(String securityCode) {
        mSecurityCode = securityCode;
    }

    /**
     *
     * @return
     */
    public CreditCardUtilities.CardIssuer getCardIssuer() {
        return mCardIssuer;
    }

    /**
     *
     * @param cardIssuer
     */
    public void setCardIssuer(CreditCardUtilities.CardIssuer cardIssuer) {
        mCardIssuer = cardIssuer;
    }
}
