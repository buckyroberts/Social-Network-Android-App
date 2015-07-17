package com.thenewboston;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;

import org.json.JSONException;
import org.json.JSONObject;


public class RegistrationActivity extends NBBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.currentActivity = "register";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setNBToolbar();
    }

    public void gotoLoginActivity(View view) {
        Intent gotoLogin = new Intent(this, LoginActivity.class);
        startActivity(gotoLogin);
        finish();
    }

    public void doRegister(View v) {
        hideInputKeyboard();

        //Getting Input Value
        EditText tFirstNameText = (EditText)findViewById(R.id.firstName);
        String tFirstName = tFirstNameText.getText().toString().trim();

        EditText tLastNameText = (EditText)findViewById(R.id.lastName);
        String tLastName = tLastNameText.getText().toString().trim();

        EditText tEmailText = (EditText)findViewById(R.id.email);
        String tEmail = tEmailText.getText().toString().trim();

        EditText tPasswordText = (EditText)findViewById(R.id.password);
        String tPassword = tPasswordText.getText().toString().trim();

        EditText tPassword2Text = (EditText)findViewById(R.id.password2);
        String tPassword2 = tPassword2Text.getText().toString().trim();

        tFirstNameText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));
        tLastNameText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));
        tEmailText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));
        tPasswordText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));
        tPassword2Text.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));

        Boolean tIsValid = true;

        cleanPopupMessages();

        if (TextUtils.isEmpty(tFirstName)) {
            tFirstNameText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        }

        if (TextUtils.isEmpty(tLastName)) {
            tLastNameText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        }

        if (TextUtils.isEmpty(tEmail)) {
            tEmailText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        }

        if (TextUtils.isEmpty(tPassword)) {
            tPasswordText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        }

        if (TextUtils.isEmpty(tPassword2)) {
            tPassword2Text.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        }

        if (!tIsValid) {
            showPopupMessage(getString(R.string.complete_fields_in_red));
            return;
        }

        //Validate email address
        if(!Patterns.EMAIL_ADDRESS.matcher(tEmail).matches()) {
            tEmailText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            addPopupMessage(getString(R.string.enter_valid_email));
            tIsValid = false;
        }

        //Validate password
        if (!tPassword.equals(tPassword2)) {
            tPasswordText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tPassword2Text.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            addPopupMessage(getString(R.string.passwords_not_match));
            tIsValid = false;
        }

        if (!tIsValid) {
            showPopupMessage();
            return;
        } else {
            //Try Login Using Rest Api
            NBRestAPIManager apiManager = new NBRestAPIManager("account", "register", NBRestHttpClient.PUBLIC_API_TOKEN, "post");

            apiManager.addField("firstName", tFirstName);
            apiManager.addField("lastName", tLastName);
            apiManager.addField("email", tEmail);
            apiManager.addField("password", tPassword);
            apiManager.addField("password2", tPassword2);

            showOverlay();

            NBRestAPIListener registerListener = new NBRestAPIListener() {

                @Override
                public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                    hideOverlay();

                    try {
                        if (pResponseData.getString("STATUS").equals("ERROR")) {
                            showPopupMessage(pResponseData.getString("ERROR"));
                        } else {
                            //Go to Login Activity
                            Intent tGotoLogin = new Intent(getApplicationContext(), LoginActivity.class);
                            tGotoLogin.putExtra("notification", pResponseData.getString("MESSAGE"));
                            startActivity(tGotoLogin);
                            finish();
                        }
                    } catch (JSONException e) {
                        showPopupMessage(e.getMessage());
                    }
                }

                @Override
                public void onFailure(String pError, int pStatusCode) {
                    showPopupMessage(pError);
                    hideOverlay();
                }
            };

            apiManager.execute(registerListener);
        }
    }

}
