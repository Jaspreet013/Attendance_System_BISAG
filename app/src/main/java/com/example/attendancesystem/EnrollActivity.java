package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EnrollActivity extends AppCompatActivity {
    private Person new_person;
    private EditText code,id;
    private TextInputLayout border7,border8;
    private Button enroll;
    private DatabaseReference people,events,users;
    private ProgressBar bar;
    private boolean set;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        code=findViewById(R.id.event_code);
        id=findViewById(R.id.event_id);
        border7=findViewById(R.id.border7);
        border8=findViewById(R.id.border8);
        enroll=findViewById(R.id.event_enroll);
        bar=findViewById(R.id.check_attendance_progress);
        events=FirebaseDatabase.getInstance().getReference("Events");
        people = FirebaseDatabase.getInstance().getReference("People");
        users = FirebaseDatabase.getInstance().getReference("Users");
        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(code.getText().toString().trim().length()<8){
                    border7.setError("Event code should be 8 or 9 letters and numbers");
                }
                else{
                    boolean set=true;
                    for (int i=0;i<code.getText().toString().length();i++) {
                        if ((!Character.isLetterOrDigit(code.getText().toString().toLowerCase().charAt(i)))) {
                            border7.setError("Event code should be 8 or 9 letters and numbers");
                            set=false;
                            break;
                        }
                    }
                    if(set) {
                        border7.setError(null);
                    }
                }
                if(id.getText().toString().trim().isEmpty()){
                    border8.setError("ID cannot be left blank");
                }
                else{
                    border8.setError(null);
                }
                if(bar.getVisibility()==View.GONE && TextUtils.isEmpty(border7.getError()) && TextUtils.isEmpty(border8.getError()) && !code.getText().toString().trim().contains(".") && !code.getText().toString().trim().contains("$") && !code.getText().toString().trim().contains("#") && !code.getText().toString().trim().contains("[") && !code.getText().toString().trim().contains("]") &&  !code.getText().toString().trim().contains("/") && !code.getText().toString().trim().contains(Character.getName(92))) {
                    bar.setVisibility(View.VISIBLE);
                    new_person = new Person(id.getText().toString().trim(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getEmail(),FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString().replace("s96-c","s700-c"));
                    events.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(code.getText().toString().trim().toLowerCase()).exists()) {
                                people.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.child(code.getText().toString().trim().toLowerCase()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                                            set=true;
                                            users.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot udataSnapshot) {
                                                    Iterable<DataSnapshot> children=dataSnapshot.child(code.getText().toString().trim().toLowerCase()).getChildren();
                                                    for(DataSnapshot child:children){
                                                        Person person=child.getValue(Person.class);
                                                        if(person.getPerson_ID().equals(id.getText().toString().trim())){
                                                            set=false;
                                                            break;
                                                        }
                                                    }
                                                    if(set) {
                                                        people.child(code.getText().toString().trim().toLowerCase()+ "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new_person);
                                                        User user = udataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(User.class);
                                                        Date date=new Date();
                                                        SimpleDateFormat datef=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                                                        user.events.put(datef.format(date),code.getText().toString().trim().toLowerCase());
                                                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("events").setValue(user.events);
                                                        bar.setVisibility(View.GONE);
                                                        Toast.makeText(EnrollActivity.this, "Successfully joined this event", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                    else{
                                                        border8.setError("This ID is already taken");
                                                        bar.setVisibility(View.GONE);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        } else {
                                            Toast.makeText(EnrollActivity.this, "You have already joined this event", Toast.LENGTH_SHORT).show();
                                            bar.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else{
                                border7.setError("Invalid Event Code");
                                bar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }
}
