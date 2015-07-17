package com.thenewboston;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.restclient.NBRestAPIListener;
import com.thenewboston.restclient.NBRestAPIManager;
import com.thenewboston.restclient.NBRestHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfilePhotosFragment extends Fragment {
    JSONObject profileData;
    Boolean loadingMore = false;
    NBBaseActivity mActivity;
    String mProfileId;
    View mView;

    MyAdapter viewAdapter;

    public ProfilePhotosFragment(){
        super();
    }

    public void setProfileData(JSONObject pProfileData) {
        this.profileData = pProfileData;
    }

    public JSONObject getProfileData() {
        return profileData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.tab_profile_photos, container, false);
        profileData = getProfileData();

        Bundle extras = getActivity().getIntent().getExtras();
        mProfileId = extras.getString("profileId");

        mActivity = (NBBaseActivity) getActivity();

        final GridView gridView = (GridView) mView.findViewById(R.id.profile_photos_grid);

        ArrayList<PhotoItem> photosList = new ArrayList<>();

        try {
            JSONArray photos = profileData.getJSONArray("PHOTOS");
            for(int i=0; i < photos.length(); i++)
            {
                JSONObject tPhoto = (JSONObject) photos.get(i);
                photosList.add(new PhotoItem(tPhoto.getString("thumbnail"), tPhoto.getString("original"), tPhoto.getString("posted_date")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final MyAdapter gridAdapter = new MyAdapter(mActivity, photosList);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoItem photoItem = (PhotoItem)gridAdapter.getItem(position);

                Dialog settingsDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

                View v = getActivity().getLayoutInflater().inflate(R.layout.preview_layout
                        , null);

                settingsDialog.setContentView(v);

                ImageView imageView = (ImageView)v.findViewById(R.id.imageView);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                settingsDialog.show();

                UrlImageViewHelper.setUrlDrawable(imageView, photoItem.originalImage, R.drawable.landscape);
            }
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    if (!loadingMore) {
                        //Loading More Photos
                        NBRestAPIManager apiManager = new NBRestAPIManager("profile", "getPhotos", mActivity.nbUserApiToken, "post");

                        apiManager.addField("profileId", mProfileId);
                        apiManager.addField("lastDate", gridAdapter.getLastItemPostedDate());

                        mView.findViewById(R.id.loading_more_progress_bar).setVisibility(View.VISIBLE);
                        loadingMore = true;

                        NBRestAPIListener tRestApiListener = new NBRestAPIListener() {
                            @Override
                            public void onSuccess(JSONObject pResponseData, int pStatusCode) {
                                mView.findViewById(R.id.loading_more_progress_bar).setVisibility(View.GONE);

                                try {
                                    if (pResponseData.getString("STATUS").equals("ERROR")) {
                                        mActivity.showPopupMessage(pResponseData.getString("ERROR"));
                                    } else {
                                        JSONArray newPhotos = pResponseData.getJSONArray("PHOTOS");
                                        if (newPhotos.length() > 0){
                                            gridAdapter.appendPhotos(newPhotos);
                                            gridAdapter.notifyDataSetChanged();
                                            loadingMore = false;
                                        }
                                    }
                                } catch (JSONException e) {
                                    mActivity.showPopupMessage(e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(String pError, int pStatusCode) {
                                mView.findViewById(R.id.loading_more_progress_bar).setVisibility(View.GONE);
                                loadingMore = false;
                                if (pStatusCode == NBRestHttpClient.STATUS_UNAUTHORIZED) { //Api Token is not valid, user should be logged out
                                    mActivity.clearNBUserInfo();
                                    mActivity.gotoOtherActivity(LoginActivity.class, pError, true);
                                } else {
                                    mActivity.showPopupMessage(pError);
                                }
                            }
                        };

                        apiManager.execute(tRestApiListener);
                    }
                }
            }
        });

        return mView;
    }


    private class MyAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<PhotoItem> items;
        LayoutInflater inflater;

        public MyAdapter(Context context, ArrayList<PhotoItem> pItems) {
            this.context = context;
            this.items = pItems;

            inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.photo_grid_item, null);
            }

            SquareImageView imageView = (SquareImageView) convertView.findViewById(R.id.profile_photo_grid_item);
            PhotoItem pItem = (PhotoItem) getItem(position);
            UrlImageViewHelper.setUrlDrawable(imageView, pItem.thumbnail, R.drawable.default_profile_image);

            return convertView;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        public void appendPhotos(JSONArray pPhotos)
        {
            for(int i=0; i < pPhotos.length(); i++)            {

                try {
                    JSONObject tPhoto = (JSONObject) pPhotos.get(i);
                    items.add(new PhotoItem(tPhoto.getString("thumbnail"), tPhoto.getString("original"), tPhoto.getString("posted_date")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getLastItemPostedDate()
        {
            if (getCount() > 0) {
                return items.get(getCount() - 1).postedDate;
            }else{
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    private class PhotoItem{
        String thumbnail;
        String originalImage;
        String postedDate;

        public PhotoItem(String pThumbnail, String pOriginalImage, String pPostedDate)
        {
            thumbnail = pThumbnail;
            originalImage = pOriginalImage;
            postedDate = pPostedDate;
        }
    }



}
