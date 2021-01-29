package com.example.attendancesystem;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class AttendanceInfoActivity extends AppCompatActivity {
    private final ArrayList<String> arraylist=new ArrayList<>();
    private Person person;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        person=new Gson().fromJson(getIntent().getStringExtra("Person"),Person.class);
        TextView name=findViewById(R.id.disp_user_name);
        TextView duration=findViewById(R.id.disp_duration);
        name.setText(person.getName());
        String start_date=getIntent().getStringExtra("Start_date");
        String end_date=getIntent().getStringExtra("End_date");
        duration.setText("Duration : "+start_date+" to "+end_date);
        TextView id=findViewById(R.id.disp_user_id);
        id.setText(id.getText()+person.getPerson_ID());
        TextView email=findViewById(R.id.disp_user_email);
        email.setText(email.getText().toString()+person.getEmail());
        TextView attendance=findViewById(R.id.disp_user_attendance);
        ImageView imageView=findViewById(R.id.person_image);
        Glide.with(this).load(person.getPhotourl()).into(imageView);
        MyBaseAdapter adapter = new MyBaseAdapter(AttendanceInfoActivity.this);
        ListView listView = findViewById(R.id.list_view);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format1=new SimpleDateFormat("dd/MM/yyyy");
        float percent;
        long present_count;
        if(start_date.equals("Start") && end_date.equals("Today")) {
            if (person.getAttendance() != 0) {
                percent = ((float) person.getAttendance() / (float) person.getAttendance_total()) * 100;
                attendance.setText("Attendance : " + person.getAttendance() + "/" + person.getAttendance_total() + " -- " + String.format("%.2f", percent) + "%");
            } else {
                attendance.setText("Attendance : " + person.getAttendance() + "/" + person.getAttendance_total());
            }
            arraylist.addAll(person.dates.keySet());
        }
        else if(start_date.equals("Start")){
            try {
                Date end=format1.parse(end_date);
                for(String key:person.dates.keySet()){
                    if(format.parse(key).compareTo(end)<=0){
                        arraylist.add(key);
                    }
                }
                present_count=get_present_count(arraylist);
                if(present_count!=0) {
                    percent = ((float) present_count / (float) arraylist.size())*100;
                    attendance.setText("Attendance : " + present_count + "/" + arraylist.size() + " -- " + String.format("%.2f", percent) + "%");
                } else {
                    attendance.setText("Attendance : " + present_count + "/" + arraylist.size());
                }
            }
            catch (Exception e){
                Log.e("Exception",e.getMessage());
            }
        }
        else if(end_date.equals("Today")){
            try{
                Date start=format1.parse(start_date);
                for(String key:person.dates.keySet()){
                    if(format.parse(key).compareTo(start)>=0){
                        arraylist.add(key);
                    }
                }
                present_count=get_present_count(arraylist);
                if(present_count!=0) {
                    percent = ((float) present_count / (float) arraylist.size())*100;
                    attendance.setText("Attendance : " + present_count + "/" + arraylist.size() + " -- " + String.format("%.2f", percent) + "%");
                } else {
                    attendance.setText("Attendance : " + present_count + "/" + arraylist.size());
                }
            }
            catch (Exception e){}
        }
        else{
            try{
                Date start=format1.parse(start_date);
                Date end=format1.parse(end_date);
                for(String key:person.dates.keySet()){
                    if(format.parse(key).compareTo(start)>=0 && format.parse(key).compareTo(end)<=0){
                        arraylist.add(key);
                    }
                }
                present_count=get_present_count(arraylist);
                if(present_count!=0) {
                    percent = ((float) present_count / (float) arraylist.size())*100;
                    attendance.setText("Attendance : " + present_count + "/" + arraylist.size() + " -- " + String.format("%.2f", percent) + "%");
                } else {
                    attendance.setText("Attendance : " + present_count + "/" + arraylist.size());
                }
            }
            catch (Exception e){}
        }
        Collections.sort(arraylist);
        Collections.reverse(arraylist);
        adapter.notifyDataSetChanged();
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
            return arraylist.size();
        }
        @Override
        public String getItem(int position) {
            return arraylist.get(position);
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
            if (!DateFormat.is24HourFormat(AttendanceInfoActivity.this))
            {
                format1=new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
            }
            else{
                format1=new SimpleDateFormat("dd/MM/yyyy  HH:mm");
            }
            try{
                tv1.setText(format1.format(format.parse(arraylist.get(position))).replace("am","AM").replace("pm","PM"));
            }
            catch (Exception e){}
            tv2.setText(person.dates.get(arraylist.get(position)));
            if(person.dates.get(arraylist.get(position)).equals("Absent")){
                tv2.setTextColor(Color.parseColor("#FF0000"));
            }
            return view;
        }
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