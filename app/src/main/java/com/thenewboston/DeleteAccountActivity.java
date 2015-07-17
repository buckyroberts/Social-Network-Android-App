package com.thenewboston;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

public class DeleteAccountActivity extends NBBaseActivity {
    private EditText textPassword;
    private EditText textConfirmPassword;

    private Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "delete account";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        setTitle("Delete Account");
        setNBToolbar();
        addNBNavigationMenu();

        initComponent();
    }

    private void initComponent()
    {
        textPassword = (EditText) findViewById(R.id.textBody);
        textConfirmPassword = (EditText) findViewById(R.id.newPassword2);

        buttonSubmit = (Button) findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmToDelete();
            }
        });
    }

    private void confirmToDelete()
    {
        final String currentPwd = textPassword.getText().toString();
        String confirmPwd = textConfirmPassword.getText().toString();

        if (currentPwd.equals(""))
        {
            showPopupMessage("Please enter current password");
            return;
        }

        if (confirmPwd.equals(""))
        {
            showPopupMessage("Please enter confirm password");
            return;
        }

        if (!confirmPwd.equals(currentPwd))
        {
            showPopupMessage("Password doesn't match");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure?")
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount(currentPwd);
                    }
                }).show();
    }

    private void deleteAccount(String currentPwd)
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("account", "deleteAccount", nbUserApiToken, "post");
        apiManager.addField("password", currentPwd);

        showOverlay();

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        SharedPreferences nbSettings = getSharedPreferences(nbPrefsName, MODE_PRIVATE);
                        SharedPreferences.Editor nbSettingsEditor = nbSettings.edit();

                        nbSettingsEditor.putString("nb_user_id", "");
                        nbSettingsEditor.putString("nb_user_email", "");
                        nbSettingsEditor.putString("nb_user_api_token", "");

                        nbSettingsEditor.commit();


                        gotoOtherActivity(LoginActivity.class, "Account deleted", true);
                    }
                } catch (JSONException e) {
                    showPopupMessage(e.getMessage());
                }
            }

            @Override
            public void onFailure(String pError, int pStatusCode) {
                if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                    clearNBUserInfo();
                    gotoOtherActivity(LoginActivity.class, pError, true);
                }else{
                    hideOverlay();
                    showPopupMessage(pError);
                }
            }
        };

        apiManager.execute(restApiListener);
    }

}
