package com.example.attendancesystem;

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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class AddEventActivity extends AppCompatActivity {
    EditText event_name;
    EditText event_organisation;
    Button button;
    String key;
    SharedPreferences preferences;
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
        key=preferences.getString("Key","");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(AddEventActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(event_organisation.getText().toString().trim()) || TextUtils.isEmpty(event_name.getText().toString().trim())){
                    Toast.makeText(AddEventActivity.this,"Please fill all details properly",Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        progressDialog=new ProgressDialog(AddEventActivity.this);
                        progressDialog.setMessage("Please Wait");
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        databaseReference = FirebaseDatabase.getInstance().getReference("events/"+key);
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                boolean set=true;
                                event ev=new event(event_name.getText().toString().trim().toUpperCase(),event_organisation.getText().toString().trim().toUpperCase());
                                for(DataSnapshot child:children){
                                    event eve=child.getValue(event.class);
                                    if(eve.getName().equals(ev.getName()) && eve.getOrganisation().equals(ev.getOrganisation())){
                                        set=false;
                                    }
                                }
                                if(!set){
                                    progressDialog.dismiss();
                                    Toast.makeText(AddEventActivity.this,"Your another event with same name and organisation already exists",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    SharedPreferences prefs = getSharedPreferences("All users",MODE_PRIVATE);
                                    Gson gson = new Gson();
                                    String json = prefs.getString("users", null);
                                    Type type = new TypeToken<ArrayList<Person>>() {}.getType();
                                    ArrayList<Person> person=gson.fromJson(json, type);
                                    if(person!=null){
                                        for(Person temp:person) {
                                            DatabaseReference dbreference=FirebaseDatabase.getInstance().getReference("Persons/"+key);
                                            String key = dbreference.push().getKey();
                                            temp.setAttendance(0);
                                            temp.setAttendance_total(0);
                                            temp.dates.clear();
                                            temp.setEvent_name(event_name.getText().toString().trim().toUpperCase());
                                            temp.setOrganisation(event_organisation.getText().toString().trim().toUpperCase());
                                            dbreference.child(key).setValue(temp);
                                        }
                                        SharedPreferences.Editor edit=prefs.edit();
                                        edit.remove("users");
                                        edit.clear();
                                        edit.apply();
                                    }
                                    Toast.makeText(AddEventActivity.this,"Event Added",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    String key=databaseReference.push().getKey();
                                    databaseReference.child(key).setValue(ev);
                                    finish();
                                }
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

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = getSharedPreferences("All users",MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        edit.remove("users");
        edit.clear();
        edit.apply();
        super.onBackPressed();
    }
}