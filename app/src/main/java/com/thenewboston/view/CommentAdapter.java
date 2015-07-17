package com.thenewboston.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.NBBaseActivity;
import com.thenewboston.R;
import com.thenewboston.view.items.NBCommentItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentItemHolder> implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private String nbUserID;
    private String nbUserApiToken;

    private ArrayList<NBCommentItem> mDataset = new ArrayList<>();
    private NBBaseActivity mParentActivity;
    private Context mContext;
    private GestureDetector gestureDetector;

    //Constructor: populates a local variable using data passed in
    public CommentAdapter(Context context, String pUserId, String pUserApiToken) {
        this.mContext = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());

        mParentActivity = (NBBaseActivity)context;

        this.nbUserID = pUserId;
        this.nbUserApiToken = pUserApiToken;
    }

    //Called by LayoutManager each time it needs to create a new row
    @Override
    public CommentItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentItemHolder(v);
    }

    //Replaces the contents of a view at each position
    public void onBindViewHolder(final CommentItemHolder pCommentItemHolder, int position) {
        NBCommentItem tComment = mDataset.get(position);

        pCommentItemHolder.commenterName.setText(tComment.commenterName);

        pCommentItemHolder.commentedDate.setText(tComment.commentedDate);


        UrlImageViewHelper.setUrlDrawable(pCommentItemHolder.commenterThumbnail, tComment.commenterThumbnail, R.drawable.default_profile_image);

        pCommentItemHolder.commentContent.setText(tComment.commentContent);
        if (TextUtils.isEmpty(tComment.commentContent)) {
            pCommentItemHolder.commentContent.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(tComment.commentImage)) {
            UrlImageViewHelper.setUrlDrawable(pCommentItemHolder.commentImage, tComment.commentImage);
        } else {
            pCommentItemHolder.commentImage.setVisibility(View.GONE);
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

    //Class to represent CommentItemHolder
    public static class CommentItemHolder extends RecyclerView.ViewHolder {
        TextView commenterName;
        TextView commentedDate;
        TextView commentContent;
        ImageView commenterThumbnail;
        ImageView commentImage;

        public CommentItemHolder(View itemView) {
            super(itemView);

            commenterName = (TextView) itemView.findViewById(R.id.commenter_name);
            commentedDate = (TextView) itemView.findViewById(R.id.commented_date);
            commentContent = (TextView) itemView.findViewById(R.id.comment_content);
            commenterThumbnail = (ImageView) itemView.findViewById(R.id.commenter_thumbnail);
            commentImage = (ImageView) itemView.findViewById(R.id.comment_image);
        }
    }

    public void clearData()
    {
        mDataset.clear();
    }
    public void appendData(ArrayList<NBCommentItem> pData)
    {
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

    public void addNewComment(NBCommentItem pComment)
    {
        mDataset.add(0, pComment);
    }

    public ArrayList<NBCommentItem> getCommentArrayListFromJSONArray(JSONArray pArticles) {
        ArrayList<NBCommentItem> tCommentList = new ArrayList<>();

        if (pArticles != null) {
            for (int i=0; i < pArticles.length(); i++) {

                try {
                    tCommentList.add(new NBCommentItem(pArticles.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return tCommentList;
    }

}