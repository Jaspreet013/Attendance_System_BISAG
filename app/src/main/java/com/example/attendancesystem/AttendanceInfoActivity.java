package com.example.attendancesystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;

public class AttendanceInfoActivity extends AppCompatActivity {
    ArrayList<String> arraylist=new ArrayList<>();
    ListView listView;
    Person person;
    MyBaseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SharedPreferences sharedPreferences=getSharedPreferences("Person",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=sharedPreferences.getString("Current Person","");
        person=gson.fromJson(json,Person.class);
        TextView name=findViewById(R.id.disp_user_name);
        name.setText(person.getLname()+" "+person.getFname());
        TextView id=findViewById(R.id.disp_user_id);
        id.setText(id.getText()+person.getPerson_ID());
        TextView email=findViewById(R.id.disp_user_email);
        email.setText(email.getText().toString()+person.getPerson_email());
        TextView attendance=findViewById(R.id.disp_user_attendance);
        attendance.setText(attendance.getText().toString()+Long.toString(person.getAttendance()));
        TextView total_attendance=findViewById(R.id.disp_user_total_attendance);
        total_attendance.setText(total_attendance.getText().toString()+Long.toString(person.getAttendance_total()));
        TextView percent_attendance=findViewById(R.id.disp_user_percent_attendance);
        float percent;
        if(person.getAttendance()!=0) {
            percent = ((float)person.getAttendance() / (float)person.getAttendance_total()) * 100;
        }
        else{
            percent=0;
        }
        percent_attendance.setText(percent_attendance.getText().toString()+String.format("%.2f",percent)+"%");
        adapter=new MyBaseAdapter(AttendanceInfoActivity.this);
        listView=findViewById(R.id.list_view);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter);
        for(String i:person.dates.keySet()){
            arraylist.add(i);
        }
        Collections.sort(arraylist);
        Collections.reverse(arraylist);
        adapter.notifyDataSetChanged();
    }
    public class MyBaseAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflater;

        MyBaseAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return person.dates.size();
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
            String date[]=arraylist.get(position).split("-",5);
            tv1.setText(date[2]+"/"+date[1]+"/"+date[0]+"  "+date[3]+":"+date[4]);
            tv2.setText(person.dates.get(arraylist.get(position)));
            if(person.dates.get(arraylist.get(position)).equals("Absent")){
                tv2.setTextColor(Color.parseColor("#FF0000"));
            }
            return view;
        }
    }
}
