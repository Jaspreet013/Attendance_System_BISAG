package com.example.attendancesystem;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class ModifyAttendanceActivity extends AppCompatActivity {
    SharedPreferences get_person,get_user;
    Person current_person;
    String key;
    EditText Attendance,Total_Attendance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        get_person = getSharedPreferences("Person",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_person.getString("Current Person","");
        current_person=gson.fromJson(json,Person.class);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Gson gson1=new Gson();
        String json1=get_user.getString("Current User","");
        final User current_user=gson1.fromJson(json1,User.class);
        key=get_person.getString("Key","");
        TextView username=findViewById(R.id.disp_user_name);
        TextView id=findViewById(R.id.disp_user_id);
        TextView email=findViewById(R.id.disp_user_email);
        Attendance=findViewById(R.id.edit_attendance);
        Total_Attendance=findViewById(R.id.edit_total_attendance);
        username.setText(current_person.getFname()+" "+current_person.getLname());
        id.setText(id.getText().toString()+current_person.getPerson_ID());
        email.setText(email.getText().toString()+current_person.getPerson_email());
        Attendance.setText(Long.toString(current_person.getAttendance()));
        Total_Attendance.setText(Long.toString(current_person.getAttendance_total()));
        Button submit=findViewById(R.id.update_attendance);
        Attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Attendance.selectAll();
                Attendance.requestFocus();
            }
        });
        Total_Attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Total_Attendance.selectAll();
                Total_Attendance.requestFocus();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(ModifyAttendanceActivity.this);
                builder.setTitle("Are you sure you want to update attendance?");
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(TextUtils.isEmpty(Attendance.getText().toString()) || TextUtils.isEmpty(Total_Attendance.getText().toString())){
                            AlertDialog.Builder builder=new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("Attendance or Total Attendance cannot be blank");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok",null);
                            builder.show();
                        }
                        else if(Long.parseLong(Attendance.getText().toString())>Long.parseLong(Total_Attendance.getText().toString())){
                            AlertDialog.Builder builder=new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("Attendance cannot be more than Total Attendance");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok",null);
                            builder.show();
                        }
                        else if(!isNetworkAvailable()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(Long.parseLong(Attendance.getText().toString())!=current_person.getAttendance() || Long.parseLong(Total_Attendance.getText().toString())!=current_person.getAttendance_total()) {
                            ProgressDialog progressDialog = new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = database.getReference("Persons/" + current_user.getEmail().replace(".", ""));
                            current_person.setAttendance(Long.parseLong(Attendance.getText().toString()));
                            current_person.setAttendance_total(Long.parseLong(Total_Attendance.getText().toString()));
                            databaseReference.child(key).setValue(current_person);
                            progressDialog.dismiss();
                            Toast.makeText(ModifyAttendanceActivity.this,"Attendance data changed successfully",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
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