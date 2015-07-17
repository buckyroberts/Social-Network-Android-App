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

import com.thenewboston.view.FriendPendingAdapter;

public class FriendRequestSentFragment extends Fragment {
    RecyclerView mRecyclerView;
    LinearLayoutManager mRecyclerViewManager;
    FriendPendingAdapter mViewAdapter;

    FriendRequestActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_request_sent, container, false);

        FragmentActivity c = getActivity();

        initRequestListView(view);

        return view;
    }

    private void initRequestListView(View view)
    {
        FragmentActivity c = getActivity();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.friend_request_sent_recycler_view);
        mRecyclerViewManager = new LinearLayoutManager(c.getApplicationContext());
        mRecyclerView.setLayoutManager(mRecyclerViewManager);

        mViewAdapter = new FriendPendingAdapter(this);
        mRecyclerView.setAdapter(mViewAdapter);
    }

    public void setParentActivity(FriendRequestActivity activity)
    {
        this.activity = activity;
    }

    public FriendPendingAdapter getAdapter()
    {
        return mViewAdapter;
    }

    public void delete(int position)
    {
        activity.delete(mViewAdapter.getFriendItemAt(position).friendID);
    }

}