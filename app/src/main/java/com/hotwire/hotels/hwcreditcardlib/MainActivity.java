/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcreditcardlib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hotwire.hotels.hwcclib.CreditCardController;
import com.hotwire.hotels.hwcclib.CreditCardModel;
import com.hotwire.hotels.hwcclib.fields.CreditCardModule;

/**
 *
 */
public class MainActivity extends Activity implements CreditCardController.CreditCardModelCompleteListener {

    CreditCardModule creditCardModule;
    TextView reportingTextView;

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
                boolean complete = creditCardModule.getCreditCardController().isComplete();
                if (complete) {
                    statusTextView.setText("Status: Complete.");
                } else {
                    statusTextView.setText("Status: Incomplete.");
                }
            }
        });

        creditCardModule.setCreditCardModelCompleteListener(this);
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

    @Override
    public void onCreditCardModelComplete(CreditCardModel creditCardModel) {
        reportingTextView.setText("All necessary credit card information has been entered.\nRock it like a hurricane!");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reportingTextView.setText("Keep calm and carry on.");
            }
        }, 10000);
    }
}
