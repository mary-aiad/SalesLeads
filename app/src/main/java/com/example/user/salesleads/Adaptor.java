package com.example.user.salesleads;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

class Adaptor extends RecyclerView.Adapter<Adaptor.ViewHolder> {

    private Context context;
    ArrayList<Lead> leads = new ArrayList<>();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    public DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


    public Adaptor(Context context, ArrayList <Lead> leadArrayList){
        this.context = context;
        this.leads = leadArrayList;
    }

    public Adaptor(Context context){
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recycler_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final Adaptor.ViewHolder viewHolder, final int position) {
        final Lead lead = leads.get(position);
        viewHolder.name.setText(lead.getLeadName());
        viewHolder.product.setText(lead.getProduct());
        viewHolder.leadDate.setText(lead.getDate());

        viewHolder.edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditLead.class);
                intent.putExtra("index", viewHolder.getAdapterPosition());
                intent.putExtra("name", lead.getLeadName());
                intent.putExtra("product", lead.getProduct());
                intent.putExtra("date", lead.getDate());
                intent.putExtra("comment", lead.getComment());
                context.startActivity(intent);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                builder.setTitle("Delete Confirmation")
                        .setMessage("Are you sure you need to delete this entery?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Query applesQuery = databaseReference.child("leads").orderByChild("leadName").equalTo(lead.getLeadName());
                                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                            appleSnapshot.getRef().removeValue();
                                            Toast.makeText(context, "Done Deleted " + lead.getLeadName(), Toast.LENGTH_LONG).show();
                                            }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(context, "Delete failes " + lead.getLeadName(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(leads == null){
            System.err.println("error");
            return 0;
        }
        else
            return leads.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, product, leadDate, edit, delete;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.lead_name);
            product = (TextView) itemView.findViewById(R.id.product_choosen);
            leadDate = (TextView) itemView.findViewById(R.id.show_date);
            edit = (TextView) itemView.findViewById(R.id.edit);
            delete = (TextView) itemView.findViewById(R.id.delete_lead);
        }
    }
}
