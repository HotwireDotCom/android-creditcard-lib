package com.hotwire.hotels.hwcclib.fields;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.CreditCardUtilities;
import com.hotwire.hotels.hwcclib.R;
import com.hotwire.hotels.hwcclib.dialog.date.ExpirationPickerDialogFragment;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardExpirationEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardNumberEditField;
import com.hotwire.hotels.hwcclib.fields.edit.CreditCardSecurityCodeEditField;
import com.hotwire.hotels.hwcclib.filter.CreditCardInputFilter;
import com.hotwire.hotels.hwcclib.filter.SecurityCodeInputFilter;

import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

/**
 * Created by ahobbs on 8/29/14.
 */
@RunWith(RobolectricTestRunner.class)
// TODO: this needs to be in a custom runner
@Config(manifest = "src/main/AndroidManifest.xml", emulateSdk = 18)
public class CreditCardModuleTest {

    @Test
    public void creditCardModuleInflationTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().resume().start().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField =
                creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

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
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "4111");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111");
        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.VISA);

        InputFilter[] filters = creditCardNumberEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        filters = creditCardSecurityCodeEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        Assertions.assertThat(transformationMethod).isNotNull();
        Assertions.assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "111111111111");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111111111111111");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("4111 1111 1111 1111");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNotNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleValidMastercardTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        // check security code stuff
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "5149");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("5149");
        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.MASTERCARD);

        InputFilter[] filters = creditCardNumberEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        filters = creditCardSecurityCodeEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }

        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        Assertions.assertThat(transformationMethod).isNotNull();
        Assertions.assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "955873292557");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("5149955873292557");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("5149 9558 7329 2557");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNotNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleValidDiscoverTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "6011");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("6011");
        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.DISCOVER);

        InputFilter[] filters = creditCardNumberEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        filters = creditCardSecurityCodeEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        Assertions.assertThat(transformationMethod).isNotNull();
        Assertions.assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "478899975827");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("6011478899975827");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("6011 4788 9997 5827");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNotNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleValidAmericanExpressTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        // check security code stuff
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "3456");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("3456");
        Assertions.assertThat(creditCardModule.getCardIssuer())
                .isEqualTo(CreditCardUtilities.CardIssuer.AMERICANEXPRESS);

        InputFilter[] filters = creditCardNumberEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        filters = creditCardSecurityCodeEditField.getFilters();

        Assertions.assertThat(filters).isNotNull();

        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName())
                    .isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();

        Assertions.assertThat(transformationMethod).isNotNull();
        Assertions.assertThat(transformationMethod.getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "15853052313");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("345615853052313");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("3456 158530 52313");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNotNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isEqualTo(dialogFragment.getDialog());
    }

    @Test
    public void creditCardModuleInvalidVisaTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "4111");

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.VISA);
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "211111111111");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111211111111111");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("4111 2111 1111 1111");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleInvalidMastercardTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "5149");

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.MASTERCARD);
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "955879292557");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("5149955879292557");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("5149 9558 7929 2557");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleInvalidDiscoverTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "6011");

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.DISCOVER);
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "472899975827");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("6011472899975827");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("6011 4728 9997 5827");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleInvalidAmericanExpressTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "3456");

        Assertions.assertThat(creditCardModule.getCardIssuer())
                .isEqualTo(CreditCardUtilities.CardIssuer.AMERICANEXPRESS);
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        enterText(creditCardNumberEditField, "75853052313");

        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("345675853052313");
        Assertions.assertThat(creditCardNumberEditField.getText().toString()).isEqualTo("3456 758530 52313");
        Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor())
                .isEqualTo(activity.getResources().getColor(R.color.field_text_color_error));

        DialogFragment dialogFragment =
                (DialogFragment) activity.getFragmentManager().findFragmentByTag(ExpirationPickerDialogFragment.TAG);

        Assertions.assertThat(dialogFragment).isNull();
        Assertions.assertThat(ShadowDialog.getLatestDialog()).isNull();
    }

    @Test
    public void creditCardModuleDeleteCardNumber() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);

        enterText(creditCardNumberEditField, "4111");

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.VISA);
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();

        creditCardNumberEditField.setText("");

        Assertions.assertThat(creditCardModule.getCardIssuer()).isEqualTo(CreditCardUtilities.CardIssuer.INVALID);
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
    }

    @Test
    public void creditCardModuleSecurityCodeTransformationMethodTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = new CreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();

        creditCardNumberEditField.requestFocus();

        // Incomplete card number to prevent dialog from coming up
        enterText(creditCardNumberEditField, "411111111111111");

        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        Assertions.assertThat(creditCardSecurityCodeEditField.getTransformationMethod().getClass().getSimpleName())
                .isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        creditCardSecurityCodeEditField.requestFocus();

        Assertions.assertThat(creditCardSecurityCodeEditField.getTransformationMethod()).isNull();
    }

    public void creditCardModuleConfigurationChanged() {
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().start().resume().visible().get();
        CreditCardModule creditCardModule = activity.getCreditCardModule();

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField =
                creditCardModule.getCreditCardSecurityCodeEditField();


        activity.recreate();
    }

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
