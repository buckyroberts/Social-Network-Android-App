package com.thenewboston;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.ArticleAdapter;

import org.json.JSONException;
import org.json.JSONObject;


public class StreamActivity extends NBBaseActivity {
    Boolean loadingMore = false;

    Boolean hideNewPostNav = false;
    RecyclerView articleListView;
    LinearLayoutManager mArticleListLayoutManger;
    ArticleAdapter mArticleListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.currentActivity = "main";
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stream);
        setTitle("Stream");

        if ( !isUserLoggedIn() ) {

            //Go to Login Activity
            Intent toLoginIntent = new Intent(this, LoginActivity.class);
            this.startActivity(toLoginIntent);
            this.finish();

            return;
        }

        setNBToolbar();
        addNBNavigationMenu();

        initArticlesList();

        //Getting article_item list using rest api
        NBRestAPIManager apiManager = new NBRestAPIManager("stream", "getList", nbUserApiToken, "get");

        NBRestAPIListener streamListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {

                hideOverlay();
                showNewPostNav();
                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        mArticleListAdapter.appendArticles(mArticleListAdapter.getArticleArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));
                        mArticleListAdapter.notifyDataSetChanged();
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

        apiManager.execute(streamListener);



    }

    private void initArticlesList() {
        articleListView = (RecyclerView) findViewById(R.id.article_list_recycler_view);
        mArticleListLayoutManger = new LinearLayoutManager(this);
        articleListView.setLayoutManager(mArticleListLayoutManger);
        mArticleListAdapter = new ArticleAdapter(this, nbUserID, nbUserApiToken, "Stream");
        articleListView.setAdapter(mArticleListAdapter);
    }

    //load more articles
    public void loadMoreArticles()
    {
        articleListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mArticleListLayoutManger.findFirstCompletelyVisibleItemPosition();
                int viewItems = mArticleListLayoutManger.getChildCount();
                int totalItems = mArticleListLayoutManger.getItemCount();

                if (dy <= 0) {
                    if(hideNewPostNav)
                        showNewPostNav();
                } else if(!hideNewPostNav) {
                    hideNewPostNav();
                }

                /*
                if (totalItems > 0) {
                    if (firstVisibleItem == 0 && dy <= 0) {
                        if(hideNewPostNav)
                            showNewPostNav();
                    } else if(!hideNewPostNav) {
                        hideNewPostNav();
                    }
                }
                */

                if(!loadingMore && (firstVisibleItem + viewItems) > totalItems){
                    ProgressBar tLoadingMoreBar = (ProgressBar)findViewById(R.id.loading_more_progress_bar);
                    tLoadingMoreBar.setVisibility(View.VISIBLE);

                    loadingMore = true;

                    String tLastDate = mArticleListAdapter.getLastArticleDate();

                    //Getting article_item list using rest api
                    NBRestAPIManager apiManager = new NBRestAPIManager("stream", "getList", nbUserApiToken, "get");

                    if (tLastDate != null) {
                        apiManager.addField("lastDate", tLastDate);
                    }

                    NBRestAPIListener streamListener = new NBRestAPIListener() {
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

                                    mArticleListAdapter.appendArticles(mArticleListAdapter.getArticleArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));
                                    mArticleListAdapter.notifyDataSetChanged();
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

                    apiManager.execute(streamListener);

                }

            }
        });
    }

    private void showNewPostNav() {
        LinearLayout tNewPostNav = (LinearLayout)findViewById(R.id.new_post_buttons_wrap);
        if (tNewPostNav.getVisibility() != View.VISIBLE)
        {
            tNewPostNav.setVisibility(View.VISIBLE);
        }
        Animation tSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        tNewPostNav.startAnimation(tSlideUp);
        hideNewPostNav = false;
    }

    private void hideNewPostNav() {
        LinearLayout tNewPostNav = (LinearLayout)findViewById(R.id.new_post_buttons_wrap);

        Animation tSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_out);
        tNewPostNav.startAnimation(tSlideIn);
        hideNewPostNav = true;
    }

    public void createNewPost(View view) {
        Intent tCreateNewPost = new Intent(this, CreatePostActivity.class);
        tCreateNewPost.putExtra("selectPhoto", false);
        this.startActivity(tCreateNewPost);
    }

    public void createNewPhoto(View view) {
        Intent tCreateNewPhoto = new Intent(this, CreatePostActivity.class);
        tCreateNewPhoto.putExtra("selectPhoto", true);
        this.startActivity(tCreateNewPhoto);
    }
}
