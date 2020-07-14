package com.example.attendancesystem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences get_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Gson gson=new Gson();
        TextView person_name=findViewById(R.id.message_person_name);
        String json=get_user.getString("Current User","");
        User current_user=gson.fromJson(json,User.class);
        person_name.setText(current_user.getFname()+" "+current_user.getLname());
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
                builder.setTitle("Exit");
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
            editor.commit();
            finish();
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
