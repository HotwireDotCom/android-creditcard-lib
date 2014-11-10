package com.hotwire.hotels.hwcclib;

import android.util.Log;

/**
 * Log wrapper
 */
public class CreditCardLogger {

    // Variable to determine if logging in the application should happen
    public static boolean LOGGING_ENABLED = true;

    public static void i(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void i(String tag, String message, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message, tr);
        }
    }

    public static void e(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message, tr);
        }
    }

    public static void d(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message);
        }
    }

    public static void d(String tag, String message, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.d(tag, message, tr);
        }
    }

    public static void v(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.v(tag, message);
        }
    }

    public static void v(String tag, String message, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.v(tag, message, tr);
        }
    }

    public static void w(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message);
        }
    }

    public static void w(String tag, String message, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message, tr);
        }
    }

    public static void wtf(String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, message);
        }
    }

    public static void wtf(String tag, String message, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, message, tr);
        }
    }
}
