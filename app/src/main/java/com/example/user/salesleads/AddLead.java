package com.example.user.salesleads;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddLead extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthentication;
    private DatabaseReference reference;

    private EditText leadName, commentL;
    private Spinner productL;
    private String date;
    private TextView addedDate;
    private int year, month, day;
    private SimpleDateFormat simpleDateFormat;
    private ProgressDialog progressDialog;
    private Button addLead ;

    private String currentUserID;
    private FirebaseUser currentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lead);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();


        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public  void  onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    currentUser = user;
              }
            }
        };

        leadName = (EditText) findViewById(R.id.lead_name);
        commentL = (EditText) findViewById(R.id.any_comment);
        productL = (Spinner) findViewById(R.id.interest);
        addedDate = (TextView) findViewById(R.id.choose_date);

        addLead = (Button) findViewById(R.id.btn_add);

        addLead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewLead();
            }
        });

        addedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentDate = Calendar.getInstance();
                year = currentDate.get(Calendar.YEAR);
                month = currentDate.get(Calendar.MONTH);
                day = currentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddLead.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, selectedYear);
                                calendar.set(Calendar.MONTH, selectedMonth);
                                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                                String myformat = "dd/MM/yy";
                                simpleDateFormat = new SimpleDateFormat(myformat, Locale.FRANCE);
                                addedDate.setText(simpleDateFormat.format(calendar.getTime()));
                                date = addedDate.getText().toString();

                                day = selectedDay;
                                month = selectedMonth;
                                year = selectedYear;
                            }
                        },year, month, day);
                datePickerDialog.show();
            }
        });
        }

    private void addNewLead(){

        progressDialog.show();

        String leadNameText = leadName.getText().toString();
        String commenText = commentL.getText().toString();
        String interestProduct = productL.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        if(leadNameText.isEmpty()){
            leadName.setError(getString(R.string.error_field_required));
            focusView = leadName;
            cancel = true;
        }
        if(interestProduct.isEmpty()){
            Toast.makeText(this, getString(R.string.error_interest_product),Toast.LENGTH_LONG).show();
            cancel = true;
        }
        if(addedDate.equals("Lead interest")){
            Toast.makeText(this, getString(R.string.error_date),Toast.LENGTH_LONG).show();
            cancel = true;
        }
        else if(cancel){
            focusView.requestFocus();
        }
        else{

            Lead lead = new Lead();
            lead.setLeadName(leadNameText);
            lead.setProduct(interestProduct);
            lead.setDate(date);
            lead.setComment(commenText);

            reference.child("leads").push().setValue(lead)
                    .addOnCompleteListener(AddLead.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(AddLead.this, "Successful added lead", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AddLead.this, Profile.class);
                                startActivity(intent);
                                finish();
                            } else{
                                progressDialog.dismiss();
                                Toast.makeText(AddLead.this, "Failed added", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(firebaseAuth != null) {
            firebaseAuth.addAuthStateListener(mAuthListener);
            currentUser = firebaseAuth.getCurrentUser();
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
        firebaseAuth.addAuthStateListener(mAuthListener);
        currentUser = firebaseAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(mAuthListener);
    }
}