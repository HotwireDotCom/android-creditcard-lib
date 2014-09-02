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
    private enum CreditCardEvent {
        NUMBER_FIELD_ON_FOCUS_EVENT,
        CREDIT_CARD_NUMBER_VALIDATED_EVENT,
        EXP_DATE_FIELD_ON_FOCUS_EVENT,
        OPEN_DATE_PICKER_EVENT,
        CLOSE_DATE_PICKER_EVENT,
        EXP_DATE_VALIDATED_EVENT,
        SEC_CODE_FIELD_ON_FOCUS_EVENT,
        SEC_CODE_VALIDATED_EVENT,
        TEXT_CHANGED_EVENT,
        FOCUS_LOST_EVENT
    }

    public interface CreditCardModelCompleteListener {
        void onCreditCardModelComplete(CreditCardModel creditCardModel);
    }

    private Map<CreditCardState, Map<CreditCardEvent, Transition>> mTransitionMap;

    private boolean mHappyPathIsBroken;
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
        mHappyPathIsBroken = false;
        mCardIssuer = CreditCardUtilities.CardIssuer.INVALID;
        initTransitionTable();

        InputFilter creditCardNumFilter = new CreditCardInputFilter(mCardIssuer.getOffset(),
                mCardIssuer.getModulo(),
                mCardIssuer.getFormattedLength());
        mCreditCardNumEditField.setFilters(new InputFilter[] {creditCardNumFilter});

        if (mCreditCardNumEditField.hasFocus()) {
            handleEvent(CreditCardEvent.NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        else if (mExpDateEditField.hasFocus()) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
    }

    private void setCurrentState(CreditCardState state) {
        Log.i("debug", "Setting current state: " + state.toString());
        mCurrentState = state;
    }

    private void handleEvent(CreditCardEvent event) {
        Transition transition = mTransitionMap.get(mCurrentState).get(event);
        if (transition != null && !mIgnoringEvents) {
            Log.i("debug", "Handling event: " + event.toString() + " for state: " + mCurrentState.toString());
            transition.execute();
        }
        else if (LOGGING_ENABLED) {
            Log.i(TAG, "Ignoring event: " + event.toString() + " for state: " + mCurrentState.toString());
        }
    }

    public void setCreditCardModelCompleteListener(CreditCardModelCompleteListener creditCardModelCompleteListener) {
        mCreditCardModelCompleteListener = creditCardModelCompleteListener;
    }

    private void evaluateCreditCardNumber() {
        getCreditCardType();
        // Reset the happy path and all of the fields.
        if (mCreditCardNumEditField.getRawCreditCardNumber().isEmpty() && mNumberTextHasBeenEntered) {
            mHappyPathIsBroken = false;
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
            if (mCreditCardNumEditField.getText().length() == mCardIssuer.getFormattedLength()) {
                mCreditCardNumEditField.setErrorState();
            }
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

    private void getCreditCardType() {
        CreditCardUtilities.CardIssuer previousCardType = mCardIssuer;
        mCardIssuer = CreditCardUtilities.getCardIssuer(mCreditCardNumEditField.getRawCreditCardNumber());
        if (mCardIssuer != previousCardType) {
            cardTypeChanged();
            evaluateSecurityCode();
        }
    }

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
            if (mCurrentState == CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE ||
                    mCurrentState == CreditCardState.SEC_CODE_FIELD_EDIT_STATE) {
                mSecCodeEditField.clearErrors();
            }
            else if (mSecCodeEditField.getText().length() != mCardIssuer.getSecurityLength()) {
                mSecCodeEditField.setErrorState();
            }
        }
    }

    private void updateTransformationMethod(EditText editText, TransformationMethod transformationMethod) {
        if (editText != null) {
            int start, stop;
            start = editText.getSelectionStart();
            stop = editText.getSelectionEnd();
            editText.setTransformationMethod(transformationMethod);
            editText.setSelection(start, stop);
        }
    }

    private void evaluateExpDate(Date expirationDate) {
        if (expirationDate == null) {
            mExpDateCompleted = false;
            return;
        }
        mExpirationDate = expirationDate;
        Calendar today = Calendar.getInstance();
        Calendar expDate = Calendar.getInstance();
        expDate.setTime(expirationDate);
        if (today.get(Calendar.YEAR) < expDate.get(Calendar.YEAR) ||
                (today.get(Calendar.YEAR) == expDate.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) <= expDate.get(Calendar.MONTH))) {
            mExpDateEditField.clearErrors();
            mExpDateCompleted = true;
            if (isComplete() && !mIgnoringEvents){
                complete();
            }
        }
        else {
            mExpDateEditField.setErrorState();
            mExpDateCompleted = false;
        }
    }

    public void complete() {
        if (mNumberCompleted && mExpDateCompleted && mSecCodeCompleted && mCreditCardModelCompleteListener != null) {
            mCreditCardModelCompleteListener.onCreditCardModelComplete(new CreditCardModel(
                    mCreditCardNumEditField.getRawCreditCardNumber(),
                    mExpirationDate, mSecCodeEditField.getText().toString()));
        }
    }

    public boolean isComplete() {
        if (mNumberCompleted && mExpDateCompleted && mSecCodeCompleted) {
            return true;
        }
        else {
            return false;
        }
    }

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
        savedInstanceState.putBoolean(HAPPY_PATH_IS_BROKEN_KEY, mHappyPathIsBroken);
        savedInstanceState.putInt(CARD_ISSUER_KEY, mCardIssuer.ordinal());
    }

    public void onRestoreSavedInstanceState(Bundle savedInstanceState) {
        mIgnoringEvents = true;

        mCurrentState = CreditCardState.values()[savedInstanceState.getInt(CURRENT_STATE_KEY, 0)];
        String savedDateText = savedInstanceState.getString(EXP_DATE_TEXT_KEY, "");
        mExpDateEditField.setText(savedDateText);
        mNumberCompleted = savedInstanceState.getBoolean(NUMBER_COMPLETED_KEY, false);
        mNumberTextHasBeenEntered = savedInstanceState.getBoolean(NUMBER_TEXT_HAS_BEEN_ENTERED_KEY, false);
        mExpDateCompleted = savedInstanceState.getBoolean(EXP_DATE_COMPLETED_KEY, false);
        mSecCodeCompleted = savedInstanceState.getBoolean(SEC_CODE_COMPLETED_KEY, false);
        mHappyPathIsBroken = savedInstanceState.getBoolean(HAPPY_PATH_IS_BROKEN_KEY, false);
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
        if (mCurrentState == CreditCardState.NUMBER_FIELD_EDIT_STATE ||
                mCurrentState == CreditCardState.NUMBER_FIELD_FOCUSED_STATE) {
            mCreditCardNumEditField.requestFocus();
        }
        else if (mCurrentState == CreditCardState.SEC_CODE_FIELD_EDIT_STATE ||
                mCurrentState == CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE) {
            mSecCodeEditField.requestFocus();
        }
        mExpirationPickerDialogFragment = (ExpirationPickerDialogFragment) ((Activity) mContext).getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        if (mExpirationPickerDialogFragment != null) {
            mExpirationPickerDialogFragment.setDatePickerDestroyedListener(this);
            mExpirationPickerDialogFragment.setDatePickerListener(this);
        }
        evaluateCreditCardNumber();
        evaluateSecurityCode();

        if (mSecCodeEditField.hasFocus()) {
            updateTransformationMethod(mSecCodeEditField, null);
        }
        mIgnoringEvents = false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getClass().equals(CreditCardExpirationEditField.class) &&
                motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardNumberEditField.class) &&
                motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            handleEvent(CreditCardEvent.NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        return false;
    }

    @Override
    public void onExpirationDateSelected(Date selectedDate) {
        mDatePickerOpen = false;
        mExpDateEditField.setExpirationDate(selectedDate);
        evaluateExpDate(selectedDate);
    }

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

    @Override
    public void datePickerDestroyed() {
        if (!mHappyPathIsBroken) {
            mSecCodeEditField.requestFocus();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSecCodeEditField, InputMethodManager.SHOW_IMPLICIT);
        }
        else {
            handleEvent(CreditCardEvent.CLOSE_DATE_PICKER_EVENT);
        }
    }

    @Override
    public void onDialogPickerCanceled() {
        mDatePickerOpen = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // NO OP
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        handleEvent(CreditCardEvent.TEXT_CHANGED_EVENT);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // NO OP
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            handleEvent(CreditCardEvent.FOCUS_LOST_EVENT);
        }
        else if (view.getClass().equals(CreditCardNumberEditField.class)) {
            handleEvent(CreditCardEvent.NUMBER_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardExpirationEditField.class)) {
            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
        else if (view.getClass().equals(CreditCardSecurityCodeEditField.class)) {
            handleEvent(CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        }
    }

    private void initTransitionTable() {

        mTransitionMap = new HashMap<CreditCardState, Map<CreditCardEvent, Transition>>();

        Map<CreditCardEvent, Transition> idleStateMap = new HashMap<CreditCardEvent, Transition>();
        idleStateMap.put(CreditCardEvent.NUMBER_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                setCurrentState(CreditCardState.NUMBER_FIELD_FOCUSED_STATE);
                evaluateCreditCardNumber();
            }
        });
        idleStateMap.put(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                setCurrentState(CreditCardState.DATE_PICKER_OPEN_STATE);
                openDatePicker();
            }
        });
        idleStateMap.put(CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
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
                mHappyPathIsBroken = true;
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateCreditCardNumber();
            }
        });
        mTransitionMap.put(CreditCardState.NUMBER_FIELD_FOCUSED_STATE, numberFocusedStateMap);


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
                if (!mHappyPathIsBroken) {
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
                mHappyPathIsBroken = true;
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateCreditCardNumber();
            }
        });
        mTransitionMap.put(CreditCardState.NUMBER_FIELD_EDIT_STATE, numberEditStateMap);

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
                    mHappyPathIsBroken = true;
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

        // Set cvv focused transitions
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
                mHappyPathIsBroken = true;
                updateTransformationMethod(mSecCodeEditField, PasswordTransformationMethod.getInstance());
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateSecurityCode();
            }
        });
        mTransitionMap.put(CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE, secCodeFocusedStateMap);

        // Set cvv edit transitions
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
                mHappyPathIsBroken = true;
                updateTransformationMethod(mSecCodeEditField, PasswordTransformationMethod.getInstance());
                setCurrentState(CreditCardState.IDLE_STATE);
                evaluateSecurityCode();
            }
        });
        mTransitionMap.put(CreditCardState.SEC_CODE_FIELD_EDIT_STATE, secCodeEditStateMap);
    }
}
