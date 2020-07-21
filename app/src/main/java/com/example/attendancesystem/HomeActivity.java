package com.example.attendancesystem;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences get_user;
    String key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Gson gson=new Gson();
        TextView person_name=findViewById(R.id.message_person_name);
        String json=get_user.getString("Current User","");
        final User current_user=gson.fromJson(json,User.class);
        key=get_user.getString("Key",null);
        person_name.setText(current_user.getFname()+" "+current_user.getLname());
        Button take_attendance=findViewById(R.id.take_attendance);
        TextView change_name=findViewById(R.id.change_name);
        change_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(HomeActivity.this);
                alertDialog.setTitle("Change your name");
                final EditText fname = new EditText(HomeActivity.this);
                final EditText lname = new EditText(HomeActivity.this);
                fname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                fname.setHint("Enter First Name");
                fname.setText(current_user.getFname());
                fname.setSelectAllOnFocus(true);
                lname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                lname.setHint("Enter Last Name");
                lname.setText(current_user.getLname());
                lname.setSelectAllOnFocus(true);
                LinearLayout ll=new LinearLayout(HomeActivity.this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(fname);
                ll.addView(lname);
                alertDialog.setView(ll);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Update",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(TextUtils.isEmpty(fname.getText().toString()) || TextUtils.isEmpty(lname.getText().toString())){
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle("Name fields cannot be left blank");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(fname.getText().toString().length()>15 || lname.getText().toString().length()>15){
                            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                            builder.setTitle("Limit of length of fname is 15 and lname is also 15");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else {
                            try {
                                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("users/"+key);
                                current_user.setFname(fname.getText().toString());
                                current_user.setLname(lname.getText().toString());
                                databaseReference.setValue(current_user);
                                SharedPreferences.Editor editor=get_user.edit();
                                Gson gson = new Gson();
                                String json = gson.toJson(current_user);
                                editor.putString("Current User", json);
                                editor.apply();
                                TextView disp=findViewById(R.id.message_person_name);
                                disp.setText(fname.getText().toString()+" "+lname.getText().toString());
                            }
                            catch (Exception e) {
                                Log.e("Exception is", e.toString());
                            }
                        }
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();
            }
        });
        take_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
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
                builder.setCancelable(false);
                builder.setPositiveButton("Ok",new HomeActivity.Logout());
                builder.setNegativeButton("Cancel",null);
                builder.show();
            }
        });
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
        builder.setMessage("Are you sure you want to exit?");
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok",new HomeActivity.MyListener());
        builder.setNegativeButton("Cancel",null);
        builder.show();
    }
    public class Logout implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SharedPreferences.Editor editor=get_user.edit();
            editor.remove("Current User");
            editor.apply();
            finish();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }
    }
    public class MyListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
