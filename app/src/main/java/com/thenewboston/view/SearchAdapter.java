package com.thenewboston.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.LoginActivity;
import com.thenewboston.NBBaseActivity;
import com.thenewboston.ProfileActivity;
import com.thenewboston.R;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.items.NBSearchItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchItemHolder> implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private String nbUserID;
    private String nbUserApiToken;

    private ArrayList<NBSearchItem> mDataset = new ArrayList<>();
    private NBBaseActivity mParentActivity;
    private Context mContext;
    private GestureDetector gestureDetector;

    //Constructor: populates a local variable using data passed in
    public SearchAdapter(Context context, String pUserId, String pUserApiToken) {
        this.mContext = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());

        mParentActivity = (NBBaseActivity) context;

        this.nbUserID = pUserId;
        this.nbUserApiToken = pUserApiToken;
    }

    //Called by LayoutManager each time it needs to create a new row
    @Override
    public SearchItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SearchItemHolder(v);
    }

    //Replaces the contents of a view at each position
    public void onBindViewHolder(final SearchItemHolder pSearchItemHolder, int position) {
        NBSearchItem tSearchItem = mDataset.get(position);

        pSearchItemHolder.itemTitle.setText(tSearchItem.itemTitle);

        pSearchItemHolder.itemDescription.setText(tSearchItem.itemDescription);


        UrlImageViewHelper.setUrlDrawable(pSearchItemHolder.itemThumbnail, tSearchItem.itemImage, R.drawable.default_profile_image);

        //If item is User
        if (tSearchItem.itemType.equals("user")) {
            pSearchItemHolder.followButton.setVisibility(View.GONE);
            //Goto Profile Page
            Bundle tBundlePosterId = new Bundle();
            tBundlePosterId.putString("profileId", tSearchItem.itemID);
            pSearchItemHolder.itemContainer.setTag(tBundlePosterId);
            pSearchItemHolder.itemContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle tData = (Bundle) v.getTag();
                    Intent tIntent = new Intent(mContext, ProfileActivity.class);
                    tIntent.putExtra("profileId", tData.getString("profileId"));
                    mContext.startActivity(tIntent);
                }
            });

            if (tSearchItem.isFriend.equals("no")) {
                pSearchItemHolder.addButton.setVisibility(View.VISIBLE);
                //Add Friend Action
                Bundle tData = new Bundle();
                tData.putString("friend_id", tSearchItem.itemID);
                pSearchItemHolder.addButton.setTag(tData);
                pSearchItemHolder.addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pSearchItemHolder.actionProgressBar.setVisibility(View.VISIBLE);

                        Bundle tData = (Bundle) v.getTag();
                        v.setEnabled(false);

                        String tFriendId = tData.getString("friend_id");

                        //Call Rest API
                        NBRestAPIManager apiManager = new NBRestAPIManager("profile", "addFriend", nbUserApiToken, "post");

                        apiManager.addField("friendID", tFriendId);

                        NBRestAPIListener tRestApiListener = new NBRestAPIListener() {
                            @Override
                            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                                pSearchItemHolder.actionProgressBar.setVisibility(View.GONE);
                                pSearchItemHolder.addButton.setEnabled(true);

                                try {
                                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                                        mParentActivity.showPopupMessage(pResponseData.getString("ERROR"));
                                    } else {
                                        mParentActivity.showPopupMessage(pResponseData.getString("MESSAGE"));
                                        pSearchItemHolder.addButton.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    mParentActivity.showPopupMessage(e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(String pError, int pStatusCode) {
                                if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                                    mParentActivity.clearNBUserInfo();
                                    mParentActivity.gotoOtherActivity(LoginActivity.class, pError, true);
                                }else {
                                    pSearchItemHolder.actionProgressBar.setVisibility(View.GONE);
                                    pSearchItemHolder.addButton.setEnabled(true);

                                    mParentActivity.showPopupMessage(pError);
                                }
                            }
                        };

                        apiManager.execute(tRestApiListener);
                    }
                });
            }else{
                pSearchItemHolder.addButton.setVisibility(View.GONE);
            }

        //If item is Page
        } else {
            pSearchItemHolder.addButton.setVisibility(View.GONE);

            //Todo: tSearchItem.isFollowed is always empty String
            if (tSearchItem.isFollowed.equals("no")) {
                pSearchItemHolder.followButton.setVisibility(View.VISIBLE);

                //Follow Action
                Bundle tData = new Bundle();
                tData.putString("page_id", tSearchItem.itemID);
                pSearchItemHolder.followButton.setTag(tData);
                pSearchItemHolder.followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        pSearchItemHolder.actionProgressBar.setVisibility(View.VISIBLE);

                        Bundle tData = (Bundle) v.getTag();
                        v.setEnabled(false);

                        String tPageId = tData.getString("page_id");

                        //Call Rest API
                        NBRestAPIManager apiManager = new NBRestAPIManager("profile", "follow", nbUserApiToken, "post");

                        apiManager.addField("pageID", tPageId);

                        NBRestAPIListener tRestApiListener = new NBRestAPIListener() {
                            @Override
                            public void onSuccess(JSONObject pResponseData, int pStatusCode) {

                                pSearchItemHolder.actionProgressBar.setVisibility(View.GONE);
                                pSearchItemHolder.followButton.setEnabled(true);

                                try {
                                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                                        mParentActivity.showPopupMessage(pResponseData.getString("ERROR"));
                                    } else {
                                        mParentActivity.showPopupMessage(pResponseData.getString("MESSAGE"));
                                        pSearchItemHolder.itemDescription.setText(pResponseData.getString("FOLLOWERS"));
                                        pSearchItemHolder.followButton.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    mParentActivity.showPopupMessage(e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(String pError, int pStatusCode) {
                                if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                                    mParentActivity.clearNBUserInfo();
                                    mParentActivity.gotoOtherActivity(LoginActivity.class, pError, true);
                                }else {
                                    pSearchItemHolder.actionProgressBar.setVisibility(View.GONE);
                                    pSearchItemHolder.followButton.setEnabled(true);

                                    mParentActivity.showPopupMessage(pError);
                                }
                            }
                        };

                        apiManager.execute(tRestApiListener);
                    }
                });
            }else{
                pSearchItemHolder.followButton.setVisibility(View.GONE);
            }
        }
    }

    //Listen for double taps
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        //Only return true for double taps
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    }

    //Class to represent SearchItemHolder
    public static class SearchItemHolder extends RecyclerView.ViewHolder {

        RelativeLayout itemContainer;
        TextView itemTitle;
        TextView itemDescription;
        ImageView itemThumbnail;
        Button addButton;
        Button followButton;
        ProgressBar actionProgressBar;

        public SearchItemHolder(View itemView) {
            super(itemView);
            itemContainer = (RelativeLayout) itemView.findViewById(R.id.search_item_container);
            itemTitle = (TextView) itemView.findViewById(R.id.item_title);
            itemDescription = (TextView) itemView.findViewById(R.id.item_description);
            itemThumbnail = (ImageView) itemView.findViewById(R.id.item_thumbnail);

            addButton = (Button) itemView.findViewById(R.id.buttonAccept);
            followButton = (Button) itemView.findViewById(R.id.buttonUnfriend);

            actionProgressBar = (ProgressBar) itemView.findViewById(R.id.action_progress_bar);
        }

    }

    public void clearData() {
        mDataset.clear();
    }

    public void appendData(ArrayList<NBSearchItem> pData) {
        mDataset.addAll(pData);
    }


    //Size of data set is needed by LayoutManager
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public ArrayList<NBSearchItem> getSearchItemArrayListFromJSONArray(JSONArray pSearchItems) {
        ArrayList<NBSearchItem> tSearchItems = new ArrayList<>();

        if (pSearchItems != null) {
            for (int i = 0; i < pSearchItems.length(); i++) {

                try {
                    tSearchItems.add(new NBSearchItem(pSearchItems.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return tSearchItems;
    }

}