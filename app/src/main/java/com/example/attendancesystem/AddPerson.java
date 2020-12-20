package com.example.attendancesystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddPerson extends AppCompatActivity {
    private EditText fname,lname,email,id;
    private boolean set=true;
    private String event_key;
    private TextInputLayout border2,border3,border4,border5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fname=findViewById(R.id.register_fname);
        lname=findViewById(R.id.register_lname);
        email=findViewById(R.id.register_email);
        id=findViewById(R.id.register_id);
        event_key=getIntent().getStringExtra("Key");
        Button submit=findViewById(R.id.register_submit);
        Button clear=findViewById(R.id.register_clear);
        border2=findViewById(R.id.border2);
        border3=findViewById(R.id.border3);
        border4=findViewById(R.id.border4);
        border5=findViewById(R.id.border5);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fname.getText().clear();
                lname.getText().clear();
                email.getText().clear();
                id.getText().clear();
                border2.setError(null);
                border3.setError(null);
                border4.setError(null);
                border5.setError(null);
                fname.requestFocus();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set=true;
                if (!isNetworkAvailable()) {
                    Toast.makeText(AddPerson.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else {
                    if (TextUtils.isEmpty(fname.getText().toString().trim())) {
                        border2.setError("First name cannot be left blank");
                    } else if (!fname.getText().toString().trim().matches("^[a-zA-Z]*$")) {
                        border2.setError("Please enter a proper first name");
                    } else {
                        border2.setError(null);
                    }
                    if (TextUtils.isEmpty(lname.getText().toString().trim())) {
                        border3.setError("Last name cannot be left blank");
                    } else if (!lname.getText().toString().trim().matches("^[a-zA-Z]*$")) {
                        border3.setError("Please enter a proper last name");
                    } else {
                        border3.setError(null);
                    }
                    if (email.getText().toString().isEmpty()) {
                        border4.setError("Email cannot be left blank");
                    } else if (!email.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")) {
                        border4.setError("Please enter a proper Email");
                    } else {
                        border4.setError(null);
                    }
                    if (id.getText().toString().trim().isEmpty()) {
                        border5.setError("ID cannot be left blank");
                    } else {
                        border5.setError(null);
                    }
                    if (TextUtils.isEmpty(border2.getError()) && TextUtils.isEmpty(border3.getError()) && TextUtils.isEmpty(border4.getError()) && TextUtils.isEmpty((border5.getError()))) {
                        final ProgressDialog progressDialog = new ProgressDialog(AddPerson.this);
                        progressDialog.setMessage("Please Wait");
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        try {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference databaseReference = database.getReference("People/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + event_key);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                    for (DataSnapshot child : children) {
                                        Person person = child.getValue(Person.class);
                                        if(person.getPerson_email().equals(email.getText().toString().trim()) && person.getPerson_ID().equals(id.getText().toString().trim())){
                                            set = false;
                                            progressDialog.dismiss();
                                            border4.setError("This email is already registered to this Event");
                                            border5.setError("This ID is already registered to this Event");
                                            break;
                                        }
                                        if (person.getPerson_email().equals(email.getText().toString().trim())) {
                                            set = false;
                                            progressDialog.dismiss();
                                            border4.setError("This email is already registered to this Event");
                                            break;
                                        }
                                        if (person.getPerson_ID().equals(id.getText().toString().trim())) {
                                            set = false;
                                            progressDialog.dismiss();
                                            border5.setError("This ID is already registered to this Event");
                                            break;
                                        }
                                    }
                                    if (set) {
                                        String key = databaseReference.push().getKey();
                                        databaseReference.child(key).setValue(new Person(fname.getText().toString().trim(), lname.getText().toString().trim(), email.getText().toString().trim(), id.getText().toString().trim()));
                                        progressDialog.dismiss();
                                        Toast.makeText(AddPerson.this, "Person successfully added to the Event", Toast.LENGTH_SHORT).show();
                                        fname.getText().clear();
                                        lname.getText().clear();
                                        email.getText().clear();
                                        id.getText().clear();
                                        fname.requestFocus();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } catch (Exception ex) {
                            Log.e("Exception", ex.getMessage());
                        }
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