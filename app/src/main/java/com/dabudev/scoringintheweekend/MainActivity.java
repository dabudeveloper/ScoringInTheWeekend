package com.dabudev.scoringintheweekend;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    //create instance of the views
    private EditText mEditText1;
    private Button mButton1;
    private TextView mTextView1;
    private TextView mTextView2;


    private String mUsername;


    //resultcode
    public static final int RC_SIGN_IN = 1;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNumberDatabaseReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //Checks for messages in a JSON?
    private ChildEventListener mChildEventListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mNumberDatabaseReference = mFirebaseDatabase.getReference().child("phoneNumberAmount");


        //initialize the views
        mEditText1 = findViewById(R.id.Input1);
        mButton1 = findViewById(R.id.Button1);
        mTextView1 = findViewById(R.id.textView1);
        mTextView2 = findViewById(R.id.textView2);


        //enable button when input is given
        mEditText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mButton1.setEnabled(true);
                } else {
                    mButton1.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Only allow for number input to the EditTextBox
        mEditText1.setInputType(InputType.TYPE_CLASS_NUMBER);

        //Button sends a number and clears the edittextbox
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNumberDatabaseReference.push().setValue(Integer.parseInt(mEditText1.getText().toString()));

                mEditText1.setText("");
            }

        });

        //Set authenticate state listner
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user signed in

                    onSignedInInitialize(user.getDisplayName());
                } else {
                        //user signed out
                        onSignedOutCleanup();
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(Collections.singletonList(
                                                new AuthUI.IdpConfig.EmailBuilder().build()
                                        ))
                                        .build(),
                                RC_SIGN_IN);
                }
            }
        };

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String gebruiker =  user.getDisplayName();
        mTextView2.setText(gebruiker);
        Log.i("Test", "onCreate: " + gebruiker);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        //this makes sure that the activity is detroyed.

        detachtDatabaseReadListner();
    }

    private void onSignedInInitialize(String displayName) {
        mUsername = displayName;
        attachDatabaseReadListner();
    }

    private void onSignedOutCleanup() {
        mUsername = "ANONYMOUS";

        detachtDatabaseReadListner();
    }

    private void attachDatabaseReadListner() {
        if (mChildEventListner == null) {
            mChildEventListner = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //called when a new message is inserted
                    //also checks for existing messages when initiated
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // called when an existing message is changed
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                // called when an existing message is removed
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // called when an existing message changes its position
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                // called when an existing message is cancelled, eg error is occurred
                }
            };
            mNumberDatabaseReference.addChildEventListener(mChildEventListner);
        }
    }

    private void detachtDatabaseReadListner() {
        //make sure that mChildeventlistner is not null
        if (mChildEventListner != null) {
            mNumberDatabaseReference.removeEventListener(mChildEventListner);
            mChildEventListner = null;
        }
    }
}
