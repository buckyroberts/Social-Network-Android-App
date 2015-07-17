package com.thenewboston;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.InfoUserAdapter;
import com.thenewboston.view.items.NBUserItem;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoContactActivity extends NBBaseActivity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    InfoUserAdapter mViewAdapter;

    EditText cellPhone;
    EditText homePhone;
    EditText workPhone;
    EditText email;

    Spinner emailSpinner;

    ToggleButton cellPhoneToggle;
    ToggleButton homePhoneToggle;
    ToggleButton workPhoneToggle;

    Button mButtonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "contact";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_contact);
        setTitle("Contact Info");
        setNBToolbar();
        addNBNavigationMenu();

        loadComponent();
        loadContactInformation();
    }

    private void loadComponent()
    {
        email = (EditText) findViewById(R.id.email);
        cellPhone = (EditText) findViewById(R.id.cellPhone);
        workPhone = (EditText) findViewById(R.id.workPhone);
        homePhone = (EditText) findViewById(R.id.homePhone);
        emailSpinner = (Spinner) findViewById(R.id.emailSpinner);
        cellPhoneToggle = (ToggleButton) findViewById(R.id.cellPhoneToggle);
        workPhoneToggle = (ToggleButton) findViewById(R.id.workPhoneToggle);
        homePhoneToggle = (ToggleButton) findViewById(R.id.homePhoneToggle);

        mButtonSubmit = (Button) findViewById(R.id.button_submit);
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContactInfo();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.eductaion_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerViewManager);

        mViewAdapter = new InfoUserAdapter(this);
        mRecyclerView.setAdapter(mViewAdapter);
    }

    private void loadContactInformation()
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("account", "getContactInfo", nbUserApiToken, "get");

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        JSONObject object = pResponseData.getJSONObject("RESULT");

                        email.setText(object.getString("email"));
                        workPhone.setText(object.getString("work_phone"));
                        homePhone.setText(object.getString("home_phone"));
                        cellPhone.setText(object.getString("cell_phone"));

                        emailSpinner.setSelection(object.getString("email_visibility").equals("0") ? 1 : 0);
                        homePhoneToggle.setChecked(!object.getString("home_phone_visibility").equals("1"));
                        workPhoneToggle.setChecked(!object.getString("work_phone_visibility").equals("1"));
                        cellPhoneToggle.setChecked(!object.getString("cell_phone_visibility").equals("1"));

                        mViewAdapter.clearData();
                        mViewAdapter.appendData(mViewAdapter.getUserArrayListFromJSONArray(object.getJSONArray("contact")));

                        mViewAdapter.notifyDataSetChanged();

                        setListViewHeightBasedOnChildren();
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

    private void saveContactInfo()
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("account", "saveContactInfo", nbUserApiToken, "post");

        apiManager.addField("email", email.getText().toString());
        apiManager.addField("work_phone", workPhone.getText().toString());
        apiManager.addField("home_phone", homePhone.getText().toString());
        apiManager.addField("cell_phone", cellPhone.getText().toString());
        apiManager.addField("email_visibility", emailSpinner.getSelectedItem().toString().equals("Public") ? "1" : "0");
        apiManager.addField("home_phone_visibility", homePhoneToggle.isChecked() ? "0" : "1");
        apiManager.addField("work_phone_visibility", workPhoneToggle.isChecked() ? "0" : "1");
        apiManager.addField("cell_phone_visibility", cellPhoneToggle.isChecked() ? "0" : "1");

        int index = 0;

        for (int i = 0; i < mViewAdapter.getItemCount(); i ++)
        {
            NBUserItem item = mViewAdapter.getItemAt(i);

            if (!item.userName.equals(""))
            {
                apiManager.addField(String.format("CONTACT_NAME%d", index), item.userName);
                apiManager.addField(String.format("CONTACT_TYPE%d", index), item.userType);
                apiManager.addField(String.format("VISIBILITY%d", index), item.isPublic);

                index ++;
            }
        }

        apiManager.addField("COUNT", String.valueOf(index));

        showOverlay();

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        showPopupMessage("Your Contact Information has been updated successfully!");
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

    public void addRow()
    {
        mViewAdapter.appendData(new NBUserItem());
        mViewAdapter.notifyDataSetChanged();

        setListViewHeightBasedOnChildren();
    }

    public void removeRow(int position)
    {
        mViewAdapter.removeData(position);
        mViewAdapter.notifyDataSetChanged();

        setListViewHeightBasedOnChildren();
    }

    public void refreshRow(int position)
    {
        mViewAdapter.notifyItemChanged(position);
    }

    private void setListViewHeightBasedOnChildren() {
        int totalHeight = mRecyclerView.getPaddingTop() + mRecyclerView.getPaddingBottom();

        for (int i = 0; i < mViewAdapter.getItemCount(); i++)
            totalHeight += 550;

        mRecyclerView.getLayoutParams().height = totalHeight + (25 * (mViewAdapter.getItemCount() - 1));
    }

    private int getInt(String strNum)
    {
        int num = 0;

        try {
            num = Integer.parseInt(strNum);
        }
        catch (Exception e)
        {
            num = 0;
        }

        return num;
    }
}
