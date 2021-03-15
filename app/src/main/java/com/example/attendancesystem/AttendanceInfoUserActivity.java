package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class AttendanceInfoUserActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView name,organisation,id,coordinator_name,email,attendance;
    private ListView listView;
    private TextView empty_message,duration,start,to,end;
    private MyBaseAdapter adapter;
    private User coordinator;
    private Event event;
    private Person person;
    private String key,start_date,end_date;
    private DatabaseReference people;
    private ArrayList<String> arrayList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_info_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        adapter=new MyBaseAdapter(AttendanceInfoUserActivity.this);
        key=getIntent().getStringExtra("Event_Key");
        event=new Gson().fromJson(getIntent().getStringExtra("Event"),Event.class);
        coordinator=new Gson().fromJson(getIntent().getStringExtra("Coordinator"),User.class);
        name=findViewById(R.id.disp_event_name);
        name.setText(event.getName());
        organisation=findViewById(R.id.disp_organisation_name);
        people= FirebaseDatabase.getInstance().getReference("People");
        organisation.setText(event.getOrganisation());
        empty_message=findViewById(R.id.select_empty_message);
        id=findViewById(R.id.disp_user_id);
        coordinator_name=findViewById(R.id.disp_coordinator_name);
        email=findViewById(R.id.disp_user_email);
        attendance=findViewById(R.id.disp_user_attendance);
        duration=findViewById(R.id.disp_duration);
        start=findViewById(R.id.start_date);
        to=findViewById(R.id.disp_to);
        end=findViewById(R.id.end_date);
        listView=findViewById(R.id.list_view);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter);
        progressBar=findViewById(R.id.check_progress);
        name.setVisibility(View.GONE);
        organisation.setVisibility(View.GONE);
        id.setVisibility(View.GONE);
        coordinator_name.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        attendance.setVisibility(View.GONE);
        duration.setVisibility(View.GONE);
        start.setVisibility(View.GONE);
        to.setVisibility(View.GONE);
        end.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if(isNetworkAvailable()) {
            try {
                people.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        person=dataSnapshot.child(key).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Person.class);
                        arrayList.addAll(person.dates.keySet());
                        adapter.notifyDataSetChanged();
                        Collections.sort(arrayList);
                        Collections.reverse(arrayList);
                        id.setText("ID : "+person.getPerson_ID());
                        coordinator_name.setText("Coordinator Name : "+coordinator.getName());
                        email.setText("Coordinator Email : "+coordinator.getEmail());
                        Log.e("Person",person.getPerson_ID());
                        float percent_count;
                        if(person.getAttendance()!=0) {
                            percent_count = ((float)person.getAttendance() / (float)person.getAttendance_total()) * 100;
                            attendance.setText("Attendance : "+person.getAttendance()+"/"+person.getAttendance_total()+" -- "+String.format("%.2f",percent_count)+"%");

                        }
                        else{
                            attendance.setText("Attendance : "+person.getAttendance()+"/"+person.getAttendance_total());
                        }
                        name.setVisibility(View.VISIBLE);
                        organisation.setVisibility(View.VISIBLE);
                        id.setVisibility(View.VISIBLE);
                        coordinator_name.setVisibility(View.VISIBLE);
                        email.setVisibility(View.VISIBLE);
                        duration.setVisibility(View.VISIBLE);
                        start.setVisibility(View.VISIBLE);
                        to.setVisibility(View.VISIBLE);
                        end.setVisibility(View.VISIBLE);
                        listView.setEmptyView(empty_message);
                        if(arrayList.isEmpty()){
                            listView.setVisibility(View.GONE);
                            empty_message.setVisibility(View.VISIBLE);
                        }
                        else {
                            listView.setVisibility(View.VISIBLE);
                        }
                        attendance.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } catch (Exception e) {}
            id.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AttendanceInfoUserActivity.this);
                    View view=getLayoutInflater().inflate(R.layout.alert_dialog_text_input_layout,null);
                    final TextInputEditText input = view.findViewById(R.id.input);
                    input.setText(person.getPerson_ID());
                    alertDialog.setTitle("Rename ID");
                    alertDialog.setView(view);
                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isNetworkAvailable()) {
                                Toast.makeText(AttendanceInfoUserActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                            }
                            else if(TextUtils.isEmpty(input.getText().toString().trim())){
                                Toast.makeText(AttendanceInfoUserActivity.this,"ID cannot be left blank",Toast.LENGTH_SHORT).show();
                            }
                            else if(!input.getText().toString().trim().equals(person.getPerson_ID())){
                                try {
                                    Toast.makeText(AttendanceInfoUserActivity.this,"Please Wait....",Toast.LENGTH_SHORT).show();
                                    people.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            boolean set=true;
                                            for(DataSnapshot child:children){
                                                Person tperson=child.getValue(Person.class);
                                                if(tperson.getPerson_ID().equals(input.getText().toString().trim())){
                                                    set=false;
                                                    Toast.makeText(AttendanceInfoUserActivity.this,"This ID is already registered to this Event",Toast.LENGTH_SHORT).show();
                                                    break;
                                                }
                                            }
                                            if(set) {
                                                person.setPerson_ID(input.getText().toString().trim());
                                                people.child(key+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(person);
                                                Toast.makeText(AttendanceInfoUserActivity.this,"ID changed successfully",Toast.LENGTH_SHORT).show();
                                                id.setText("ID : "+input.getText().toString());
                                                setResult(RESULT_OK);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } catch (Exception ex) {
                                    Log.e("Exception",ex.getMessage());
                                }
                            }
                        }
                    });
                    alertDialog.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog dialog = alertDialog.create();
                    dialog.show();
                    input.selectAll();
                    input.requestFocus();
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar=Calendar.getInstance();
                    if(TextUtils.isEmpty(start_date)) {
                        Date date=new Date();
                        calendar.setTime(date);
                    }
                    else{
                        try {
                            calendar.setTime(format.parse(start_date));
                        }
                        catch (Exception e){}
                    }
                    DatePickerDialog datePickerDialog=new DatePickerDialog(AttendanceInfoUserActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            month+=1;
                            try {
                                Date startd = format.parse(year + "-" + month + "-" + dayOfMonth);
                                if(TextUtils.isEmpty(end_date)){
                                    start_date=year+"-"+month+"-"+dayOfMonth;
                                    start.setText(dayOfMonth+"/"+month+"/"+year);
                                    arrayList=new ArrayList<>();
                                    for(String Key:person.dates.keySet()){
                                        if(format.parse(Key).compareTo(startd)>=0){
                                            arrayList.add(Key);
                                        }
                                    }
                                    Collections.sort(arrayList);
                                    Collections.reverse(arrayList);
                                    adapter.notifyDataSetChanged();
                                    long present_count=get_present_count(arrayList);
                                    float percent;
                                    if(present_count!=0) {
                                        percent = ((float) present_count / (float) arrayList.size())*100;
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size() + " -- " + String.format("%.2f", percent) + "%");
                                    }
                                    else {
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size());
                                    }
                                }
                                else if(startd.compareTo(format.parse(end_date))>0){
                                    Toast.makeText(AttendanceInfoUserActivity.this,"Start Date cannot be more than End Date",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    start_date=year+"-"+month+"-"+dayOfMonth;
                                    start.setText(dayOfMonth+"/"+month+"/"+year);
                                    arrayList=new ArrayList<>();
                                    for(String Key:person.dates.keySet()){
                                        if(format.parse(Key).compareTo(startd)>=0 && format.parse(Key).compareTo(format.parse(end_date))<=0){
                                            arrayList.add(Key);
                                        }
                                    }
                                    Collections.sort(arrayList);
                                    Collections.reverse(arrayList);
                                    adapter.notifyDataSetChanged();
                                    long present_count=get_present_count(arrayList);
                                    float percent;
                                    if(present_count!=0) {
                                        percent = ((float) present_count / (float) arrayList.size())*100;
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size() + " -- " + String.format("%.2f", percent) + "%");
                                    }
                                    else {
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size());
                                    }
                                }
                            }
                            catch (Exception e){}
                        }
                    },calendar.get(calendar.YEAR),calendar.get(calendar.MONTH),calendar.get(calendar.DAY_OF_MONTH));
                    try {
                        datePickerDialog.show();
                    }
                    catch (Exception e){}
                }
            });
            end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar=Calendar.getInstance();
                    if(TextUtils.isEmpty(end_date)) {
                        Date date=new Date();
                        calendar.setTime(date);
                    }
                    else{
                        try {
                            calendar.setTime(format.parse(end_date));
                        }
                        catch (Exception e){}
                    }
                    DatePickerDialog datePickerDialog=new DatePickerDialog(AttendanceInfoUserActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            month+=1;
                            try {
                                Date endd = format.parse(year + "-" + month + "-" + dayOfMonth);
                                if(TextUtils.isEmpty(start_date)){
                                    end_date=year+"-"+month+"-"+dayOfMonth;
                                    end.setText(dayOfMonth+"/"+month+"/"+year);
                                    arrayList=new ArrayList<>();
                                    for(String Key:person.dates.keySet()){
                                        if(format.parse(Key).compareTo(endd)<=0){
                                            arrayList.add(Key);
                                        }
                                    }
                                    Collections.sort(arrayList);
                                    Collections.reverse(arrayList);
                                    adapter.notifyDataSetChanged();
                                    long present_count=get_present_count(arrayList);
                                    float percent;
                                    if(present_count!=0) {
                                        percent = ((float) present_count / (float) arrayList.size())*100;
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size() + " -- " + String.format("%.2f", percent) + "%");
                                    }
                                    else {
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size());
                                    }
                                }
                                else if(endd.compareTo(format.parse(start_date))<0){
                                    Toast.makeText(AttendanceInfoUserActivity.this,"End Date cannot be less than Start Date",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    end_date=year+"-"+month+"-"+dayOfMonth;
                                    end.setText(dayOfMonth+"/"+month+"/"+year);
                                    arrayList=new ArrayList<>();
                                    for(String Key:person.dates.keySet()){
                                        if(format.parse(Key).compareTo(endd)<=0 && format.parse(Key).compareTo(format.parse(start_date))>=0){
                                            arrayList.add(Key);
                                        }
                                    }
                                    Collections.sort(arrayList);
                                    Collections.reverse(arrayList);
                                    adapter.notifyDataSetChanged();
                                    long present_count=get_present_count(arrayList);
                                    float percent;
                                    if(present_count!=0) {
                                        percent = ((float) present_count / (float) arrayList.size())*100;
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size() + " -- " + String.format("%.2f", percent) + "%");
                                    }
                                    else {
                                        attendance.setText("Attendance : " + present_count + "/" + arrayList.size());
                                    }
                                }
                            }
                            catch (Exception e){}
                        }
                    },calendar.get(calendar.YEAR),calendar.get(calendar.MONTH),calendar.get(calendar.DAY_OF_MONTH));
                    try {
                        datePickerDialog.show();
                    }
                    catch (Exception e){}
                }
            });
        }
        else{
            Toast.makeText(AttendanceInfoUserActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private class MyBaseAdapter extends BaseAdapter {
        final Context context;
        final LayoutInflater inflater;

        MyBaseAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
        @Override
        public String getItem(int position) {
            return arrayList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.event_list_view, null);
            TextView tv1=view.findViewById(R.id.dispname);
            TextView tv2=view.findViewById(R.id.disporganisation);
            TextView tv3=view.findViewById(R.id.coordinator_name);
            tv3.setVisibility(View.GONE);
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            SimpleDateFormat format1;
            if (!DateFormat.is24HourFormat(AttendanceInfoUserActivity.this))
            {
                format1=new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
            }
            else{
                format1=new SimpleDateFormat("dd/MM/yyyy  HH:mm");
            }
            try{
                tv1.setText(format1.format(format.parse(arrayList.get(position))).replace("am","AM").replace("pm","PM"));
            }
            catch (Exception e){}
            tv2.setText(person.dates.get(arrayList.get(position)));
            if(person.dates.get(arrayList.get(position)).equals("Absent")){
                tv2.setTextColor(Color.parseColor("#FF0000"));
            }
            return view;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private long get_present_count(ArrayList<String> arraylist){
        long count=0;
        for(String key:arraylist){
            if(person.dates.get(key).equals("Present")){
                count+=1;
            }
        }
        return count;
    }
}