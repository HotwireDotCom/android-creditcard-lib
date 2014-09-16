package com.hotwire.hotels.hwcclib;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.dialog.date.ExpirationPickerDialogFragment;
import com.hotwire.hotels.hwcclib.dialog.date.ExpirationPickerListener;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardExpirationEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardNumberEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardSecurityCodeEditField;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The CreditCardController ties together all of the input mechanisms and logic necessary to collect and validate
 * a user's credit card information. The CreditCardController has four main functions:
 *
 *  1: It implements a state machine for organizing the execution of logic based on user interaction with the
 *  the CreditCardNumberEditField, CreditCardExpirationEditField, CreditCardSecurityCodeEditField, and the
 *  ExpirationPickerDialogFragment. The state machine encourages, but does not enforce, an ideal way to move
 *  from field to field, known as the Happy Path. The different states in the state machine represent the
 *  state of the user's interaction with the credit card information entry mechanisms. This is different than
 *  a state machine representing all of the possible states of the CreditCardController or the state of the
 *  information that the user has entered. For example, the state machine can reflect that the user is editing
 *  the CreditCardNumberEditField, but it cannot reflect that the user has entered an invalid credit card number.
 *
 *  2: It contains evaluators that determine whether or not the information a user has entered into a specific
 *  field is valid. These evaluators modify the entry fields' error states when they contain invalid information and
 *  determine when users have completed entering all of their credit card information.
 *
 *  3: It listens and responds to events coming from the CreditCardNumberEditField, CreditCardExpirationEditField,
 *  CreditCardSecurityCodeEditField, and the ExpirationPickerDialogFragment. When different events, e.g. a focus
 *  event occurs on one of the entry fields, the CreditCardController determines on which field the focus event
 *  occured and then dispatches an appropriate CreditCardEvent to the state machine.
 *
 *  4: It saves and restores the state of the CreditCardNumberEditField, CreditCardExpirationEditField,
 *  CreditCardSecurityCodeEditField, ExpirationPickerDialogFragment, and the state machine.
 *
 *  The logic used for validating what users have entered into different fields is called by the state machine, but exists outside of it, in the Controller; this makes it easy to decouple the state machine from the Controller or modify and extend the state machine as needed.
 * Created by epark on 8/19/14.
 */
