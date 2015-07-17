package com.thenewboston;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends NBBaseActivity {

    protected void onCreate(Bundle savedInstanceState) {
        this.currentActivity = "login";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setNBToolbar();

    }

    public void gotoRegistrationActivity(View view) {
        Intent gotoRegistration = new Intent(this, RegistrationActivity.class);
        startActivity(gotoRegistration);
        finish();
    }

    public void doLogin(View view) {
        //Getting Input Value
        EditText tEmailText = (EditText)findViewById(R.id.input_email);
        String tEmail = tEmailText.getText().toString().trim();

        EditText tPasswordText = (EditText)findViewById(R.id.input_password);
        String tPassword = tPasswordText.getText().toString().trim();

        tEmailText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));
        tPasswordText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox));

        Boolean tIsValid = true;

        cleanPopupMessages();

        //Validate email address
        if (TextUtils.isEmpty(tEmail)) {
            addPopupMessage(getString(R.string.enter_email));
            tEmailText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(tEmail).matches()) {
            tEmailText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            addPopupMessage(getString(R.string.enter_valid_email));
            tIsValid = false;
        }

        //Validate Password;
        if (TextUtils.isEmpty(tPassword)) {
            addPopupMessage(getString(R.string.enter_password));
            tPasswordText.setBackgroundDrawable(getResources().getDrawable(R.drawable.inputbox_not_valid));
            tIsValid = false;
        }

        if (!tIsValid) {
            showPopupMessage();
        } else {

            //Remove Focus from EditText
            tEmailText.clearFocus();
            tPasswordText.clearFocus();

            //Try Login Using Rest Api
            NBRestAPIManager apiManager = new NBRestAPIManager("account", "login", NBRestHttpClient.PUBLIC_API_TOKEN, "post");

            apiManager.addField("email", tEmail);
            apiManager.addField("password", tPassword);

            showOverlay();

            NBRestAPIListener loginApiListener = new NBRestAPIListener() {

                @Override
                public void onSuccess(JSONObject pResponseData, int pStatusCode) {

                    hideOverlay();

                    try {
                        if (pResponseData.getString("STATUS").equals("ERROR")) {
                            showPopupMessage(pResponseData.getString("ERROR"));
                        } else { //Login Success
                            SharedPreferences nbSettings = getSharedPreferences(nbPrefsName, MODE_PRIVATE);
                            SharedPreferences.Editor nbSettingsEditor = nbSettings.edit();

                            nbSettingsEditor.putString("nb_user_id", pResponseData.getString("USERID"));
                            nbSettingsEditor.putString("nb_user_email", pResponseData.getString("EMAIL"));
                            nbSettingsEditor.putString("nb_user_api_token", pResponseData.getString("TOKEN"));

                            nbSettingsEditor.commit();

                            //Go to Main Activity
                            Intent toLoginIntent = new Intent(getApplicationContext(), StreamActivity.class);
                            startActivity(toLoginIntent);
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

            apiManager.execute(loginApiListener);
        }

        

    }
}
