package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AddPerson extends AppCompatActivity {
    event current_event;
    EditText fname;
    EditText lname;
    EditText email;
    EditText id;
    User current_user;
    boolean set=true;
    SharedPreferences preferences;
    SharedPreferences get_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fname=findViewById(R.id.register_fname);
        lname=findViewById(R.id.register_lname);
        email=findViewById(R.id.register_email);
        id=findViewById(R.id.register_id);
        Button submit=findViewById(R.id.register_submit);
        Button clear=findViewById(R.id.register_clear);
        preferences=getSharedPreferences("Events",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("Current event", "");
        current_event = gson.fromJson(json, event.class);
        Gson gson1=new Gson();
        get_user=getSharedPreferences("User",MODE_PRIVATE);
        String json1=get_user.getString("Current User","");
        current_user=gson1.fromJson(json1,User.class);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fname.getText().clear();
                lname.getText().clear();
                email.getText().clear();
                id.getText().clear();
                fname.requestFocus();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set=true;
                if (!isNetworkAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPerson.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if (TextUtils.isEmpty(fname.getText().toString()) || TextUtils.isEmpty(lname.getText().toString()) ||
                        TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(id.getText().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPerson.this);
                    builder.setTitle("Please fill up all details properly");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(!(fname.getText().toString().matches("^[a-zA-Z]*$")) || !(lname.getText().toString().matches("^[a-zA-Z]*$"))){
                    AlertDialog.Builder builder=new AlertDialog.Builder(AddPerson.this);
                    builder.setTitle("Please provide a proper name");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(!email.getText().toString().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                    AlertDialog.Builder builder=new AlertDialog.Builder(AddPerson.this);
                    builder.setTitle("Please provide a valid email");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else{
                    final ProgressDialog progressDialog=new ProgressDialog(AddPerson.this);
                    progressDialog.setMessage("Please Wait");
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    try {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference databaseReference = database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                AlertDialog.Builder builder=new AlertDialog.Builder(AddPerson.this);
                                for(DataSnapshot child:children){
                                    Person person=child.getValue(Person.class);
                                    if(person.getOrganisation().equals(current_event.getOrganisation()) &&
                                            person.getPerson_email().equals(email.getText().toString()) && person.getEvent_name().equals(current_event.getName().toUpperCase())){
                                        set=false;
                                        builder.setTitle("This email is already registered to this event");
                                    }
                                    if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && person.getPerson_ID().equals(id.getText().toString())){
                                        set=false;
                                        builder.setTitle("This ID is already registered to this event");
                                    }
                                }
                                if(set) {
                                    String key = databaseReference.push().getKey();
                                    databaseReference.child(key).setValue(new Person(fname.getText().toString(), lname.getText().toString(), email.getText().toString(), id.getText().toString(), current_event.getName(), current_event.getOrganisation()));
                                    progressDialog.dismiss();
                                    builder.setTitle("Person Added");
                                    fname.getText().clear();
                                    lname.getText().clear();
                                    email.getText().clear();
                                    id.getText().clear();
                                    fname.requestFocus();
                                }
                                else{
                                    progressDialog.dismiss();
                                }
                                builder.setCancelable(false);
                                builder.setPositiveButton("Ok",null);
                                builder.show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } catch (Exception ex) {
                        Log.e("Super exception",ex.getMessage());
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
