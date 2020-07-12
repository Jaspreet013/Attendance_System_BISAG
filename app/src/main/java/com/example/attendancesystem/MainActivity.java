package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ProgressDialog waiting;
    FirebaseAuth firebaseAuth;
    EditText email,password;
    TextView new_user;
    Button login;
    User final_user;
    SharedPreferences get_user;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        if(get_user.contains("Current User")){
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
        }
        else {
            login = findViewById(R.id.login_submit);
            new_user = findViewById(R.id.new_user);
            new_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                }
            });
            firebaseAuth = FirebaseAuth.getInstance();
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email = findViewById(R.id.login_email);
                    password = findViewById(R.id.login_password);
                    new_user = findViewById(R.id.new_user);
                    final Pattern pattern = Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$");
                    final Matcher matcher = pattern.matcher(email.getText().toString());
                    waiting = new ProgressDialog(MainActivity.this);
                    if (TextUtils.isEmpty(email.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Email cannot be left blank", Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(password.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Password cannot be left Blank", Toast.LENGTH_SHORT).show();
                    } else if (!matcher.matches()) {
                        Toast.makeText(MainActivity.this, "Please Enter a valid email", Toast.LENGTH_SHORT).show();
                    } else if (password.getText().toString().length() < 8) {
                        Toast.makeText(MainActivity.this, "Please Enter proper password", Toast.LENGTH_SHORT).show();
                    } else if (!isNetworkAvailable()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("No Internet");
                        builder.setMessage("Please check your internet connection");
                        builder.setPositiveButton("Ok", null);
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        waiting.setMessage("Please Wait");
                        waiting.setCancelable(false);
                        waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        waiting.show();
                        firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    try {
                                        FirebaseUser verify = firebaseAuth.getCurrentUser();
                                        String userID = verify.getUid();
                                        Log.e("UID", userID);
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users/" + userID);
                                        databaseReference.getClass();
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                try {
                                                    User user = dataSnapshot.getValue(User.class);
                                                    Log.e("email ", user.getEmail());
                                                    final_user = user;
                                                    SharedPreferences.Editor prefsEditor = get_user.edit();
                                                    Gson gson = new Gson();
                                                    String json = gson.toJson(final_user);
                                                    prefsEditor.putString("Current User", json);
                                                    prefsEditor.commit();
                                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                    firebaseAuth.signOut();
                                                    waiting.dismiss();
                                                } catch (Exception e) {
                                                    Log.e("Exception is", e.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e("Error : ", databaseError.getMessage());
                                            }
                                        });

                                    } catch (Exception e) {
                                        Log.e("Exception is", e.toString());
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                                    waiting.dismiss();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure you want to exit?");
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok",new MyListener());
        builder.setNegativeButton("Cancel",null);
        builder.show();
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