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

import android.util.Log;

/**
 * Log wrapper
 */
public class CreditCardLogger {

    // Variable to determine if logging in the application should happen
    public static boolean LOGGING_ENABLED = false;

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
