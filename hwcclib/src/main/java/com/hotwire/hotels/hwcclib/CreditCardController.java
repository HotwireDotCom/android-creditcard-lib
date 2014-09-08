package com.hotwire.hotels.hwcclib;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by epark on 8/19/14.
 */
public class CreditCardController implements View.OnFocusChangeListener, TextWatcher, ExpirationPickerListener, View.OnTouchListener, ExpirationPickerDialogFragment.DatePickerDestroyedListener {

    public static final String TAG = "CreditCardController";
    public static boolean LOGGING_ENABLED = true;
    private static final String CURRENT_STATE_KEY = CreditCardController.class.getCanonicalName() + ".current_state_key";
    private static final String CREDIT_CARD_NUMBER_TEXT_KEY = CreditCardController.class.getCanonicalName() + ".credit_card_number_text_key";
    private static final String EXP_DATE_KEY = CreditCardController.class.getCanonicalName() + ".exp_date_key";
    private static final String EXP_DATE_TEXT_KEY = CreditCardController.class.getCanonicalName() + ".exp_date_text_key";
    private static final String SEC_CODE_TEXT_KEY = CreditCardController.class.getCanonicalName() + ".sec_code_text_key";
    private static final String NUMBER_COMPLETED_KEY = CreditCardController.class.getCanonicalName() + ".number_completed_key";
    private static final String NUMBER_TEXT_HAS_BEEN_ENTERED_KEY = CreditCardController.class.getCanonicalName() + ".number_completed_key";
    private static final String EXP_DATE_COMPLETED_KEY = CreditCardController.class.getCanonicalName() + ".exp_date_completed_key";
    private static final String SEC_CODE_COMPLETED_KEY = CreditCardController.class.getCanonicalName() + ".sec_code_completed_key";
    private static final String HAPPY_PATH_IS_BROKEN_KEY = CreditCardController.class.getCanonicalName() + ".happy_path_is_broken_key";
    private static final String CARD_ISSUER_KEY = CreditCardController.class.getCanonicalName() + ".card_issuer_key";

    /**
     *
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
     *
     */
    public enum CreditCardEvent {
        CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT,
        CREDIT_CARD_NUMBER_VALIDATED_EVENT,
        EXP_DATE_FIELD_ON_FOCUS_EVENT,
        CLOSE_DATE_PICKER_EVENT,
        SEC_CODE_FIELD_ON_FOCUS_EVENT,
        TEXT_CHANGED_EVENT,
        FOCUS_LOST_EVENT
    }

    public interface CreditCardModelCompleteListener {
        void onCreditCardModelComplete(CreditCardModel creditCardModel);
    }

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
    private ExpirationPickerDialogFragment mExpirationPickerDialogFragment;

    public interface Transition {
        void execute();
    }

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
            handleEvent(CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
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
        if (LOGGING_ENABLED) {
            Log.i(TAG, "Setting current state: " + state.toString());
        }
        mCurrentState = state;
    }

