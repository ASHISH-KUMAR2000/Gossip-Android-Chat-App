package com.ashish.gossip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ashish.gossip.model.User;
import com.ashish.gossip.ui.UserApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.CreateDocumentRequest;

import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userNameEditText, emailIdEditText, passwordEditText, phNoEditText;
    private Button createAccountButton;
    private ProgressBar progressBar;

    //FireBase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;


    //Firestore Connection
    private FirebaseFirestore db =FirebaseFirestore.getInstance();
    //private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        userNameEditText = findViewById(R.id.createAcc_username_editText);
        emailIdEditText = findViewById(R.id.createAcc_email_editText);
        passwordEditText = findViewById(R.id.createAcc_password_editText);
        phNoEditText = findViewById(R.id.createAcc_phNo_editText);
        createAccountButton = findViewById(R.id.createAcc_createAcc_button);
        progressBar = findViewById(R.id.createAcc_progressBar);

        firebaseAuth = FirebaseAuth.getInstance();

        createAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String userName, emailId, password, phNo;

        userName = userNameEditText.getText().toString().trim();
        emailId = emailIdEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        phNo = phNoEditText.getText().toString().trim();

        if(!TextUtils.isEmpty(userName)&&
        !TextUtils.isEmpty(emailId)&&
        !TextUtils.isEmpty(password)&&
        !TextUtils.isEmpty(phNo)) {
            createAccount(userName, emailId, password, phNo);
        } else {
            Toast.makeText(CreateAccountActivity.this,
                    "Empty fields are not allowed",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void createAccount(final String userName, final String emailId, String password, final String phNo) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(emailId, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            currentUser = firebaseAuth.getCurrentUser();
                            UserApi userApi = UserApi.getInstance();

                            userApi.setUserId(currentUser.getUid());
                            userApi.setUserName(userName);
                            userApi.setPhoneNumber(phNo);

                            startActivity(new Intent(CreateAccountActivity.this,
                                    CurrentDpActivity.class));
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CreateAccountActivity.this,
                                "Try another email Id.\nThis email is already taken.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
