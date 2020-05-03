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
import java.util.Stack;

public class ChatRecordRecyclerAdapter extends RecyclerView.Adapter<ChatRecordRecyclerAdapter.ViewHolder> {

    private Context context;

    private List<ChatMessage> messageList;
    public ChatRecordRecyclerAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.chat_record_row, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        ChatMessage message = messageList.get(position);

        viewHolder.messageTextView.setText(message.getTextMessage());
        viewHolder.userNameTextView.setText(message.getUserName());

        //timeAdded time ago..
        //src = https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87

        String timeAgo =(String) DateUtils.getRelativeTimeSpanString(message.getTimeAdded().getTime());
        viewHolder.timeAddedTextView.setText(timeAgo);


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTextView, userNameTextView, timeAddedTextView;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;

            messageTextView = itemView.findViewById(R.id.chatRecord_message_textView);
            userNameTextView = itemView.findViewById(R.id.chatRecord_userName_textView);
            timeAddedTextView = itemView.findViewById(R.id.chatRecord_time_textView);

        }
    }
}

