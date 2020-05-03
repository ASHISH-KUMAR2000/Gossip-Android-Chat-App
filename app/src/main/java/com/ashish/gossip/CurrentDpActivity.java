package com.ashish.gossip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ashish.gossip.model.User;
import com.ashish.gossip.ui.UserApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class CurrentDpActivity extends AppCompatActivity implements View.OnClickListener {

    //For intent to open gallery
    private static final int REQUEST_CODE = 1;
    //private static final String TAG = "CurrentDpActivity";

    private ImageView addPhotoButton, dpImage;
    private ProgressBar progressBar;
    private Button changeDpButton;
    private Uri imageUri;
    private String imageUrl;

    //FireBase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //For saving dp at firebase storage
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();



    //For database Users/" "/imageUrl
    //Connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_dp);

        addPhotoButton = findViewById(R.id.currentDp_addPhoto_imageViewButton);
        changeDpButton = findViewById(R.id.currentDp_change_button);
        dpImage = findViewById(R.id.currentDp_dp_imageView);
        progressBar  = findViewById(R.id.currentAcc_progressBar);

        firebaseAuth = FirebaseAuth.getInstance();

        addPhotoButton.setOnClickListener(this);
        changeDpButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.currentDp_addPhoto_imageViewButton:
                //Open Gallery

                try{
                    if (ActivityCompat.checkSelfPermission(CurrentDpActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(CurrentDpActivity.this,
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                        startActivityForResult(galleryIntent, REQUEST_CODE);
                        progressBar.setVisibility(View.VISIBLE);
                        Picasso.get()
                                .load(imageUri)
                                .fit()
                                .centerCrop()
                                .into(dpImage);
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                } catch (Exception e){
                    //Log.d(TAG, e.getMessage());
                }
                break;
            case R.id.currentDp_change_button:
                //Upload image to database storage
                //change in database at Users/"current user"/dpImgUrl

                //Upload dp on firebase storage
                uploadDp();
                break;
        }
    }

    private void uploadDp() {

        progressBar.setVisibility(View.VISIBLE);

        final UserApi userApi = UserApi.getInstance();//Global Api

        if(imageUri == null && userApi.getDpImgUrl() == null) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(CurrentDpActivity.this,
                    "Please add a dp",
                    Toast.LENGTH_SHORT
                    ).show();
        }
        else if (imageUri == null && userApi.getDpImgUrl() != null){
            startActivity(new Intent(CurrentDpActivity.this,
                    FriendChatRecord.class));
        }
        else {
            final StorageReference filepath = storageReference
                    .child("users_dp")
                    .child("image"+userApi.getUserId());
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    imageUrl = uri.toString();
                                    //Add User to database
                                    if(userApi.getDpImgUrl()==null) {
                                        addUserToDatabase();
                                        userApi.setDpImgUrl(imageUrl);
                                    }
                                    else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        userApi.setDpImgUrl(imageUrl);
                                        startActivity(new Intent(CurrentDpActivity.this,
                                                ChatListActivity.class));
                                    }

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CurrentDpActivity.this,
                                    "Please check your Internet Connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    public void addUserToDatabase() {

        currentUser = firebaseAuth.getCurrentUser();

        UserApi userApi = UserApi.getInstance();
        userApi.setDpImgUrl(imageUrl);


        final String currentUserId = currentUser.getUid();

        User userObject = new User();
        userObject.setUserId(currentUserId);
        userObject.setUserName(userApi.getUserName());
        userObject.setPhoneNumber(userApi.getPhoneNumber());
        userObject.setDpImgUrl(userApi.getDpImgUrl());

        collectionReference.add(userObject)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(Objects.requireNonNull(task).getResult().exists()) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            startActivity(new Intent(CurrentDpActivity.this,
                                                    ChatListActivity.class));
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CurrentDpActivity.this,
                                "Please check your Internet connection.\nOr try again after sometime.",
                                Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();//get the path of image
                //Log.d(TAG, " "+imageUri);
                progressBar.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(imageUri)
                        .fit()
                        .centerCrop()
                        .into(dpImage);
                progressBar.setVisibility(View.INVISIBLE);
            }
            else {
                Toast.makeText(CurrentDpActivity.this,
                        "Something went wrong.Please Try again",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
                if(UserApi.getInstance().getDpImgUrl() != null && imageUri == null){
                    String imgUrl = UserApi.getInstance().getDpImgUrl();

                    //Use Picasso library to download and show image
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.get()
                            .load(imgUrl)
                            .fit()
                            .centerCrop()
                            .into(dpImage);
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.get()
                            .load(imageUri)
                            .fit()
                            .centerCrop()
                            .into(dpImage);
                    progressBar.setVisibility(View.INVISIBLE);
                }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            //If request is cancelled, the result array are empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_CODE);
            } else {
                //do something like displaying a message that he did not allow the app to access gallery and you wont be able to let him select from gallery
                Toast.makeText(CurrentDpActivity.this,
                        "Please allow the permission.\nTo set the Dp.",
                        Toast.LENGTH_LONG).show();
            }
        }

        onResume();
    }
}
