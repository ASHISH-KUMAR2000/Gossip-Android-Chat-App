package com.ashish.gossip;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.ashish.gossip.model.FriendsInfo;
import com.ashish.gossip.ui.FriendListRecyclerAdapter;
import com.ashish.gossip.ui.RecyclerItemClickListener;
import com.ashish.gossip.ui.UserApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.remote.WatchChange;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ChatListActivity extends AppCompatActivity {

    //private static final String TAG = "ChatListActivity";
    private static final String CHANNEL_ID = "666";
    private List<FriendsInfo> friendsInfoList;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser ;
    boolean firstTime = true;


    FirebaseFirestore db ;
    CollectionReference collectionReference;

    private RecyclerView recyclerView;
    private FriendListRecyclerAdapter friendListRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = findViewById(R.id.friend_chat_toolbar);
        toolbar.setTitle("Friend List");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser  = firebaseAuth.getCurrentUser();


        recyclerView = findViewById(R.id.friend_list_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Goto AddFriendActivity
                startActivity(new Intent(ChatListActivity.this,
                        AddFriendActivity.class));
            }
        });


        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ChatListActivity.this, new RecyclerItemClickListener.OnItemClickListener(){
                    @Override
                    public void onItemClick(View v, int position) {

                        String friendUserId = friendListRecyclerAdapter.getAdapterPositionUserId(position);
                        Intent intent = new Intent(ChatListActivity.this,
                                FriendChatRecord.class);
                        intent.putExtra("friendUserId", friendUserId);
                        startActivity(intent);
                    }
                })
        );

        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Chat")
                .document(currentUser.getUid())
                .collection("FriendLastMessage");

        friendsInfoList = new ArrayList<>();

        getFriendsDataFromFireStore();
    }

    private void getFriendsDataFromFireStore() {
        collectionReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Toast.makeText(ChatListActivity.this,
                                    "Please check your internet connection.\nTry again after sometime.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            if (!queryDocumentSnapshots.getDocumentChanges().isEmpty()) {
                                for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()) {
                                    switch (document.getType()) {
                                        case ADDED:
                                            DocumentSnapshot snapshot = document.getDocument();
                                            //snapshot to object
                                            FriendsInfo friend = snapshotToObject(snapshot);
                                            friendsInfoList.add(friend);
                                            //Log.d(TAG, "Exist" + " " + friend.getFriendUserName());
                                            sortFriendList(friendsInfoList);

                                            if (friendsInfoList.size() != 0 && firstTime == true) {//Invoke Recycler View
                                                friendListRecyclerAdapter = new FriendListRecyclerAdapter(ChatListActivity.this,
                                                        friendsInfoList);
                                                recyclerView.setAdapter(friendListRecyclerAdapter);
                                                firstTime = false;
                                            }

                                            friendListRecyclerAdapter.notifyDataSetChanged();
                                            break;
                                        case MODIFIED:
                                            snapshot = document.getDocument();
                                            //snapshot to object
                                            friend = snapshotToObject(snapshot);
                                            for(int i=0 ; i < friendsInfoList.size() ; i++){
                                                if(friendsInfoList.get(i).getFriendUserId().equals(friend.getFriendUserId())){
                                                    friendsInfoList.set(i, friend);
                                                }
                                            }
                                            if(!TextUtils.isEmpty(friend.getLastMessage())) {

                                                showNotificationMessageReceived(friend);
                                                sortFriendList(friendsInfoList);

                                                if (friendsInfoList.size() != 0 && firstTime == true) {
                                                    //Invoke Recycler View
                                                    friendListRecyclerAdapter = new FriendListRecyclerAdapter(ChatListActivity.this,
                                                            friendsInfoList);
                                                    recyclerView.setAdapter(friendListRecyclerAdapter);
                                                    firstTime = false;
                                                }
                                            }
                                            friendListRecyclerAdapter.notifyDataSetChanged();
                                            break;
                                    }
                                }
                            }
                        }
                    }
                                });
    }

    private void showNotificationMessageReceived(FriendsInfo friend) {
        NotificationCompat.Builder builderNew = new NotificationCompat.Builder(ChatListActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Message Received")
                .setContentText(friend.getFriendUserName() + " - " + friend.getLastMessage())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerNew = NotificationManagerCompat.from(ChatListActivity.this);

        // notificationId is a unique int for each notification that you must define
        notificationManagerNew.notify(friendsInfoList.size(), builderNew.build());
    }

    private void sortFriendList(List<FriendsInfo> friendsInfoList) {
        Collections.sort(friendsInfoList, new Comparator<FriendsInfo>() {
            @Override
            public int compare(FriendsInfo o1, FriendsInfo o2) {
                try {
                    //sorting in ascending order
                    if (o1.getTimeAdded() != null && o2.getTimeAdded() != null)
                        return o2.getTimeAdded().compareTo(o1.getTimeAdded());
                    else if (o1.getTimeAdded() == null)
                        return 1;
                    else if (o2.getTimeAdded() == null)
                        return -1;
                    else
                        return -1;
                } catch (Exception e) {
                    //Log.d(TAG, e.getMessage());
                    return 0;
                }
            }
        });
    }

    private FriendsInfo snapshotToObject(DocumentSnapshot snapshot){
        String friendUserId, friendUserName, friendDpUrl, lastMessage;
        Timestamp timeAdded;
        friendUserId = snapshot.getString("friendUserId");
        friendUserName = snapshot.getString("friendUserName");
        friendDpUrl = snapshot.getString("friendDpUrl");
        lastMessage = snapshot.getString("lastMessage");

        FriendsInfo friend = new FriendsInfo(
                friendUserId,
                friendUserName,
                friendDpUrl
        );

        if (!TextUtils.isEmpty(lastMessage)) {
            long time = snapshot.getTimestamp("timeAdded").getSeconds() * 1000;
            timeAdded = new Timestamp(time);
            friend.setLastMessage(lastMessage);
            friend.setTimeAdded(timeAdded);
        }

        return friend;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_action_change_dp:
                startActivity(new Intent(ChatListActivity.this,
                        CurrentDpActivity.class));
                break;
            case R.id.menu_action_signout:
                firebaseAuth.signOut();
                //Delete UserApi
                UserApi userApi = UserApi.getInstance();
                userApi.setDpImgUrl(null);
                userApi.setPhoneNumber(null);
                userApi.setUserId(null);
                userApi.setUserName(null);
                startActivity(new Intent(ChatListActivity.this,
                        MainActivity.class));
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    //setting notification channel
    //https://developer.android.com/training/notify-user/build-notification
    private void createNotificationChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
