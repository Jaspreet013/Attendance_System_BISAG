package com.example.attendancesystem;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        //if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                }
            },300);
        //}
        /*else{
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                }
            },300);
        }*/
    }
}
