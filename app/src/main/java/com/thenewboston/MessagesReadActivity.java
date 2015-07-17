package com.thenewboston;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.items.NBMessageItem;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagesReadActivity extends NBBaseActivity {
    private ImageView imageFrom;
    private ImageView imageTo;
    private ImageView imageDelete;
    private ImageView imageReply;

    private Button buttonPrev;
    private Button buttonNext;

    private TextView textSubject;
    private TextView textDescription;
    private TextView textDate;

    private TextView textFrom;
    private TextView textTo;

    private String messageType;
    private String messageId;

    private NBMessageItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "read";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_read);
        setTitle("Read Message");
        setNBToolbar();
        addNBNavigationMenu();

        initComponent();
        loadExtraInfo();
    }


    private void initComponent()
    {
        imageFrom = (ImageView) findViewById(R.id.imageFrom);
        imageTo = (ImageView) findViewById(R.id.imageTo);
        imageDelete = (ImageView) findViewById(R.id.imageDelete);
        imageReply = (ImageView) findViewById(R.id.imageReply);

        buttonPrev = (Button) findViewById(R.id.buttonPrev);
        buttonNext = (Button) findViewById(R.id.buttonNext);

        textSubject = (TextView) findViewById(R.id.messageSubject);
        textDescription = (TextView) findViewById(R.id.messageContent);
        textDate = (TextView) findViewById(R.id.messageDate);

        textFrom = (TextView) findViewById(R.id.textFrom);
        textTo = (TextView) findViewById(R.id.textTo);

        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMessageId(item.prevMessageId);

                showOverlay();

                loadMessageInfo();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setMessageId(item.nextMessageId);

                showOverlay();

                loadMessageInfo();
            }
        });

        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmToDelete();
            }
        });

        imageReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent i = new Intent(getApplicationContext(), MessagesComposeActivity.class);

            if (item != null) {
                i.putExtra("friendId", item.senderId);
                i.putExtra("friendName", item.senderName);
                i.putExtra("friendThumbnail", item.senderThumbnail);

                i.putExtra("subject", String.format("Re: %s", item.subject));
            }

            startActivity(i);
            }
        });
    }

    private void loadExtraInfo()
    {
        Intent intent = getIntent();

        messageType = intent.getStringExtra("messageType");
        messageId = intent.getStringExtra("messageId");

        if (messageType.equals("trash"))
            imageDelete.setVisibility(ImageView.GONE);
        else
            imageDelete.setVisibility(ImageView.VISIBLE);

        if (messageType.equals("inbox"))
            imageReply.setVisibility(ImageView.VISIBLE);
        else
            imageReply.setVisibility(ImageView.GONE);

        loadMessageInfo();
    }

    private void loadMessageInfo()
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("message", "getMessageInfo", nbUserApiToken, "get");
        apiManager.addField("messageID", messageId);
        apiManager.addField("messageType", messageType);

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    }
                    else {
                        item = new NBMessageItem();

                        JSONObject object = pResponseData.getJSONObject("RESULT");

                        try
                        {
                            item.messageId = object.getString("messageID");
                            item.senderId = object.getString("sender");
                            item.senderName = object.getString("senderName");
                            item.senderThumbnail = object.getString("senderThumbnail");
                            item.receiverId = object.getString("receiver");
                            item.receiverName = object.getString("receiverName");
                            item.receiverThumbnail = object.getString("receiverThumbnail");
                            item.subject = object.getString("subject");
                            item.body = object.getString("body");
                            item.createdDate = object.getString("created_date");
                            item.status = object.getString("status");
                            item.nextMessageId = object.getString("nextId");
                            item.prevMessageId = object.getString("prevId");
                        }
                        catch (Exception ex)
                        {

                        }

                        setMessageInfo(item);
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

    private void confirmToDelete()
    {
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
                        deleteMessageInfo();
                    }
                }).show();
    }

    private void deleteMessageInfo()
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("message", "deleteMessageInfo", nbUserApiToken, "post");
        apiManager.addField("messageID", messageId);

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    }
                    else {
                        finish();
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

    private void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    private void setMessageInfo(NBMessageItem item)
    {
        textFrom.setText(item.senderName);
        textTo.setText(item.receiverName);
        textSubject.setText(item.subject);
        textDescription.setText(item.body);
        textDate.setText(item.createdDate);

        UrlImageViewHelper.setUrlDrawable(imageFrom, item.senderThumbnail, R.drawable.default_profile_image);
        UrlImageViewHelper.setUrlDrawable(imageTo, item.receiverThumbnail, R.drawable.default_profile_image);

        if (item.nextMessageId.equals("null")) {
            buttonNext.setBackgroundColor(Color.rgb(184, 184, 184));
            buttonNext.setEnabled(false);
        }
        else {
            buttonNext.setBackgroundColor(Color.rgb(22, 160, 133));
            buttonNext.setEnabled(true);
        }

        if (item.prevMessageId.equals("null")) {
            buttonPrev.setBackgroundColor(Color.rgb(184, 184, 184));
            buttonPrev.setEnabled(false);
        }
        else {
            buttonPrev.setBackgroundColor(Color.rgb(22, 160, 133));
            buttonPrev.setEnabled(true);
        }
      }
}
