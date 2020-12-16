package com.example.attendancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);
        final TextView person_name=findViewById(R.id.message_person_name);
        person_name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        Button take_attendance=findViewById(R.id.take_attendance);
        person_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name[]= FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ",2);
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(HomeActivity.this);
                alertDialog.setTitle("Change your name");
                final EditText fname = new EditText(HomeActivity.this);
                final EditText lname = new EditText(HomeActivity.this);
                fname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                fname.setHint("Enter First Name");
                fname.setText(name[0]);
                fname.setSelectAllOnFocus(true);
                lname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                lname.setHint("Enter Last Name");
                lname.setText(name[1]);
                lname.setSelectAllOnFocus(true);
                LinearLayout ll=new LinearLayout(HomeActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(fname);
                ll.addView(lname);
                alertDialog.setView(ll);
                alertDialog.setNegativeButton("Cancel",null);
                alertDialog.setPositiveButton("Update",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name[]=FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ",2);
                        if (!isNetworkAvailable()) {
                            Toast.makeText(HomeActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                        }
                        else if(TextUtils.isEmpty(fname.getText().toString().trim()) || TextUtils.isEmpty(lname.getText().toString().trim())){
                            Toast.makeText(HomeActivity.this,"Name fields cannot be left blank",Toast.LENGTH_SHORT).show();
                        }
                        else if(fname.getText().toString().trim().length()>15 || lname.getText().toString().trim().length()>15){
                            Toast.makeText(HomeActivity.this,"Limit of length of first name is 15 and last name is also 15",Toast.LENGTH_SHORT).show();
                        }
                        else if(!(fname.getText().toString().trim().matches("^[a-zA-Z]*$")) || !(lname.getText().toString().trim().matches("^[a-zA-Z]*$"))){
                            Toast.makeText(HomeActivity.this,"Please provide a proper name",Toast.LENGTH_SHORT).show();

                        }
                        else if(!fname.getText().toString().trim().equals(name[0]) || !lname.getText().toString().trim().equals(name[1])){
                            try {
                                FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fname.getText().toString()+" "+lname.getText().toString()).build()).isSuccessful();
                                Toast.makeText(HomeActivity.this,"Name changed succeessfully",Toast.LENGTH_SHORT).show();
                                person_name.setText(fname.getText().toString().trim()+" "+lname.getText().toString().trim());
                            }
                            catch (Exception e) {
                                Log.e("Exception is", e.toString());
                            }
                        }
                    }
                });
                AlertDialog alert = alertDialog.create();
                fname.requestFocus();
                alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                alert.show();
            }
        });
        take_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(HomeActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                }
                else {
                    startActivity(new Intent(HomeActivity.this, SelectSubjectActivity.class));
                }
            }
        });
        Button checkAttendance=findViewById(R.id.check_attendance);
        checkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(HomeActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                }
                else {
                    startActivity(new Intent(HomeActivity.this, CheckSelectActivity.class));
                }
            }
        });
        Button logout=findViewById(R.id.logout),modify_events=findViewById(R.id.modify_subjects);
        modify_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(HomeActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                }
                else{
                    startActivity(new Intent(HomeActivity.this,ModifyEventActivity.class));
                }
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Are you sure you want to logout?");
                builder.setTitle("Logout");
                builder.setCancelable(true);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this,MainActivity.class));
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.show();
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