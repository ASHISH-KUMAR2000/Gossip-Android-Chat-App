package com.ashish.gossip;

import android.content.Intent;
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
    private List<FriendsInfo> friendsInfoList;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser ;
    //A flag to ensure that recycler adapter is just called once
    boolean flag = true;


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

        collectionReference
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {

                        if( e != null){
                            Toast.makeText(ChatListActivity.this,
                                    "Please check your internet connection.\nTry again after sometime.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            if(!queryDocumentSnapshots.isEmpty()) {
                                friendsInfoList.clear();
                                for (DocumentSnapshot documentChange : queryDocumentSnapshots) {
                                    DocumentSnapshot snapshot  = documentChange;

                                    //snapshot to object
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

                                    if(!TextUtils.isEmpty(lastMessage)) {
                                        long time = snapshot.getTimestamp("timeAdded").getSeconds()*1000;
                                        timeAdded = new Timestamp(time);
                                        friend.setLastMessage(lastMessage);
                                        friend.setTimeAdded(timeAdded);
                                    }

                                    friendsInfoList.add(friend);

                                }
                                Collections.sort(friendsInfoList, new Comparator<FriendsInfo>() {
                                    @Override
                                    public int compare(FriendsInfo o1, FriendsInfo o2) {
                                        try {
                                            //sorting in ascending order
                                            if(o1.getTimeAdded()!=null&&o2.getTimeAdded()!=null)
                                                return o2.getTimeAdded().compareTo(o1.getTimeAdded());
                                            else if(o1.getTimeAdded()==null)
                                                return 1;
                                            else if(o2.getTimeAdded()==null)
                                                return -1;
                                            else
                                                return -1;
                                        } catch (Exception e) {
                                            //Log.d(TAG, e.getMessage());
                                            return 0;
                                        }
                                    }
                                });

                                if(friendsInfoList.size() != 0 && flag ){
                                    //Invoke Recycler View
                                    friendListRecyclerAdapter = new FriendListRecyclerAdapter(ChatListActivity.this,
                                            friendsInfoList);
                                    recyclerView.setAdapter(friendListRecyclerAdapter);
                                    flag = false;
                                }

                                friendListRecyclerAdapter.notifyDataSetChanged();


                            }
                        }
                    }
                });
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
}
