package com.hotwire.hotels.hwcclib;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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
 * Created by a-elpark on 8/19/14.
 */
public class CreditCardController implements View.OnFocusChangeListener, TextWatcher, ExpirationPickerListener, View.OnTouchListener {

    private static final String CURRENT_STATE_KEY = CreditCardController.class.getCanonicalName() + ".current_state_key";
    private static final String CREDIT_CARD_NUMBER_TEXT_KEY = CreditCardController.class.getCanonicalName() + ".credit_card_number_text_key";
    private static final String EXP_DATE_KEY = CreditCardController.class.getCanonicalName() + ".exp_date_key";
    private static final String SEC_CODE_TEXT_KEY = CreditCardController.class.getCanonicalName() + ".sec_code_text_key";
    private static final String NUMBER_COMPLETED_KEY = CreditCardController.class.getCanonicalName() + ".number_completed_key";
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
        SET_DATE_EVENT,
        CANCEL_DATE_EVENT,
        EXP_DATE_VALIDATED_EVENT,
        SEC_CODE_FIELD_ON_FOCUS_EVENT,
        SEC_CODE_VALIDATED_EVENT,
        TEXT_CHANGED_EVENT,
        FOCUS_LOST_EVENT
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
    private boolean mRestoringState;
    private boolean mDatePickerOpen;

    private CreditCardNumberEditField mCreditCardNumEditField;
    private CreditCardExpirationEditField mExpDateEditField;
    private CreditCardSecurityCodeEditField mSecCodeEditField;

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

        mSecCodeEditField.setEnabled(false);

        mCreditCardNumEditField.setNextFocusDownId(mExpDateEditField.getId());
        mCreditCardNumEditField.setNextFocusRightId(mExpDateEditField.getId());

