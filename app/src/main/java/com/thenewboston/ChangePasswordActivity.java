package com.thenewboston;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

public class ChangePasswordActivity extends NBBaseActivity {
    EditText currentPassword;
    EditText newPassword;
    EditText confirmPassword;

    Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "change password";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setTitle("Change Password");
        setNBToolbar();
        addNBNavigationMenu();

        initComponent();
    }

    private void initComponent()
    {
        currentPassword = (EditText) findViewById(R.id.currentPassword);
        newPassword = (EditText) findViewById(R.id.textBody);
        confirmPassword = (EditText) findViewById(R.id.newPassword2);

        buttonSubmit = (Button) findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword()
    {
        String currentPwd = currentPassword.getText().toString();
        String newPwd = newPassword.getText().toString();
        String confirmPwd = confirmPassword.getText().toString();

        if (currentPwd.equals(""))
        {
            showPopupMessage("Please enter current password");
            return;
        }

        if (newPwd.equals(""))
        {
            showPopupMessage("Please enter new password");
            return;
        }

        if (!newPwd.equals(confirmPwd))
        {
            showPopupMessage("New password doesn't match");
            return;
        }

        if (newPwd.length() < 8)
        {
            showPopupMessage("Password should have 8 more characters");
            return;
        }

        NBRestAPIManager apiManager = new NBRestAPIManager("account", "changePassword", nbUserApiToken, "post");
        apiManager.addField("current_password", currentPwd);
        apiManager.addField("new_password", newPwd);

        showOverlay();

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        showPopupMessage("Your password has been changed successfully!");
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
