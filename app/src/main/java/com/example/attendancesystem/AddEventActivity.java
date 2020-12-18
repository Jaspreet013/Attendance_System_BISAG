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
import com.google.firebase.auth.FirebaseAuth;
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
    private EditText event_name,event_organisation;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        event_name=findViewById(R.id.event_name);
        event_organisation=findViewById(R.id.event_organisation);
        Button button=findViewById(R.id.event_submit);
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
                        databaseReference = FirebaseDatabase.getInstance().getReference("Events/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                boolean set=true;
                                Event ev=new Event(event_name.getText().toString().trim().toUpperCase(),event_organisation.getText().toString().trim().toUpperCase());
                                for(DataSnapshot child:children){
                                    Event eve=child.getValue(Event.class);
                                    if(eve.getName().equals(ev.getName()) && eve.getOrganisation().equals(ev.getOrganisation())){
                                        set=false;
                                    }
                                }
                                if(!set){
                                    progressDialog.dismiss();
                                    Toast.makeText(AddEventActivity.this,"Your another Event with same name and organisation already exists",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    String key=databaseReference.push().getKey();
                                    databaseReference.child(key).setValue(ev);
                                    if(getIntent().getExtras()!=null){
                                        Type type=new TypeToken<ArrayList<Person>>(){}.getType();
                                        ArrayList<Person> person=new Gson().fromJson(getIntent().getStringExtra("People"),type);
                                        for(Person temp:person) {
                                            DatabaseReference dbreference=FirebaseDatabase.getInstance().getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+key);
                                            String push_key = dbreference.push().getKey();
                                            temp.setAttendance(0);
                                            temp.setAttendance_total(0);
                                            temp.dates.clear();
                                            dbreference.child(push_key).setValue(temp);
                                        }
                                    }
                                    Toast.makeText(AddEventActivity.this,"Event Added",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
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
}