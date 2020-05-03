package com.ashish.gossip.helper;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ashish.gossip.FriendChatRecord;
import com.ashish.gossip.model.ChatMessage;
import com.ashish.gossip.model.FriendsInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UploadInfoToFriendDb {

    public UploadInfoToFriendDb(){}

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference;
    DocumentReference documentReference;
    public void uploadChatMessage(final Context ctx, String userId, String friendUserId, ChatMessage mess) {
        collectionReference = db.collection("Chat")
                .document(friendUserId)
                .collection("FriendChatRecord")
                .document(userId)
                .collection("ChatHistory");
        
        collectionReference.add(mess)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ctx,
                                "Check Internet Connection",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void UploadLastMessage(final Context ctx, String userId, String friendUserId, FriendsInfo lastChatMessage) {
        documentReference = db.collection("Chat")
                .document(friendUserId)
                .collection("FriendLastMessage")
                .document(userId);

        documentReference.set(lastChatMessage)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ctx,
                                "Please Check Your Internet Connection.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
