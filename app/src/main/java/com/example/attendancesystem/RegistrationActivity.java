package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Button submit,clear;
        submit=findViewById(R.id.registration_submit);
        clear=findViewById(R.id.registration_clear);
        progressDialog=new ProgressDialog(RegistrationActivity.this);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText fname=findViewById(R.id.register_fname);
                EditText lname=findViewById(R.id.register_lname);
                EditText email=findViewById(R.id.register_email);
                EditText password=findViewById(R.id.register_password);
                EditText cnfpassword=findViewById(R.id.register_cnfpassword);
                if(TextUtils.isEmpty(fname.getText().toString()) || TextUtils.isEmpty(lname.getText().toString())
                || TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString())
                || TextUtils.isEmpty(cnfpassword.getText().toString())){
                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setTitle("Please fill up all the values");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(!(fname.getText().toString().matches("^[a-zA-Z]*$")) || !(lname.getText().toString().matches("^[a-zA-Z]*$"))){
                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setTitle("Please provide a proper name");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(!email.getText().toString().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setTitle("Please provide a valid email");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(!password.getText().toString().equals(cnfpassword.getText().toString())){
                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setTitle("Please confirm password properly");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(password.getText().toString().length()<8){
                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setTitle("Password length should be in between 8 to 12 characters");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(!isNetworkAvailable()){
                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok",null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else{
                    progressDialog.setMessage("Please Wait");
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    try{
                        final User new_user=new User(fname.getText().toString(),lname.getText().toString(),email.getText().toString());
                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if(task.isSuccessful()){
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
                                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                                    databaseReference = FirebaseDatabase.getInstance().getReference("users");
                                                    databaseReference.child(user.getUid()).setValue(new_user);
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
                                                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                                    }
                                                });
                                                builder.setCancelable(false);
                                                builder.show();
                                            }
                                        }
                                    });

                                }
                            }
                        });
                    }catch(Exception e){
                        Log.e("Exception is ",e.toString());
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

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(RegistrationActivity.this,MainActivity.class));
    }
}