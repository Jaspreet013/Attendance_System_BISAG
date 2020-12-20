package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegistrationActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private final FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private TextInputLayout border2,border3,border4,border5,border6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Button submit,clear;
        submit=findViewById(R.id.registration_submit);
        clear=findViewById(R.id.registration_clear);
        border2=findViewById(R.id.border2);
        border3=findViewById(R.id.border3);
        border4=findViewById(R.id.border4);
        border5=findViewById(R.id.border5);
        border6=findViewById(R.id.border6);
        progressDialog=new ProgressDialog(RegistrationActivity.this);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText fname=findViewById(R.id.register_fname);
                final EditText lname=findViewById(R.id.register_lname);
                EditText email=findViewById(R.id.register_email);
                EditText password=findViewById(R.id.register_password);
                EditText cnfpassword=findViewById(R.id.register_cnfpassword);
                if(!isNetworkAvailable()){
                    Toast.makeText(RegistrationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else {
                    if(!fname.getText().toString().trim().matches("^[a-zA-Z]*$") || fname.getText().toString().isEmpty()) {
                        if (fname.getText().toString().isEmpty()) {
                            border2.setError("First name cannot be left blank");
                        } else {
                            border2.setError("Please enter a proper first name");
                        }
                    } else {
                        border2.setError(null);
                    }
                    if (!lname.getText().toString().trim().matches("^[a-zA-Z]*$") || lname.getText().toString().isEmpty()) {
                        if (lname.getText().toString().isEmpty()) {
                            border3.setError("Last name cannot be left blank");
                        } else {
                            border3.setError("Please enter a proper last name");
                        }
                    } else {
                        border3.setError(null);
                    }
                    if (!email.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")) {
                        if (email.getText().toString().trim().isEmpty()) {
                            border4.setError("Email cannot be left blank");
                        } else {
                            border4.setError("Please enter a proper Email");
                        }
                    } else {
                        border4.setError(null);
                    }
                    if (password.getText().toString().trim().isEmpty()) {
                        border5.setError("Password cannot be left blank");
                    } else if(password.getText().toString().trim().length()<6){
                        border5.setError("Password length cannot be less than 6");
                    } else {
                        border5.setError(null);
                    }
                    if (cnfpassword.getText().toString().trim().isEmpty()) {
                        border6.setError("Please confirm password");
                    } else if (cnfpassword.getText().toString().trim().length()<6) {
                        border6.setError("Password length cannot be less than 6");
                    } else if (!cnfpassword.getText().toString().trim().equals(password.getText().toString().trim())) {
                        border5.setError("Password doesn't match with confirm password field");
                        border6.setError("Password doesn't match with confirm password field");
                    } else {
                        border6.setError(null);
                    }
                    if (TextUtils.isEmpty(border2.getError()) && TextUtils.isEmpty(border3.getError()) && TextUtils.isEmpty(border4.getError()) && TextUtils.isEmpty(border5.getError()) && TextUtils.isEmpty(border6.getError())) {
                        progressDialog.setMessage("Please Wait");
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        try {
                            firebaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();
                                                if (!task.isSuccessful()) {
                                                    firebaseAuth.signOut();
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                                                    builder.setTitle("Error");
                                                    builder.setMessage(task.getException().getMessage());
                                                    builder.setPositiveButton("Ok", null);
                                                    builder.setCancelable(false);
                                                    builder.show();
                                                } else {
                                                    try {
                                                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fname.getText().toString().trim() + " " + lname.getText().toString().trim()).build());
                                                    } catch (Exception e) {
                                                        Log.e("Exception is", e.toString());
                                                    }
                                                    firebaseAuth.signOut();
                                                    progressDialog.dismiss();
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                                                    builder.setTitle("Verify your email");
                                                    builder.setMessage("Please check your email for verification and after that you will be able to login");
                                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    });
                                                    builder.setCancelable(false);
                                                    builder.show();
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        progressDialog.dismiss();
                                        firebaseAuth.signOut();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                                        builder.setTitle("Error");
                                        builder.setMessage(task.getException().getMessage());
                                        builder.setPositiveButton("Ok", null);
                                        builder.setCancelable(false);
                                        builder.show();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.e("Exception is ", e.toString());
                        }
                    }
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               EditText fname=findViewById(R.id.register_fname);
               EditText lname=findViewById(R.id.register_lname);
               EditText email=findViewById(R.id.register_email);
               EditText password=findViewById(R.id.register_password);
               EditText cnfpassword=findViewById(R.id.register_cnfpassword);
               fname.getText().clear();
               lname.getText().clear();
               email.getText().clear();
               password.getText().clear();
               cnfpassword.getText().clear();
               border2.setError(null);
               border3.setError(null);
               border4.setError(null);
               border5.setError(null);
               border6.setError(null);
               fname.requestFocus();
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