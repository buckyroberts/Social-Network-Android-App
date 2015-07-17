package com.thenewboston;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thenewboston.view.FriendIncomingAdapter;

public class FriendRequestReceivedFragment extends Fragment {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    FriendIncomingAdapter mViewAdapter;

    FriendRequestActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_request_received, container, false);

        initRequestListView(view);

        return view;
    }

    private void initRequestListView(View view)
    {
        FragmentActivity c = getActivity();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.friend_request_received_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(c.getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerViewManager);

        mViewAdapter = new FriendIncomingAdapter(this);
        mRecyclerView.setAdapter(mViewAdapter);
    }

    public void setParentActivity(FriendRequestActivity activity)
    {
        this.activity = activity;
    }

    public FriendIncomingAdapter getAdapter()
    {
        return mViewAdapter;
    }

    public void accept(int position)
    {
        activity.accept(mViewAdapter.getFriendItemAt(position).friendID);
    }

    public void decline(int position)
    {
        activity.decline(mViewAdapter.getFriendItemAt(position).friendID);
    }
}