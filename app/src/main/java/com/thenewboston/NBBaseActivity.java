package com.thenewboston;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thenewboston.navigation.NavigationDrawer;
import com.thenewboston.navigation.NavigationDrawerAdapter;
import com.thenewboston.navigation.NavigationDrawerItem;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Base Activity Class for Thenewboston
 */
public class NBBaseActivity extends ActionBarActivity {

    protected String nbPrefsName = "thenewboston";

    protected String currentActivity;

    protected String nbUserID = null;
    protected String nbUserEmail = null;
    protected String nbUserApiToken = null;

    //ArrayLists to hold NavigationDrawer items
    protected ArrayList<NavigationDrawerItem> parentItems = new ArrayList<>();
    protected ArrayList<Object> childItems = new ArrayList<>();

    protected String popupMessage = null;

    protected Toast messageBox;

    private TextView notificationCount;
    private NavigationDrawerItem friendRequestCount;
    private NavigationDrawerItem newMessageCount;

    protected void onCreate(Bundle savedInstanceStateBundle) {
        super.onCreate(savedInstanceStateBundle);

        //Init Toast Message
        messageBox = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);

        messageBox.setGravity(Gravity.CENTER, 0, 0);

        Bundle tExtras = getIntent().getExtras();
        if (tExtras != null) {
            String tNotification = tExtras.getString("notification");
            if (!TextUtils.isEmpty(tNotification)) {
                messageBox.setText(tNotification);
                messageBox.show();

                getIntent().removeExtra("notification");
            }
        }

