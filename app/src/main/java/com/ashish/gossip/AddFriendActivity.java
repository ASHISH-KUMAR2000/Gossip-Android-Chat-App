package com.ashish.gossip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ashish.gossip.model.FriendsInfo;
import com.ashish.gossip.ui.UserApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class AddFriendActivity extends AppCompatActivity {

    //private static final String TAG = "AddFriendActivity";
    private static final int REQUEST_CODE = 1;
    private EditText friendPhoneNoEditText;
    private Button addFriendButton;
    private String friendPhoneNumber;
    private ProgressBar progressBar;
    private ImageButton contact_list;
    private String contactNoReceived;
    Uri contactUri;


    private List<FriendsInfo> friendsInfoList;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    FirebaseFirestore db ;
    CollectionReference usersReference;
    DocumentReference chatUserReference, friendUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        usersReference = db.collection("Users");

        friendPhoneNoEditText = findViewById(R.id.addFriend_phone_no_editText);
        addFriendButton = findViewById(R.id.add_friend_button);
        progressBar = findViewById(R.id.addFriendActivity_progressBar);
        contact_list = findViewById(R.id.addFriend_contact_list_imageButton);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendPhoneNumber = friendPhoneNoEditText.getText().toString().trim();

                if(!TextUtils.isEmpty(friendPhoneNumber) && friendPhoneNumber.length()==10) {
                    //Search for a user with this phone number
                    userWithPhoneNumber();
                }else{
                    Toast.makeText(AddFriendActivity.this,
                            "Empty Fields are not allowed.\nMobile number must be 10 digit long",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        contact_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForContactPermission();
            }
        });


    }

    private void askForContactPermission() {

        if(ActivityCompat.checkSelfPermission(AddFriendActivity.this,
                permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddFriendActivity.this,
                    new String[] {permission.READ_CONTACTS},
                    REQUEST_CODE);
        } else {
            getContacts();
        }
    }

    private void getContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                contactUri = data.getData();
                Cursor c = getContentResolver().query(contactUri, null, null, null, null);
                if (c.moveToFirst()) {
                    String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    String hasNumber = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    String num = "";
                    if (Integer.valueOf(hasNumber) == 1) {
                        Cursor numbers = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                        while (numbers.moveToNext()) {
                            num = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                    }

                    //To convert the number into a 10 digit phone number
                    int len = num.length();
                    char[] chArray = new char[num.length()];
                    for(int i=0;i<len;i++){
                       chArray[i]=num.charAt(i);
                    }
                    char[] newChArray = new char[len];
                    int idx=-1;
                    for(int i=0;i<len;i++){
                        if(chArray[i]>='0' && chArray[i]<='9')
                            newChArray[++idx]=chArray[i];
                    }
                    String number = new String(newChArray);
                    number.trim();
                    if(idx>=9) {
                        contactNoReceived = number.substring(idx - 9, idx+1);
                        friendPhoneNoEditText.setText(null);
                        friendPhoneNoEditText.setText(contactNoReceived);
                    }else{
                        Toast.makeText(AddFriendActivity.this,
                                "Please select a valid 10 digit number.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }else {
                Toast.makeText(AddFriendActivity.this,
                        "Something went wrong.Please Try again",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts();
                // permission was granted, yay!

            } else {
                Toast.makeText(AddFriendActivity.this,
                        "Please grand the permission",
                        Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(AddFriendActivity.this,
                    "Something went wrong.\nPlease try again after sometimes.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void userWithPhoneNumber() {
        friendsInfoList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        usersReference.whereEqualTo("phoneNumber", friendPhoneNumber)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){

                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                FriendsInfo friendsInfo = new FriendsInfo();

                                friendsInfo.setFriendUserId(snapshot.getString("userId"));
                                friendsInfo.setFriendUserName(snapshot.getString("userName"));
                                friendsInfo.setFriendDpUrl(snapshot.getString("dpImgUrl"));


                                friendsInfoList.add(friendsInfo);
                            }

                            //Add this friend to Chat/userId/FriendLastMessage

                            if(friendsInfoList.size()!=0){
                                addFriendToUserDataBase();
                            }
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(AddFriendActivity.this,
                                    "There is no one with this phone number.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(AddFriendActivity.this,
                                "There is no one with this phone number.",
                                Toast.LENGTH_LONG).show();
                        //Log.d(TAG, "Failed "+e.getMessage());
                    }
                });
    }

    private void addFriendToUserDataBase() {

        UserApi userApi = UserApi.getInstance();

        FriendsInfo ownInfo = new FriendsInfo();
        ownInfo.setFriendUserId(userApi.getUserId());
        ownInfo.setFriendUserName(userApi.getUserName());
        ownInfo.setFriendDpUrl(userApi.getDpImgUrl());

        for( FriendsInfo friendsInfo : friendsInfoList) {

            String friendUserIdNew = friendsInfo.getFriendUserId();

            chatUserReference = db.collection("Chat")
                    .document(currentUser.getUid())
                    .collection("FriendLastMessage")
                    .document(friendUserIdNew);

            friendUserReference = db.collection("Chat")
                    .document(friendUserIdNew)
                    .collection("FriendLastMessage")
                    .document(currentUser.getUid());


            chatUserReference.set(friendsInfo);
            friendUserReference.set(ownInfo);

            progressBar.setVisibility(View.INVISIBLE);

            startActivity(new Intent(AddFriendActivity.this,
                    ChatListActivity.class));
        }
    }
}