public class CreditCardController implements View.OnFocusChangeListener, TextWatcher, ExpirationPickerListener,
        View.OnTouchListener {

    public static final String TAG = CreditCardController.class.getCanonicalName();

    private static class CreditCardControllerState implements Serializable {
        private static final long serialVersionUID = 1L;
        private int currentState = CreditCardState.IDLE_STATE.ordinal();
        private String creditCardNumberText = "";
        private Date expDate = new Date();
        private String expDateText = "";
        private String secCodeText = "";
        private boolean numberTextHasBeenEntered = false;
        private boolean happyPathIsBroken = false;
        private int cardIssuer = CreditCardUtilities.CardIssuer.INVALID.ordinal();
    }

    private static final String CREDIT_CARD_CONTROLLER_STATE_KEY = CreditCardController.class.getCanonicalName() +
            ".credit_card_controller_state_key";

    /**
     * Enumerated types representing the possible states of the CreditCardController's state machine.
     * These states are used as keys to look up CreditCardEvent-Transision HashMaps in the mTransitionMap HashMap. They
     * are also used to keep track of the state machine's current state.
     */
    public enum CreditCardState {
        IDLE_STATE,
        NUMBER_FIELD_FOCUSED_STATE,
        NUMBER_FIELD_EDIT_STATE,
        DATE_PICKER_OPEN_STATE,
        SEC_CODE_FIELD_FOCUSED_STATE,
        SEC_CODE_FIELD_EDIT_STATE
    }

    /**
     * Enumerated types representing the events that the CreditCardController's state machine handles.
     * These events are used to look up Transitions in the CreditCardEvent-Transision HashMaps that are
     * associated with each CreditCardState in mTransitionMap.
     */
    public enum CreditCardEvent {
        CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT,
        CREDIT_CARD_NUMBER_VALIDATED_EVENT,
        EXP_DATE_FIELD_ON_FOCUS_EVENT,
        CLOSE_DATE_PICKER_EVENT,
        SEC_CODE_FIELD_ON_FOCUS_EVENT,
        TEXT_CHANGED_EVENT,
        FOCUS_LOST_EVENT
    }

    /**
     * An interface used to create a listener for reporting when all of the credit card entry fields
     * have been competed.
     */
    public interface CreditCardModelCompleteListener {
        void onCreditCardModelComplete(CreditCardModel creditCardModel);
    }

    /**
     *
     */
    public interface Transition {
        void execute();
    }

    /**
     * The transition map used by the CreditCardController's state machine.
     */
    private Map<CreditCardState, Map<CreditCardEvent, Transition>> mTransitionMap;

    private boolean mHappyPathBroken;
    private CreditCardState mCurrentState;
    private boolean mNumberCompleted;
    private boolean mExpDateCompleted;
    private boolean mSecCodeCompleted;
    private CreditCardUtilities.CardIssuer mCardIssuer;
    private Context mContext;
    private Date mExpirationDate;
    private boolean mIgnoringEvents;
    private boolean mDatePickerOpen;
    private boolean mNumberTextHasBeenEntered;

    private CreditCardModelCompleteListener mCreditCardModelCompleteListener;

    private CreditCardNumberEditField mCreditCardNumEditField;
    private CreditCardExpirationEditField mExpDateEditField;
    private CreditCardSecurityCodeEditField mSecCodeEditField;

    public CreditCardController(Context context,
                                CreditCardNumberEditField numberEditField,
                                CreditCardExpirationEditField expirationEditField,
                                CreditCardSecurityCodeEditField secCodeEditField) {

        mContext = context;
        mCreditCardNumEditField = numberEditField;
        mCreditCardNumEditField.setOnFocusChangeListener(this);
        mCreditCardNumEditField.addTextChangedListener(this);
        mCreditCardNumEditField.setOnTouchListener(this);
        mExpDateEditField = expirationEditField;
        mExpDateEditField.setOnFocusChangeListener(this);
        mExpDateEditField.setOnTouchListener(this);
        mSecCodeEditField = secCodeEditField;
        mSecCodeEditField.setOnFocusChangeListener(this);
        mSecCodeEditField.addTextChangedListener(this);
        mNumberTextHasBeenEntered = false;

        mSecCodeEditField.setEnabled(false);
        mSecCodeEditField.setFocusable(false);
        mSecCodeEditField.setFocusableInTouchMode(false);

        mCreditCardNumEditField.setNextFocusDownId(mExpDateEditField.getId());
        mCreditCardNumEditField.setNextFocusRightId(mExpDateEditField.getId());

        mDatePickerOpen = false;
        mIgnoringEvents = false;
        mExpirationDate = new Date();
        mCurrentState = CreditCardState.IDLE_STATE;
        mHappyPathBroken = false;
        mCardIssuer = CreditCardUtilities.CardIssuer.INVALID;
        initTransitionTable();

        InputFilter creditCardNumFilter = new CreditCardInputFilter(mCardIssuer.getOffset(),
                mCardIssuer.getModulo(),
                mCardIssuer.getFormattedLength());
        mCreditCardNumEditField.setFilters(new InputFilter[] {creditCardNumFilter});

        /*
         * If the credit card number field or the expiration date field have focus when the controller
         * is initialized, dispatch an appropriate on focus event.
         *
         */
        if (mCreditCardNumEditField.hasFocus()) {
            handleEvent(CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        else if (mExpDateEditField.hasFocus()) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
    }

    /**
     *
     *
     * @param state
     */
    private void setCurrentState(CreditCardState state) {
        CreditCardLogger.i(TAG, "Setting current state: " + state.toString());
        mCurrentState = state;
    }

    /**
     *
     * @param event
     */
    public void handleEvent(CreditCardEvent event) {
        Transition transition = mTransitionMap.get(mCurrentState).get(event);
        if (transition != null && !mIgnoringEvents) {
            CreditCardLogger.i(TAG, "Handling event: " + event.toString() + " for state: " + mCurrentState.toString());
            transition.execute();
        }
        else {
            CreditCardLogger.i(TAG, "Ignoring event: " + event.toString() + " for state: " + mCurrentState.toString());
        }
    }

    /**
     *
     * @param creditCardModelCompleteListener
     */
    public void setCreditCardModelCompleteListener(CreditCardModelCompleteListener creditCardModelCompleteListener) {
        mCreditCardModelCompleteListener = creditCardModelCompleteListener;
    }


    /**
     *
     */
    public void complete() {
        if (mNumberCompleted && mExpDateCompleted && mSecCodeCompleted && mCreditCardModelCompleteListener != null) {
            CreditCardModel creditCardModel = new CreditCardModel(mCreditCardNumEditField.getRawCreditCardNumber(),
                                                                  mExpirationDate,
                                                                  mSecCodeEditField.getText().toString(),
                                                                  mCardIssuer);
            mCreditCardModelCompleteListener.onCreditCardModelComplete(creditCardModel);
        }
    }

    /**
     *
     * @return
     */
    public boolean isComplete() {
        if (mNumberCompleted && mExpDateCompleted && mSecCodeCompleted) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     *
     */
    private void evaluateCreditCardNumber() {
        updateCreditCardType();
        /*
         * If the user has entered and then deleted all of the numbers in the CreditCardNumberEditField,
         * clear all of the fields and reset the happy path.
         * Note: If the user has already selected an expiration date, the text in the ExpirationDateEditField
         * will be reset, but the stored date will not be reset to today's date.
         */
        if (mCreditCardNumEditField.getRawCreditCardNumber().isEmpty() && mNumberTextHasBeenEntered) {
            mHappyPathBroken = false;
            mNumberCompleted = false;
            mExpDateCompleted = false;
            mSecCodeCompleted = false;
            mNumberTextHasBeenEntered = false;
            mIgnoringEvents = true;
            mSecCodeEditField.setText("");
            mExpDateEditField.setText("");
            mIgnoringEvents = false;
        }
        else if (CreditCardUtilities.isValidCreditCard(mCreditCardNumEditField.getRawCreditCardNumber()) &&
                CreditCardUtilities.isValidUsingLuhn(mCreditCardNumEditField.getRawCreditCardNumber())) {
                mNumberCompleted = true;
                if (isComplete() && !mIgnoringEvents){
                    complete();
                }
                mCreditCardNumEditField.clearErrors();
                handleEvent(CreditCardEvent.CREDIT_CARD_NUMBER_VALIDATED_EVENT);
        }
        else {
            /*
             * It is already known that the number the user entered is not valid, so, if the user has
             * entered the maximum number of numbers allowed relative to their card type,
             * set the CreditCardNumberEditField's error state.
             */
            if (mCreditCardNumEditField.getText().length() == mCardIssuer.getFormattedLength()) {
                mCreditCardNumEditField.setErrorState();
            }
            /*
             * If the user is still editing the card, clear any errors.
             */
            else if (mCurrentState == CreditCardState.NUMBER_FIELD_FOCUSED_STATE ||
                    mCurrentState == CreditCardState.NUMBER_FIELD_EDIT_STATE) {
                mCreditCardNumEditField.clearErrors();
            }
            else {
                mCreditCardNumEditField.setErrorState();
            }
            mNumberCompleted = false;
        }
    }

    /**
     *
     */
    private void updateCreditCardType() {
        CreditCardUtilities.CardIssuer previousCardType = mCardIssuer;
        mCardIssuer = CreditCardUtilities.getCardIssuer(mCreditCardNumEditField.getRawCreditCardNumber());
        if (mCardIssuer != previousCardType) {
            cardTypeChanged();
            evaluateSecurityCode();
        }
    }

    /**
     *
     */
    private void cardTypeChanged() {
        if (mCardIssuer == CreditCardUtilities.CardIssuer.INVALID) {
            mSecCodeEditField.setEnabled(false);
            mSecCodeEditField.setFocusable(false);
            mSecCodeEditField.setFocusableInTouchMode(false);
        }
        else
        {
            mSecCodeEditField.setEnabled(true);
            mSecCodeEditField.setFocusable(true);
            mSecCodeEditField.setFocusableInTouchMode(true);
        }
        mCreditCardNumEditField.updateCardType(mCardIssuer, true);
        mSecCodeEditField.updateCardType(mCardIssuer, true);
    }

    /**
     *
     */
    private void evaluateSecurityCode() {
        if (mCardIssuer != CreditCardUtilities.CardIssuer.INVALID &&
                mSecCodeEditField.getText().length() == mCardIssuer.getSecurityLength()) {
            mSecCodeCompleted = true;
            if (isComplete() && !mIgnoringEvents){
                complete();
            }
            mSecCodeEditField.clearErrors();
        }
        else {
            mSecCodeCompleted = false;
            /*
             * If the user is currently editing or focused on the SecCodeEditField,
             * clear any errors.
             */
            if (mCurrentState == CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE ||
                    mCurrentState == CreditCardState.SEC_CODE_FIELD_EDIT_STATE) {
                mSecCodeEditField.clearErrors();
            }
            /*
             * If the user is not editing or focused on the SecCodeEditField and the length
             * of the code that has been entered is not equal to the length required for
             * their card type, then put the field into its error state.
             */
            else if (mSecCodeEditField.getText().length() != mCardIssuer.getSecurityLength()) {
                mSecCodeEditField.setErrorState();
            }
        }
    }

    /**
     *
     * @param expirationDate
     */
    private void evaluateExpDate(Date expirationDate) {
        if (expirationDate == null) {
            mExpDateCompleted = false;
            return;
        }
        mExpirationDate = expirationDate;
        Calendar today = Calendar.getInstance();
        Calendar expDate = Calendar.getInstance();
        expDate.setTime(expirationDate);
        /*
         * Check that the expiration date is not before the current date. If it is a valid date, clear
         * any errors.
         */
        if (today.get(Calendar.YEAR) < expDate.get(Calendar.YEAR) ||
                (today.get(Calendar.YEAR) == expDate.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) <= expDate.get(Calendar.MONTH))) {
            mExpDateEditField.clearErrors();
            mExpDateCompleted = true;
            if (isComplete() && !mIgnoringEvents){
                complete();
            }
        }
        /*
         * If the date is before the current date, set the ExpDateEditField's error state.
         */
        else {
            mExpDateEditField.setErrorState();
            mExpDateCompleted = false;
        }
    }

    /**
     * This is used to mask the numbers that have been entered into the SecCodeEditField, i.e. turn
     * letters into shoulder-surfer-baffling dots.
     * @param editText
     * @param transformationMethod
     */
    private void updateTransformationMethod(EditText editText, TransformationMethod transformationMethod) {
        if (editText != null) {
            int start, stop;
            start = editText.getSelectionStart();
            stop = editText.getSelectionEnd();
            editText.setTransformationMethod(transformationMethod);
            editText.setSelection(start, stop);
        }
    }

    /**
     *
     * @param creditCardModel
     */
    public void loadCreditCardInfoFromModel(CreditCardModel creditCardModel) {

        boolean wasIgnoringEvents = mIgnoringEvents;
        mIgnoringEvents = true;

        String dateFormat = mContext.getResources().getString(R.string.expiration_field_date_format);
        String dateString = CreditCardUtilities.getFormattedDate(dateFormat, creditCardModel.getExpirationDate());
        loadCreditCardInfo(creditCardModel.getCreditCardNumber(), creditCardModel.getExpirationDate(),
                dateString, creditCardModel.getSecurityCode());

        mIgnoringEvents = wasIgnoringEvents;
    }

    /**
     *
     *
     * Note: This method does not set the flag that tells the state machine to ignore events. It is
     * expected that the calling function will set the flag.
     *
     * @param creditCardNumber
     * @param expDate
     * @param expDateText
     * @param secCode
     */
    private void loadCreditCardInfo(String creditCardNumber, Date expDate, String expDateText, String secCode) {

        mCreditCardNumEditField.setText(creditCardNumber);
        mExpirationDate = expDate;
        mExpDateEditField.setText(expDateText);
        mSecCodeEditField.setText(secCode);
        evaluateCreditCardNumber();
        evaluateSecurityCode();
        evaluateExpDate(mExpirationDate);
        CreditCardLogger.i("debug", mNumberCompleted + ", " + mExpDateCompleted + ", " + mSecCodeCompleted);
    }

    /**
     *
     * @param savedInstanceState
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {

        CreditCardControllerState creditCardControllerState = new CreditCardControllerState();

        creditCardControllerState.currentState = mCurrentState.ordinal();
        creditCardControllerState.creditCardNumberText = mCreditCardNumEditField.getRawCreditCardNumber();
        creditCardControllerState.expDate = mExpirationDate;
        creditCardControllerState.expDateText = mExpDateEditField.getText().toString();
        creditCardControllerState.secCodeText = mSecCodeEditField.getText().toString();
        creditCardControllerState.numberTextHasBeenEntered = mNumberTextHasBeenEntered;
        creditCardControllerState.happyPathIsBroken = mHappyPathBroken;
        creditCardControllerState.cardIssuer = mCardIssuer.ordinal();

        byte[] creditCardControllerStateBytes = serializeObject(creditCardControllerState);
        if (creditCardControllerStateBytes != null) {
            savedInstanceState.putByteArray(CREDIT_CARD_CONTROLLER_STATE_KEY, creditCardControllerStateBytes);
        }
        else {
            CreditCardLogger.e(TAG, "onSaveInstanceState(): creditCardControllerStateBytes is null");
        }
    }

    /**
     *
     * Note: The order in which things are restored matters. Be careful when making changes.
     * @param savedInstanceState
     */
    public void onRestoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        /*
         * Tell the state machine to ignore events while the saved state
         * is being restored.
         */
        mIgnoringEvents = true;

        byte[] creditCardControllerStateBytes = savedInstanceState.getByteArray(CREDIT_CARD_CONTROLLER_STATE_KEY);
        CreditCardControllerState creditCardControllerState = null;
        if (creditCardControllerStateBytes != null) {
            creditCardControllerState = (CreditCardControllerState) deserializeObject(creditCardControllerStateBytes);
        }
        if (creditCardControllerState == null) {
            creditCardControllerState = new CreditCardControllerState();
        }

        mCurrentState = CreditCardState.values()[creditCardControllerState.currentState];
        mNumberTextHasBeenEntered = creditCardControllerState.numberTextHasBeenEntered;
        mHappyPathBroken = creditCardControllerState.happyPathIsBroken;
        mCardIssuer = CreditCardUtilities.CardIssuer
                .values()[creditCardControllerState.cardIssuer];
        if (mCardIssuer == CreditCardUtilities.CardIssuer.INVALID) {
            mSecCodeEditField.setEnabled(false);
            mSecCodeEditField.setFocusable(false);
            mSecCodeEditField.setFocusableInTouchMode(false);
        }
        else {
            mSecCodeEditField.setEnabled(true);
            mSecCodeEditField.setFocusable(true);
            mSecCodeEditField.setFocusableInTouchMode(true);
        }
        mCreditCardNumEditField.updateCardType(mCardIssuer, false);
        mSecCodeEditField.updateCardType(mCardIssuer, false);
        loadCreditCardInfo(creditCardControllerState.creditCardNumberText, creditCardControllerState.expDate,
                creditCardControllerState.expDateText, creditCardControllerState.secCodeText);

        /*
         * Refocus any fields that were previously focused.
         */
        if (mCurrentState == CreditCardState.NUMBER_FIELD_EDIT_STATE ||
                mCurrentState == CreditCardState.NUMBER_FIELD_FOCUSED_STATE) {
            mCreditCardNumEditField.requestFocus();
        }
        else if (mCurrentState == CreditCardState.SEC_CODE_FIELD_EDIT_STATE ||
                mCurrentState == CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE) {
            mSecCodeEditField.requestFocus();
            updateTransformationMethod(mSecCodeEditField, null);
        }
        /*
         * If the ExpirationPickerDialogFragment was open, a reference to it will need to be obtained
         * from the fragment manager.
         */
        ExpirationPickerDialogFragment expirationPickerDialogFragment = (ExpirationPickerDialogFragment) ((Activity) mContext).getFragmentManager()
                .findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        if (expirationPickerDialogFragment != null) {
            expirationPickerDialogFragment.setDatePickerListener(this);
        }

        /*
         * Now that the saved state has been restored, tell the state machine to
         * pay attention to events again.
         */
        mIgnoringEvents = false;
    }

    private byte[] serializeObject(Object object) {
        byte[] objectBytes = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectBytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            CreditCardLogger.e(TAG, e.getMessage());
        }
        return objectBytes;
    }

    private Object deserializeObject(byte[] objectBytes) {
        Object object = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            object = objectInputStream.readObject();
        } catch (IOException e) {
            CreditCardLogger.e(TAG, e.getMessage());
        } catch (ClassNotFoundException e) {
            CreditCardLogger.e(TAG, e.getMessage());
        }
        return object;
    }

    /**
     *
     * @param view
     * @param motionEvent
     * @return false so that the touch event is not consumed.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getClass().equals(CreditCardExpirationEditField.class) &&
                motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardNumberEditField.class) &&
                motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            handleEvent(CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        return false;
    }

    /**
     *
     */
    private void openDatePicker() {
        try {
            FragmentTransaction fragmentTransaction = ((Activity) mContext).getFragmentManager().beginTransaction();
            ExpirationPickerDialogFragment expirationPickerDialogFragment = ExpirationPickerDialogFragment
                    .newInstance(R.string.expiration_picker_default_title, mExpirationDate);

            expirationPickerDialogFragment.setDatePickerListener(this);
            expirationPickerDialogFragment.show(fragmentTransaction, ExpirationPickerDialogFragment.TAG);
            mDatePickerOpen = true;
        }
        catch (ClassCastException e) {
            CreditCardLogger.e(TAG, "Error: " + e);
        }
    }

    /**
     *
     * @param selectedDate
     */
    @Override
    public void onExpirationDateSelected(Date selectedDate) {
        mDatePickerOpen = false;
        mExpDateEditField.setExpirationDate(selectedDate);
        evaluateExpDate(selectedDate);
    }

    /**
     *
     */
    @Override
    public void onDialogPickerCanceled() {
        mDatePickerOpen = false;
    }

    /**
     * If the happy path has not been broken, the SecCodeEditField should be given focus and the keyboard
     * should automatically open. The keyboard cannot be opened while the date picker is open even if the
     * SecCodeEditField has focus because the ExpirationPickerDialogFragment's containing window will not
     * have focus. For this reason, the keyboard cannot be opened when a date has been selected. Instead,
     * the app must wait for the ExpirationPickerDialogFragment to be destroyed so its containing window
     * will no longer have focus.
     * Note: In order to open the keyboard for a specific TextView, that TextView's containing window
     * must have focus.
     */
    @Override
    public void onDestroy() {
        if (!mHappyPathBroken) {
            /*
             * Requesting focus will send an on_focus event
             */
            mSecCodeEditField.requestFocus();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSecCodeEditField, InputMethodManager.SHOW_IMPLICIT);
        }
        handleEvent(CreditCardEvent.CLOSE_DATE_PICKER_EVENT);
    }

    /**
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {/*NO OP*/ }

    /**
     *
     * @param text
     * @param start
     * @param lengthBefore
     * @param lengthAfter
     */
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        handleEvent(CreditCardEvent.TEXT_CHANGED_EVENT);
    }

    /**
     *
     * @param editable
     */
    @Override
    public void afterTextChanged(Editable editable) {/*NO OP*/ }

    /**
     *
     * @param view
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            handleEvent(CreditCardEvent.FOCUS_LOST_EVENT);
        }
        else if (view.getClass().equals(CreditCardNumberEditField.class)) {
            handleEvent(CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardExpirationEditField.class)) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardSecurityCodeEditField.class)) {
            handleEvent(CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        }
    }

    public CreditCardUtilities.CardIssuer getCardIssuer() {
        return mCardIssuer;
    }

    /**
     * The transition table has been implemented as a HashMap. Its entries represent states where
     * the key is the CreditCardState and the value is the HashMap of all of the valid transitions
     * for that state. The HashMap of transitions uses CreditCardEvents as keys and Transitions as
     * values.
     */
    private void initTransitionTable() {

        mTransitionMap = new HashMap<CreditCardState, Map<CreditCardEvent, Transition>>();

        /*
         *
         */
        Map<CreditCardEvent, Transition> idleStateMap = new HashMap<CreditCardEvent, Transition>();
        idleStateMap.put(CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                setCurrentState(CreditCardState.NUMBER_FIELD_FOCUSED_STATE);
                evaluateCreditCardNumber();
            }
        });
        idleStateMap.put(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathBroken = true;
                setCurrentState(CreditCardState.DATE_PICKER_OPEN_STATE);
                openDatePicker();
            }
        });
        idleStateMap.put(CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathBroken = true;
                updateTransformationMethod(mSecCodeEditField, null);
                setCurrentState(CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE);
                evaluateSecurityCode();
            }
        });
        idleStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                if (mCreditCardNumEditField.hasFocus()) {
                    mNumberTextHasBeenEntered = true;
                    setCurrentState(CreditCardState.NUMBER_FIELD_EDIT_STATE);
                    evaluateCreditCardNumber();
                }
                else if (mSecCodeEditField.hasFocus()) {
                    setCurrentState(CreditCardState.SEC_CODE_FIELD_EDIT_STATE);
                    evaluateSecurityCode();
                }
                // This shouldn't happen but handle it if it does.
                else if (mExpDateEditField.hasFocus()) {
                    setCurrentState(CreditCardState.DATE_PICKER_OPEN_STATE);
                    openDatePicker();
                }
            }
        });
        mTransitionMap.put(CreditCardState.IDLE_STATE, idleStateMap);

        /*
         *
         */
        Map<CreditCardEvent, Transition> numberFocusedStateMap = new HashMap<CreditCardEvent, Transition>();
        numberFocusedStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                mNumberTextHasBeenEntered = true;
                setCurrentState(CreditCardState.NUMBER_FIELD_EDIT_STATE);
                evaluateCreditCardNumber();
            }
        });
        numberFocusedStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathBroken = true;
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateCreditCardNumber();
            }
        });
        mTransitionMap.put(CreditCardState.NUMBER_FIELD_FOCUSED_STATE, numberFocusedStateMap);

        /*
         *
         */
        Map<CreditCardEvent, Transition> numberEditStateMap = new HashMap<CreditCardEvent, Transition>();
        numberEditStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                evaluateCreditCardNumber();
            }
        });
        numberEditStateMap.put(CreditCardEvent.CREDIT_CARD_NUMBER_VALIDATED_EVENT, new Transition() {
            @Override
            public void execute() {
                if (!mHappyPathBroken) {
                    setCurrentState(CreditCardState.DATE_PICKER_OPEN_STATE);
                    openDatePicker();
                }
                else {
                    setCurrentState(CreditCardState.IDLE_STATE);
                }
            }
        });
        numberEditStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathBroken = true;
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateCreditCardNumber();
            }
        });
        mTransitionMap.put(CreditCardState.NUMBER_FIELD_EDIT_STATE, numberEditStateMap);

        /*
         *
         */
        Map<CreditCardEvent, Transition> datePickerOpenStateMap = new HashMap<CreditCardEvent, Transition>();
        datePickerOpenStateMap.put(CreditCardEvent.CLOSE_DATE_PICKER_EVENT, new Transition() {
            @Override
            public void execute() {
                setCurrentState(CreditCardState.IDLE_STATE);
            }
        });
        datePickerOpenStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                if (!mDatePickerOpen) {
                    mHappyPathBroken = true;
                    setCurrentState(CreditCardState.IDLE_STATE);
                }
            }
        });
        datePickerOpenStateMap.put(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                if (!mDatePickerOpen) {
                    openDatePicker();
                }
            }
        });
        mTransitionMap.put(CreditCardState.DATE_PICKER_OPEN_STATE, datePickerOpenStateMap);

        /*
         *
         */
        Map<CreditCardEvent, Transition> secCodeFocusedStateMap = new HashMap<CreditCardEvent, Transition>();
        secCodeFocusedStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                setCurrentState(CreditCardState.SEC_CODE_FIELD_EDIT_STATE);
                evaluateSecurityCode();
            }
        });
        secCodeFocusedStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathBroken = true;
                updateTransformationMethod(mSecCodeEditField, PasswordTransformationMethod.getInstance());
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateSecurityCode();
            }
        });
        mTransitionMap.put(CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE, secCodeFocusedStateMap);

        /*
         *
         */
        Map<CreditCardEvent, Transition> secCodeEditStateMap = new HashMap<CreditCardEvent, Transition>();
        secCodeEditStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                evaluateSecurityCode();
            }
        });
        secCodeEditStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathBroken = true;
                updateTransformationMethod(mSecCodeEditField, PasswordTransformationMethod.getInstance());
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateSecurityCode();
            }
        });
        mTransitionMap.put(CreditCardState.SEC_CODE_FIELD_EDIT_STATE, secCodeEditStateMap);
    }

    /******************************
     * Methods for unit testing
     ******************************/

    public CreditCardState getCurrentState() {
        return mCurrentState;
    }

    public Date getExpirationDate() {
        return mExpirationDate;
    }

    public boolean isHappyPathBroken() {
        return mHappyPathBroken;
    }

    public void setHappyPathBroken(boolean broken) {
        mHappyPathBroken = broken;
    }

    public void setDatePickerOpen(boolean open) {
        mDatePickerOpen = open;
    }

    public boolean isDatePickerOpen() {
        return mDatePickerOpen;
    }
}
