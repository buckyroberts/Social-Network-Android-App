package com.thenewboston.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thenewboston.MessagesReadActivity;
import com.thenewboston.R;
import com.thenewboston.view.items.NBMessageItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageRow> {

    private ArrayList<NBMessageItem> mDataset = new ArrayList<>();

    private Context mContext;

    //Constructor: populates a local variable using data passed in
    public MessageAdapter(Context context) {
        this.mContext = context;
    }

    //Called by LayoutManager each time it needs to create a new row
    @Override
    public MessageRow onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout, parent, false);
        return new MessageRow(v);
    }

    //Replaces the contents of a view at each position
    public void onBindViewHolder(final MessageRow messageRow, int position) {
        final NBMessageItem tMessage = mDataset.get(position);

        messageRow.itemName.setText(tMessage.subject);
        messageRow.itemSubject.setText(tMessage.body);
        messageRow.itemDate.setText(tMessage.createdDate);

        UrlImageViewHelper.setUrlDrawable(messageRow.itemImage, tMessage.senderThumbnail, R.drawable.default_profile_image);

        if (tMessage.status.equals("read") && tMessage.type.equals("inbox"))
            messageRow.singleMessageLayout.setBackgroundColor(Color.LTGRAY);

        messageRow.singleMessageLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ColorDrawable bg = (ColorDrawable) messageRow.singleMessageLayout.getBackground();
                int colorFrom = bg.getColor();
                int colorTo =  Color.rgb(230, 230, 230);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                colorAnimation.setDuration(800);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        messageRow.singleMessageLayout.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorAnimation.start();

                Intent i = new Intent(mContext, MessagesReadActivity.class);

                i.putExtra("messageId", tMessage.messageId);
                i.putExtra("messageType", tMessage.type);

                mContext.startActivity(i);
            }
        });
    }

    //Size of data set is needed by LayoutManager
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class MessageRow extends RecyclerView.ViewHolder {
        RelativeLayout singleMessageLayout;

        ImageView itemImage;
        TextView itemName;
        TextView itemSubject;
        TextView itemDate;

        public MessageRow(View itemView) {
            super(itemView);

            singleMessageLayout = (RelativeLayout) itemView.findViewById(R.id.singleMessageLayout);

            itemImage = (ImageView) itemView.findViewById(R.id.item_thumbnail);
            itemName = (TextView) itemView.findViewById(R.id.item_title);
            itemSubject = (TextView) itemView.findViewById(R.id.itemSubject);
            itemDate = (TextView) itemView.findViewById(R.id.itemDate);
        }
    }

    public ArrayList<NBMessageItem> getMessageArrayListFromJSONArray(JSONArray pMessages) {
        ArrayList<NBMessageItem> tMessageList = new ArrayList<>();

        if (pMessages != null) {
            for (int i=0; i < pMessages.length(); i++) {

                try {
                    tMessageList.add(new NBMessageItem(pMessages.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return tMessageList;
    }

    public void clearData()
    {
        mDataset.clear();
    }

    public void appendData(ArrayList<NBMessageItem> pData)
    {
        mDataset.addAll(pData);
    }
}