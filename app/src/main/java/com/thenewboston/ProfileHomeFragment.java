package com.thenewboston;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.ArticleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileHomeFragment extends Fragment {
    Boolean loadingMore = false;

    RecyclerView articleListView;
    LinearLayoutManager mArticleListLayoutManger;
    ArticleAdapter mArticleListAdapter;
    NBBaseActivity mActivity;

    private String mProfileId;
    private View mView;
    private JSONObject mProfileData;

    private String imageFile = null;

    public ProfileHomeFragment(){
        super();
    }

    public void setProfileData(JSONObject pProfileData) {
        this.mProfileData = pProfileData;
    }

    public JSONObject getProfileData() {
        return mProfileData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.tab_profile_home, container, false);
        mProfileData = getProfileData();

        Bundle extras = getActivity().getIntent().getExtras();
        mProfileId = extras.getString("profileId");

        mActivity =(ProfileActivity) getActivity();

        try {
            JSONObject profileInfo = mProfileData.getJSONObject("INFO");
            articleListView = (RecyclerView) mView.findViewById(R.id.profile_articles_recycler_view);
            mArticleListLayoutManger = new LinearLayoutManager(mActivity.getApplicationContext());
            articleListView.setLayoutManager(mArticleListLayoutManger);
            mArticleListAdapter = new ArticleAdapter(mActivity, mActivity.nbUserID, mActivity.nbUserApiToken, "Profile", profileInfo, mProfileId);
            articleListView.setAdapter(mArticleListAdapter);

            JSONArray profilePosts = mProfileData.getJSONArray("POSTS");
            mArticleListAdapter.appendArticles(mArticleListAdapter.getArticleArrayListFromJSONArray(profilePosts));
            mArticleListAdapter.notifyDataSetChanged();
            loadMoreArticles();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mView;
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

                if(!loadingMore && (firstVisibleItem + viewItems) > totalItems){
                    ProgressBar tLoadingMoreBar = (ProgressBar)mView.findViewById(R.id.loading_more_progress_bar);
                    tLoadingMoreBar.setVisibility(View.VISIBLE);

                    loadingMore = true;

                    String tLastDate = mArticleListAdapter.getLastArticleDate();

                    //Getting article_item list using rest api
                    NBRestAPIManager apiManager = new NBRestAPIManager("profile", "getPosts", mActivity.nbUserApiToken, "post");

                    apiManager.addField("profileId", mProfileId);

                    if (tLastDate != null) {
                        apiManager.addField("lastDate", tLastDate);
                    }

                    NBRestAPIListener streamListener = new NBRestAPIListener() {
                        @Override
                        public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                            loadingMore = false;

                            ProgressBar tLoadingMoreBar = (ProgressBar)mView.findViewById(R.id.loading_more_progress_bar);
                            tLoadingMoreBar.setVisibility(View.GONE);

                            mActivity.hideOverlay();
                            try {
                                if (pResponseData.getString("STATUS").equals("ERROR")) {
                                    mActivity.showPopupMessage(pResponseData.getString("ERROR"));
                                } else {

                                    mArticleListAdapter.appendArticles(mArticleListAdapter.getArticleArrayListFromJSONArray(pResponseData.getJSONArray("POSTS")));
                                    mArticleListAdapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                mActivity.showPopupMessage(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String pError, int pStatusCode) {
                            if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                                mActivity.clearNBUserInfo();
                                mActivity.gotoOtherActivity(LoginActivity.class, pError, true);
                            }else {
                                loadingMore = false;

                                ProgressBar tLoadingMoreBar = (ProgressBar) mView.findViewById(R.id.loading_more_progress_bar);
                                tLoadingMoreBar.setVisibility(View.GONE);

                                mActivity.hideOverlay();
                                mActivity.showPopupMessage(pError);
                            }
                        }
                    };

                    apiManager.execute(streamListener);

                }

            }
        });
    }

}