        mDatePickerOpen = false;
        mRestoringState = false;
        mExpirationDate = null;
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
        } else if (mExpDateEditField.hasFocus()) {

            handleEvent(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        }
    }

    private void setCurrentState(CreditCardState state) {
        Log.d("debug", "Setting current state: " + state.toString());
        mCurrentState = state;
    }

    private void handleEvent(CreditCardEvent event) {
        Transition transition = mTransitionMap.get(mCurrentState).get(event);
        if (transition != null && !mRestoringState) {
            Log.d("debug", "Handling event: " + event.toString() + " for state: " + mCurrentState.toString());
            transition.execute();
        } else {
            // TODO logging
            Log.e("debug", "Ignoring event: " + event.toString() + " for state: " + mCurrentState.toString());
        }
    }

    private void evaluateCreditCardNumber() {
        getCreditCardType();
        if (CreditCardUtilities.isValidCreditCard(mCreditCardNumEditField.getRawCreditCardNumber()) &&
                CreditCardUtilities.isValidUsingLuhn(mCreditCardNumEditField.getRawCreditCardNumber())) {
                mNumberCompleted = true;
                mCreditCardNumEditField.clearErrors();
                handleEvent(CreditCardEvent.CREDIT_CARD_NUMBER_VALIDATED_EVENT);
        }
        else {
            if (mCreditCardNumEditField.getText().length() == mCardIssuer.getFormattedLength()) {
                mCreditCardNumEditField.setErrorState();
            }
            else {
                mCreditCardNumEditField.clearErrors();
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
        if (mCardIssuer != CreditCardUtilities.CardIssuer.INVALID) {
            mSecCodeEditField.setEnabled(true);
        }
        else
        {
            mSecCodeEditField.setEnabled(false);
        }

        mCreditCardNumEditField.updateCardType(mCardIssuer);
        mSecCodeEditField.updateCardType(mCardIssuer);

        InputFilter secCodeFilter = new InputFilter.LengthFilter(mCardIssuer.getSecurityLength());
        mSecCodeEditField.setFilters(new InputFilter[]{secCodeFilter});
    }

    private void evaluateSecurityCode() {
        if (mCardIssuer != CreditCardUtilities.CardIssuer.INVALID &&
                mSecCodeEditField.getText().length() == mCardIssuer.getSecurityLength()) {
            mSecCodeCompleted = true;
            mSecCodeEditField.clearErrors();
            handleEvent(CreditCardEvent.SEC_CODE_VALIDATED_EVENT);
        }
        else {
            mSecCodeCompleted = false;
            if (mSecCodeEditField.getText().length() != mCardIssuer.getSecurityLength() &&
                    mCurrentState != CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE &&
                    mCurrentState != CreditCardState.SEC_CODE_FIELD_EDIT_STATE) {
                mSecCodeEditField.setErrorState();
            }
        }
    }

    private void openDatePicker() {
        Log.wtf("debug", "Date picker open: " + mCurrentState.name());
        try {
            FragmentTransaction fragmentTransaction = ((Activity) mContext).getFragmentManager().beginTransaction();
            ExpirationPickerDialogFragment mExpirationPickerDialogFragment;
            if (mExpirationDate == null) {
                mExpirationDate = new Date();
            }
            mExpirationPickerDialogFragment = ExpirationPickerDialogFragment.newInstance(R.string.expiration_picker_default_title, mExpirationDate);
            mExpirationPickerDialogFragment.addDatePickerListener(this);
            mExpirationPickerDialogFragment.show(fragmentTransaction, ExpirationPickerDialogFragment.TAG);
            mDatePickerOpen = true;
        } catch (ClassCastException e) {
            // TODO how to log errors properly?
            Log.d("debug", "Error: " + e);
        }
    }

    public CreditCardModel save() {
        if (mNumberCompleted && mExpDateCompleted && mSecCodeCompleted) {
            return new CreditCardModel(mCreditCardNumEditField.getRawCreditCardNumber(),
                    mExpDateEditField.getText().toString(), mSecCodeEditField.getText().toString());
        }
        else {
            return null;
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
        savedInstanceState.putBoolean(EXP_DATE_COMPLETED_KEY, mExpDateCompleted);
        savedInstanceState.putBoolean(SEC_CODE_COMPLETED_KEY, mSecCodeCompleted);
        savedInstanceState.putBoolean(HAPPY_PATH_IS_BROKEN_KEY, mHappyPathIsBroken);
        savedInstanceState.putInt(CARD_ISSUER_KEY, mCardIssuer.ordinal());
    }

    public void onRestoreSavedInstanceState(Bundle savedInstanceState) {

        /*
         * Don't respond to textChanged, focus or other events while restoring state.
         */
        mRestoringState = true;

        mCurrentState = CreditCardState.values()[savedInstanceState.getInt(CURRENT_STATE_KEY, 0)];
        mCreditCardNumEditField.setText(savedInstanceState
                .getString(CREDIT_CARD_NUMBER_TEXT_KEY, ""));
        /*
        String secCodeText = savedInstanceState.getString(SEC_CODE_TEXT_KEY, "");
        if (secCodeText != null && !secCodeText.isEmpty()) {
            mSecCodeEditField.setText(secCodeText);
        }
        */
        mSecCodeEditField.setText(savedInstanceState.getString(SEC_CODE_TEXT_KEY, ""));
        mNumberCompleted = savedInstanceState.getBoolean(NUMBER_COMPLETED_KEY, false);
        mExpDateCompleted = savedInstanceState.getBoolean(EXP_DATE_COMPLETED_KEY, false);
        mSecCodeCompleted = savedInstanceState.getBoolean(SEC_CODE_COMPLETED_KEY, false);
        mHappyPathIsBroken = savedInstanceState.getBoolean(HAPPY_PATH_IS_BROKEN_KEY, false);
        mCardIssuer = CreditCardUtilities.CardIssuer
                .values()[savedInstanceState.getInt(CARD_ISSUER_KEY,
                CreditCardUtilities.CardIssuer.INVALID.ordinal())];

        long savedDate = savedInstanceState.getLong(EXP_DATE_KEY, -1);
        if (savedDate == -1) {
            mExpirationDate = null; // TODO new date
        } else {
            mExpirationDate = new Date(savedDate);
            mExpDateEditField.setExpirationDate(mExpirationDate);
        }

        if (mCurrentState == CreditCardState.NUMBER_FIELD_EDIT_STATE ||
                mCurrentState == CreditCardState.NUMBER_FIELD_FOCUSED_STATE) {
            mCreditCardNumEditField.requestFocus();
        }
        else if (mCurrentState == CreditCardState.SEC_CODE_FIELD_EDIT_STATE ||
                mCurrentState == CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE) {
            mSecCodeEditField.requestFocus();
        }
        evaluateCreditCardNumber();
        evaluateSecurityCode();

        mRestoringState = false;
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
        boolean validDate = false;
        if (today.get(Calendar.YEAR) < expDate.get(Calendar.YEAR) ||
                (today.get(Calendar.YEAR) == expDate.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) <= expDate.get(Calendar.MONTH))) {
            validDate = true;
        }
        if (validDate) {
            mExpDateEditField.clearErrors();
            mExpDateCompleted = true;
        }
        else {
            mExpDateCompleted = false;
        }
        handleEvent(CreditCardEvent.SET_DATE_EVENT);
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

    @Override
    public void onDialogPickerCanceled() {
        mDatePickerOpen = false;
        handleEvent(CreditCardEvent.CANCEL_DATE_EVENT);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // NO OP
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        Log.d("debug", "Before Text Changed: " + mCurrentState.toString());
        // TODO better place to evaluate
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
                mCreditCardNumEditField.clearErrors();
                // TODO new
                mCreditCardNumEditField.clearErrors();
                setCurrentState(CreditCardState.NUMBER_FIELD_FOCUSED_STATE);
            }
        });
        idleStateMap.put(CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                mExpDateEditField.clearErrors();
                setCurrentState(CreditCardState.DATE_PICKER_OPEN_STATE);
                openDatePicker();
            }
        });
        idleStateMap.put(CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                mSecCodeEditField.clearErrors();
                setCurrentState(CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE);
            }
        });
        idleStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                if (mCreditCardNumEditField.hasFocus()) {
                    evaluateCreditCardNumber();
                    setCurrentState(CreditCardState.NUMBER_FIELD_EDIT_STATE);
                }
                else if (mSecCodeEditField.hasFocus()) {
                    evaluateSecurityCode();
                    setCurrentState(CreditCardState.SEC_CODE_FIELD_EDIT_STATE);
                }
                else if (mExpDateEditField.hasFocus()) { // TODO this shouldn't happen
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
                evaluateCreditCardNumber();
                setCurrentState(CreditCardState.NUMBER_FIELD_EDIT_STATE);
            }
        });
        numberFocusedStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                evaluateCreditCardNumber();
                if (!mNumberCompleted) {
                    // TODO move this
                    mCreditCardNumEditField.setErrorState();
                }
                setCurrentState(CreditCardState.IDLE_STATE);
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
                    mExpDateEditField.clearErrors();
                    setCurrentState(CreditCardState.DATE_PICKER_OPEN_STATE);
                    openDatePicker();
                } else {
                    setCurrentState(CreditCardState.IDLE_STATE);
                }
            }
        });
        numberEditStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                evaluateCreditCardNumber();
                // TODO remove this
                if (!mNumberCompleted) {
                    mCreditCardNumEditField.setErrorState();
                }
                setCurrentState(CreditCardState.IDLE_STATE);
            }
        });
        mTransitionMap.put(CreditCardState.NUMBER_FIELD_EDIT_STATE, numberEditStateMap);

        Map<CreditCardEvent, Transition> datePickerOpenStateMap = new HashMap<CreditCardEvent, Transition>();
        datePickerOpenStateMap.put(CreditCardEvent.SET_DATE_EVENT, new Transition() {
            @Override
            public void execute() {
                if (mExpDateCompleted) {
                    if (!mHappyPathIsBroken) {
                        mSecCodeEditField.requestFocus();
                        // TODO transition?
                        // TODO open keyboard
                    } else if (mNumberCompleted && mSecCodeCompleted) {
                        setCurrentState(CreditCardState.IDLE_STATE);
                    }
                } else {
                    mHappyPathIsBroken = true;
                    // TODO move to eval function
                    if (!mExpDateCompleted) {
                        mExpDateEditField.setErrorState();
                    }
                    setCurrentState(CreditCardState.IDLE_STATE);
                }
            }
        });
        datePickerOpenStateMap.put(CreditCardEvent.CANCEL_DATE_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                // TODO move to eval function
                if (!mExpDateCompleted) {
                    mExpDateEditField.setErrorState();
                }
                setCurrentState(CreditCardState.IDLE_STATE);
            }
        });
        datePickerOpenStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                if (!mDatePickerOpen) {
                    mHappyPathIsBroken = true;
                    // TODO move to eval function
                    if (!mExpDateCompleted) {
                        mExpDateEditField.setErrorState();
                    }
                    setCurrentState(CreditCardState.IDLE_STATE);
                }
            }
        });
        mTransitionMap.put(CreditCardState.DATE_PICKER_OPEN_STATE, datePickerOpenStateMap);

        // Set cvv focused transitions
        Map<CreditCardEvent, Transition> secCodeFocusedStateMap = new HashMap<CreditCardEvent, Transition>();
        secCodeFocusedStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                // TODO move to eval function
                mSecCodeEditField.clearErrors();
                evaluateSecurityCode();
                setCurrentState(CreditCardState.SEC_CODE_FIELD_EDIT_STATE);
            }
        });
        secCodeFocusedStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                mHappyPathIsBroken = true;
                evaluateSecurityCode();
                // TODO move to eval function
                if (!mSecCodeCompleted) {
                    mSecCodeEditField.setErrorState();
                }
                setCurrentState(CreditCardState.IDLE_STATE);
            }
        });
        mTransitionMap.put(CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE, secCodeFocusedStateMap);

        // Set cvv edit transitions
        Map<CreditCardEvent, Transition> secCodeEditStateMap = new HashMap<CreditCardEvent, Transition>();
        secCodeEditStateMap.put(CreditCardEvent.TEXT_CHANGED_EVENT, new Transition() {
            @Override
            public void execute() {
                /*
                 * Don't transition anywhere directly. Call evaluate and let it
                 * perform any necessary transitions.
                 */
                evaluateSecurityCode();
            }
        });
        secCodeEditStateMap.put(CreditCardEvent.SEC_CODE_VALIDATED_EVENT, new Transition() {
            @Override
            public void execute() {
                setCurrentState(CreditCardState.IDLE_STATE);
            }
        });
        secCodeEditStateMap.put(CreditCardEvent.FOCUS_LOST_EVENT, new Transition() {
            @Override
            public void execute() {
                evaluateSecurityCode();
                mHappyPathIsBroken = true;
                // TODO move to eval function
                if (!mSecCodeCompleted) {
                    mSecCodeEditField.setErrorState();
                }
                 setCurrentState(CreditCardState.IDLE_STATE);
            }
        });
        mTransitionMap.put(CreditCardState.SEC_CODE_FIELD_EDIT_STATE, secCodeEditStateMap);
    }
}