        //Load User Info that have stored to Shared Preferences
        loadNBUserInfo();
    }

    protected void onDestroy() {
        super.onDestroy();

        //Hide Toast Message
        messageBox.cancel();
    }

    /**
     * Set Custom Toolbar
     *
     */
    protected void setNBToolbar() {
        //Set Toolbar to act as Actionbar for this Activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    protected void addNBNavigationMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        //Add NavigationDrawer
        NavigationDrawer drawerFragment = (NavigationDrawer) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        drawerFragment.setUp(R.id.left_drawer, (DrawerLayout) findViewById(R.id.base_layout), toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Create ExpandableList and set properties
        ExpandableListView expandableList = (ExpandableListView) findViewById(R.id.expandableListView);
        //expandableList.setDividerHeight(1);
        expandableList.setGroupIndicator(null);
        expandableList.setClickable(true);

        //Set Parent and Children items for navigation
        setParentItems();
        setChildItems();

        //Create the Adapter
        final NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(parentItems, childItems);
        adapter.setInflater((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
        expandableList.setAdapter(adapter);

        //Handle clicks on child items
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                NavigationDrawerItem childItem = (NavigationDrawerItem) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                String childName = childItem.getItemTitle();
                Toast.makeText(NBBaseActivity.this, childName, Toast.LENGTH_SHORT).show();
                switch(childName){
                    case "Inbox":
                        hideNavigationDrawer();
                        gotoOtherActivity(MessagesInboxActivity.class, null, true);
                        break;
                    case "Sent":
                        hideNavigationDrawer();
                        gotoOtherActivity(MessagesSentActivity.class, null, true);
                        break;
                    case "Trash":
                        hideNavigationDrawer();
                        gotoOtherActivity(MessagesTrashActivity.class, null, true);
                        break;
                    case "Compose":
                        hideNavigationDrawer();

                        gotoOtherActivity(FriendChooseActivity.class, null, false);

                        break;
                    case "Basic Info":
                        hideNavigationDrawer();
                        gotoOtherActivity(InfoBasicActivity.class, null, true);
                        break;
                    case "Contact":
                        hideNavigationDrawer();
                        gotoOtherActivity(InfoContactActivity.class, null, true);
                        break;
                    case "Education":
                        hideNavigationDrawer();
                        gotoOtherActivity(InfoEducationActivity.class, null, true);
                        break;
                    case "Employment":
                        hideNavigationDrawer();
                        gotoOtherActivity(InfoEmploymentActivity.class, null, true);
                        break;
                    case "Links":
                        hideNavigationDrawer();
                        gotoOtherActivity(InfoLinksActivity.class, null, true);
                        break;
                    case "Friend Requests":
                        hideNavigationDrawer();
                        gotoOtherActivity(FriendRequestActivity.class, null, true);
                        break;
                    case "Change Password":
                        hideNavigationDrawer();
                        gotoOtherActivity(ChangePasswordActivity.class, null, true);
                        break;
                    case "Delete Account":
                        hideNavigationDrawer();
                        gotoOtherActivity(DeleteAccountActivity.class, null, true);
                        break;
                }

                return true;
            }
        });

        //Handle clicks on parent items without children
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (adapter.getChildrenCount(groupPosition) > 0) return false;
                String tMenu = ((NavigationDrawerItem) adapter.getGroup(groupPosition)).getItemTitle();
                //When menu item clicked, hide drawer and go to activity
                switch (tMenu) {
                    case "Logout":
                        clearNBUserInfo();
                        gotoOtherActivity(StreamActivity.class, null, true);
                        break;
                    case "Stream":
                        hideNavigationDrawer();
                        gotoOtherActivity(StreamActivity.class, null, true);
                        break;
                    case "Search":
                        hideNavigationDrawer();
                        gotoOtherActivity(SearchActivity.class, null, false);
                        break;
                    case "Profile":
                        hideNavigationDrawer();
                        Intent tIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                        tIntent.putExtra("profileId", nbUserID);
                        startActivity(tIntent);
                        //gotoOtherActivity(ProfileActivity.class, null, false);
                        break;
                }

                return true;
            }
        });

        addNotificationBar();
    }

    private void addNotificationBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        LayoutInflater inflater = getLayoutInflater();
        View notification_layout = inflater.inflate(R.layout.notification_layout, (ViewGroup) findViewById(R.id.notification_layout_root));

        ImageView imageView = (ImageView) notification_layout.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoOtherActivity(NotificationActivity.class, null, true);
            }
        });

        notificationCount = (TextView) notification_layout.findViewById(R.id.textCount);

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        notification_layout.setLayoutParams(params);
        params.rightMargin = 30;
        toolbar.addView(notification_layout);
    }

    //Add parent items
    public void setParentItems() {
        parentItems.add(new NavigationDrawerItem("Stream", R.drawable.ic_nav_home));
        parentItems.add(new NavigationDrawerItem("Messages", R.drawable.ic_nav_message));
        parentItems.add(new NavigationDrawerItem("Profile", R.drawable.ic_nav_profile));
        parentItems.add(new NavigationDrawerItem("Information", R.drawable.ic_nav_notepad));
        parentItems.add(new NavigationDrawerItem("Search", R.drawable.ic_nav_search));
        parentItems.add(new NavigationDrawerItem("Other", R.drawable.ic_nav_wrench));
        parentItems.add(new NavigationDrawerItem("Logout", R.drawable.ic_nav_logout));
    }

    //Set child items
    public void setChildItems() {
        //Stream
        ArrayList<NavigationDrawerItem> child = new ArrayList<>();
        childItems.add(child);

        //Messages
        child = new ArrayList<>();
        newMessageCount = new NavigationDrawerItem("Inbox", R.drawable.ic_minus);
        newMessageCount.setBadgeNumber(0);
        child.add(newMessageCount);
        child.add(new NavigationDrawerItem("Sent", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Trash", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Compose", R.drawable.ic_minus));
        childItems.add(child);

        //Profile
        child = new ArrayList<>();
        childItems.add(child);

        //Info
        child = new ArrayList<>();
        child.add(new NavigationDrawerItem("Basic Info", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Contact", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Education", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Employment", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Links", R.drawable.ic_minus));
        childItems.add(child);

        //Search
        child = new ArrayList<>();
        childItems.add(child);

        //Other
        child = new ArrayList<>();
        friendRequestCount = new NavigationDrawerItem("Friend Requests", R.drawable.ic_minus);
        friendRequestCount.setBadgeNumber(0);
        child.add(friendRequestCount);
        child.add(new NavigationDrawerItem("Change Password", R.drawable.ic_minus));
        child.add(new NavigationDrawerItem("Delete Account", R.drawable.ic_minus));
        childItems.add(child);

        //Logout
        child = new ArrayList<>();
        childItems.add(child);
    }

    /**
     * Get User ID, Email, Api Token From Shared Preferences
     *
     * @return void;
     */
    protected void loadNBUserInfo() {
        SharedPreferences nbSettings = getSharedPreferences(nbPrefsName, MODE_PRIVATE);

        //Get user info from the SharedPreferences Storage
        nbUserID = nbSettings.getString("nb_user_id", null);
        nbUserEmail = nbSettings.getString("nb_user_email", null);
        nbUserApiToken = nbSettings.getString("nb_user_api_token", null);

        return;
    }

    /**
     * Check user has logged in or not
     * @return Boolean
     */
    protected Boolean isUserLoggedIn() {
        return nbUserID == null ||nbUserApiToken == null ? false : true;
    }

    public void cleanPopupMessages() {
        popupMessage = null;
    }

    public void addPopupMessage(String pMessage) {
        if (TextUtils.isEmpty(popupMessage)) {
            popupMessage = pMessage;
        } else {
            popupMessage += System.getProperty("line.separator") + pMessage;
        }
    }

    public void showPopupMessage() {
        messageBox.setText(popupMessage);
        messageBox.show();

        /*LayoutInflater inflater = getLayoutInflater();
        View toast_layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) toast_layout.findViewById(R.id.popup_text);
        text.setText(popupMessage);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toast_layout);
        toast.show();*/
    }

    public void showPopupMessage(String pMessage) {
        messageBox.setText(pMessage);
        messageBox.show();
    }


    /**
     * Create Empty Layout
     * @param v
     */
    public void overlayClick(View v) {

    }

    public void showOverlay() {
        hideInputKeyboard();

        RelativeLayout overlay = (RelativeLayout)findViewById(R.id.progress_overlay);
        overlay.setVisibility(View.VISIBLE);
    }

    public void hideOverlay() {
        RelativeLayout overlay = (RelativeLayout)findViewById(R.id.progress_overlay);
        overlay.setVisibility(View.INVISIBLE);
    }

    public void hideInputKeyboard() {
        //Hide Input Keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Clear User Information on the device
     *
     */
    public void clearNBUserInfo() {
        SharedPreferences nbSettings = getSharedPreferences(nbPrefsName, MODE_PRIVATE);
        SharedPreferences.Editor nbSettingsEditor = nbSettings.edit();

        nbSettingsEditor.remove("nb_user_id");
        nbSettingsEditor.remove("nb_user_email");
        nbSettingsEditor.remove("nb_user_api_token");

        nbSettingsEditor.commit();
    }

    public void gotoOtherActivity(Class pDestClass, String pMessage, Boolean pFinishCurrentActivity) {

        Intent tIntent = new Intent(this, pDestClass);

        if (pMessage != null) {
            tIntent.putExtra("notification", pMessage);
        }

        startActivity(tIntent);

        if (pFinishCurrentActivity) {
            finish();
        }
    }

    protected void hideNavigationDrawer() {
        NavigationDrawer tDrawerFragment = (NavigationDrawer) getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        tDrawerFragment.closeNavigationDrawer();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        loadCountInformation();
    }

    private void loadCountInformation()
    {
        if (nbUserApiToken == null)
            return;

        //Getting Initial Information
        NBRestAPIManager apiManager = new NBRestAPIManager("notification", "getNotificationCount", nbUserApiToken, "get");

        NBRestAPIListener streamListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();
                try {
                    if (!pResponseData.getString("STATUS").equals("ERROR")) {
                        JSONObject object = pResponseData.getJSONObject("RESULT");

                        int friend_request = object.getInt("friend_request");
                        int notification = object.getInt("new_notification");
                        int new_message = object.getInt("new_message");

                        if (notificationCount != null)
                            notificationCount.setText(String.valueOf(notification));

                        if (friendRequestCount != null)
                            friendRequestCount.setBadgeNumber(friend_request);

                        if (newMessageCount != null)
                            newMessageCount.setBadgeNumber(new_message);
                    }
                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(String pError, int pStatusCode) {

            }
        };

        apiManager.execute(streamListener);
    }
}
