package com.hotwire.hotels.hwcclib.fields;

import android.app.Activity;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.hotwire.hotels.hwcclib.R;
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

/**
 * Created by ahobbs on 8/29/14.
 */
@RunWith(RobolectricTestRunner.class)
// TODO: this needs to be in a custom runner
@Config(manifest = "src/main/AndroidManifest.xml", emulateSdk = 18)
public class CreditCardModuleTest {

    @Test
    public void creditCardModuleInflationTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        CreditCardModule creditCardModule = getCreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();

        Assertions.assertThat(creditCardModule).isNotNull();
        Assertions.assertThat(creditCardModule.getChildCount()).isEqualTo(2);

        Assertions.assertThat(creditCardNumberEditField).isNotNull();
        Assertions.assertThat(creditCardExpirationEditField).isNotNull();
        Assertions.assertThat(creditCardSecurityCodeEditField).isNotNull();
    }

    @Test
    public void creditCardModuleValidVisaTest() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        CreditCardModule creditCardModule = getCreditCardModule(activity);

        CreditCardNumberEditField creditCardNumberEditField = creditCardModule.getCreditCardNumberEditField();
        CreditCardExpirationEditField creditCardExpirationEditField = creditCardModule.getCreditCardExpirationEditField();
        CreditCardSecurityCodeEditField creditCardSecurityCodeEditField = creditCardModule.getCreditCardSecurityCodeEditField();

        // check security code stuff
        Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isFalse();
        Assertions.assertThat(creditCardSecurityCodeEditField.hasFocus()).isFalse();

        enterText(creditCardNumberEditField, "4111");
        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111");

        InputFilter[] filters = creditCardNumberEditField.getFilters();
        Assertions.assertThat(filters).isNotNull();
        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName()).isEqualTo(CreditCardInputFilter.class.getSimpleName());
        }

        // TODO: Why does this work properly?
        //Assertions.assertThat(creditCardSecurityCodeEditField.isEnabled()).isTrue();
        filters = creditCardSecurityCodeEditField.getFilters();
        Assertions.assertThat(filters).isNotNull();
        for (InputFilter filter : filters) {
            Assertions.assertThat(filter.getClass().getSimpleName()).isEqualTo(SecurityCodeInputFilter.class.getSimpleName());
        }
        TransformationMethod transformationMethod = creditCardSecurityCodeEditField.getTransformationMethod();
        Assertions.assertThat(transformationMethod).isNotNull();
        Assertions.assertThat(transformationMethod.getClass().getSimpleName()).isEqualTo(PasswordTransformationMethod.class.getSimpleName());

        enterText(creditCardNumberEditField, "111111111111");
        Assertions.assertThat(creditCardNumberEditField.getRawCreditCardNumber()).isEqualTo("4111111111111111");
        // TODO: Why does this work properly?
        //Assertions.assertThat(creditCardNumberEditField.getCurrentTextColor()).isEqualTo(activity.getResources().getColor(R.color.field_text_color_default));
    }

    private void enterText(EditText editText, String text) {
        for (int i = 0; i < text.length(); i++) {
            editText.append(String.valueOf(text.charAt(i)));
        }
    }

    private CreditCardModule getCreditCardModule(Activity activity) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.test_credit_card_layout, null);

        return (CreditCardModule) layout.findViewById(R.id.credit_card_module);
    }
}
