package com.hotwire.hotels.hwcclib;

import android.util.Log;

/**
 * Created by a-elpark on 9/4/14.
 */
public class CreditCardLogger {

    public static boolean LOGGING_ENABLED = true;

    public static void wtf(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }
}
