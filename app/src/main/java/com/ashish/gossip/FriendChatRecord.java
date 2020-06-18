package com.ashish.gossip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;

import com.ashish.gossip.helper.UploadInfoToFriendDb;
import com.ashish.gossip.model.ChatMessage;
import com.ashish.gossip.model.FriendsInfo;
import com.ashish.gossip.model.User;
import com.ashish.gossip.ui.ChatRecordRecyclerAdapter;
import com.ashish.gossip.ui.UserApi;
import com.ashish.gossip.util.NotificationUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class FriendChatRecord extends AppCompatActivity implements View.OnClickListener {

    private static final String CHANNEL_ID = "666";
    //private static final String TAG = "FriendChatRecord";
    private String friendUserId, friendUserName;
    UploadInfoToFriendDb obj ;
    private FriendsInfo lastChatMessage;
    private boolean flag = true;

    private FloatingActionButton sendMessageButton;
    private EditText messageEditView;
    private RecyclerView recyclerView;
    private ChatRecordRecyclerAdapter recyclerAdapter;
    private List<ChatMessage> messageList;
    LinearLayoutManager linearLayoutManager;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    FirebaseFirestore db;
    CollectionReference collectionReference, userReference;
    CollectionReference friendCollectionReference;

    NotificationUtil mNotificationUtil ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chat_record);
        Intent intent = getIntent();
        friendUserId = intent.getStringExtra("friendUserId");

        mNotificationUtil = new NotificationUtil(FriendChatRecord.this);

        obj = new UploadInfoToFriendDb();

        sendMessageButton = findViewById(R.id.friend_chat_send_message_imageButton);
        messageEditView = findViewById(R.id.friend_chat_send_message_editText);

        recyclerView = findViewById(R.id.friend_chat_recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        UserApi userApi = UserApi.getInstance();//Global Api

        db = FirebaseFirestore.getInstance();

        lastChatMessage = new FriendsInfo();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        collectionReference = db.collection("Chat")
                .document(userApi.getUserId())
                .collection("FriendChatRecord")
                .document(friendUserId)
                .collection("ChatHistory");

        friendCollectionReference = db.collection("Chat")
                .document(userApi.getUserId())
                .collection("FriendLastMessage");

        //Get FriendName to set it on title
        userReference = db.collection("Users");
        userReference.whereEqualTo("userId", friendUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            FriendChatRecord.this.setTitle("Anonymous");
                        } else {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                friendUserName = snapshot.getString("userName");
                            }
                            FriendChatRecord.this.setTitle(friendUserName);
                        }
                    }
                });

        messageList = new ArrayList<>();

        //Real time listener
        getFriendsDataFromFireStore();
        sendMessageButton.setOnClickListener(this);

        //Check if someone else messaged you
        otherMessageListener();


    }

    private void otherMessageListener() {
        friendCollectionReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            return ;
                        } else if(!queryDocumentSnapshots.getDocumentChanges().isEmpty()){
                            for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                                switch (dc.getType()){
                                    case ADDED:
                                        //New friend added
                                        break;
                                    case MODIFIED:
                                        DocumentSnapshot snapshot = dc.getDocument();
                                        //snapshot to object
                                        FriendsInfo friend = snapshotToObject(snapshot);
                                        if(friend.getFriendUserId() != null
                                        &&!friend.getFriendUserId().equals(friendUserId)) {
                                            //Log.d(TAG, "It's showing");
                                            //Log.d(TAG, "\n\n\n"+friend.getFriendUserId()+" "+friendUserId+"\n\n\n");
                                            mNotificationUtil.showNotificationMessageReceived(FriendChatRecord.this,
                                                    friend);
                                        }
                                        break;
                                }
                            }
                        }

                    }
                });
    }

    private void getFriendsDataFromFireStore() {
        collectionReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            //Log.d(TAG, e.getMessage());
                        } else if(!queryDocumentSnapshots.getDocumentChanges().isEmpty()){
                            //messageList.clear();
                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                                switch (documentChange.getType()){
                                    case ADDED:
                                        DocumentSnapshot doc = documentChange.getDocument();

                                        ChatMessage message = new ChatMessage();
                                        message.setUserName(doc.getString("userName"));

                                        long time = doc.getTimestamp("timeAdded").getSeconds() * 1000;
                                        Timestamp timestamp = new Timestamp(time);
                                        message.setTimeAdded(timestamp);
                                        message.setUserId(doc.getString("userId"));
                                        message.setTextMessage(doc.getString("textMessage"));

                                        messageList.add(message);

                                        if (messageList.size() != 0 && flag) {
                                            recyclerAdapter = new ChatRecordRecyclerAdapter(FriendChatRecord.this,
                                                    messageList);
                                            recyclerView.setAdapter(recyclerAdapter);
                                            linearLayoutManager.smoothScrollToPosition(recyclerView,
                                                    null,
                                                    messageList.size() - 1);
                                            flag = false;
                                        }

                                        if (messageList.size() != 0) {
                                            Collections.sort(messageList, new Comparator<ChatMessage>() {
                                            @Override
                                            public int compare(ChatMessage o1, ChatMessage o2) {
                                                    try {
                                                        return o1.getTimeAdded().compareTo(o2.getTimeAdded());
                                                    } catch (Exception e) {
                                                        //Log.d(TAG, e.getMessage());
                                                        return 0;
                                                    }
                                                }
                                        });
                                        recyclerAdapter.notifyDataSetChanged();
                                        linearLayoutManager.smoothScrollToPosition(recyclerView,
                                                null,
                                                messageList.size() - 1);

                                    }
                                }
                            }
                        }
                    }

                });
    }

    @Override
    public void onClick(View v) {
        String message, userName;
        UserApi userApi = UserApi.getInstance();

        userName = UserApi.getInstance().getUserName();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        message = messageEditView.getText().toString().trim();

        if (!TextUtils.isEmpty(message)) {

            ChatMessage mess = new ChatMessage(
                    message,
                    userName,
                    timestamp,
                    currentUser.getUid()
            );

            lastChatMessage.setFriendDpUrl(userApi.getDpImgUrl());
            lastChatMessage.setFriendUserName(userApi.getUserName());
            lastChatMessage.setFriendUserId(userApi.getUserId());
            lastChatMessage.setLastMessage(message);
            lastChatMessage.setTimeAdded(timestamp);


            //Message to add on own database
            collectionReference.add(mess)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            messageEditView.setText(null);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(FriendChatRecord.this,
                                    "Please check your internet connection.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            //Message to add on friend database
            //When you sent message to yourself
            //It should be updated only once in the database
            if(currentUser.getUid() != friendUserId) {

                obj.uploadChatMessage(FriendChatRecord.this,
                        currentUser.getUid(),
                        friendUserId,
                        mess);


                obj.UploadLastMessage(FriendChatRecord.this,
                        currentUser.getUid(),
                        friendUserId,
                        lastChatMessage);
            }


        }
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

}