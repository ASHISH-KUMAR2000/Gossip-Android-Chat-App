package com.ashish.gossip.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.gossip.R;
import com.ashish.gossip.model.FriendsInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListRecyclerAdapter extends RecyclerView.Adapter<FriendListRecyclerAdapter.ViewHolder> {

    private static final String TAG = "FriendListRecyclerAdapter";

    private Context context;
    private List<FriendsInfo> friendList;


    public FriendListRecyclerAdapter(Context context, List<FriendsInfo> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendListRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.friend_list_row, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListRecyclerAdapter.ViewHolder viewHolder, int position) {
        FriendsInfo friendsInfo = friendList.get(position);
        String dpUrl;

        viewHolder.userNameTextView.setText(friendsInfo.getFriendUserName());
        dpUrl = friendsInfo.getFriendDpUrl();

        if( friendsInfo.getLastMessage() != null && friendsInfo.getTimeAdded() != null){
            String lastMessage;

            lastMessage = friendsInfo.getLastMessage();
            //timeAdded time ago..
            //src = https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87

            String timeAgo =(String) DateUtils.getRelativeTimeSpanString(friendsInfo.getTimeAdded().getTime());

            viewHolder.lastMessageTextView.setText(lastMessage);
            viewHolder.timeAddedTextView.setText(timeAgo);
        }

        //Use Picasso library to download and show image

        Picasso.get()
                .load(dpUrl)
                .fit()
                .centerCrop()
                .into(viewHolder.userDpImageView);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView lastMessageTextView, timeAddedTextView;
        TextView userNameTextView;
        ImageView userDpImageView;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;

            userNameTextView = itemView.findViewById(R.id.friend_row_userName_textView);
            userDpImageView = itemView.findViewById(R.id.friend_row_dp_imageView);
            lastMessageTextView = itemView.findViewById(R.id.friend_row_last_mess_textView);
            timeAddedTextView = itemView.findViewById(R.id.friend_row_timeAdded_textView);
        }
    }

    public String getAdapterPositionUserId(int position) {
     if (position >= 0 && position<friendList.size()){
         return friendList.get(position).getFriendUserId();
     }

     return null;
    }
}
