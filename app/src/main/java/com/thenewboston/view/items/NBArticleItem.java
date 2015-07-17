package com.thenewboston.view.items;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class NBArticleItem {
    public String articleId;

    public String posterId;

    public String posterName;

//    public String posterEmail;

    public String postedDate;

    public String purePostedDate;

    public String posterThumbnail;

    public String articleText = null;

    public String articleImage = null;

    public String articleVideo = null;

    public String articleVideoId = null;

    public int articleLikes = 0;

    public int articleComments = 0;

    public String isLiked;


    public NBArticleItem(JSONObject pData){
        try {
            articleId = pData.getString("articleId");
            posterName = pData.getString("posterName");
            posterId = pData.getString("posterId");

            postedDate = pData.getString("postedDate");
            purePostedDate = pData.getString("purePostedDate");

            posterThumbnail = pData.getString("posterThumbnail");
            articleText = pData.getString("articleContent");
            articleLikes = pData.getInt("articleLikes");
            articleComments = pData.getInt("articleComments");

            //Need to initialize String (even if empty) so that we can test length
            articleImage = pData.getString("articleImage");
            articleVideo = pData.getString("articleVideo");
            articleVideoId = pData.getString("articleVideoId");

            /*
            if (!TextUtils.isEmpty(pData.getString("articleImage"))) {
                articleImage = pData.getString("articleImage");
            }
            if (!TextUtils.isEmpty(pData.getString("articleVideo"))) {
                articleVideo = pData.getString("articleVideo");
                articleVideoId = pData.getString("articleVideoId");
            }
            */

            isLiked = pData.getString("isLiked");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
