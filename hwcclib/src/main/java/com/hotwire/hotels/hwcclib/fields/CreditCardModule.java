/*
 * Copyright 2014 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */
package com.hotwire.hotels.hwcclib.fields;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by ahobbs on 8/8/14.
 */
public class CreditCardModule extends LinearLayout {

    private LinearLayout mHorizontalLayout;

    /**
     *
     * @param context
     */
    public CreditCardModule(Context context) {
        super(context);
        init(context);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public CreditCardModule(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CreditCardModule(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     *
     */
    private void init(Context context) {

    }
}
