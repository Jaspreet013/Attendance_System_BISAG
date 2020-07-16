package com.example.attendancesystem;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class ModifyAttendanceActivity extends AppCompatActivity {
    SharedPreferences get_person;
    Person current_person;
    String key;
    EditText Attendance,Total_Attendance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_attendance);
        get_person = getSharedPreferences("Person",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_person.getString("Current Person","");
        current_person=gson.fromJson(json,Person.class);
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
                if(Long.parseLong(Attendance.getText().toString())>Long.parseLong(Total_Attendance.getText().toString())){
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
                else if(Long.parseLong(Attendance.getText().toString())!=current_person.getAttendance() || Long.parseLong(Total_Attendance.getText().toString())!=current_person.getAttendance_total()){
                    ProgressDialog dialog=new ProgressDialog(ModifyAttendanceActivity.this);
                    dialog.setMessage("Please Wait");
                    dialog.setCancelable(false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.show();
                    FirebaseDatabase database=FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference=database.getReference("Persons");
                    current_person.setAttendance(Long.parseLong(Attendance.getText().toString()));
                    current_person.setAttendance_total(Long.parseLong(Total_Attendance.getText().toString()));
                    databaseReference.child(key).setValue(current_person);
                    dialog.dismiss();
                    Toast.makeText(ModifyAttendanceActivity.this,"Attendance data changed successfully",Toast.LENGTH_SHORT).show();
                    finish();
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