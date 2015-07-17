package com.thenewboston.view.items;

import org.json.JSONException;
import org.json.JSONObject;

public class NBSearchItem {
    public String itemID;

    public String itemType;

    public String itemTitle;

    public String itemImage;

    public String itemDescription;

    public String isFriend = "";

    public String isFollowed = "";

    public NBSearchItem(JSONObject pData) {
        try{
            itemID = pData.getString("id");
            itemType = pData.getString("type");
            itemTitle = pData.getString("title");
            itemDescription = pData.getString("description");
            itemImage = pData.getString("image");
            isFriend = pData.getString("isFriend");
            isFollowed = pData.getString("isFollowed");

            if (itemType.equals("user")) {
                isFriend = pData.getString("isFriend");
            } else if (itemType.equals("page")) {
                isFollowed = pData.getString("isFollowed");
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
