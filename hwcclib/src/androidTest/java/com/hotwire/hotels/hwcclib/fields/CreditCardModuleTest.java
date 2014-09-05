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

    private void enterText(EditText editText, String text) {
        for (int i = 0; i < text.length(); i++) {
            editText.append(String.valueOf(text.charAt(i)));
        }
    }


}
