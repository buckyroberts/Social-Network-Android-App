package com.thenewboston.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.FriendRequestReceivedFragment;
import com.thenewboston.R;
import com.thenewboston.view.items.NBFriendItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FriendIncomingAdapter extends RecyclerView.Adapter<FriendIncomingAdapter.SimpleRow> {
    private ArrayList<NBFriendItem> mDataset = new ArrayList<>();
    private FriendRequestReceivedFragment fragment;

    //Constructor: populates a local variable using data passed in
    public FriendIncomingAdapter(FriendRequestReceivedFragment fragment) {
        this.fragment = fragment;
    }

    //Called by LayoutManager each time it needs to create a new row
    @Override
    public SimpleRow onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_incoming_row_layout, parent, false);

        final SimpleRow row = new SimpleRow(v);

        row.buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.accept(row.getPosition());
            }
        });

        row.buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.decline(row.getPosition());
            }
        });

        return row;
    }

    //Replaces the contents of a view at each position
    public void onBindViewHolder(final SimpleRow simpleRow, int position) {
        NBFriendItem tFriend = mDataset.get(position);

        simpleRow.itemName.setText(tFriend.friendName);
        simpleRow.itemDetails.setText(tFriend.friendDescription);

        UrlImageViewHelper.setUrlDrawable(simpleRow.itemImage, tFriend.friendThumbnail, R.drawable.default_profile_image);

        simpleRow.simpleRowLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Animate background color
                ColorDrawable bg = (ColorDrawable) simpleRow.simpleRowLayout.getBackground();
                int colorFrom = bg.getColor();
                int colorTo =  Color.rgb(230, 230, 230);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                colorAnimation.setDuration(800);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        simpleRow.simpleRowLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorAnimation.start();
            }
        });
    }

    //Size of data set is needed by LayoutManager
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class SimpleRow extends RecyclerView.ViewHolder {
        RelativeLayout simpleRowLayout;
        ImageView itemImage;
        TextView itemName;
        TextView itemDetails;

        Button buttonAccept;
        Button buttonDecline;

        public SimpleRow(View itemView) {
            super(itemView);
            simpleRowLayout = (RelativeLayout) itemView.findViewById(R.id.simpleRowLayout);
            itemName = (TextView) itemView.findViewById(R.id.item_title);
            itemDetails = (TextView) itemView.findViewById(R.id.itemDetails);
            itemImage = (ImageView) itemView.findViewById(R.id.item_thumbnail);
            buttonAccept = (Button) itemView.findViewById(R.id.buttonAccept);
            buttonDecline = (Button) itemView.findViewById(R.id.buttonDecline);
        }
    }

    public ArrayList<NBFriendItem> getFriendArrayListFromJSONArray(JSONArray pFriends) {
        ArrayList<NBFriendItem> tFriendList = new ArrayList<>();

        if (pFriends != null) {
            for (int i=0; i < pFriends.length(); i++) {

                try {
                    tFriendList.add(new NBFriendItem(pFriends.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return tFriendList;
    }

    public void clearData()
    {
        mDataset.clear();
    }

    public void appendData(ArrayList<NBFriendItem> pData)
    {
        mDataset.addAll(pData);
    }

    public NBFriendItem getFriendItemAt(int position)
    {
        return mDataset.get(position);
    }
}