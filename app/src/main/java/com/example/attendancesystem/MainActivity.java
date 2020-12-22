package com.example.attendancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText email,password;
    private TextInputLayout border,border1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        Button login = findViewById(R.id.login_submit);
        TextView new_user = findViewById(R.id.new_user);
        border=findViewById(R.id.border);
        border1=findViewById(R.id.border1);
        TextView forgot_password=findViewById(R.id.forgot_password);
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
                if (!isNetworkAvailable()) {
                    Toast.makeText(MainActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else{
                    if (!email.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")) {
                        if(TextUtils.isEmpty(email.getText().toString().trim())){
                            border.setError("Email cannot be left blank");
                        }
                        else{
                            border.setError("Please enter a proper Email");
                        }
                    }
                    else{
                        border.setError(null);
                    }
                    if(password.getText().toString().trim().length()==0){
                        border1.setError("Password cannot be left blank");
                    }
                    else if(password.getText().toString().trim().length()<6){
                        border1.setError("Password length cannot be less than 6");
                    }
                    else{
                        border1.setError(null);
                    }
                    if(TextUtils.isEmpty(border.getError()) && TextUtils.isEmpty(border1.getError())) {
                        final AlertDialog dialog=setProgressDialog();
                        dialog.show();
                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                        try {
                                            dialog.dismiss();
                                            finish();
                                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                        } catch (Exception e) {
                                            Log.e("Exception is", e.toString());
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Your email is not verified, please check your mail");
                                        builder.setPositiveButton("Ok", null);
                                        builder.setCancelable(false);
                                        builder.show();
                                        dialog.dismiss();
                                    }
                                } else {
                                    if (task.getException().getMessage().equals("The password is invalid or the user does not have a password.")) {
                                        border1.setError("Wrong Password");
                                    } else if (task.getException().getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                        border.setError("This email is not registered");
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Error");
                                        builder.setMessage(task.getException().getMessage());
                                        builder.setPositiveButton("Ok", null);
                                        builder.setCancelable(false);
                                        builder.show();
                                    }
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    public AlertDialog setProgressDialog() {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.LEFT;
        ll.setLayoutParams(llParam);
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);
        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this);
        tvText.setText("Please Wait.....");
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);
        ll.addView(progressBar);
        ll.addView(tvText);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(ll);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return dialog;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}