package com.thenewboston;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagesComposeActivity extends NBBaseActivity {
    private ImageView imageTo;
    private TextView textTo;

    private TextView textSubject;
    private TextView textBody;

    private Button buttonSubmit;

    private String friendId;
    private String friendName;
    private String friendThumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "compose";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_compose);
        setTitle("Write Message");
        setNBToolbar();
        addNBNavigationMenu();

        initComponent();

        loadFriendInfo();
    }

    private void initComponent()
    {
        imageTo = (ImageView) findViewById(R.id.imageTo);

        textTo = (TextView) findViewById(R.id.textTo);
        textSubject = (TextView) findViewById(R.id.textSubject);
        textBody = (TextView) findViewById(R.id.textBody);

        buttonSubmit = (Button) findViewById(R.id.button_submit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeMessage();
            }
        });
    }

    private void loadFriendInfo()
    {
        hideOverlay();

        Intent i = getIntent();

        friendId = i.getStringExtra("friendId");
        friendName = i.getStringExtra("friendName");
        friendThumbnail = i.getStringExtra("friendThumbnail");

        if (friendId != null)
        {
            textTo.setText(friendName);

            UrlImageViewHelper.setUrlDrawable(imageTo, friendThumbnail, R.drawable.default_profile_image);
        }

        String subject = i.getStringExtra("subject");

        if (subject != null)
        {
            textSubject.setText(subject);
            textBody.requestFocus();
        }
    }

    private void composeMessage()
    {
        String subject = textSubject.getText().toString();
        String body = textBody.getText().toString();

        if (friendId == null)
        {
            showPopupMessage("Please choose friend you want to send message!");
            return;
        }

        if (subject.equals(""))
        {
            showPopupMessage("Please input the message subject!");
            return;
        }

        if (body.equals(""))
        {
            showPopupMessage("Please input the message body!");
            return;
        }

        NBRestAPIManager apiManager = new NBRestAPIManager("message", "composeMessage", nbUserApiToken, "post");

        apiManager.addField("to", friendId);
        apiManager.addField("subject", subject);
        apiManager.addField("body", body);

        showOverlay();

        NBRestAPIListener infoListener = new NBRestAPIListener(){
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        showPopupMessage(String.format("Message sent to %s successfully!", friendName));

                        clear();
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
                }else {
                    hideOverlay();

                    showPopupMessage(pError);
                }
            }
        };

        apiManager.execute(infoListener);
    }

    private void clear()
    {
        textSubject.setText("");
        textBody.setText("");

        textSubject.requestFocus();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.v("boston", "resume");
    }
}
