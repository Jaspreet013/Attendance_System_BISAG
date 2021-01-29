package com.example.attendancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {
    private int RC_SIGN_IN = 1;
    private ScrollView scrollView;
    private DatabaseReference users;
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);
        scrollView=findViewById(R.id.home_layout);
        users=FirebaseDatabase.getInstance().getReference("Users");
        dialog=setProgressDialog();
        if(FirebaseAuth.getInstance().getCurrentUser()==null && isNetworkAvailable()){
            startActivityForResult(GoogleSignIn.getClient(this,new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()).getSignInIntent(),RC_SIGN_IN);
            scrollView.setVisibility(View.GONE);
        }
        else if(FirebaseAuth.getInstance().getCurrentUser()==null || !isNetworkAvailable()){
            Toast.makeText(HomeActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
            scrollView.setVisibility(View.GONE);
            finish();
        }
        else {
            dialog.show();
            final TextView person_name = findViewById(R.id.message_person_name);
            final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            person_name.setText(account.getDisplayName());
            users.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(User.class);
                    if(!(account.getGivenName().equals(user.getFname()) && account.getFamilyName().equals(user.getLname()) && account.getPhotoUrl().toString().replace("s96-c","s700-c").equals(user.getPhotourl()))) {
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Fname").setValue(account.getGivenName());
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/Lname").setValue(account.getFamilyName());
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/photourl").setValue(account.getPhotoUrl().toString().replace("s96-c","s700-c"));
                        for (String key : user.events.keySet()) {
                            users.getParent().child("People/" + user.events.get(key) + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/name").setValue(account.getDisplayName());
                            users.getParent().child("People/" + user.events.get(key) + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/photourl").setValue(account.getPhotoUrl().toString().replace("s96-c","s700-c"));
                        }
                    }
                    dialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            scrollView.setVisibility(View.VISIBLE);
            Button take_attendance = findViewById(R.id.take_attendance);
            take_attendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(HomeActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(HomeActivity.this, SelectSubjectActivity.class));
                    }
                }
            });
            Button enroll = findViewById(R.id.enroll_button);
            enroll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(HomeActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();

                    } else {
                        startActivity(new Intent(HomeActivity.this, EnrollActivity.class));
                    }
                }
            });
            Button checkAttendance = findViewById(R.id.check_attendance);
            checkAttendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(HomeActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();

                    } else {
                        startActivity(new Intent(HomeActivity.this, CheckSelectActivity.class));
                    }
                }
            });
            Button check = findViewById(R.id.check_button);
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(HomeActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();

                    } else {
                        startActivity(new Intent(HomeActivity.this, SelectEventActivity.class));
                    }
                }
            });
            Button logout = findViewById(R.id.logout), modify_events = findViewById(R.id.modify_subjects);
            modify_events.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(HomeActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();

                    } else {
                        startActivity(new Intent(HomeActivity.this, ModifyEventActivity.class));
                    }
                }
            });
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setMessage("Are you sure you want to logout?");
                    builder.setTitle("Logout");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build();
                            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(HomeActivity.this, gso);
                            mGoogleSignInClient.signOut();
                            FirebaseAuth.getInstance().signOut();
                            recreate();
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                }
            });
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private  void handleSignInResult(Task<GoogleSignInAccount> task){
        try{
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            FirebaseGoogleAuth(acc);
        }
        catch (ApiException e){
            finish();
        }
    }
    private void FirebaseGoogleAuth(GoogleSignInAccount acct){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(HomeActivity.this,"Sign In Successful",Toast.LENGTH_SHORT).show();
                    final DatabaseReference users=FirebaseDatabase.getInstance().getReference("Users");
                    users.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                                String str[] = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ", 2);
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(str[0], str[1], FirebaseAuth.getInstance().getCurrentUser().getEmail(), FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString()));
                            }
                            else {
                            }
                            recreate();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    Toast.makeText(HomeActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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
}