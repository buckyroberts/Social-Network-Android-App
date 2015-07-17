package com.thenewboston.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.CommentActivity;
import com.thenewboston.LoginActivity;
import com.thenewboston.MessagesComposeActivity;
import com.thenewboston.NBBaseActivity;
import com.thenewboston.NBYoutubeVideoPlayActivity;
import com.thenewboston.ProfileActivity;
import com.thenewboston.R;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.view.items.NBArticleItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private String nbUserID;
    private String nbUserApiToken;
    private ArrayList<NBArticleItem> mDataset = new ArrayList<>();
    private NBBaseActivity mParentActivity;
    private Context mContext;
    private GestureDetector gestureDetector;
    private String mAdapterType;
    private JSONObject mProfileInfo;
    private String mProfileId;

    //Constructor for Stream
    public ArticleAdapter(Context context, String pUserId, String pUserApiToken, String pType) {
        this.mContext = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.mParentActivity = (NBBaseActivity) context;
        this.nbUserID = pUserId;
        this.nbUserApiToken = pUserApiToken;
        this.mAdapterType = pType;
    }

    //Constructor for Profile
    public ArticleAdapter(Context context, String pUserId, String pUserApiToken, String pType, JSONObject pProfileInfo, String pProfileId) {
        this.mContext = context;
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.mParentActivity = (NBBaseActivity) context;
        this.nbUserID = pUserId;
        this.nbUserApiToken = pUserApiToken;
        this.mAdapterType = pType;
        this.mProfileInfo = pProfileInfo;
        this.mProfileId = pProfileId;
    }

    //Size of data set is needed by LayoutManager
    @Override
    public int getItemCount() {
        if (mAdapterType.equals("Profile"))
            return mDataset.size() + 1;
        else
            return mDataset.size();
    }

    //Used to determine if a view can be recycled or not
    @Override
    public int getItemViewType(int position) {
        if (mAdapterType.equals("Profile") && position == 0)
            return 0;
        else
            return 1;
    }

    //Called each time it needs to create a new row
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_header_item, parent, false);
            return new ProfileHeaderItemHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_item, parent, false);
            return new ArticleItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mAdapterType.equals("Profile")) {
            if (position == 0)
                setProfileData((ProfileHeaderItemHolder) holder);
            else
                setArticleData((ArticleItemHolder) holder, position - 1);
        } else {
            setArticleData((ArticleItemHolder) holder, position);
        }
    }

    //Set profile data
    public void setProfileData(final ProfileHeaderItemHolder pProfileHeaderItemHolder) {
        try {
            UrlImageViewHelper.setUrlDrawable(pProfileHeaderItemHolder.profileThumbnail, mProfileInfo.getString("thumbnail"));
            pProfileHeaderItemHolder.profileName.setText(mProfileInfo.getString("name"));
            pProfileHeaderItemHolder.profilePoints.setText("Points: " + mProfileInfo.getString("reputation"));

            //If not friends, display friend request button instead of message
            if (mProfileId.equals(nbUserID)) {
                pProfileHeaderItemHolder.profileEdit.setVisibility(View.VISIBLE);
                pProfileHeaderItemHolder.buttonSendMessage.setVisibility(View.GONE);
            } else {
                pProfileHeaderItemHolder.profileEdit.setVisibility(View.GONE);
                pProfileHeaderItemHolder.buttonSendMessage.setVisibility(View.VISIBLE);
                pProfileHeaderItemHolder.buttonSendMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, MessagesComposeActivity.class);

                        try {
                            i.putExtra("friendId", mProfileId);
                            i.putExtra("friendName", mProfileInfo.getString("name"));
                            i.putExtra("friendThumbnail", mProfileInfo.getString("thumbnail"));
                        } catch (JSONException e) {

                        }

                        mContext.startActivity(i);
                    }
                });
            }

            pProfileHeaderItemHolder.profileEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                ((ProfileActivity)mContext).selectPhoto();
                }
            });

            if (mProfileInfo.getString("can_post").equals("yes") && !mProfileId.equals(nbUserID)) {
                pProfileHeaderItemHolder.buttonCreatePost.setVisibility(View.VISIBLE);
                pProfileHeaderItemHolder.buttonCreatePost.setText("Write " + mProfileInfo.getString("name") + " a Post");
                pProfileHeaderItemHolder.buttonCreatePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                   Toast.makeText(mContext, "Create post activity", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                pProfileHeaderItemHolder.buttonCreatePost.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Set article data
    public void setArticleData(final ArticleItemHolder pArticleItemHolder, int position) {
        NBArticleItem tArticle = mDataset.get(position);
        pArticleItemHolder.posterName.setText(tArticle.posterName);
        pArticleItemHolder.postedDate.setText(tArticle.postedDate);
        UrlImageViewHelper.setUrlDrawable(pArticleItemHolder.posterThumbnail, tArticle.posterThumbnail, R.drawable.default_profile_image);

        if (tArticle.articleText.length() == 0) {
            pArticleItemHolder.articleText.setVisibility(View.GONE);
        } else {
            pArticleItemHolder.articleText.setVisibility(View.VISIBLE);
            pArticleItemHolder.articleText.setText(tArticle.articleText);
        }

        if (tArticle.articleImage.length() == 0) {
            pArticleItemHolder.articleImage.setVisibility(View.GONE);
        } else {
            pArticleItemHolder.articleImage.setVisibility(View.VISIBLE);
            UrlImageViewHelper.setUrlDrawable(pArticleItemHolder.articleImage, tArticle.articleImage);
        }

        if (tArticle.articleVideo.length() != 0) {
            pArticleItemHolder.videoLayout.setVisibility(View.VISIBLE);
            pArticleItemHolder.articleVideoImage.setVisibility(View.VISIBLE);
            pArticleItemHolder.articleVideoPlayImage.setVisibility(View.VISIBLE);
            UrlImageViewHelper.setUrlDrawable(pArticleItemHolder.articleVideoImage, "http://img.youtube.com/vi/" + tArticle.articleVideoId + "/mqdefault.jpg");
            System.out.println("http://img.youtube.com/vi/" + tArticle.articleVideoId + "/mqdefault.jpg");
            Bundle tVideoId = new Bundle();
            tVideoId.putString("video_id", tArticle.articleVideoId);
            pArticleItemHolder.articleVideoImage.setTag(tVideoId);

            pArticleItemHolder.articleVideoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle tVideoId = (Bundle) v.getTag();

                    Intent tVideoIntent = new Intent(v.getContext(), NBYoutubeVideoPlayActivity.class);
                    tVideoIntent.putExtra("video_id", tVideoId.getString("video_id"));
                    Activity tActivity = (Activity) mContext;
                    tActivity.startActivityForResult(tVideoIntent, 2);
                }
            });
        } else {
            pArticleItemHolder.videoLayout.setVisibility(View.GONE);
            pArticleItemHolder.articleVideoImage.setVisibility(View.GONE);
            pArticleItemHolder.articleVideoPlayImage.setVisibility(View.GONE);
        }

        pArticleItemHolder.articleLikes.setText(tArticle.articleLikes + " like" + (tArticle.articleLikes > 1 ? "s" : ""));
        pArticleItemHolder.articleComments.setText(tArticle.articleComments + " comment" + (tArticle.articleComments > 1 ? "s" : ""));

        if (tArticle.posterId.equals(nbUserID)) {
            pArticleItemHolder.buttonLike.setVisibility(View.GONE);
        } else {
            pArticleItemHolder.buttonLike.setVisibility(View.VISIBLE);

            if (tArticle.isLiked.equals("yes")) {
                pArticleItemHolder.buttonLike.setText("Unlike");
            } else {
                pArticleItemHolder.buttonLike.setText("Like");
            }

            Bundle tPostId = new Bundle();
            tPostId.putString("article_id", tArticle.articleId);
            pArticleItemHolder.buttonLike.setTag(tPostId);

            pArticleItemHolder.buttonLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pArticleItemHolder.likeProgress.setVisibility(View.VISIBLE);
                    Bundle tArticleId = (Bundle) v.getTag();

                    pArticleItemHolder.buttonLike.setEnabled(false);
                    //Like Comments
                    String tAction = pArticleItemHolder.buttonLike.getText().toString();

                    if (tAction.equals("Like")) {
                        tAction = "likePost";
                    } else {
                        tAction = "unlikePost";
                    }

                    //Call Rest API
                    NBRestAPIManager apiManager = new NBRestAPIManager("post", "likePost", nbUserApiToken, "post");

                    apiManager.addField("actionType", tAction);
                    apiManager.addField("postID", tArticleId.getString("article_id"));

                    NBRestAPIListener tRestApiListener = new NBRestAPIListener() {
                        @Override
                        public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                            pArticleItemHolder.likeProgress.setVisibility(View.GONE);

                            pArticleItemHolder.buttonLike.setEnabled(true);

                            try {
                                if (pResponseData.getString("STATUS").equals("ERROR")) {
                                    mParentActivity.showPopupMessage(pResponseData.getString("ERROR"));
                                } else {

                                    if (pResponseData.getString("isLiked").equals("yes")) {
                                        pArticleItemHolder.buttonLike.setText("Unlike");
                                    } else {
                                        pArticleItemHolder.buttonLike.setText("Like");
                                    }

                                    pArticleItemHolder.articleLikes.setText(pResponseData.getInt("LIKES") + " like" + (pResponseData.getInt("LIKES") > 1 ? "s" : ""));

                                }
                            } catch (JSONException e) {
                                mParentActivity.showPopupMessage(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String pError, int pStatusCode) {
                            if (pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                                mParentActivity.clearNBUserInfo();
                                mParentActivity.gotoOtherActivity(LoginActivity.class, pError, true);
                            } else {
                                pArticleItemHolder.likeProgress.setVisibility(View.GONE);
                                pArticleItemHolder.buttonLike.setEnabled(true);
                                mParentActivity.showPopupMessage(pError);
                            }
                        }
                    };

                    apiManager.execute(tRestApiListener);
                }
            });
        }

        //Go to profile page
        Bundle tBundlePosterId = new Bundle();
        tBundlePosterId.putString("profileId", tArticle.posterId);
        pArticleItemHolder.articleLayout.setTag(tBundlePosterId);
        pArticleItemHolder.articleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle tData = (Bundle) v.getTag();
                Intent tIntent = new Intent(mContext, ProfileActivity.class);
                tIntent.putExtra("profileId", tData.getString("profileId"));
                mContext.startActivity(tIntent);
            }
        });

        Bundle tPostId = new Bundle();
        tPostId.putString("article_id", tArticle.articleId);
        pArticleItemHolder.buttonComment.setTag(tPostId);

        pArticleItemHolder.buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tIntent = new Intent(mContext, CommentActivity.class);
                Bundle tData = (Bundle) v.getTag();
                tIntent.putExtra("post_id", tData.getString("article_id"));
                mParentActivity.startActivityForResult(tIntent, 3);
            }
        });

        final String printMe = String.valueOf(position);

        //+1 on double taps (return false so the touch event isn't consumed)
        pArticleItemHolder.articleLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                boolean isDoubleTap = gestureDetector.onTouchEvent(event);
                if (isDoubleTap) {
                    Toast.makeText(mContext, printMe, Toast.LENGTH_SHORT).show();
                    //Animate background color
                    ColorDrawable bg = (ColorDrawable) pArticleItemHolder.articleLayout.getBackground();
                    int colorFrom = bg.getColor();
                    int colorTo = Color.rgb(230, 230, 245);
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                    colorAnimation.setDuration(800);
                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            pArticleItemHolder.articleLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation.start();
                }
                return false;
            }

        });

        //Display dialog for onLongClicks
        pArticleItemHolder.articleLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_article);
                RelativeLayout delete = (RelativeLayout) dialog.findViewById(R.id.delete);
                RelativeLayout edit = (RelativeLayout) dialog.findViewById(R.id.edit);
                RelativeLayout report = (RelativeLayout) dialog.findViewById(R.id.report);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "Delete", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "Edit", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                report.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "Report", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });

    }

    public void appendArticles(ArrayList<NBArticleItem> pData) {
        mDataset.addAll(pData);
    }

    public String getLastArticleDate() {
        if (mAdapterType.equals("Profile")) {
            return getItemCount() > 0 ? mDataset.get(getItemCount() - 2).purePostedDate : null;
        } else {
            return getItemCount() > 0 ? mDataset.get(getItemCount() - 1).purePostedDate : null;
        }

    }

    public ArrayList<NBArticleItem> getArticleArrayListFromJSONArray(JSONArray pArticles) {
        ArrayList<NBArticleItem> tArticleList = new ArrayList<>();

        if (pArticles != null) {
            for (int i = 0; i < pArticles.length(); i++) {

                try {
                    tArticleList.add(new NBArticleItem(pArticles.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        return tArticleList;
    }

    //Profile header class
    public static class ProfileHeaderItemHolder extends RecyclerView.ViewHolder {
        ImageView profileThumbnail;
        ImageView profileEdit;
        TextView profileName;
        TextView profilePoints;
        Button buttonSendMessage;
        Button buttonCreatePost;

        public ProfileHeaderItemHolder(View itemView) {
            super(itemView);

            profileThumbnail = (ImageView) itemView.findViewById(R.id.profileThumbnail);
            profileEdit = (ImageView) itemView.findViewById(R.id.profileEdit);
            profileName = (TextView) itemView.findViewById(R.id.profileName);
            profilePoints = (TextView) itemView.findViewById(R.id.profilePoints);
            buttonSendMessage = (Button) itemView.findViewById(R.id.buttonSendMessage);
            buttonCreatePost = (Button) itemView.findViewById(R.id.buttonCreatePost);
        }
    }

    //Article item class
    public static class ArticleItemHolder extends RecyclerView.ViewHolder {

        RelativeLayout articleLayout;
        RelativeLayout videoLayout;
        TextView posterName;
        TextView postedDate;
        TextView articleText;
        ImageView posterThumbnail;
        ImageView articleImage;
        TextView articleLikes;
        TextView articleComments;
        ImageView articleVideoImage;
        ImageView articleVideoPlayImage;
        Button buttonLike;
        Button buttonComment;
        ProgressBar likeProgress;

        public ArticleItemHolder(View itemView) {
            super(itemView);
            articleLayout = (RelativeLayout) itemView.findViewById(R.id.articleLayout);
            videoLayout = (RelativeLayout) itemView.findViewById(R.id.videoLayout);
            posterName = (TextView) itemView.findViewById(R.id.poster_name);
            postedDate = (TextView) itemView.findViewById(R.id.posted_date);
            articleText = (TextView) itemView.findViewById(R.id.article_text);
            posterThumbnail = (ImageView) itemView.findViewById(R.id.poster_thumbnail);
            articleImage = (ImageView) itemView.findViewById(R.id.article_image);
            articleLikes = (TextView) itemView.findViewById(R.id.article_likes);
            articleComments = (TextView) itemView.findViewById(R.id.article_comments);
            articleVideoImage = (ImageView) itemView.findViewById(R.id.article_video_image);
            articleVideoPlayImage = (ImageView) itemView.findViewById(R.id.article_video_play_image);

            buttonLike = (Button) itemView.findViewById(R.id.buttonLike);
            buttonComment = (Button) itemView.findViewById(R.id.buttonComment);
            likeProgress = (ProgressBar) itemView.findViewById(R.id.likeProgress);
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
}