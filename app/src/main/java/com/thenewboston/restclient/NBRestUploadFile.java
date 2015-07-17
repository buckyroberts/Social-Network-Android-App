package com.thenewboston.restclient;


import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NBRestUploadFile extends AsyncTask<NBRestAPIListener, Integer, Boolean> {
    public static final String API_URL = "http://192.168.0.6/buckysroom/api/v1/api.php";
//    public static final String API_URL = "http://192.168.0.84:8888/thenewboston/api/v1/api.php";

    public static final int STATUS_OK = 200;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_CODE_INVALID_METHOD = 405;
    public static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

    private String apiType;

    private String apiAction;

    //Api Token: Public Token OR Private Token
    private String apiToken;

    private String filePath = null;

    private List<NameValuePair> fields = new ArrayList<NameValuePair>();

    private Boolean isSecure;

    private HttpURLConnection con;

    private OutputStream outputStream;

    private String boundary;

    private String newline = "\r\n";

    private String delimiter = "--";

    private String response;

    private int statusCode;

    private JSONObject responseData;

    private String errorMessage;

    int maxBufferSize = 1 * 1024 * 1024;

    private NBRestAPIListener listener;

    public NBRestUploadFile(String pType, String pAction, String pToken) {
        apiType = pType;
        apiAction = pAction;
        apiToken = pToken;
    }

    public void setFile(String pFile) {
        filePath = pFile;
    }

    public void addField(String pKey, String pValue) {
        fields.add(new BasicNameValuePair(pKey, pValue));
    }

    @Override
    protected Boolean doInBackground(NBRestAPIListener... params) {
        listener = params[0];

        try{
            URL tUrl = new URL(NBRestUploadFile.API_URL);

            con = (HttpURLConnection)tUrl.openConnection();
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);

            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = con.getOutputStream();

            addParam("TYPE", apiType);
            addParam("ACTION", apiAction);
            addParam("TOKEN", apiToken);

            for (NameValuePair tField : fields) {
                addParam(tField.getName(), tField.getValue());
            }

            if (filePath != null)
                addFile();

            outputStream.flush();
            outputStream.close();

            statusCode = con.getResponseCode();

            if (statusCode == STATUS_OK) {

                InputStream tInputStream = con.getInputStream();
                response = readStream(tInputStream);
                System.out.println(response);
                responseData = new JSONObject(response);
                return true;
            } else {
                errorMessage = "Error: " + con.getResponseMessage();
                return false;
            }

        }catch(Exception e) {
            errorMessage = e.getMessage();
        }

        return false;
    }

    private void addParam(String pName, String pValue) throws IOException {
        outputStream.write((delimiter + boundary + newline).getBytes());
        outputStream.write(("Content-Type: text/plain" + newline).getBytes());
        outputStream.write(("Content-Disposition: form-data; name=\"" + pName + "\"" + newline).getBytes());
        outputStream.write((newline + pValue + newline).getBytes());
    }

    private void addFile() throws IOException {
        MimeTypeMap tTypeMap = MimeTypeMap.getSingleton();
        String tExtension = MimeTypeMap.getFileExtensionFromUrl(filePath).toLowerCase();
        File tFile = new File(filePath);
        String tType = tTypeMap.getMimeTypeFromExtension(tExtension);

        outputStream.write((delimiter + boundary + newline).getBytes());

        outputStream.write(("Content-Disposition: form-data; name=\"image\"; filename=\"" + tFile.getName() + "\"" + newline).getBytes());
        outputStream.write(("Content-Type: " + tType + newline).getBytes());

        outputStream.write(("Content-Length: " + tFile.length() + newline).getBytes());
        outputStream.write((newline).getBytes());

        FileInputStream tFileInputStream = new FileInputStream(new File(filePath));

        int tBytesAvailable = tFileInputStream.available();
        int tBufferSize = Math.min(tBytesAvailable, maxBufferSize);

        byte[] tBuffer = new byte[tBufferSize];

        int tBytesRead = tFileInputStream.read(tBuffer, 0, tBufferSize);

        while(tBytesRead > 0) {
            outputStream.write(tBuffer, 0, tBufferSize);
            tBytesAvailable = tFileInputStream.available();
            tBufferSize = Math.min(tBytesAvailable, maxBufferSize);
            tBytesRead = tFileInputStream.read(tBuffer, 0, tBufferSize);
        }

        outputStream.write(newline.getBytes());
        outputStream.write((delimiter + boundary + newline).getBytes());
        tFileInputStream.close();
    }

    @Override
    protected void onPostExecute(Boolean pSuccess) {

        if (!pSuccess) {
            listener.onFailure(errorMessage, statusCode);
        } else {
            listener.onSuccess(responseData, statusCode);
        }
    }

    @Override
    protected  void onPreExecute() {

    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }


}
