package com.example.attendancesystem;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                final Pattern pattern=Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$");
                final Matcher matcher=pattern.matcher(email.getText().toString());
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
                else if(!matcher.matches()){
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
                                if (task.isSuccessful()) {
                                    try {
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        databaseReference = FirebaseDatabase.getInstance().getReference("users");
                                        databaseReference.child(user.getUid()).setValue(new_user);
                                    }catch (Exception e){
                                        Log.e("Exception is",e.toString());
                                    }
                                    firebaseAuth.signOut();
                                    progressDialog.dismiss();
                                    Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                    finish();
                                    startActivity(new Intent(RegistrationActivity.this,MainActivity.class));
                                } else {
                                    firebaseAuth.signOut();
                                    AlertDialog.Builder builder=new AlertDialog.Builder(RegistrationActivity.this);
                                    builder.setTitle("Cannot Register");
                                    builder.setPositiveButton("Ok",null);
                                    builder.setCancelable(false);
                                    builder.show();
                                    Log.e("Exception is", task.getException().toString());
                                    progressDialog.dismiss();
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
