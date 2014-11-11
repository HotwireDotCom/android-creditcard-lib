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
