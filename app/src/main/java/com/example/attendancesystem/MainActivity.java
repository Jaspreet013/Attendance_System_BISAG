package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    ProgressDialog waiting;
    FirebaseAuth firebaseAuth;
    EditText email,password;
    TextView new_user,forgot_password;
    Button login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            login = findViewById(R.id.login_submit);
            new_user = findViewById(R.id.new_user);
            forgot_password=findViewById(R.id.forgot_password);
            new_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                }
            });
            forgot_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this,ForgotPasswordActivity.class));
                }
            });
            firebaseAuth = FirebaseAuth.getInstance();
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email = findViewById(R.id.login_email);
                    password = findViewById(R.id.login_password);
                    new_user = findViewById(R.id.new_user);
                    waiting = new ProgressDialog(MainActivity.this);
                    if (TextUtils.isEmpty(email.getText().toString().trim())) {
                        Toast.makeText(MainActivity.this, "Email cannot be left blank", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(password.getText().toString().trim())) {
                        Toast.makeText(MainActivity.this, "Password cannot be left Blank", Toast.LENGTH_SHORT).show();
                    } else if (!email.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")) {
                        Toast.makeText(MainActivity.this, "Please Enter a valid email", Toast.LENGTH_SHORT).show();
                    } else if (password.getText().toString().trim().length() < 8) {
                        Toast.makeText(MainActivity.this, "Please Enter proper password", Toast.LENGTH_SHORT).show();
                    } else if (!isNetworkAvailable()) {
                        Toast.makeText(MainActivity.this,"Please check your internet connection",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        waiting.setMessage("Please Wait");
                        waiting.setCancelable(false);
                        waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        waiting.show();
                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                        try {
                                            waiting.dismiss();
                                            finish();
                                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                        } catch (Exception e) {
                                            Log.e("Exception is", e.toString());
                                        }
                                    }
                                    else{
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Your email is not verified, please check your mail");
                                        builder.setPositiveButton("Ok", null);
                                        builder.setCancelable(false);
                                        builder.show();
                                        waiting.dismiss();
                                    }
                                }
                                else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Error");
                                    if(task.getException().getMessage().equals("The password is invalid or the user does not have a password.")){
                                        builder.setMessage("Wrong Password");
                                    }
                                    else {
                                        builder.setMessage(task.getException().getMessage());
                                    }
                                    builder.setPositiveButton("Ok", null);
                                    builder.setCancelable(false);
                                    builder.show();
                                    waiting.dismiss();
                                }
                            }
                        });
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