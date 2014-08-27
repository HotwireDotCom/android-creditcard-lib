/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcreditcardlib;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hotwire.hotels.hwcclib.CreditCardModel;
import com.hotwire.hotels.hwcclib.fields.CreditCardModule;

/**
 *
 */
public class MainActivity extends Activity {

    CreditCardModule creditCardModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView statusTextView = (TextView) findViewById(R.id.status_text_view);
        creditCardModule = (CreditCardModule) findViewById(R.id.credit_card_module);
        Button saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreditCardModel ccm = creditCardModule.getCreditCardController().save();
                if (ccm != null) {
                    statusTextView.setText("Status: Complete.");
                } else {
                    statusTextView.setText("Status: Incomplete.");
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        creditCardModule.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        creditCardModule.onRestoreSavedInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
