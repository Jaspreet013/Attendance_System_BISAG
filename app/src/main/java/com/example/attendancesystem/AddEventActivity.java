package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class AddEventActivity extends AppCompatActivity {
    boolean set=true;
    EditText event_name;
    EditText event_organisation;
    Button button;
    SharedPreferences preferences;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        event_name=findViewById(R.id.event_name);
        event_organisation=findViewById(R.id.event_organisation);
        button=findViewById(R.id.event_submit);
        preferences=getSharedPreferences("User",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=preferences.getString("Current User","");
        final User current_user=gson.fromJson(json,User.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = event_name.getText().toString().toLowerCase();
                if(!isNetworkAvailable()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(TextUtils.isEmpty(event_organisation.getText().toString()) || TextUtils.isEmpty(event_name.getText().toString())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                    builder.setTitle("Please fill up all details properly");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                /*else if(!event_name.getText().toString().trim().matches("^[a-zA-Z]*$") || !event_organisation.getText().toString().trim().matches("^[a-zA-Z]*$")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                    builder.setTitle("Alphabets Only");
                    builder.setMessage("You cannot fill up any other letter except for alphabets");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }*/
                else {
                    try {
                        final event ev=new event(event_name.getText().toString().toUpperCase(),event_organisation.getText().toString().toUpperCase(),current_user.getEmail());
                        progressDialog=new ProgressDialog(AddEventActivity.this);
                        progressDialog.setMessage("Please Wait");
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        databaseReference = FirebaseDatabase.getInstance().getReference("events");
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                for(DataSnapshot child:children){
                                    event eve=child.getValue(event.class);
                                    if(eve.getName().equals(ev.getName()) && eve.getOrganisation().equals(ev.getOrganisation()) && eve.getCoordinator_email().equals(eve.getCoordinator_email())){
                                        set=false;
                                    }
                                    /*if(!dataSnapshot.exists()) {
                                        DatabaseReference dbreference=FirebaseDatabase.getInstance().getReference("events");
                                        String key=databaseReference.push().getKey();
                                        //Log.e("String : ",current_user.getEmail().replaceAll(".",""));
                                        dbreference.child(key).setValue(ev);
                                        firebaseAuth.signOut();
                                        progressDialog.dismiss();
                                        firebaseAuth.signOut();
                                        Toast.makeText(AddEventActivity.this, "Event Registered Successfully", Toast.LENGTH_SHORT).show();
                                        set=false;
                                        break;
                                    }*/
                                }
                                if(!set){
                                    progressDialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                                    builder.setTitle("Event Already Exists");
                                    builder.setMessage("Your another event with same name and organisation already exists");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Ok", null);
                                    builder.show();
                                    firebaseAuth.signOut();
                                }
                                else{
                                    progressDialog.dismiss();
                                    String key=databaseReference.push().getKey();
                                    databaseReference.child(key).setValue(ev);
                                    finish();
                                    //startActivity(new Intent(AddEventActivity.this,HomeActivity.class));
                                }
                                    /*boolean set = true;
                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                    for (DataSnapshot child : children) {
                                        event ev = child.getValue(event.class);
                                        if (ev.getCoordinator_email().equals(current_user.getEmail()) && ev.getName().equals(event_name.getText().toString())) {
                                            progressDialog.dismiss();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                                            set = false;
                                            builder.setTitle("Event Already Exists");
                                            builder.setMessage("Your another event with same name and organisation already exists");
                                            builder.setCancelable(false);
                                            builder.setPositiveButton("Ok", null);
                                            builder.show();
                                            firebaseAuth.signOut();
                                            break;
                                        }
                                    }
                                    if (set) {
                                        progressDialog.dismiss();
                                        databaseReference.child(event_name.getText().toString()+", "+current_user.getEmail().replaceAll(".","")).setValue(ev);
                                        firebaseAuth.signOut();
                                        Toast.makeText(AddEventActivity.this, "Event Registered Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                        startActivity(new Intent(AddEventActivity.this,HomeActivity.class));
                                    }*/
                                }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } catch (Exception e) {
                        Log.e("Error : ", e.getMessage());
                    }
                }
            }
        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
