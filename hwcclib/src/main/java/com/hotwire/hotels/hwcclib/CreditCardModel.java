/**
 * Copyright 2015 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hotwire.hotels.hwcclib;

import java.util.Date;

/**
 * A data object that will store all of the information that has been entered into the credit card, expiration, and
 * security code fields. The CreditCardController will build and return this object if the developer chooses to use it.
 * All of the data stored in this object can be retrieved from the views themselves.
 */
public class CreditCardModel {

    private String mCreditCardNumber;
    private Date mExpirationDate;
    private String mSecurityCode;
    private CreditCardUtilities.CardIssuer mCardIssuer;

    /**
     * Constructor to store all of the credit card information.
     *
     * @param creditCardNumber un-formatted credit card number to store.
     * @param expirationDate expiration date of the credit card
     * @param securityCode security code of the credit card
     * @param cardIssuer card issuer of the credit card
     */
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
     * @return an un-formatted credit card number.
     */
    public String getCreditCardNumber() {
        return mCreditCardNumber;
    }

    /**
     * @param creditCardNumber un-formatted credit card number to store.
     */
    public void setCreditCardNumber(String creditCardNumber) {
        mCreditCardNumber = creditCardNumber;
    }

    /**
     * @return the expiration date for the credit card.
     */
    public Date getExpirationDate() {
        return mExpirationDate;
    }

    /**
     * @param expirationDate Date object to use as expiration date for credit card. Ensure day field is a day that
     *                       exists in all months (eg. 1st of the month).
     */
    public void setExpirationDate(Date expirationDate) {
        mExpirationDate = expirationDate;
    }

    /**
     * @return the security code for the credit card.
     */
    public String getSecurityCode() {
        return mSecurityCode;
    }

    /**
     * @param securityCode security code to use with the stored card.
     */
    public void setSecurityCode(String securityCode) {
        mSecurityCode = securityCode;
    }

    /**
     * @return the CardIssuer of the credit card.
     */
    public CreditCardUtilities.CardIssuer getCardIssuer() {
        return mCardIssuer;
    }

    /**
     * @param cardIssuer the CardIssuer of the credit card.
     */
    public void setCardIssuer(CreditCardUtilities.CardIssuer cardIssuer) {
        mCardIssuer = cardIssuer;
    }
}
