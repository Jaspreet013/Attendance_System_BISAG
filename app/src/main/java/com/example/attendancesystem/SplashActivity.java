package com.example.attendancesystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {
    SharedPreferences get_user;
    private static int SPLASH_TIME_OUT=300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        if(get_user.contains("Current User")){
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                }
            },SPLASH_TIME_OUT);
        }
        else{
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                }
            },SPLASH_TIME_OUT);
        }
    }
}
