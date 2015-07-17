package com.thenewboston.restclient;

import android.os.AsyncTask;


public class NBRestAPIManager extends  AsyncTask<NBRestAPIListener, Integer, Boolean> {
    //Api Token: Public Token OR Private Token
    private String apiToken;

    //HTTP Request: get, post, delete, put
    private String method;

    //Type: Account, Video....
    private String type;

    //Account Action: login, register, etc
    private String action;

    private NBRestHttpClient restClient = new NBRestHttpClient();

    private NBRestAPIListener listener;

    /**
     * Constructor
     *
     * @param pType: API type
     * @param pAction: Action
     * @param pApiToken: Token
     * @param pMethod: Request Method
     */
    public NBRestAPIManager(String pType, String pAction, String pApiToken, String pMethod) {
        type = pType;
        action = pAction;
        apiToken = pApiToken;
        method = pMethod;
    }

    /**
     * Clean request data
     */
    public void cleanFields() {
        restClient.cleanFields();
    }

    /**
     * Add request parameters
     *
     * @param pKey
     * @param pValue
     */
    public void addField(String pKey, String pValue) {
        restClient.addField(pKey, pValue);
    }


    @Override
    protected  void onPreExecute() {
        
    }

    @Override
    protected Boolean doInBackground(NBRestAPIListener... params) {
        listener = params[0];

        restClient.addField("TOKEN",   apiToken);
        restClient.addField("TYPE",    type);
        restClient.addField("ACTION",  action);

        return restClient.execute(method);
    }

    @Override
    protected void onPostExecute(Boolean pSuccess) {

        if (!pSuccess) {
            listener.onFailure(restClient.getErrorMessage(), restClient.getStatusCode());
        } else {
            listener.onSuccess(restClient.getResponseData(), restClient.getStatusCode());
        }
    }


}
