package com.ashish.gossip.util;

//For Displaying Notifications we need 3 things
//1. Notification Channel ( Mandatory above api 26)
//2. Notification Builder
//3. Notification Manager

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ashish.gossip.ChatListActivity;
import com.ashish.gossip.R;
import com.ashish.gossip.model.FriendsInfo;

import static android.app.Notification.DEFAULT_SOUND;

public class NotificationUtil extends ContextWrapper {
    public static final String CHANNEL_ID = "666";
    public static final String CHANNEL_NAME = "I'm coming";
    public static final String CHANNEL_DESC = "You better be prepared";

    public NotificationUtil(Context context) {
        super(context);
        createChannel();
    }

    private void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.setDescription(CHANNEL_DESC);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            //mChannel.enableVibration(true);
            //mChannel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    public void showNotificationMessageReceived(Context context, FriendsInfo friend) {
        NotificationCompat.Builder builderNew = new NotificationCompat.Builder(context, CHANNEL_ID);
        builderNew.setSmallIcon(R.drawable.app_icon);
        builderNew.setContentTitle(friend.getFriendUserName());
        builderNew.setContentText(friend.getLastMessage());
        builderNew.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(friend.getLastMessage().substring(0, Math.min(30, friend.getLastMessage().length())) + "..."));
        builderNew.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builderNew.setAutoCancel(true);
        builderNew.setColor(Color.BLUE);
        //builderNew.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        NotificationManagerCompat notificationManagerNew = NotificationManagerCompat.from(context);


        // notificationId is a unique int for each notification that you must define
        notificationManagerNew.notify(1, builderNew.build());
    }

}
