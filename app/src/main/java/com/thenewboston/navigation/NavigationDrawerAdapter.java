package com.thenewboston.navigation;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thenewboston.R;

import java.util.ArrayList;

public class NavigationDrawerAdapter extends BaseExpandableListAdapter {

    private LayoutInflater inflater;
    private ArrayList<Object> childItems;
    private ArrayList<NavigationDrawerItem> parentItems;

    //Constructor
    public NavigationDrawerAdapter(ArrayList<NavigationDrawerItem> parents, ArrayList<Object> children) {
        this.parentItems = parents;
        this.childItems = children;
    }


    public void setInflater(LayoutInflater inflater, Activity activity) {
        this.inflater = inflater;
    }


    //Called automatically for each child view (implement per requirement)
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        ChildMenuHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.navigation_drawer_child, parent, false);

            holder = new ChildMenuHolder();
            holder.imageViewChildIcon = (ImageView) convertView.findViewById(R.id.childImage);
            holder.textViewChildName = (TextView) convertView.findViewById(R.id.textViewChild);
            holder.textViewBadge = (TextView) convertView.findViewById(R.id.badgeTextView);
            holder.layoutBadge = (LinearLayout) convertView.findViewById(R.id.childBadgeLayout);
            convertView.setTag(holder);
        } else {
            holder = (ChildMenuHolder) convertView.getTag();
        }

        NavigationDrawerItem item = (NavigationDrawerItem) getChild(groupPosition, childPosition);
        //Set the text
        holder.textViewChildName.setText(item.getItemTitle());
        holder.textViewBadge.setText(item.getBadgeNumber() + "");
        holder.layoutBadge.setVisibility(item.getBadgeNumber() > 0 ? View.VISIBLE : View.GONE);
        holder.imageViewChildIcon.setImageResource(item.getItemIconResourceId());

        return convertView;
    }


    //Called automatically for each parent item (implement per requirement)
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupMenuHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.navigation_drawer_parent, parent, false);

            holder = new GroupMenuHolder();
            holder.imageViewGroupIcon = (ImageView) convertView.findViewById(R.id.imageViewGroupIcon);
            holder.textViewGroupName = (CheckedTextView) convertView.findViewById(R.id.textViewGroupName);
            holder.imageViewExpanded = (ImageView) convertView.findViewById(R.id.imageViewExpanded);
            convertView.setTag(holder);
        } else {
            holder = (GroupMenuHolder) convertView.getTag();
        }
        holder.textViewGroupName.setText(parentItems.get(groupPosition).getItemTitle());
        holder.textViewGroupName.setChecked(isExpanded);
        holder.imageViewGroupIcon.setImageResource(parentItems.get(groupPosition).getItemIconResourceId());
        holder.imageViewExpanded.setVisibility(getChildrenCount(groupPosition) > 0 ? View.VISIBLE : View.GONE);
        holder.imageViewExpanded.setImageResource(isExpanded ? R.drawable.ic_nav_up : R.drawable.ic_nav_down);
        return convertView;
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return ((ArrayList<Object>) childItems.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<String>) childItems.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parentItems.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return parentItems.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GroupMenuHolder {
        ImageView imageViewGroupIcon;
        CheckedTextView textViewGroupName;
        ImageView imageViewExpanded;
    }

    class ChildMenuHolder {
        ImageView imageViewChildIcon;
        TextView textViewChildName;
        TextView textViewBadge;
        LinearLayout layoutBadge;
    }
}