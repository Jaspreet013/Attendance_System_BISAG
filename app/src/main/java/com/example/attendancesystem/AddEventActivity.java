package com.example.attendancesystem;

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
import android.widget.ProgressBar;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class AddEventActivity extends AppCompatActivity {
    private EditText event_name,event_organisation;
    private DatabaseReference events,getUser;
    private User user=new User();
    private TextInputLayout border7,border8;
    private ProgressBar loading;
    private Event ev;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        event_name=findViewById(R.id.event_name);
        event_organisation=findViewById(R.id.event_organisation);
        button = findViewById(R.id.event_submit);
        border7=findViewById(R.id.border7);
        border8=findViewById(R.id.border8);
        loading=findViewById(R.id.check_attendance_progress);
        events = FirebaseDatabase.getInstance().getReference("Events");
        getUser=FirebaseDatabase.getInstance().getReference("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        getUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(AddEventActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else {
                    if (TextUtils.isEmpty(event_organisation.getText().toString().trim())) {
                        border8.setError("Organisation name cannot be left blank");
                    }
                    else{
                        border8.setError(null);
                    }
                    if (TextUtils.isEmpty(event_name.getText().toString().trim())) {
                        border7.setError("Event name cannot be left blank");
                    }
                    else{
                        border7.setError(null);
                    }
                    if(TextUtils.isEmpty(border7.getError()) && TextUtils.isEmpty(border8.getError()) && loading.getVisibility()==View.GONE){
                        try {
                            loading.setVisibility(View.VISIBLE);
                            events.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean set = true;
                                    ev = new Event(event_name.getText().toString().trim().toUpperCase(), event_organisation.getText().toString().trim().toUpperCase(),FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    if(!user.admin_events.isEmpty()) {
                                        for (String ev_key : user.admin_events.keySet()) {
                                            Event eve = dataSnapshot.child(user.admin_events.get(ev_key)).getValue(Event.class);
                                            if (eve.getName().equals(ev.getName()) && eve.getOrganisation().equals(ev.getOrganisation())) {
                                                set = false;
                                                break;
                                            }
                                        }
                                    }
                                    if(!set) {
                                        loading.setVisibility(View.GONE);
                                        Toast.makeText(AddEventActivity.this, "Your another Event with same name and organisation already exists", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        events.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String key;
                                                do {
                                                    key=generateRandomCode();
                                                }while(dataSnapshot.child(key).exists());
                                                events.child(key).setValue(ev);
                                                Date date=new Date();
                                                SimpleDateFormat datef=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                                                user.admin_events.put(datef.format(date),key);
                                                FirebaseDatabase.getInstance().getReference("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/admin_events").setValue(user.admin_events);
                                                Toast.makeText(AddEventActivity.this, "Event Added", Toast.LENGTH_SHORT).show();
                                                loading.setVisibility(View.GONE);
                                                setResult(RESULT_OK);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
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
            }
        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String generateRandomCode(){
        String givenCharacters="0123456789abcdefghijklmnopqrstuvxyz";
        StringBuilder builder;
        int limit=new Random().nextInt(10);
        if(limit<7){
            builder=new StringBuilder(7);
            for(int i=0;i<9;i++){
                builder.append(givenCharacters.charAt(new Random().nextInt(givenCharacters.length())));
            }
        }
        else{
            builder=new StringBuilder(6);
            for(int i=0;i<8;i++){
                builder.append(givenCharacters.charAt(new Random().nextInt(givenCharacters.length())));
            }
        }
        return builder.toString();
    }
}