package com.thenewboston;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.InfoEmployementAdapter;
import com.thenewboston.view.items.NBEmployementItem;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoEmploymentActivity extends NBBaseActivity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    InfoEmployementAdapter mViewAdapter;

    Button mButtonSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "employment";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_employment);
        setTitle("Employment");
        setNBToolbar();
        addNBNavigationMenu();

        loadComponent();
        loadEmployeeInformation();
    }

    private void loadComponent()
    {
        mButtonSubmit = (Button) findViewById(R.id.button_submit);
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEmployerInfo();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.eductaion_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerViewManager);

        mViewAdapter = new InfoEmployementAdapter(this);
        mRecyclerView.setAdapter(mViewAdapter);
    }

    private void loadEmployeeInformation()
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("account", "getEmployeeInfo", nbUserApiToken, "get");

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        mViewAdapter.clearData();
                        mViewAdapter.appendData(mViewAdapter.getEmployeeArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));

                        mViewAdapter.notifyDataSetChanged();
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

    private void saveEmployerInfo()
    {
        NBRestAPIManager apiManager = new NBRestAPIManager("account", "saveEmployeeInfo", nbUserApiToken, "post");

        int index = 0;

        for (int i = 0; i < mViewAdapter.getItemCount(); i ++)
        {
            NBEmployementItem item = mViewAdapter.getItemAt(i);

            if (!item.employeeName.equals(""))
            {
                apiManager.addField(String.format("NAME%d", index), item.employeeName);
                apiManager.addField(String.format("START%d", index), item.from);
                apiManager.addField(String.format("END%d", index), item.to);
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
                        showPopupMessage("Your Employer Information has been updated successfully!");
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
        mViewAdapter.appendData(new NBEmployementItem());
        mViewAdapter.notifyDataSetChanged();
    }

    public void removeRow(int position)
    {
        mViewAdapter.removeData(position);
        mViewAdapter.notifyDataSetChanged();
    }

    public void refreshRow(int position)
    {
        mViewAdapter.notifyItemChanged(position);
    }
}
