package com.thenewboston.restclient;


import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NBRestHttpClient {
    public static final String API_URL = "https://www.thenewboston.com/api/v1/api.php";
//    public static final String API_URL = "http://192.168.0.84:8888/thenewboston/api/v1/api.php";

    public static final String PUBLIC_API_TOKEN = "thenewboston-api-key-201519861216";

    public static final int STATUS_OK = 200;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_CODE_INVALID_METHOD = 405;
    public static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

    protected String method = "";

    protected HttpClient client = new DefaultHttpClient();

    private HttpResponse response;

    private List<NameValuePair> fields = new ArrayList<NameValuePair>();

    /*** Response ***/
    private String errorMessage = "";

    private JSONObject responseData;

    private int statusCode = STATUS_OK;

    /****
     * Add new data
     * @param pKey
     * @param pValue
     */
    public void addField(String pKey, String pValue) {
        fields.add(new BasicNameValuePair(pKey, pValue));
        return;
    }

    /***
     * Clean data
     */
    public void cleanFields() {
        fields.clear();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public JSONObject getResponseData() {
        return responseData;
    }


    private Boolean parseResponse(HttpResponse pResponse) {
        try {
            statusCode = pResponse.getStatusLine().getStatusCode();
            String tResponseString = EntityUtils.toString(pResponse.getEntity());
            System.out.println(tResponseString);
            responseData = new JSONObject(tResponseString);

            if (statusCode == NBRestHttpClient.STATUS_OK) {
                return true;
            } else {
                errorMessage = "Error " + Integer.toString(statusCode) + "(" + pResponse.getStatusLine().getReasonPhrase() + "): " + responseData.getString("ERROR");
                return false;
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();

            return false;
        }
    }

    /**
     * Convert List<NameValuePair> to query string
     *
     * @return String
     */
    private String getParamsQueryString() {
        String tQueryString = "";

        for (NameValuePair tField : fields) {
            if (!TextUtils.isEmpty(tQueryString)) {
                tQueryString += "&";
            }

            try {
                tQueryString += URLEncoder.encode(tField.getName(), "UTF-8")
                        + "="
                        + URLEncoder.encode(tField.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return tQueryString;
    }


    public Boolean get ()  {
        method = "get";

        String tApiURL = API_URL +  "?" + getParamsQueryString();

        HttpGet getMethod = new HttpGet(tApiURL);

        try {
            response = client.execute(getMethod);
            return parseResponse(response);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        }
    }

    public Boolean delete ()  {
        method = "delete";

        String tApiURL = API_URL + "?" + getParamsQueryString();

        HttpDelete deleteMethod = new HttpDelete(tApiURL);

        try {
            response = client.execute(deleteMethod);
            return parseResponse(response);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        }
    }

    public Boolean post ()  {
        method = "post";

        String tApiURL = API_URL;

        HttpPost postMethod = new HttpPost(tApiURL);

        try {
            postMethod.setEntity(new UrlEncodedFormEntity(fields));

            response = client.execute(postMethod);

            return parseResponse(response);
        } catch (Exception e) {
            errorMessage = e.getMessage
                    ();
            return false;
        }
    }


    public Boolean put ()  {
        method = "put";

        String tApiURL = API_URL;

        tApiURL += "?" + getParamsQueryString();

        HttpPut putMethod = new HttpPut(tApiURL);

        try {
            response = client.execute(putMethod);

            return parseResponse(response);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        }
    }

    public Boolean execute(String pMethod) {
        switch (pMethod.toLowerCase()) {
            case "get":
                return this.get();
            case "post":
                return this.post();
            case "delete":
                return this.delete();
            case "put":
                return this.put();
        }

        errorMessage = "Invalid Method";

        return false;
    }


}
