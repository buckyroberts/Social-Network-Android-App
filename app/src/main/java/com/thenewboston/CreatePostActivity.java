package com.thenewboston;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.restclient.NBRestUploadFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class CreatePostActivity extends NBBaseActivity {
    final int USE_CAMERA = 91;
    final int SELECT_FILE = 90;

    private String imageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = "create_post";
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_post);

        setNBToolbar();
        addNBNavigationMenu();

        initPostVisibilitySelectbox();

        //Getting Extra
        Bundle tParam = getIntent().getExtras();
        if (tParam.getBoolean("selectPhoto")) {
            showSelectPhotoDialog();
        }

    }

    private void initPostVisibilitySelectbox() {
        Spinner tPostVisibility = (Spinner) findViewById(R.id.post_visibility);

        ArrayAdapter tPostVisibilityAdapter = new ArrayAdapter(this, R.layout.selectbox, getResources().getStringArray(R.array.post_visibilities));
        tPostVisibilityAdapter.setDropDownViewResource(R.layout.selectbox_item);
        tPostVisibility.setAdapter(tPostVisibilityAdapter);
    }

    public void selectPhoto(View v) {
        showSelectPhotoDialog();
    }

    private void showSelectPhotoDialog() {
        //Show Select photo dialog
        final CharSequence[] tItems = {"Take Photo", "Choose from Library", "Cancel"};


        final AlertDialog.Builder tDialogBuilder = new AlertDialog.Builder(this);
        tDialogBuilder.setTitle("Add Photo");
        tDialogBuilder.setItems(tItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tItems[which].equals("Take Photo")) {
                    Intent tIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "bn_temp.jpg");
                    tIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(tIntent, USE_CAMERA);
                } else if (tItems[which].equals("Choose from Library")) {
                    Intent tIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    tIntent.setType("image/*");
                    startActivityForResult(Intent.createChooser(tIntent, "Select File"), SELECT_FILE);
                } else if (tItems[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        tDialogBuilder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            ImageView tPreviewPhoto = (ImageView)findViewById(R.id.previewPhoto);

            if (requestCode == USE_CAMERA) {
                File tFile = new File(Environment.getExternalStorageDirectory().toString());
                for(File temp: tFile.listFiles()) {
                    if (temp.getName().equals("bn_temp.jpg")) {
                        tFile = temp;
                        break;
                    }
                }

                try {
                    Bitmap tBM;
                    BitmapFactory.Options tBMOptions = new BitmapFactory.Options();

                    tBM = BitmapFactory.decodeFile(tFile.getAbsolutePath(), tBMOptions);

                    tPreviewPhoto.setImageBitmap(tBM);

                    String tPath = Environment.getExternalStorageDirectory().toString() + File.separator + "NB" + File.separator + "default";

                    File tDir = new File(tPath);
                    tDir.mkdirs();

                    tFile.delete();

                    OutputStream tOut = null;
                    File tNFile = new File(tPath, String.valueOf(System.currentTimeMillis()) + ".jpg");

                    imageFile = tNFile.getPath();

                    try {
                        tOut = new FileOutputStream(tNFile);
                        tBM.compress(Bitmap.CompressFormat.JPEG, 85, tOut);
                        tOut.flush();
                        tOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE) {
                Uri tSelectedImageUri = data.getData();

                imageFile = getPath(tSelectedImageUri, this);
                Bitmap tBM;
                BitmapFactory.Options tBMOptions = new BitmapFactory.Options();

                tBM = BitmapFactory.decodeFile(imageFile, tBMOptions);

                tPreviewPhoto.setImageBitmap(tBM);
            }
            RelativeLayout tPreviewLayout = (RelativeLayout)findViewById(R.id.photo_preview_layout);
            tPreviewLayout.setVisibility(View.VISIBLE);

        }
    }

    public String getPath(Uri uri, Activity activity) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void removePhoto(View view) {
        if (imageFile != null) {
            imageFile = null;

            RelativeLayout tPreviewLayout = (RelativeLayout)findViewById(R.id.photo_preview_layout);
            tPreviewLayout.setVisibility(View.GONE);

        }
    }

    public void createNewPost(View view) {
        EditText tPostContent = (EditText)findViewById(R.id.article_text);

        String tPostContentText = tPostContent.getText().toString().trim();

        Spinner tPostVisibility = (Spinner)findViewById(R.id.post_visibility);
        String tPostVisibilityText = tPostVisibility.getSelectedItem().toString();

        if (TextUtils.isEmpty(tPostContentText) && imageFile == null) {
            showPopupMessage("Please write something hoss");

            return;
        } else {
            if (imageFile == null) {
                NBRestAPIManager apiManager = new NBRestAPIManager("post", "create", nbUserApiToken, "post");

                apiManager.addField("content", tPostContentText);
                apiManager.addField("post_visibility", tPostVisibilityText);

                apiManager.addField("post_type", "text");

                showOverlay();

                NBRestAPIListener streamListener = new NBRestAPIListener() {
                    @Override
                    public void onSuccess(JSONObject pResponseData, int pStatusCode) {

                        hideOverlay();

                        try {
                            if (pResponseData.getString("STATUS").equals("ERROR")) {
                                showPopupMessage("Error: " + pResponseData.getString("ERROR"));
                            } else {
                                //Go to Stream Section
                                gotoOtherActivity(StreamActivity.class, pResponseData.getString("MESSAGE"), true);
                            }
                        } catch (JSONException e) {
                            showPopupMessage(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String pError, int pStatusCode) {
                        if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                            clearNBUserInfo();
                            gotoOtherActivity(LoginActivity.class, pError, true);
                        }else {
                            hideOverlay();
                            showPopupMessage(pError);
                        }
                    }
                };

                apiManager.execute(streamListener);
            } else {
                NBRestUploadFile fileUploader = new NBRestUploadFile("post", "create", nbUserApiToken);

                fileUploader.addField("content", tPostContentText);
                fileUploader.addField("post_visibility", tPostVisibilityText);
                fileUploader.addField("post_type", "image");

                fileUploader.setFile(imageFile);

                showOverlay();

                NBRestAPIListener streamListener = new NBRestAPIListener() {
                    @Override
                    public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                        hideOverlay();

                        try {
                            if (pResponseData.getString("STATUS").equals("ERROR")) {
                                showPopupMessage("Error: " + pResponseData.getString("ERROR"));
                            } else {
                                //Go to Stream Section
                                gotoOtherActivity(StreamActivity.class, pResponseData.getString("MESSAGE"), true);
                            }
                        } catch (JSONException e) {
                            showPopupMessage(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String pError, int pStatusCode) {
                        if(pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                            clearNBUserInfo();
                            gotoOtherActivity(LoginActivity.class, pError, true);
                        }else {
                            hideOverlay();
                            showPopupMessage(pError);
                        }
                    }
                };

                fileUploader.execute(streamListener);

            }
        }
    }

}























