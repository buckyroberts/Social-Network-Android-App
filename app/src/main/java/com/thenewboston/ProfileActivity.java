package com.thenewboston;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;
import com.thenewboston.restclient.NBRestUploadFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class ProfileActivity extends NBBaseActivity {
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    SlidingTabLayout slidingTabLayout;
    CharSequence pageTitles[] = {"Profiles", "About", "Photos", "Friends"};
    int numberOfTabs = 4;
    String mProfileId = null;

    private String imageFile = null;

    protected void onCreate(Bundle savedInstanceState) {
        //Getting profile id
        Bundle extras = getIntent().getExtras();
        mProfileId = extras.getString("profileId");

        currentActivity = "profile";
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        setNBToolbar();
        addNBNavigationMenu();

        loadProfileInformation();
    }

    protected void initProfileTabs(JSONObject pInitData) {

        //Create a sliding tab layout
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), pageTitles, numberOfTabs, "Profile", pInitData);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);

        //Space tabs evenly
        slidingTabLayout.setDistributeEvenly(true);

        //Set custom tab indicator color
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tab_bg);
            }
        });
        slidingTabLayout.setViewPager(viewPager);
    }

    public void selectPhoto()
    {
        Crop.pickImage(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            uploadProfileImage(Crop.getOutput(result).getPath());
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfileInformation()
    {
        //Getting Initial Information
        NBRestAPIManager apiManager = new NBRestAPIManager("profile", "getInfo", nbUserApiToken, "post");

        apiManager.addField("profileId", mProfileId);

        NBRestAPIListener streamListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                hideOverlay();
                try {
                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                        showPopupMessage(pResponseData.getString("ERROR"));
                    } else {
                        initProfileTabs(pResponseData);
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
    }

    private void uploadProfileImage(String imagePath)
    {
        NBRestUploadFile fileUploader = new NBRestUploadFile("post", "changeProfileImage", nbUserApiToken);
        fileUploader.setFile(imagePath);

        showOverlay();

        NBRestAPIListener streamListener = new NBRestAPIListener() {
            @Override
            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                loadProfileInformation();
            }

            @Override
            public void onFailure(String pError, int pStatusCode) {
                hideOverlay();

                showPopupMessage("can't update the profile image");
            }
        };

        fileUploader.execute(streamListener);
    }
}
