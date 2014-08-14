/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */

package com.hotwire.hotels.hwcclib;

import junit.framework.TestCase;

/**
 * Created by ankpal on 8/13/14.
 */
public class CreditCardUtilitiesTest extends TestCase {

    public void testPreconditions() {
        assertNotNull(CreditCardUtilities.CardIssuer.values());
    }

    public void testGetCardIssuer() {
        //valid first 4 digits
        assertEquals(CreditCardUtilities.getCardIssuer("4111"), CreditCardUtilities.CardIssuer.VISA);
        assertEquals(CreditCardUtilities.getCardIssuer("3409"), CreditCardUtilities.CardIssuer.AMERICANEXPRESS);
        assertEquals(CreditCardUtilities.getCardIssuer("6011"), CreditCardUtilities.CardIssuer.DISCOVER);
        assertEquals(CreditCardUtilities.getCardIssuer("5568"), CreditCardUtilities.CardIssuer.MASTERCARD);

        //testing for value returned for more than 4 digits as input number
        assertEquals(CreditCardUtilities.getCardIssuer("556890334"), CreditCardUtilities.CardIssuer.MASTERCARD);
        assertEquals(CreditCardUtilities.getCardIssuer("5568903344592744"), CreditCardUtilities.CardIssuer.MASTERCARD);

        //testing negative case - less than 4 digits
        assertEquals(CreditCardUtilities.getCardIssuer("12"), CreditCardUtilities.CardIssuer.INVALID);

    }

    public void testLengthOfSecurityCode() {
        assertEquals(CreditCardUtilities.lengthOfSecurityCode(CreditCardUtilities.CardIssuer.AMERICANEXPRESS), 4);
        assertEquals(CreditCardUtilities.lengthOfSecurityCode(CreditCardUtilities.CardIssuer.DISCOVER), 3);
        assertEquals(CreditCardUtilities.lengthOfSecurityCode(CreditCardUtilities.CardIssuer.VISA), 3);
        assertEquals(CreditCardUtilities.lengthOfSecurityCode(CreditCardUtilities.CardIssuer.MASTERCARD), 3);
    }

    public void testIsValidUsingLuhn() {
        //valid JCB
        assertTrue(CreditCardUtilities.isValidUsingLuhn("3088647942200780"));
        //valid enroute
        assertTrue(CreditCardUtilities.isValidUsingLuhn("214912945170650"));

    }

    public void testIsValidCreditCard() {
        //valid credit cards
        assertTrue(CreditCardUtilities.isValidCreditCard("4539630110965791"));
        assertTrue(CreditCardUtilities.isValidCreditCard("5149955873292557"));
        assertTrue(CreditCardUtilities.isValidCreditCard("6011478899975827"));
        assertTrue(CreditCardUtilities.isValidCreditCard("345615853052313"));

        //incomplete and invalid credit cards
        assertFalse(CreditCardUtilities.isValidCreditCard("4718790956"));
        assertFalse(CreditCardUtilities.isValidCreditCard("4716620978790950"));

        //valid JCB card - but not supported by this library
        assertFalse(CreditCardUtilities.isValidCreditCard("3088647942200780"));
    }

}
