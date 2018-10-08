package com.example.user.salesleads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sign_up extends AppCompatActivity {

    public static final String TAG = Sign_up.class.getSimpleName();
    private ProgressDialog progressDialog;

    private EditText password, firstName, lastName, mobile, userName, email, confirmPass;
    private Spinner jobTitle;
    private Button signUp;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    public DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        firstName = (EditText) findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);
        jobTitle = (Spinner) findViewById(R.id.job_title);
        mobile   = (EditText) findViewById(R.id.mobile);
        email = (EditText) findViewById(R.id.email);
        userName = (EditText) findViewById(R.id.user_name);
        password = (EditText) findViewById(R.id.password);
        confirmPass = (EditText) findViewById(R.id.confirm_pass);


        signUp = (Button) findViewById(R.id.sign_up_button);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

    }

    private void sendVerficationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(Sign_up.this, Login.class));
                            finish();
                        }
                        else
                        {
                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                    }
                });
    }

    private void signUp() {

        firstName.setError(null);
        lastName.setError(null);
        mobile.setError(null);
        email.setError(null);
        userName.setError(null);
        password.setError(null);

        final String fname = firstName.getText().toString();
        final String lname = lastName.getText().toString();
        final String mob = mobile.getText().toString();
        final String emailAddress = email.getText().toString();
        final String userN = userName.getText().toString();
        final String pass = password.getText().toString();
        final String job = jobTitle.getSelectedItem().toString();
        String confirmpass = confirmPass.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(fname.isEmpty())
        {
            firstName.setError(getString(R.string.error_field_required));
            focusView = firstName;
            cancel = true;
        }
        if(lname.isEmpty()){
            lastName.setError(getString(R.string.error_field_required));
            focusView = lastName;
            cancel = true;
        }
        if(mob.isEmpty()){
            mobile.setError(getString(R.string.error_field_required));
            focusView = mobile;
            cancel = true;
        }
        if(emailAddress.isEmpty()){
            email.setError(getString(R.string.error_field_required));
            focusView = email;
            cancel = true;
        }
        else if (!isEmailValid(emailAddress)) {
            email.setError(getString(R.string.error_invalid_email));
            focusView = email;
            cancel = true;
        }
        if(userN.isEmpty()){
            userName.setError(getString(R.string.error_field_required));
            focusView = userName;
            cancel = true;
        }
        else if(!isUserNameValid(userN)){
            focusView = userName;
            cancel = true;
        }
        if(pass.isEmpty()){
            password.setError(getString(R.string.error_field_required));
            focusView = password;
            cancel = true;
        }
        if(!pass.equals(confirmpass)){
            confirmPass.setError(getString(R.string.error_confirm_pass));
            focusView = confirmPass;
            cancel = true;
        }
        else if(!isPasswordValid(pass)){
            focusView = password;
            cancel = true;
        }
        if(job.equals("Job Title")){
            Toast.makeText(this, "please select job title ", Toast.LENGTH_SHORT).show();
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {

            //Check user name is not used

            progressDialog.show();
            final User test = new User();
            test.setMobile("");

            databaseReference.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(checkUserName(userN, dataSnapshot)){
                        progressDialog.dismiss();
                        Snackbar.make(signUp, "This user name already exist", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else {
                        auth.createUserWithEmailAndPassword(emailAddress, pass)
                                .addOnCompleteListener(Sign_up.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // saved data
                                             currentUser = auth.getCurrentUser();

                                            User newUser = new User();
                                            newUser.setFirstName(fname);
                                            newUser.setLastName(lname);
                                            newUser.setJobTitle(job);
                                            newUser.setEmail(emailAddress);
                                            newUser.setMobile(mob);
                                            newUser.setUserName(userN);
                                            newUser.setPassword(pass);

                                            databaseReference.child("users").child(currentUser.getUid()).setValue(newUser)
                                                    .addOnCompleteListener(Sign_up.this, new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(Sign_up.this, "Successful sign-up", Toast.LENGTH_SHORT).show();
                                                                sendVerficationEmail();

                                                                SharedPreferences sharedPreferences = getApplicationContext().
                                                                        getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                editor.putString("userName", userN);
                                                                editor.putString("currentUserId", currentUser.getUid());
                                                                editor.apply();

                                                                Intent intent = new Intent(getApplicationContext(), Profile.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(Sign_up.this, "Failed sign up", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(Sign_up.this, "Authentication failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

        }
    }

    private Boolean checkUserName(String userN, DataSnapshot dataSnapshot) {
        for (DataSnapshot dSnap : dataSnapshot.getChildren()) {
            User user = new User();
            user.setUserName(dSnap.getValue(User.class).getUserName());
            if (userN.equals(user.getUserName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Sign_up.this, Login.class);
        startActivity(intent);
        finish();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String pass) {
        if (pass.length() < 8) {
            password.setError(getString(R.string.error_short_password));
            return false;
        } else if (pass.equals(pass.toLowerCase())  || pass.equals(pass.toUpperCase())
                || !pass.matches(".*\\d+.*")) {
            password.setError(getString(R.string.error_invalid_password));
            return false;
        }
        return true;
    }

    private boolean isUserNameValid(String name) {
        Pattern pattern = Pattern.compile("[^A-Z a-z 0-9]");
        Matcher matcher = pattern.matcher(name);
        if(matcher.find() || name.contains(" ")){
            userName.setError(getString(R.string.error_invalid_user_name));
            return false;
        }
        else
            return true;
    }

}

