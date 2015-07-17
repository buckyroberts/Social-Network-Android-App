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
import com.thenewboston.view.items.NBFriendItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendItemHolder> implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener  {
    private String nbUserID;
    private String nbUserApiToken;
    private String mAdapterType;

    private ArrayList<NBFriendItem> mDataset = new ArrayList<>();
    private NBBaseActivity mParentActivity;
    private Context mContext;
    private GestureDetector gestureDetector;

    public FriendAdapter(Context context, String pAdapterType, String pUserId, String pUserApiToken)
    {
        this.mContext = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());

        mParentActivity = (NBBaseActivity) context;

        this.nbUserID = pUserId;
        this.nbUserApiToken = pUserApiToken;
        this.mAdapterType = pAdapterType;
    }

    @Override
    public int getItemViewType(int position) {
        switch (mAdapterType)
        {
            case "sent":
                return 2; //Sent Friends Requests
            case "received":
                return 3; //Received Friends Request
            default:
            case "friend":
                return 1; //Friends
        }
    }

    @Override
    public FriendAdapter.FriendItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_friend_item, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_friend_item, parent, false);
        }
        return new FriendItemHolder(v);
    }

    @Override
    public void onBindViewHolder(final FriendAdapter.FriendItemHolder holder, int position) {
        NBFriendItem tFriendItem = mDataset.get(position);

        holder.friendName.setText(tFriendItem.friendName);

        holder.friendDescription.setText(tFriendItem.friendDescription);

        UrlImageViewHelper.setUrlDrawable(holder.friendThumbnail, tFriendItem.friendThumbnail, R.drawable.default_profile_image);

        Bundle tBundleFriendId = new Bundle();
        tBundleFriendId.putString("profileId", tFriendItem.friendID);

        holder.container.setTag(tBundleFriendId);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle tData = (Bundle) v.getTag();
                Intent tIntent = new Intent(mContext, ProfileActivity.class);
                tIntent.putExtra("profileId", tData.getString("profileId"));
                mContext.startActivity(tIntent);
            }
        });

        class FriendActionButtonListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {

                final Bundle tData = (Bundle) v.getTag();

                final String friendId = tData.getString("profileId");
                final String action = tData.getString("action");

                holder.actionProgressBar.setVisibility(View.VISIBLE);
                v.setEnabled(false);

                //Call Rest API
                NBRestAPIManager apiManager = new NBRestAPIManager("profile", action, nbUserApiToken, "post");

                apiManager.addField("friendID", tData.getString("profileId"));

                NBRestAPIListener tRestApiListener = new NBRestAPIListener() {
                    @Override
                    public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                        holder.actionProgressBar.setVisibility(View.GONE);

                        try {
                            if (pResponseData.getString("STATUS").equals("ERROR")) {
                                mParentActivity.showPopupMessage(pResponseData.getString("ERROR"));
                                holder.actionButton2.setEnabled(true);
                            } else {
                                mParentActivity.showPopupMessage(pResponseData.getString("MESSAGE"));

                                if (action.equals("addFriend")) {
                                    //Show Delete Request Button
                                    tData.putString("action", "deleteFriendRequest");
                                    holder.actionButton1.setTag(tData);
                                    holder.actionButton1.setEnabled(true);
                                    holder.actionButton1.setText("Delete Request");
                                    holder.actionButton1.setBackgroundResource(R.color.red_button_bg);
                                    holder.actionButton1.setVisibility(View.VISIBLE);

                                    holder.actionButton2.setVisibility(View.GONE);
                                } else if(action.equals("deleteFriendRequest") || action.equals("declineFriendRequest")|| action.equals("unfriend")) {
                                    //Show Add Friend Button
                                    tData.putString("action", "addFriend");
                                    holder.actionButton1.setTag(tData);
                                    holder.actionButton1.setEnabled(true);
                                    holder.actionButton1.setText("Add");
                                    holder.actionButton1.setBackgroundResource(R.color.blue_button_bg);
                                    holder.actionButton1.setVisibility(View.VISIBLE);
                                    holder.actionButton2.setVisibility(View.GONE);
                                } else if(action.equals("approveFriendRequest")) {
                                    tData.putString("action", "unfriend");
                                    holder.actionButton1.setEnabled(true);
                                    holder.actionButton1.setTag(tData);
                                    holder.actionButton1.setText("Unfriend");
                                    holder.actionButton1.setBackgroundResource(R.color.red_button_bg);
                                    holder.actionButton1.setVisibility(View.VISIBLE);

                                    holder.actionButton2.setVisibility(View.GONE);
                                }

                            }
                        } catch (JSONException e) {
                            mParentActivity.showPopupMessage(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String pError, int pStatusCode) {
                        holder.actionProgressBar.setVisibility(View.GONE);
                        if (pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                            mParentActivity.clearNBUserInfo();
                            mParentActivity.gotoOtherActivity(LoginActivity.class, pError, true);
                        } else {
                            holder.actionButton2.setEnabled(true);
                            mParentActivity.showPopupMessage(pError);
                        }
                    }
                };

                apiManager.execute(tRestApiListener);
            }
        }


        if (mAdapterType.equals("friend")) {

            holder.actionButton1.setOnClickListener(new FriendActionButtonListener());
            holder.actionButton2.setOnClickListener(new FriendActionButtonListener());

            if (tFriendItem.friendType.equals("friend")) {
                //UnFriends Button
                Bundle param1 = new Bundle();

                param1.putString("profileId", tFriendItem.friendID);
                param1.putString("action", "unfriend");
                holder.actionButton1.setTag(param1);
                holder.actionButton1.setText("Unfriend");
                holder.actionButton1.setBackgroundResource(R.color.red_button_bg);
                holder.actionButton1.setVisibility(View.VISIBLE);


                holder.actionButton2.setVisibility(View.GONE);


            } else if(tFriendItem.friendType.equals("sent")) {
                //Sent Friend Request
                Bundle param1 = new Bundle();

                param1.putString("profileId", tFriendItem.friendID);
                param1.putString("action", "deleteFriendRequest");

                holder.actionButton1.setTag(param1);
                holder.actionButton1.setText("Delete Request");
                holder.actionButton1.setBackgroundResource(R.color.red_button_bg);
                holder.actionButton1.setVisibility(View.VISIBLE);

                holder.actionButton2.setVisibility(View.GONE);
            } else if(tFriendItem.friendType.equals("received")) {
                //Received Friend Request
                Bundle param1 = new Bundle();
                param1.putString("profileId", tFriendItem.friendID);
                param1.putString("action", "approveFriendRequest");
                holder.actionButton1.setTag(param1);
                holder.actionButton1.setBackgroundResource(R.color.blue_button_bg);
                holder.actionButton1.setText("Approve Request");
                holder.actionButton1.setVisibility(View.VISIBLE);

                Bundle param2 = new Bundle();
                param2.putString("profileId", tFriendItem.friendID);
                param2.putString("action", "declineFriendRequest");
                holder.actionButton2.setTag(param2);
                holder.actionButton2.setBackgroundResource(R.color.red_button_bg);
                holder.actionButton2.setText("Decline Request");
                holder.actionButton2.setVisibility(View.VISIBLE);
            } else if(tFriendItem.friendType.equals("none")){
                //Add Button
                Bundle param1 = new Bundle();

                param1.putString("profileId", tFriendItem.friendID);
                param1.putString("action", "addFriend");

                holder.actionButton1.setTag(param1);

                holder.actionButton1.setText("Add");
                holder.actionButton1.setBackgroundResource(R.color.blue_button_bg);
                holder.actionButton1.setVisibility(View.VISIBLE);
                holder.actionButton2.setVisibility(View.GONE);
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
    public static class FriendItemHolder extends RecyclerView.ViewHolder {

        RelativeLayout container;
        TextView friendName;
        TextView friendDescription;
        ImageView friendThumbnail;

        Button actionButton1;
        Button actionButton2;

        ProgressBar actionProgressBar;

        public FriendItemHolder(View itemView) {
            super(itemView);
            container = (RelativeLayout) itemView.findViewById(R.id.friend_item_container);
            friendName = (TextView) itemView.findViewById(R.id.friend_name);
            friendDescription = (TextView) itemView.findViewById(R.id.friend_description);
            friendThumbnail = (ImageView) itemView.findViewById(R.id.friend_thumbnail);

            actionButton1 = (Button) itemView.findViewById(R.id.buttonAction1);
            actionButton2 = (Button) itemView.findViewById(R.id.buttonAction2);

            actionProgressBar = (ProgressBar) itemView.findViewById(R.id.action_progress_bar);
        }

    }

    public void appendData(ArrayList<NBFriendItem> pData)
    {
        mDataset.addAll(pData);
    }


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

    public ArrayList<NBFriendItem> getFriendItemArrayListFromJSONArray(JSONArray pFriendItems) {
        ArrayList<NBFriendItem> tFriendItems = new ArrayList<>();

        if (pFriendItems != null) {
            for (int i = 0; i < pFriendItems.length(); i++) {

                try {
                    tFriendItems.add(new NBFriendItem(pFriendItems.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return tFriendItems;
    }
}
