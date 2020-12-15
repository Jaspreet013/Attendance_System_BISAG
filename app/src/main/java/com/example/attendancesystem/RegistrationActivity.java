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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
                final EditText fname=findViewById(R.id.register_fname);
                final EditText lname=findViewById(R.id.register_lname);
                EditText email=findViewById(R.id.register_email);
                EditText password=findViewById(R.id.register_password);
                EditText cnfpassword=findViewById(R.id.register_cnfpassword);
                if(TextUtils.isEmpty(fname.getText().toString().trim()) || TextUtils.isEmpty(lname.getText().toString().trim())
                || TextUtils.isEmpty(email.getText().toString().trim()) || TextUtils.isEmpty(password.getText().toString().trim())
                || TextUtils.isEmpty(cnfpassword.getText().toString().trim())){
                    Toast.makeText(RegistrationActivity.this,"Please fill up all the values",Toast.LENGTH_SHORT).show();
                }
                else if(!(fname.getText().toString().trim().matches("^[a-zA-Z]*$")) || !(lname.getText().toString().trim().matches("^[a-zA-Z]*$"))){
                    Toast.makeText(RegistrationActivity.this,"Please provide a proper name",Toast.LENGTH_SHORT).show();
                }
                else if(!email.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                    Toast.makeText(RegistrationActivity.this,"Please provide a valid E-mail",Toast.LENGTH_SHORT).show();
                }
                else if(!password.getText().toString().trim().equals(cnfpassword.getText().toString().trim())){
                    Toast.makeText(RegistrationActivity.this,"Please confirm E-mail properly",Toast.LENGTH_SHORT).show();
                }
                else if(password.getText().toString().trim().length()<8){
                    Toast.makeText(RegistrationActivity.this,"Password length should be in between 8 to 12 characters",Toast.LENGTH_SHORT).show();
                }
                else if(!isNetworkAvailable()){
                    Toast.makeText(RegistrationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog.setMessage("Please Wait");
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    try{
                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim(),password.getText().toString().trim()).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
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
                                            }
                                            else {
                                                try {
                                                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fname.getText().toString().trim()+" "+lname.getText().toString().trim()).build());
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
                                else{
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
}