# Hotwire Inc. Credit card library (hwcclib)

Hotwire’s hwcclib is a library that allows developers to easily add fields to obtain customer credit card information to their application.

## Usage

Here are examples of how to use the library:

### Layout files
Including the library in a layout file only requires the following lines of code:

```
<com.hotwire.hotels.hwcclib.fields.CreditCardModule
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

Below is an example of the library included in a layout file:

```
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    android:paddingBottom="@dimen/activity_vertical_margin"
    tools:context=".MainActivity">

    <com.hotwire.hotels.hwcclib.fields.CreditCardModule
        android:id="@+id/credit_card_module"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

    <Button
        android:id="@+id/save_button"
        android:layout_width=“match_parent
        android:layout_height="wrap_content"
        android:layout_below="@id/credit_card_module"
        android:text="Save"/>
</RelativeLayout>
```

### Programatically

For more control over the views in your project, the following methods can be used to programatically layout the library components.

#### Method 1

**First:** Instantiate each of the following `EditText` fields in an `Activity` or a `Fragment`.

- `CreditCardNumberEditField`
- `CreditCardExpirationEditField`
- `CreditCardSecurityCodeEditField`

**Second:** Instantiate the `CreditCardController` providing `Context` and each of the instantiated views.

**Example:**

```java
private CreditCardNumberEditField  mCreditCardNumber;
private CreditCardExpirationEditField  mCreditCardExpiration;
private CreditCardSecurityCodeEditField mCreditCardSecurityCode;

private void init(Context context) {
    mCreditCardNumber = new CreditCardNumberEditField(context);
    mCreditCardExpiration = new CreditCardExpirationEditField(context);
    mCreditCardSecurityCode = new CreditCardSecurityCodeEditField(context);

    mCreditCardController = new CreditCardController(context, mCreditCardNumber,
            mCreditCardExpiration, mCreditCardSecurityCode);
}
```

#### Method 2

**First:** Layout each of the above `EditText` fields in a layout file:

**test_activity.xml**
```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
              android:orientation="vertical"
              android:layout_width="match_parent"
              android:layout_height="match_parent">

    <com.hotwire.hotels.hwcclib.fields.edit.CreditCardNumberEditField
        android:id="@+id/credit_card_number_edit_field"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
    
    <com.hotwire.hotels.hwcclib.fields.edit.CreditCardExpirationEditField
        android:id="@+id/credit_card_expiration_edit_field"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
    
    <com.hotwire.hotels.hwcclib.fields.edit.CreditCardSecurityCodeEditField
        android:id="@+id/credit_card_security_code_edit_field"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
</LinearLayout>
```

**Second:** Obtain references of each of the `EditText` views in an `Activity` or a `Fragment`

```java
private CreditCardNumberEditField  mCreditCardNumber;
private CreditCardExpirationEditField  mCreditCardExpiration;
private CreditCardSecurityCodeEditField mCreditCardSecurityCode;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_activity);

    mCreditCardNumber = (CreditCardNumberEditField) findViewById(R.id. credit_card_number_edit_field);
    mCreditCardExpiration = (CreditCardExpirationEditField) findViewById(R.id. credit_card_expiration_edit_field);
    mCreditCardSecurityCode = (CreditCardSecurityCodeEditField) findViewById(R.id.credit_card_security_code_edit_field);
}
```

**Third:** Instantiate the `CreditCardController` providing `Context` and each of the view references.

```java
private void init(Context context) {
    mCreditCardController = new CreditCardController(context, mCreditCardNumber,
            mCreditCardExpiration, mCreditCardSecurityCode);
}
      
```
## Installing

You can install Hotwire’s hwcclib in your current project by adding the following lines of xml to your pom.xml file:

```
<dependency>
   <groupId>com.hotwire.hwcclib</groupId>
   <artifactId>hwcclib</artifactId>
   <version>1.0.2</version>
   <type>apklib</type>
</dependency>
```

**Note:** hwcclib uses **Robolectric** for testing some components. If **Robolectric** is already part of your current project, it may need to be excluded. Adding the following lines of xml to your pom.xml will exclude hwcclib’s **Robolectric** dependency:

```
<dependency>
   <groupId>com.hotwire.hwcclib</groupId>
   <artifactId>hwcclib</artifactId>
   <version>1.0.2</version>
   <type>apklib</type>
   <exclusions>
      <exclusion>
         <groupId>org.robolectric</groupId>
         <artifactId>robolectric</artifactId>
      </exclusion>
   </exclusions>
</dependency>
```

##Credits
Created by Elliott Park, Austin Hobbs, Snehanth Somireddy and Ankur Pal with thanks to: William Hicks.

##Legal
This project is available under the Apache 2.0 License.

Copyright 2015 Expedia Inc.