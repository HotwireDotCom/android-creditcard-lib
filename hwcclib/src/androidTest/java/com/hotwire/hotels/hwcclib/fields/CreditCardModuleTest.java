package com.hotwire.hotels.hwcclib.fields;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.CreditCardController;
import com.hotwire.hotels.hwcclib.CreditCardModel;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.dialog.date.ExpirationPickerDialogFragment;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardExpirationEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardNumberEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardSecurityCodeEditField;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;
import com.hotwire.hotels.hwcclib.filter.SecurityCodeInputFilter;

import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.util.ActivityController;

import java.util.Date;

/**
 * Created by ahobbs on 8/29/14.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class CreditCardModuleTest {

    @Test
    public void creditCardModuleSmokeTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().resume().start().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField =
                creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        assertThat(creditCardModule).isNotNull();
        assertThat(creditCardModule.getChildCount()).isEqualTo(2);

        assertThat(creditCardNumberEditField).isNotNull();
        assertThat(creditCardExpirationEditField).isNotNull();
        assertThat(creditCardSecurityCodeEditField).isNotNull();

        assertThat(creditCardNumberEditField.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(creditCardExpirationEditField.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(creditCardSecurityCodeEditField.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void creditCardModuleValidVisaTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "4111");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.VISA);

        InputFilter[] filters = creditCardNumberEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        filters = creditCardSecurityCodeEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        assertThat(transformationMethod).isNotNull();
        assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "111111111111");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111111111111111");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("4111 1111 1111 1111");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNotNull();
        assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleValidMastercardTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "5149");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("5149");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.MASTERCARD);

        InputFilter[] filters = creditCardNumberEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        filters = creditCardSecurityCodeEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        assertThat(transformationMethod).isNotNull();
        assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "955873292557");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("5149955873292557");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("5149 9558 7329 2557");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNotNull();
        assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleValidDiscoverTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "6011");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("6011");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.DISCOVER);

        InputFilter[] filters = creditCardNumberEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        filters = creditCardSecurityCodeEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        assertThat(transformationMethod).isNotNull();
        assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "478899975827");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("6011478899975827");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("6011 4788 9997 5827");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNotNull();
        assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleValidAmericanExpressTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "3456");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("3456");
        assertThat(creditCardModule.getCardIssuer())
                .isEqualTo(CreditCardUtilities.CardIssuer.AMERICANEXPRESS);

        InputFilter[] filters = creditCardNumberEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        filters = creditCardSecurityCodeEditField.getFilters();
        assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        assertThat(transformationMethod).isNotNull();
        assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "15853052313");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("345615853052313");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("3456 158530 52313");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNotNull();
        assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleInvalidVisaTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "4111");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.VISA);
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "211111111111");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111211111111111");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("4111 2111 1111 1111");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNull();
        assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleInvalidMastercardTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "5149");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.MASTERCARD);
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "955879292557");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("5149955879292557");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("5149 9558 7929 2557");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNull();
        assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleInvalidDiscoverTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "6011");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.DISCOVER);
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "472899975827");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("6011472899975827");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("6011 4728 9997 5827");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNull();
        assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleInvalidAmericanExpressTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "3456");
        assertThat(creditCardModule.getCardIssuer())
                .isEqualTo(CreditCardUtilities.CardIssuer.AMERICANEXPRESS);
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "75853052313");
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("345675853052313");
        assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("3456 758530 52313");
        assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(dialogFragment).isNull();
        assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleDeleteCardNumber() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "4111");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.VISA);
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        creditCardNumberEditField.setText("");
        assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
    }

    @Test
    public void creditCardModuleSecurityCodeTransformationMethodTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        creditCardNumberEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        // Incomplete card number to prevent dialog from coming up
        enterText(creditCardNumberEditField, "411111111111111");

        assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        assertThat(creditCardSecurityCodeEditField.getTransformationMethod().getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        creditCardSecurityCodeEditField.requestFocus();
        assertThat(creditCardSecurityCodeEditField.getTransformationMethod()).isNull();
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

        assertThat(expPickerDialogFragment).isNotNull();
        assertThat(expPickerDialogFragment.isVisible());
        assertThat(ShadowDialog.getLatestDialog()).isEqualTo(expPickerDialogFragment.getDialog());

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

    @Test
    public void configurationChangedTest() {
        // Grab the controller, as the lifecycle will need to be managed
        ActivityController<TestActivity> activityController =
                Robolectric.buildActivity(TestActivity.class).create().start().visible();
        // Grab an instance of the TestActivity
        TestActivity testActivity = activityController.get();

        // Obtain the CreditCardModule from the TestActivity, this will be saving/restoring the instance
        CreditCardModule creditCardModule = testActivity.getCreditCardModule();

        // Obtain each of the views, and the controller
        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        // Run through the happy path
        String testCreditCardNumber = "4111111111111111";
        String testSecurityCode = "111";

        creditCardNumberEditField.requestFocus();
        enterText(creditCardNumberEditField, testCreditCardNumber);
        creditCardExpirationEditField.requestFocus();

        ExpirationPickerDialogFragment expPickerDialogFragment = (ExpirationPickerDialogFragment) testActivity
                .getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        assertThat(expPickerDialogFragment).isNotNull();
        assertThat(expPickerDialogFragment.isVisible());
        assertThat(ShadowDialog.getLatestDialog()).isEqualTo(expPickerDialogFragment.getDialog());

        NumberPicker numberPickerYear = expPickerDialogFragment.getNumberPickerYear();
        NumberPicker numberPickerMonth = expPickerDialogFragment.getNumberPickerMonth();

        numberPickerYear.setValue(numberPickerYear.getDisplayedValues().length - 1);
        numberPickerMonth.setValue(numberPickerMonth.getDisplayedValues().length - 1);

        expPickerDialogFragment.positiveClick(expPickerDialogFragment.getDialog());

        String dateFieldString = creditCardExpirationEditField.getText().toString();
        Date expDate = creditCardController.getExpirationDate();
        String expDateString = android.text.format.DateFormat.format(testActivity.getResources()
                .getString(R.string.expiration_field_date_format), expDate).toString();
        assertThat(dateFieldString).isEqualTo(expDateString);

        assertThat(creditCardSecurityCodeEditField.hasFocus());
        enterText(creditCardSecurityCodeEditField, testSecurityCode);
        assertThat(creditCardController.isComplete());

        // create bundle to pass to save instance
        Bundle bundle = new Bundle();
        // save instance and destroy the activity
        activityController.saveInstanceState(bundle).pause().stop().destroy();

        // rebuild the activity, and restore instance using the same bundle passed to saveInstanceState
        testActivity = Robolectric.buildActivity(TestActivity.class).create()
                                                                    .restoreInstanceState(bundle)
                                                                    .start()
                                                                    .resume()
                                                                    .start()
                                                                    .get();
        // re-obtain the module, views, and controller from the newly restored activity
        creditCardModule = testActivity.getCreditCardModule();
        creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();
        creditCardController = creditCardModule.getCreditCardController();

        // check that the values are the same as the test data that was entered
        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo(testCreditCardNumber);
        dateFieldString = creditCardExpirationEditField.getText().toString();
        assertThat(dateFieldString).isEqualTo(expDateString);
        assertThat(creditCardSecurityCodeEditField.getText().toString()).isEqualTo(testSecurityCode);
        assertThat(creditCardController.isComplete());
    }

    /**
     * TestActivity that will call the onSaveInstanceState and onRestoreSavedInstanceState methods
     * on the CreditCardModule when they are called on the activity
     */
    static class TestActivity extends Activity {
        private CreditCardModule mCreditCardModule;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mCreditCardModule = new CreditCardModule(this);
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mCreditCardModule.onSaveInstanceState(outState);
        }

        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
            mCreditCardModule.onRestoreSavedInstanceState(savedInstanceState);
        }

        public CreditCardModule getCreditCardModule() {
            return mCreditCardModule;
        }
    }

    private void enterText(EditText editText, String text) {
        for (int i = 0; i < text.length(); i++) {
            editText.append(String.valueOf(text.charAt(i)));
        }
    }
}
