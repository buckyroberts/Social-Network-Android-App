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
import com.thenewboston.view.FriendAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFriendsFragment extends Fragment {
    Boolean loadingMore = false;

    RecyclerView friendListView;
    LinearLayoutManager mFriendListLayoutManager;
    FriendAdapter mFriendAdapter;
    NBBaseActivity mActivity;

    int page = 1;

    private String mProfileId;
    private View mView;
    private JSONObject mProfileData;

    public void setProfileData(JSONObject pProfileData) {
        this.mProfileData = pProfileData;
    }

    public JSONObject getProfileData() {
        return mProfileData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.tab_profile_friends, container, false);
        mProfileData = getProfileData();

        Bundle extras = getActivity().getIntent().getExtras();
        mProfileId = extras.getString("profileId");

        mActivity =(ProfileActivity) getActivity();

        try {
            friendListView = (RecyclerView) mView.findViewById(R.id.profile_friends_recycler_view);
            mFriendListLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
            friendListView.setLayoutManager(mFriendListLayoutManager);
            mFriendAdapter = new FriendAdapter(mActivity, "friend", mActivity.nbUserID, mActivity.nbUserApiToken);
            friendListView.setAdapter(mFriendAdapter);

            JSONArray profileFriends = mProfileData.getJSONArray("FRIENDS");

            mFriendAdapter.appendData(mFriendAdapter.getFriendItemArrayListFromJSONArray(profileFriends));
            mFriendAdapter.notifyDataSetChanged();
            loadMoreFriends();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mView;
    }

    public void loadMoreFriends()
    {
        friendListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mFriendListLayoutManager.findFirstCompletelyVisibleItemPosition();
                int viewItems = mFriendListLayoutManager.getChildCount();
                int totalItems = mFriendListLayoutManager.getItemCount();

                if(!loadingMore && (firstVisibleItem + viewItems) > totalItems){
                    ProgressBar tLoadingMoreBar = (ProgressBar)mView.findViewById(R.id.loading_more_progress_bar);
                    tLoadingMoreBar.setVisibility(View.VISIBLE);

                    loadingMore = true;

                    //Getting article_item list using rest api
                    NBRestAPIManager apiManager = new NBRestAPIManager("profile", "getFriends", mActivity.nbUserApiToken, "post");

                    apiManager.addField("profileId", mProfileId);
                    page++;
                    apiManager.addField("page", Integer.toString(page));

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
                                    if (pResponseData.getJSONArray("FRIENDS").length() == 0) {
                                        loadingMore = true;
                                    }

                                    mFriendAdapter.appendData(mFriendAdapter.getFriendItemArrayListFromJSONArray(pResponseData.getJSONArray("FRIENDS")));
                                    mFriendAdapter.notifyDataSetChanged();
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