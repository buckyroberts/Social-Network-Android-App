package com.thenewboston.restclient;


import org.json.JSONObject;

public interface NBRestAPIListener {

    public abstract void onSuccess(JSONObject pResponseData, int pStatusCode);

    public abstract void onFailure(String pError, int pStatusCode);

}