    /**
     *
     * @param event
     */
    public void handleEvent(CreditCardEvent event) {
        Transition transition = mTransitionMap.get(mCurrentState).get(event);
        if (transition != null && !mIgnoringEvents) {
            if (LOGGING_ENABLED) {
                Log.i(TAG, "Handling event: " + event.toString() + " for state: " + mCurrentState.toString());
            }
            transition.execute();
        }
        else if (LOGGING_ENABLED) {
            Log.i(TAG, "Ignoring event: " + event.toString() + " for state: " + mCurrentState.toString());
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
            mCreditCardModelCompleteListener.onCreditCardModelComplete(new CreditCardModel(
                    mCreditCardNumEditField.getRawCreditCardNumber(),
                    mExpirationDate, mSecCodeEditField.getText().toString()));
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
        getCreditCardType();
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
    private void getCreditCardType() {
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
        }
        else
        {
            mSecCodeEditField.setEnabled(true);
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
     * @param savedInstanceState
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(CURRENT_STATE_KEY, mCurrentState.ordinal());
        savedInstanceState.putString(CREDIT_CARD_NUMBER_TEXT_KEY, mCreditCardNumEditField.getRawCreditCardNumber());
        if (mExpirationDate != null) {
            savedInstanceState.putLong(EXP_DATE_KEY, mExpirationDate.getTime());
        }
        savedInstanceState.putString(SEC_CODE_TEXT_KEY, mSecCodeEditField.getText().toString());
        savedInstanceState.putBoolean(NUMBER_COMPLETED_KEY, mNumberCompleted);
        savedInstanceState.putBoolean(NUMBER_TEXT_HAS_BEEN_ENTERED_KEY, mNumberTextHasBeenEntered);
        savedInstanceState.putBoolean(EXP_DATE_COMPLETED_KEY, mExpDateCompleted);
        savedInstanceState.putString(EXP_DATE_TEXT_KEY, mExpDateEditField.getText().toString());
        savedInstanceState.putBoolean(SEC_CODE_COMPLETED_KEY, mSecCodeCompleted);
        savedInstanceState.putBoolean(HAPPY_PATH_IS_BROKEN_KEY, mHappyPathBroken);
        savedInstanceState.putInt(CARD_ISSUER_KEY, mCardIssuer.ordinal());
    }

    /**
     *
     * Note: The order in which things are restored matters. Be careful when making changes.
     * @param savedInstanceState
     */
    public void onRestoreSavedInstanceState(Bundle savedInstanceState) {
        /*
         * Tell the state machine to ignore events while the saved state
         * is being restored.
         */
        mIgnoringEvents = true;

        mCurrentState = CreditCardState.values()[savedInstanceState.getInt(CURRENT_STATE_KEY, 0)];
        String savedDateText = savedInstanceState.getString(EXP_DATE_TEXT_KEY, "");
        mExpDateEditField.setText(savedDateText);
        mNumberCompleted = savedInstanceState.getBoolean(NUMBER_COMPLETED_KEY, false);
        mNumberTextHasBeenEntered = savedInstanceState.getBoolean(NUMBER_TEXT_HAS_BEEN_ENTERED_KEY, false);
        mExpDateCompleted = savedInstanceState.getBoolean(EXP_DATE_COMPLETED_KEY, false);
        mSecCodeCompleted = savedInstanceState.getBoolean(SEC_CODE_COMPLETED_KEY, false);
        mHappyPathBroken = savedInstanceState.getBoolean(HAPPY_PATH_IS_BROKEN_KEY, false);
        mCardIssuer = CreditCardUtilities.CardIssuer
                .values()[savedInstanceState.getInt(CARD_ISSUER_KEY,
                CreditCardUtilities.CardIssuer.INVALID.ordinal())];
        if (mCardIssuer == CreditCardUtilities.CardIssuer.INVALID) {
            mSecCodeEditField.setEnabled(false);
        }
        else {
            mSecCodeEditField.setEnabled(true);
        }
        mCreditCardNumEditField.updateCardType(mCardIssuer, false);
        mSecCodeEditField.updateCardType(mCardIssuer, false);
        // need to determine the card issuer before setting the text on the number and security code fields
        mCreditCardNumEditField.setText(savedInstanceState
                .getString(CREDIT_CARD_NUMBER_TEXT_KEY, ""));
        mSecCodeEditField.setText(savedInstanceState.getString(SEC_CODE_TEXT_KEY, ""));
        mSecCodeEditField.updateCardType(mCardIssuer, false);
        mCreditCardNumEditField.updateCardType(mCardIssuer, false);

        long savedDate = savedInstanceState.getLong(EXP_DATE_KEY, new Date().getTime());
        mExpirationDate = new Date(savedDate);
        evaluateExpDate(mExpirationDate);

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
        }
        /*
         * If the ExpirationPickerDialogFragment was open, a reference to it will need to be obtained
         * from the fragment manager.
         */
        mExpirationPickerDialogFragment = (ExpirationPickerDialogFragment) ((Activity) mContext).getFragmentManager()
                .findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        if (mExpirationPickerDialogFragment != null) {
            mExpirationPickerDialogFragment.setDatePickerDestroyedListener(this);
            mExpirationPickerDialogFragment.setDatePickerListener(this);
        }
        /*
         * Call the evaluators to set the appropriate error states on the CreditCardNumberEditField and
         * the SecCodeEditField.
         */
        evaluateCreditCardNumber();
        evaluateSecurityCode();

        if (mSecCodeEditField.hasFocus()) {
            updateTransformationMethod(mSecCodeEditField, null);
        }

        /*
         * Now that the saved state has been restored, tell the state machine to
         * pay attention to events again.
         */
        mIgnoringEvents = false;
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
            handleEvent(CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        return false;
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
    private void openDatePicker() {
        try {
            FragmentTransaction fragmentTransaction = ((Activity) mContext).getFragmentManager().beginTransaction();
            mExpirationPickerDialogFragment = ExpirationPickerDialogFragment.newInstance(R.string.expiration_picker_default_title, mExpirationDate);

            mExpirationPickerDialogFragment.setDatePickerDestroyedListener(this);
            mExpirationPickerDialogFragment.setDatePickerListener(this);
            mExpirationPickerDialogFragment.show(fragmentTransaction, ExpirationPickerDialogFragment.TAG);
            mDatePickerOpen = true;
        }
        catch (ClassCastException e) {
            if (LOGGING_ENABLED) {
                Log.e(TAG, "Error: " + e);
            }
        }
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
    public void datePickerDestroyed() {
        if (!mHappyPathBroken) {
            mSecCodeEditField.requestFocus();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSecCodeEditField, InputMethodManager.SHOW_IMPLICIT);
        }
        else {
            handleEvent(CreditCardEvent.CLOSE_DATE_PICKER_EVENT);
        }
    }

    /**
     *
     */
    @Override
    public void onDialogPickerCanceled() {
        mDatePickerOpen = false;
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
            handleEvent(CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardExpirationEditField.class)) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardSecurityCodeEditField.class)) {
            handleEvent(CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        }
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
        idleStateMap.put(CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT, new Transition() {
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

    public boolean hasNumberBeenEntered() {
        return mNumberTextHasBeenEntered;
    }

    public boolean isDatePickerOpen() {
        return mDatePickerOpen;
    }

    public boolean isNumberCompleted() {
        return mNumberCompleted;
    }

    public boolean isExpDateCompleted() {
        return mExpDateCompleted;
    }

    public boolean isSecCodeCompleted() {
        return mSecCodeCompleted;
    }

}
