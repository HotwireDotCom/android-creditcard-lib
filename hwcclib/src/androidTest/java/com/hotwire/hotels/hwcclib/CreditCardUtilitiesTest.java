/*
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

import junit.framework.TestCase;

import org.junit.Test;
import org.robolectric.Robolectric;

import java.util.Calendar;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

public class CreditCardUtilitiesTest extends TestCase {

    public void testPreconditions() {
        assertNotNull(CreditCardUtilities.CardIssuer.values());
    }

    @Test
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

    @Test
    public void testIsValidUsingLuhn() {
        //valid JCB
        assertTrue(CreditCardUtilities.isValidUsingLuhn("3088647942200780"));
        //valid enroute
        assertTrue(CreditCardUtilities.isValidUsingLuhn("214912945170650"));

    }

    @Test
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

    @Test
    public void testGetCleanString() {
        // test a formatted credit card like string
        String cleanString = CreditCardUtilities.getCleanString("1111 1111 1111 1111");

        // there should be no whitespace returned
        for (char c : cleanString.toCharArray()) {
            assertFalse(Character.isWhitespace(c));
        }

        // test a formatted credit card like string with trailing and begining spaces
        cleanString = CreditCardUtilities.getCleanString(" 5555 5555 5555 5555 ");
        for (char c : cleanString.toCharArray()) {
            assertFalse(Character.isWhitespace(c));
        }

        // all null and empty strings should return an empty string
        cleanString = CreditCardUtilities.getCleanString(null);
        assertTrue(cleanString.isEmpty());

        cleanString = CreditCardUtilities.getCleanString("");
        assertTrue(cleanString.isEmpty());
    }

    @Test
    public void getFormattedStringTest() {
        Calendar calendar = Calendar.getInstance();
        // set the date to 7-15-2026 (US Date) -- formatting should output 07/26
        calendar.set(2026, Calendar.JULY, 15);

        // using raw string for date, in the event the resource is changed to a different format
        String formattedDate = CreditCardUtilities.getFormattedDate("MM/yy", calendar.getTime());
        assertThat(formattedDate).isEqualTo("07/26");

        formattedDate = CreditCardUtilities.getFormattedDate(null, calendar.getTime());
        assertThat(formattedDate).isEqualTo("");

        formattedDate = CreditCardUtilities.getFormattedDate("MM/yy", null);
        assertThat(formattedDate).isEqualTo("");

        formattedDate = CreditCardUtilities.getFormattedDate(null, null);
        assertThat(formattedDate).isEqualTo("");
    }
}
