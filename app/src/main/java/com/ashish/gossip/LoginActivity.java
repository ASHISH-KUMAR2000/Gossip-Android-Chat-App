package com.ashish.gossip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaDrm;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ashish.gossip.ui.UserApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText emailIdEditView, passwordEditView;
    ProgressBar progressBar;
    Button loginButton, createAccountButton;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = db.collection("Users");
    //private final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailIdEditView = findViewById(R.id.loginActivity_email_editText);
        passwordEditView = findViewById(R.id.loginActivity_userPassword_editText);
        progressBar = findViewById(R.id.loginActivity_progressBar);
        loginButton = findViewById(R.id.loginActivity_login_button);
        createAccountButton = findViewById((R.id.loginActivity_createAccount_button));

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(this);
        createAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginActivity_login_button:
                //Authenticate and goto ChatListActivity

                String email = emailIdEditView.getText().toString().trim();
                String password = passwordEditView.getText().toString().trim();
                loginEmailPassword(email, password);

                break;
            case R.id.loginActivity_createAccount_button:
                //Goto CreateAccountActivity
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                break;
        }
    }

    private void loginEmailPassword(String email, final String password) {

        if(!TextUtils.isEmpty(email)
                &&!TextUtils.isEmpty(password)) {

            //Log.d(TAG, email+" "+password);
            progressBar.setVisibility(View.VISIBLE);


            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                currentUser = firebaseAuth.getCurrentUser();
                                final String currentUserId = currentUser.getUid();

                                collectionReference.whereEqualTo("userId", currentUserId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                                @Nullable FirebaseFirestoreException e) {

                                                //Error occurred
                                                if (e != null) {
                                                 //   Log.d(TAG, e.getMessage());
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(LoginActivity.this,
                                                            "Something went wrong.\nPlease try after some time.",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    if (!queryDocumentSnapshots.isEmpty()) {

                                                        for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {

                                                            UserApi userApi = UserApi.getInstance();
                                                            userApi.setUserId(currentUserId);
                                                            userApi.setUserName(snapshots.getString("userName"));
                                                            userApi.setPhoneNumber(snapshots.getString("phoneNumber"));
                                                            userApi.setDpImgUrl(snapshots.getString("dpImgUrl"));
                                                            //progressBar.setVisibility(View.INVISIBLE);


                                                        }
                                                        progressBar.setVisibility(View.INVISIBLE);

                                                        startActivity(new Intent(LoginActivity.this,
                                                                ChatListActivity.class));
                                                        finish();
                                                    } else {
                                                        //Log.d(TAG, "snapshot is empty");
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this,
                                        "Check your internet connection",
                                        Toast.LENGTH_LONG).show();

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this,
                                    "Enter a valid email and password.",
                                    Toast.LENGTH_LONG).show();
                            //Log.d(TAG, "Failed "+e.getMessage());
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this,
                    "Empty Text Field Not Allowed",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
