package com.hotwire.hotels.hwcclib.fields;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
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

import java.text.SimpleDateFormat;
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
        CreditCardExpirationEditField creditCardExpirationEditField =
                creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        final CreditCardModel testCreditCardModel = new CreditCardModel(null, null, null, null);
        CreditCardController.CreditCardModelCompleteListener creditCardCompleteListener =
                new CreditCardController.CreditCardModelCompleteListener() {
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
        CreditCardNumberEditField creditCardNumberEditField =
                creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField =
                creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();
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
        String dateFormat = testActivity.getResources().getString(R.string.expiration_field_date_format);
        String expDateString = CreditCardUtilities.getFormattedDate(dateFormat, expDate);
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

    @Test
    public void loadSavedCreditCardInfoTest() {
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().start().visible().get();

        CreditCardModule creditCardModule = new CreditCardModule(testActivity);
        CreditCardNumberEditField creditCardNumberEditField =
                creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField =
                creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();
        CreditCardController creditCardController = creditCardModule.getCreditCardController();

        String testCreditCardNumber = "4111111111111111";
        Date testDate = new Date();
        String testSecurityCode = "111";


        CreditCardModel testCreditCardModel = new CreditCardModel(testCreditCardNumber,
                                                                  testDate,
                                                                  testSecurityCode,
                                                                  CreditCardUtilities.CardIssuer.VISA);

        creditCardController.loadCreditCardInfoFromModel(testCreditCardModel);

        assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo(testCreditCardNumber);

        String dateFormat = testActivity.getResources().getString(R.string.expiration_field_date_format);
        String formattedDate = CreditCardUtilities.getFormattedDate(dateFormat, testDate);
        assertThat(creditCardExpirationEditField.getText().toString()).isEqualTo(formattedDate);
        assertThat(creditCardSecurityCodeEditField.getText().toString()).isEqualTo(testSecurityCode);
        assertThat(creditCardController.isComplete()).isTrue();
    }

    @Test
    public void stateMachineIdleStateTest() {
    /*
    IDLE_STATE:
        CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT:
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
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
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
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_FOCUSED_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.TEXT_CHANGED_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.NUMBER_FIELD_EDIT_STATE);

        creditCardController.handleEvent(CreditCardController.CreditCardEvent.FOCUS_LOST_EVENT);
        assertThat(creditCardController.getCurrentState()).isEqualTo(CreditCardController.CreditCardState.IDLE_STATE);
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
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
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
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
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
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
        creditCardController.handleEvent(CreditCardController.CreditCardEvent.CREDIT_CARD_NUMBER_FIELD_ON_FOCUS_EVENT);
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
