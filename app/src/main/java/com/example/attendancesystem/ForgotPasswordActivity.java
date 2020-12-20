package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        final EditText email=findViewById(R.id.reset_password);
        final TextInputLayout border=findViewById(R.id.border);
        Button submit=findViewById(R.id.reset_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(ForgotPasswordActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                if(email.getText().toString().trim().isEmpty()){
                    border.setError("Email cannot be left blank");
                }
                else if(!email.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                    border.setError("Please enter a proper Email");
                }
                else{
                    border.setError(null);
                }
                if(TextUtils.isEmpty(border.getError())){
                    final ProgressDialog waiting=new ProgressDialog(ForgotPasswordActivity.this);
                    waiting.setMessage("Please Wait");
                    waiting.setCancelable(false);
                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    waiting.show();
                    FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
                    firebaseAuth.sendPasswordResetEmail(email.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            waiting.dismiss();
                            if(task.isSuccessful()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                builder.setTitle("Task Successful");
                                builder.setMessage("Password reset mail has been sent to the registered Email-ID, please check your mail");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                builder.setCancelable(false);
                                builder.show();
                            }
                            else{
                                if (task.getException().getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                    border.setError("This email is not registered");
                                }
                                else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                    builder.setTitle("Error");
                                    builder.setMessage(task.getException().getMessage());
                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                            startActivity(new Intent(ForgotPasswordActivity.this,MainActivity.class));
                                        }
                                    });
                                    builder.setCancelable(false);
                                    builder.show();
                                }
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