/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.dialog.date;

import java.util.Date;

/**
 * Created by ahobbs on 8/13/14.
 */
public interface ExpirationPickerListener {
    void onDialogPickerCanceled();
    void onExpirationDateSelected(Date selectedDate);
    void onDestroy();
}
