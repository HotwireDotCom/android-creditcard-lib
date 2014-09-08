package com.hotwire.hotels.hwcclib.fields;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.hotwire.hotels.hwcclib.CreditCardController;
import com.hotwire.hotels.hwcclib.CreditCardModel;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.dialog.date.ExpirationPickerDialogFragment;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardExpirationEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardNumberEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardSecurityCodeEditField;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;
import com.hotwire.hotels.hwcclib.filter.SecurityCodeInputFilter;

import org.fest.assertions.api.Assertions;
import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ahobbs on 8/29/14.
 */
@RunWith(RobolectricTestRunner.class)
// TODO: this needs to be in a custom runner
@Config(manifest = "src/main/AndroidManifest.xml", emulateSdk = 18)
public class CreditCardModuleTest {

    @Test
    public void creditCardModuleInflationTest() {
        CreditCardModule creditCardModule = new CreditCardModule(Robolectric.application);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardModule).isNotNull();
        Assertions.assertThat(creditCardModule.getChildCount()).isEqualTo(2);

        Assertions.assertThat(creditCardNumberEditField).isNotNull();
        Assertions.assertThat(creditCardExpirationEditField).isNotNull();
        Assertions.assertThat(creditCardSecurityCodeEditField).isNotNull();

        Assertions.assertThat(creditCardNumberEditField.getVisibility()).isEqualTo(View.VISIBLE);
        Assertions.assertThat(creditCardExpirationEditField.getVisibility()).isEqualTo(View.VISIBLE);
        Assertions.assertThat(creditCardSecurityCodeEditField.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void creditCardModuleValidVisaTest() {
        CreditCardModule creditCardModule = new CreditCardModule(Robolectric.application);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();

        // check security code stuff
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        Assertions.assertThat(creditCardSecurityCodeEditField.hasFocus()).isFalse();

        creditCardNumberEditField.performClick();
        creditCardNumberEditField.requestFocus();
        enterText(creditCardNumberEditField, "4111");
//        enterText(creditCardNumberEditField, "3799");
        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111");
//        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("3799");

        InputFilter[] filters = creditCardNumberEditField.getFilters();
        Assertions.assertThat(filters).isNotNull();
        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName()).isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        // TODO: Why doesn't this work properly?
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        filters = creditCardSecurityCodeEditField.getFilters();
        Assertions.assertThat(filters).isNotNull();
        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName()).isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();
        Assertions.assertThat(transformationMethod).isNotNull();
        Assertions.assertThat(transformationMethod.getClass().getSimpleName()).isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "111111111111");
        //enterText(creditCardNumberEditField, "99999999999");
        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111111111111111");
        //Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("379999999999999");
        System.out.println("******* FORMATTED: " + creditCardNumberEditField.getText());
        // TODO: Why doesn't this work properly?
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor()).isEqualTo(Robolectric.application.getResources().getColor(R.color.field_text_color_default));
    }

    @Test
    public void happyPathTest() {
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        final CreditCardModel testCreditCardModel = new CreditCardModel(null, null, null);
        CreditCardController.CreditCardModelCompleteListener creditCardCompleteListener = new CreditCardController.CreditCardModelCompleteListener() {
            @Override
            public void onCreditCardModelComplete(CreditCardModel creditCardModel) {
                testCreditCardModel.setCreditCardNumber(creditCardModel.getCreditCardNumber());
                testCreditCardModel.setExpirationDate(creditCardModel.getExpirationDate());
                testCreditCardModel.setSecurityCode(creditCardModel.getSecurityCode());
            }
        };
        creditCardModule.setCreditCardModelCompleteListener(creditCardCompleteListener);

        String testCreditCardNumber = "4111111111111111";
        String testSecurityCode = "111";

        creditCardNumberEditField.requestFocus();
        enterText(creditCardNumberEditField, testCreditCardNumber);
        creditCardExpirationEditField.requestFocus();

        ExpirationPickerDialogFragment expPickerDialogFragment = (ExpirationPickerDialogFragment) testActivity
                .getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(expPickerDialogFragment).isNotNull();
        assertThat(expPickerDialogFragment.isVisible());
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isEqualTo(expPickerDialogFragment.getDialog());

        NumberPicker numberPickerYear = expPickerDialogFragment.getNumberPickerYear();
        NumberPicker numberPickerMonth = expPickerDialogFragment.getNumberPickerMonth();

        numberPickerYear.setValue(numberPickerYear.getDisplayedValues().length - 1);
        numberPickerMonth.setValue(numberPickerMonth.getDisplayedValues().length - 1);

        expPickerDialogFragment.positiveClick(expPickerDialogFragment.getDialog());

        String dateFieldString = creditCardExpirationEditField.getText().toString();
        Date expDate = creditCardController.getExpirationDate();
        String expDateString = android.text.format.DateFormat.format(testActivity.getResources()
                .getString(R.string.expiration_field_date_format), expDate).toString();
        assertThat(expDateString).isEqualTo(dateFieldString);

        assertThat(creditCardSecurityCodeEditField.hasFocus());
        enterText(creditCardSecurityCodeEditField, testSecurityCode);
        assertThat(creditCardController.isComplete());

        assertThat(testCreditCardModel.getCreditCardNumber()).isEqualTo(testCreditCardNumber);
        String testDateString = android.text.format.DateFormat.format(testActivity.getResources()
                .getString(R.string.expiration_field_date_format),
                testCreditCardModel.getExpirationDate()).toString();
        assertThat(testDateString).isEqualTo(dateFieldString);
        assertThat(testCreditCardModel.getSecurityCode()).isEqualTo(testSecurityCode);

    }



    private void print(String str) {
        System.out.println("\n\n"+str+"\n\n");
    }

    @Test
    public void loadSavedCreditCardInfo() {
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        String testCreditCardNumber = "4111111111111111";
        Date testDate = new Date();
        String testSecurityCode = "111";


        CreditCardModel testCreditCardModel = new CreditCardModel(testCreditCardNumber, testDate, testSecurityCode);
    }


    @Test
    public void stateMachineIdleStateTest() {
    /*
    IDLE_STATE:
        CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT:
            Set State: NUMBER_FIELD_FOCUSED_STATE
            function evaluateCreditCardNumber()

        EXP_DATE_FIELD_ON_FOCUS_EVENT:
            Set State: DATE_PICKER_OPEN_STATE
            happy path is broken
            openDatePicker()

        SEC_CODE_FIELD_ON_FOCUS_EVENT:
            Set State: SEC_CODE_FIELD_FOCUSED_STATE
            happy path is broken
            demask security code text
            evaluateSecurityCode()

        TEXT_CHANGED_EVENT:
            logic <CreditCardNumberEditField has focus>:
                Set State: NUMBER_FIELD_EDIT_STATE
                record that text has been entered into the CreditCardNumberEditField
                evaluateCreditCardNumber()
            <SecCodeEditField has focus>:
                Set State: SEC_CODE_FIELD_EDIT_STATE
                evaluateSecurityCode()
            <ExpDateEditField has focus>:
                Set State: DATE_PICKER_OPEN_STATE
                openDatePicker()
     */
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();
        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardNumberEditField.requestFocus();
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardSecurityCodeEditField.setEnabled(true);
        creditCardSecurityCodeEditField.setFocusable(true);
        creditCardSecurityCodeEditField.setFocusableInTouchMode(true);
        creditCardSecurityCodeEditField.requestFocus();
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_EDIT_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardExpirationEditField.requestFocus();
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);

    }

    @Test
    public void stateMachineNumberFieldFocusedStateTest() {
        /*
        NUMBER_FIELD_FOCUSED_STATE:
            TEXT_CHANGED_EVENT:
                Set State: NUMBER_FIELD_EDIT_STATE
                record that text has been entered into the CreditCardNumberEditField
                evaluateCreditCardNumber()
            FOCUS_LOST_EVENT:
                happy path is broken
                Set State: IDLE_STATE
                evaluateCreditCardNumber()
         */
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);

    }

    @Test
    public void stateMachineNumberEditStateTest() {
        /*
        NUMBER_FIELD_EDIT_STATE:
            TEXT_CHANGED_EVENT:
                evaluateCreditCardNumber()
            CREDIT_CARD_NUMBER_VALIDATED_EVENT:
                <happy path is not broken>:
                    Set State: DATE_PICKER_OPEN_STATE
                    openDatePicker()
                <happy path is broken>:
                    Set State: IDLE_STATE
            FOCUS_LOST_EVENT:
                happy path is broken
                Set State: IDLE_STATE
                evaluateCreditCardNumber()
         */
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);

        creditCardController.setHappyPathBroken(false);
        assertThat(!creditCardController.isHappyPathBroken());
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_VALIDATED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);

        creditCardController.setHappyPathBroken(true);
        assertThat(creditCardController.isHappyPathBroken());
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_VALIDATED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);


        creditCardModule = new CreditCardModule(testActivity);
        creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);

    }

    @Test
    public void stateMachineDatePickerOpenStateTest() {
        /*
        DATE_PICKER_OPEN_STATE:
            CLOSE_DATE_PICKER_EVENT:
                Set State: IDLE_STATE
            FOCUS_LOST_EVENT:
                <date picker is not open>:
                    happy path is broken
                    Set State: IDLE_STATE
            EXP_DATE_FIELD_ON_FOCUS_EVENT:
                <date picker is not open>:
                    openDatePicker()
         */
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CLOSE_DATE_PICKER_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);

        creditCardController.setDatePickerOpen(false);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);

        creditCardController.setDatePickerOpen(false);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.EXP_DATE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.DATE_PICKER_OPEN_STATE);
        assertThat(creditCardController.isDatePickerOpen());
    }

    @Test
    public void stateMachineSecurityCodeFieldFocusedStateTest() {
        /*
        SEC_CODE_FIELD_FOCUSED_STATE:
            TEXT_CHANGED_EVENT:
                Set State: SEC_CODE_FIELD_EDIT_STATE
                evaluateSecurityCode();
            FOCUS_LOST_EVENT:
                happy path is broken
                mask text in the SecurityCodeEditField
                Set State: IDLE_STATE
                evaluateSecurityCode()
         */
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_EDIT_STATE);

    }

    @Test
    public void stateMachineSecurityCodeEditStateTest() {
        /*
        SEC_CODE_FIELD_EDIT_STATE:
            TEXT_CHANGED_EVENT:
                evaluateSecurityCode()
            FOCUS_LOST_EVENT:
                happy path is broken
                mask text in the SecurityCodeEditField
                Set State: IDLE_STATE
                evaluateSecurityCode()
         */
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.SEC_CODE_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_EDIT_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.SEC_CODE_FIELD_EDIT_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
    }

    private void enterText(EditText editText, String text) {
        for (int i = 0; i < text.length(); i++) {
            editText.append(String.valueOf(text.charAt(i)));
        }
    }


}
