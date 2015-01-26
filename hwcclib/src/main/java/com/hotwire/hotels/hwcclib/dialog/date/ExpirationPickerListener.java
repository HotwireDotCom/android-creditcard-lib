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

package com.hotwire.hotels.hwcclib.dialog.date;

import java.util.Date;

/**
 * Interface to listen to events from the ExpirationPickerDialogFragment.
 */
public interface ExpirationPickerListener {
    /**
     * Method that will be called from the ExpirationPickerDialogFragment on a canceled button click.
     */
    void onDialogPickerCanceled();

    /**
     * Method that will be called from the ExpirationPickerDialogFragment when a date is selected.
     *
     * @param selectedDate
     */
    void onExpirationDateSelected(Date selectedDate);

    /**
     * Method that is called when ExpirationDialogFragment is destroyed.
     */
    void onDestroy();
}
