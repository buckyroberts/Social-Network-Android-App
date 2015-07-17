package com.thenewboston;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.thenewboston.view.FriendIncomingAdapter;
import com.thenewboston.view.FriendPendingAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    CharSequence pageTitles[];
    int numberOfTabs;
    String adapterType;
    JSONObject profileData;

    FriendRequestActivity activity;

    FriendRequestReceivedFragment receivedFragment;
    FriendRequestSentFragment pendingFragment;

    public ViewPagerAdapter(FragmentManager fm,CharSequence titles[], int amount, String type, JSONObject pInitData) {
        super(fm);

        this.pageTitles = titles;
        this.numberOfTabs = amount;
        this.adapterType = type;
        this.profileData = pInitData;
    }

    //Return Fragment for every position in the ViewPager
    @Override
    public Fragment getItem(int position) {

        if (adapterType.equals("Friend Request")){
            switch (position) {
                case 0:
                    receivedFragment = new FriendRequestReceivedFragment();
                    receivedFragment.setParentActivity(activity);

                    return receivedFragment;
                case 1:
                    pendingFragment = new FriendRequestSentFragment();
                    pendingFragment.setParentActivity(activity);

                    return pendingFragment;
            }
        }else{
            switch (position) {
                case 0:
                    ProfileHomeFragment profileHomeFragment = new ProfileHomeFragment();
                    profileHomeFragment.setProfileData(profileData);
                    return profileHomeFragment;
                case 1:
                    ProfileAboutFragment profileAboutFragment = new ProfileAboutFragment();
                    profileAboutFragment.setProfileData(profileData);
                    return profileAboutFragment;
                case 2:
                    ProfilePhotosFragment profilePhotosFragment = new ProfilePhotosFragment();
                    profilePhotosFragment.setProfileData(profileData);
                    return profilePhotosFragment;
                case 3:
                    ProfileFriendsFragment profileFriendsFragment = new ProfileFriendsFragment();
                    profileFriendsFragment.setProfileData(profileData);
                    return profileFriendsFragment;

            }
        }

        ProfileHomeFragment fragment = new ProfileHomeFragment();
        fragment.setProfileData(profileData);

        return fragment;
        //return new ProfileHomeFragment(profileData);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    public void refreshIncomingList(JSONArray array)
    {
        FriendIncomingAdapter mViewAdapter = receivedFragment.getAdapter();

        mViewAdapter.clearData();
        mViewAdapter.appendData(mViewAdapter.getFriendArrayListFromJSONArray(array));

        mViewAdapter.notifyDataSetChanged();
    }

    public void refreshPendingList(JSONArray array)
    {
        FriendPendingAdapter mViewAdapter = pendingFragment.getAdapter();

        mViewAdapter.clearData();
        mViewAdapter.appendData(mViewAdapter.getFriendArrayListFromJSONArray(array));

        mViewAdapter.notifyDataSetChanged();
    }

    public void setParentActivity(FriendRequestActivity activity)
    {
        this.activity = activity;
    }
}