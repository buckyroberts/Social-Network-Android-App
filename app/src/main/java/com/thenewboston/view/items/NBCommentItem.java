package com.thenewboston.view.items;


import org.json.JSONObject;

public class NBCommentItem {
    public String postId;

    public String commentId;

    public String commenterId;
    public String commenterName;
    public String commenterThumbnail;

    public String commentedDate;
    public String commentContent;
    public String commentImage;

    public NBCommentItem(JSONObject pData) {
        try{
            postId = pData.getString("postId");

            commentId = pData.getString("commentId");

            commenterId = pData.getString("commenterId");
            commenterName = pData.getString("commenterName");
            commenterThumbnail = pData.getString("commenterThumbnail");

            commentedDate = pData.getString("commentedDate");
            commentContent = pData.getString("commentContent");
            commentImage = pData.getString("commentImage");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
