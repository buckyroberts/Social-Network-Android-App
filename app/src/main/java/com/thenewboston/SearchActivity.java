package com.thenewboston;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.SearchAdapter;

import org.json.JSONException;
import org.json.JSONObject;


public class SearchActivity extends NBBaseActivity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    SearchAdapter mViewAdapter;

    Boolean loadingMore = false;
    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "search";
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        setNBToolbar();
        addNBNavigationMenu();

        initSearchListView();
    }
    private void initSearchListView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.search_list_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerViewManager);
        mViewAdapter = new SearchAdapter(this, nbUserID, nbUserApiToken);
        mRecyclerView.setAdapter(mViewAdapter);
    }

    public void search(View v) {
        //Getting search_item list using rest api
        NBRestAPIManager apiManager = new NBRestAPIManager("search", "getList", nbUserApiToken, "post");

        TextView tSearchKey = (TextView)findViewById(R.id.search_keyword);
        apiManager.addField("keyword", tSearchKey.getText().toString());
        page = 1;
        showOverlay();

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {

                hideOverlay();
                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        mViewAdapter.clearData();
                        mViewAdapter.appendData(mViewAdapter.getSearchItemArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));
                        mViewAdapter.notifyDataSetChanged();
                        loadMoreArticles();
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

        apiManager.execute(restApiListener);
    }

    //load more articles
    public void loadMoreArticles()
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

                    NBRestAPIManager apiManager = new NBRestAPIManager("search", "getList", nbUserApiToken, "post");
                    page = page + 1;
                    apiManager.addField("page", Integer.toString(page));

                    TextView tSearchKey = (TextView)findViewById(R.id.search_keyword);
                    apiManager.addField("keyword", tSearchKey.getText().toString());

                    NBRestAPIListener restApiListener = new NBRestAPIListener() {
                        @Override
                        public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                            loadingMore = false;

                            ProgressBar tLoadingMoreBar = (ProgressBar)findViewById(R.id.loading_more_progress_bar);
                            tLoadingMoreBar.setVisibility(View.GONE);

                            hideOverlay();
                            try {
                                if (pResponseData.getString("STATUS").equals("ERROR")) {
                                    showPopupMessage(pResponseData.getString("ERROR"));
                                } else {
                                    mViewAdapter.appendData(mViewAdapter.getSearchItemArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));
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

                                hideOverlay();
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
