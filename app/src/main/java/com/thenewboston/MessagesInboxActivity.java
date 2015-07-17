package com.thenewboston;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.MessageAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagesInboxActivity extends NBBaseActivity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    MessageAdapter mViewAdapter;

    Boolean loadingMore = false;
    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "inbox";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_inbox);
        setTitle("Inbox");
        setNBToolbar();
        addNBNavigationMenu();

        initMessageListView();
        addLoadMore();
    }

    private void initMessageListView()
    {
        mRecyclerView = (RecyclerView) findViewById(R.id.messages_inbox_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerViewManager);

        mViewAdapter = new MessageAdapter(this);
        mRecyclerView.setAdapter(mViewAdapter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        loadMessageInfo();
    }

    private void loadMessageInfo()
    {
        page = 1;

        //Getting Received Message using rest api
        NBRestAPIManager apiManager = new NBRestAPIManager("message", "getInbox", nbUserApiToken, "post");
        apiManager.addField("page", Integer.toString(page));

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        mViewAdapter.clearData();
                        mViewAdapter.appendData(mViewAdapter.getMessageArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));

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

    private void addLoadMore()
    {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mRecyclerViewManager.findFirstCompletelyVisibleItemPosition();
                int viewItems = mRecyclerViewManager.getChildCount();
                int totalItems = mRecyclerViewManager.getItemCount();

                if (!loadingMore && (firstVisibleItem + viewItems) > totalItems) {
                    ProgressBar tLoadingMoreBar = (ProgressBar) findViewById(R.id.loading_more_progress_bar);
                    tLoadingMoreBar.setVisibility(View.VISIBLE);

                    loadingMore = true;

                    NBRestAPIManager apiManager = new NBRestAPIManager("message", "getInbox", nbUserApiToken, "post");
                    page = page + 1;
                    apiManager.addField("page", Integer.toString(page));

                    NBRestAPIListener restApiListener = new NBRestAPIListener() {
                        @Override
                        public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                            loadingMore = false;

                            ProgressBar tLoadingMoreBar = (ProgressBar)findViewById(R.id.loading_more_progress_bar);
                            tLoadingMoreBar.setVisibility(View.GONE);

                            try {
                                if (pResponseData.getString("STATUS").equals("ERROR")) {
                                    showPopupMessage(pResponseData.getString("ERROR"));
                                } else {
                                    mViewAdapter.appendData(mViewAdapter.getMessageArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));

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
                            }else {
                                loadingMore = false;

                                ProgressBar tLoadingMoreBar = (ProgressBar) findViewById(R.id.loading_more_progress_bar);
                                tLoadingMoreBar.setVisibility(View.GONE);

                                showPopupMessage(pError);
                            }
                        }
                    };

                    apiManager.execute(restApiListener);
                }
            }
        });
    }
}
