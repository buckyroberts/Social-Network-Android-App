package com.thenewboston.view.items;

import org.json.JSONException;
import org.json.JSONObject;

public class NBFriendItem {
    public String friendID;

    public String friendName;

    public String friendDescription;

    public String friendThumbnail;

    public String friendType;

    public NBFriendItem(JSONObject pData)
    {
        try {
            friendID = pData.getString("id");
            friendName = pData.getString("name");
            friendThumbnail = pData.getString("thumbnail");
            friendDescription = pData.getString("description");
            friendType = pData.getString("friendType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
