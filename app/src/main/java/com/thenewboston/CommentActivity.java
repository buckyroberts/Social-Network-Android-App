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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.restclient.NBRestUploadFile;
import com.thenewboston.view.CommentAdapter;
import com.thenewboston.view.items.NBCommentItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class CommentActivity extends NBBaseActivity {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    CommentAdapter mViewAdapter;

    final int USE_CAMERA = 91;
    final int SELECT_FILE = 90;

    private int commentsCount;

    private String imageFile = null;

    String postId;

    @Override
    protected void onCreate(Bundle savedInstanceStateBundle) {
        currentActivity = "comment";
        super.onCreate(savedInstanceStateBundle);

        setContentView(R.layout.activity_comment);

        if ( !isUserLoggedIn() ) {

            //Go to Login Activity
            Intent toLoginIntent = new Intent(this, LoginActivity.class);
            this.startActivity(toLoginIntent);
            this.finish();

            return;
        }

        setNBToolbar();
        addNBNavigationMenu();

        initCommentListView();

        //Getting Post ID
        Bundle tExtras = getIntent().getExtras();
        if (tExtras != null) {
            postId = tExtras.getString("post_id");
        }

        //Getting Comments using rest api
        NBRestAPIManager apiManager = new NBRestAPIManager("comment", "getList", nbUserApiToken, "post");

        apiManager.addField("postID", postId);

        NBRestAPIListener restApiListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();
                showNewCommentWrap();

                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        mViewAdapter.clearData();
                        mViewAdapter.appendData(mViewAdapter.getCommentArrayListFromJSONArray(pResponseData.getJSONArray("RESULT")));

                        mViewAdapter.notifyDataSetChanged();
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
                }else{
                    hideOverlay();
                    showPopupMessage(pError);
                }
            }
        };

        apiManager.execute(restApiListener);
    }

    private void initCommentListView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.comment_list_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerViewManager);
        mViewAdapter = new CommentAdapter(this, nbUserID, nbUserApiToken);
        mRecyclerView.setAdapter(mViewAdapter);
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

    public void createNewComment(View view) {
        EditText tCommentContent = (EditText)findViewById(R.id.new_comment_content);

        String tCommentContentText = tCommentContent.getText().toString().trim();

        if (TextUtils.isEmpty(tCommentContentText) && imageFile == null) {
            showPopupMessage("Please write something hoss");

            return;
        } else {


            NBRestUploadFile fileUploader = new NBRestUploadFile("comment", "create", nbUserApiToken);

            fileUploader.addField("postID", postId);
            fileUploader.addField("comment", tCommentContentText);

            fileUploader.setFile(imageFile);

            hideNewCommentWrap();
            showOverlay();

            NBRestAPIListener streamListener = new NBRestAPIListener() {
                @Override
                public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                    showNewCommentWrap();
                    hideOverlay();

                    try {
                        if (pResponseData.getString("STATUS").equals("ERROR")) {
                            showPopupMessage("Error: " + pResponseData.getString("ERROR"));
                        } else {

                            mViewAdapter.addNewComment(new NBCommentItem(pResponseData.getJSONObject("NEWCOMMENT")));
                            commentsCount = pResponseData.getInt("COMMENTS");
                            mViewAdapter.notifyDataSetChanged();
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
                        showNewCommentWrap();
                        showPopupMessage(pError);
                    }
                }
            };

            fileUploader.execute(streamListener);
        }

    }

    public void initNewCommentWrap() {
        EditText tCommentContent = (EditText)findViewById(R.id.new_comment_content);
        tCommentContent.setText("");
        if (imageFile != null) {
            imageFile = null;

            RelativeLayout tPreviewLayout = (RelativeLayout)findViewById(R.id.photo_preview_layout);
            tPreviewLayout.setVisibility(View.GONE);

        }
    }

    public void showNewCommentWrap() {
        //Show new comment form

        LinearLayout tNewCommentWrapper = (LinearLayout)findViewById(R.id.newCommentWrap);
        if(tNewCommentWrapper.getVisibility() != View.VISIBLE) {
            tNewCommentWrapper.setVisibility(View.VISIBLE);
        }

        Animation tSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        tNewCommentWrapper.startAnimation(tSlideUp);
    }

    public void hideNewCommentWrap() {
        //Show new comment form
        hideInputKeyboard();
        initNewCommentWrap();
        LinearLayout tNewCommentWrapper = (LinearLayout)findViewById(R.id.newCommentWrap);
        Animation tSlideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);
        tNewCommentWrapper.startAnimation(tSlideOut);
    }
}
