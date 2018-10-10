package com.example.user.salesleads;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditLead extends AppCompatActivity {

    private EditText name, commentL;
    private String lName, product, date, comm;
    private Spinner productL;
    private ProgressDialog progressDialog;
    private TextView addedDate;
    private int year, month, day;
    private SimpleDateFormat simpleDateFormat;
    private Lead oldData;


    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private int indexOfEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lead);

        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        Intent intent = getIntent();
        lName = intent.getStringExtra("name");
        product = intent.getStringExtra("product");
        date = intent.getStringExtra("date");
        comm = intent.getStringExtra("comment");
        indexOfEdit = intent.getIntExtra("id", 0);

        oldData = new Lead();
        oldData.setDate(date);
        oldData.setProduct(product);
        oldData.setLeadName(lName);
        oldData.setComment(comm);

        name = (EditText) findViewById(R.id.edit_lead_name);
        addedDate = (TextView) findViewById(R.id.added_date);
        commentL = (EditText) findViewById(R.id.comment);
        productL = (Spinner) findViewById(R.id.products);

        name.setText(lName);
        addedDate.setText(date);
        commentL.setText(comm);

        addedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentDate = Calendar.getInstance();
                year = currentDate.get(Calendar.YEAR);
                month = currentDate.get(Calendar.MONTH);
                day = currentDate.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditLead.this,
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

    public void edit(View v) {

        progressDialog.show();
        reference.child("leads").orderByChild("leadName").equalTo(lName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Lead lead = new Lead();
                        lead.setLeadName(name.getText().toString());
                        lead.setProduct(productL.getSelectedItem().toString());
                        lead.setDate(date);
                        lead.setComment(commentL.getText().toString());
                        dataSnapshot.getRef().push().setValue(lead);
                        update();
                        progressDialog.dismiss();
                        Toast.makeText(EditLead.this, "Edit Done", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), Profile.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(EditLead.this, "Edit Failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void update() {

        Query applesQuery = reference.child("leads").orderByChild("leadName").equalTo(oldData.getLeadName());
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    if(appleSnapshot.getValue(Lead.class).getDate().equals(oldData.getDate())
                            && appleSnapshot.getValue(Lead.class).getProduct().equals(oldData.getProduct())
                            && appleSnapshot.getValue(Lead.class).getComment().equals(oldData.getComment())){
                        appleSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Delete failes " + oldData.getLeadName(), Toast.LENGTH_LONG).show();
            }
        });

    }
}
