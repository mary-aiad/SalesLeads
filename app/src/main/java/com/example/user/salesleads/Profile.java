package com.example.user.salesleads;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Profile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth firebaseAuth;
    public  FirebaseUser currentUser;
    private DatabaseReference reference;
    private String sharedName, email;

    private RecyclerView recyclerView;
    private ArrayList<Lead> listOfLeads;
    public Adaptor adaptor;

    private NavigationView navigationView;
    private TextView profileUserName, profileEmail;
    private ProgressDialog progressDialog;
    private Button button;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "No intenet connection", Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.dismiss();
            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                email = currentUser.getEmail();
            }


            reference = FirebaseDatabase.getInstance().getReference();

            sharedPreferences = getSharedPreferences("MyPref", 0);
            if (sharedPreferences != null) {
                sharedName = sharedPreferences.getString("userName", " ");
            }


            navigationView = (NavigationView) findViewById(R.id.nav_view);
            View v = navigationView.getHeaderView(0);
            profileUserName = (TextView) v.findViewById(R.id.profName);
            profileEmail = (TextView) v.findViewById(R.id.profile_email);
            profileUserName.setText(sharedName);
            profileEmail.setText(email);


            recyclerView = (RecyclerView) findViewById(R.id.show);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            listOfLeads = new ArrayList<>();

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_add);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Profile.this, AddLead.class);
                    startActivity(intent);
                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            ShowAllLeads();
        }
    }

    private void ShowAllLeads() {
        progressDialog.show();
        reference.child("leads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                show(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void show(DataSnapshot dataSnapshot) {
        listOfLeads = new ArrayList<>();
        for(DataSnapshot dSnap : dataSnapshot.getChildren()){
            Lead lead = new Lead();
            lead.setLeadName(dSnap.getValue(Lead.class).getLeadName());
            lead.setProduct(dSnap.getValue(Lead.class).getProduct());
            lead.setDate(dSnap.getValue(Lead.class).getDate());
            lead.setComment(dSnap.getValue(Lead.class).getComment());

            listOfLeads.add(lead);
        }
        progressDialog.dismiss();
        adaptor = new Adaptor(this, listOfLeads);
        recyclerView.setAdapter(adaptor);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            builder.setTitle("Log out Confirm")
                    .setMessage("Are you sure you need to log out ?!")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseAuth.getInstance().signOut();
                            currentUser = null;

                            startActivity(new Intent(Profile.this, Login.class));
                            finish();

                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
