package com.ashish.gossip.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.gossip.R;
import com.ashish.gossip.model.ChatMessage;

import java.util.List;

public class ChatRecordRecyclerAdapter extends RecyclerView.Adapter<ChatRecordRecyclerAdapter.ViewHolder> {

    private Context context;
    private static final int VIEWTYPE_MESSAGE_RECEIVED = 1;
    public static final int VIEWTYPE_MESSAGE_SENT = 2;

    private List<ChatMessage> messageList;
    public ChatRecordRecyclerAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == VIEWTYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.chat_row_sent_message, parent, false);
        }
        else{
            view = LayoutInflater.from(context)
                    .inflate(R.layout.chat_row_received_message,parent, false);
        }
        return new ViewHolder(view, context);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        UserApi userApi = UserApi.getInstance();
        if(message.getUserId().equals(userApi.getUserId()))
            return VIEWTYPE_MESSAGE_SENT;
        else
            return VIEWTYPE_MESSAGE_RECEIVED;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        ChatMessage message = messageList.get(position);

        if(viewHolder.getItemViewType()==VIEWTYPE_MESSAGE_SENT){
            viewHolder.sendMessageTextView.setText(message.getTextMessage());

            //timeAdded time ago..
            //src = https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
            String timeAgo =(String) DateUtils.getRelativeTimeSpanString(message.getTimeAdded().getTime());
            viewHolder.sendTimeAddedTextView.setText(timeAgo);

        } else {
            viewHolder.receivedMessageTextView.setText(message.getTextMessage());

            //timeAdded time ago..
            //src = https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
            String timeAgo =(String) DateUtils.getRelativeTimeSpanString(message.getTimeAdded().getTime());
            viewHolder.receivedTimeAddedTextView.setText(timeAgo);
        }


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView sendMessageTextView, sendTimeAddedTextView;
        private TextView receivedMessageTextView, receivedTimeAddedTextView;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;

            sendMessageTextView = itemView.findViewById(R.id.chat_row_sent_message);
            sendTimeAddedTextView = itemView.findViewById(R.id.chat_row_sent_dateAdded);
            receivedMessageTextView = itemView.findViewById(R.id.chat_row_received_message);
            receivedTimeAddedTextView = itemView.findViewById(R.id.chat_row_received_dateAdded);
        }
    }
}

