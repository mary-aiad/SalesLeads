package com.example.user.salesleads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private FirebaseUser loginUser;

    private EditText userName;
    private EditText password;
    private ProgressDialog progressDialog;
    private Button signUp;

    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        } else {
            firebaseAuth = FirebaseAuth.getInstance();
            reference = FirebaseDatabase.getInstance().getReference();

            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        loginUser = user;
                        Toast.makeText(getApplicationContext(), "You are already Logged in ", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, Profile.class);
                        startActivity(intent);
                        finish();
                    }
                }
            };

            userName = (EditText) findViewById(R.id.user_name);
            password = (EditText) findViewById(R.id.password);
            Button signInButton = (Button) findViewById(R.id.sign_in_button);
            signUp = (Button) findViewById(R.id.new_user);

            signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Login.this, Sign_up.class);
                    startActivity(intent);
                    finish();
                }
            });

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");

            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }
    }

    private void attemptLogin() {

        userName.setError(null);
        password.setError(null);

        final String name = userName.getText().toString().trim();
        final String pass = password.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(pass)) {
            password.setError(getString(R.string.error_field_required));
            focusView = password;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            userName.setError(getString(R.string.error_field_required));
            focusView = userName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            progressDialog.show();
            checkLogin(name, pass);
        }
    }

    private void checkLogin(final String userName, final String pass) {
        reference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dSnap : dataSnapshot.getChildren()){

                    User user = new User();
                    user.setUserName(dSnap.getValue(User.class).getUserName());
                    user.setPassword(dSnap.getValue(User.class).getPassword());

                    if(userName.equals(user.getUserName()) && pass.equals(user.getPassword())){
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, "Login Successful ", Toast.LENGTH_LONG).show();

                        SharedPreferences sharedPreferences = getApplicationContext().
                                getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userName", userName);
                        editor.apply();

                        Intent intent = new Intent(Login.this, Profile.class);
                        startActivity(intent);
                        finish();
                    }
                }
                progressDialog.dismiss();
                Toast.makeText(Login.this, "Invalid user name or password", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

        @Override
        public void onStart() {
            super.onStart();
            if(firebaseAuth != null) {
                firebaseAuth.addAuthStateListener(mAuthListener);
                loginUser = firebaseAuth.getCurrentUser();
            }
        }

        @Override
        protected void onResume () {
            super.onResume();
            firebaseAuth.addAuthStateListener(mAuthListener);
            loginUser = firebaseAuth.getCurrentUser();
        }

        @Override
        protected void onStop() {
            super.onStop();
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }

    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
