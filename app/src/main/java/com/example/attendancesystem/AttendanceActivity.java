package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AttendanceActivity extends AppCompatActivity {
    ArrayList<Person> arrayList=new ArrayList<>();
    ArrayList<String> keys=new ArrayList<>();
    ListView listView;
    SharedPreferences get_event;
    event current_event;
    MyBaseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        get_event=getSharedPreferences("Events",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_event.getString("Current event","");
        current_event=gson.fromJson(json,event.class);
        TextView set_event_name=findViewById(R.id.message_event_name);
        TextView set_organisation_name=findViewById(R.id.message_organisation_name);
        set_event_name.setText(current_event.getName());
        set_organisation_name.setText(current_event.getOrganisation());
        adapter=new MyBaseAdapter(AttendanceActivity.this);
        listView=findViewById(R.id.list_view3);
        listView.setAdapter(adapter);
        Button submit=findViewById(R.id.attendance_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AttendanceActivity.this);
                alertDialog.setTitle("Submit Attendance?");
                alertDialog.setNegativeButton("Cancel",null);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            for(int i=0;i<arrayList.size();i++){
                                View newView=listView.getChildAt(i);
                                arrayList.get(i).increment_attendance_total();
                                CheckBox cb=newView.findViewById(R.id.ispresent);
                                if(cb.isChecked()){
                                    arrayList.get(i).increment_attendance();
                                }
                            }
                            /*int count=0;
                            for(Person person:arrayList){
                                arrayList.set(count,person);
                                count++;
                            }*/
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference databaseReference = database.getReference("Persons");
                            for(int i=0;i<keys.size();i++){
                                databaseReference.child(keys.get(i)).setValue(arrayList.get(i));
                            }
                            /*databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                        for (Person data : arrayList) {
                                            for (DataSnapshot child : children) {
                                                //Iterable<DataSnapshot> data = child.getChildren();
                                                //for (DataSnapshot Class : data) {
                                                Person person = child.getValue(Person.class);
                                                if (person.getCoordinator_email().equals(data.getCoordinator_email()) && person.getEvent_name().equals(data.getEvent_name()) && person.getOrganisation().equals(data.getOrganisation()) && person.getPerson_ID().equals(data.getPerson_ID()) && person.getPerson_email().equals(data.getPerson_email())) {
                                                    String key = child.getKey();
                                                    databaseReference.child(key).setValue(data);
                                                }
                                            }
                                        }
                                        finish();
                                    } catch (Exception e) {
                                        Log.e("Exception : ", e.getMessage());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });*/
                        } catch (Exception e) {

                        }
                    }
                });
                alertDialog.show();
            }
        });
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AttendanceActivity.this);
            builder.setTitle("No Internet");
            builder.setMessage("Please check your internet connection");
            builder.setPositiveButton("Ok", null);
            builder.setCancelable(false);
            builder.show();
        }
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference("Persons");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            //Iterable<DataSnapshot> data = child.getChildren();
                            //for (DataSnapshot Class : data) {
                            Person person = child.getValue(Person.class);
                            if (person.getCoordinator_email().equals(current_event.getCoordinator_email()) && person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation())) {
                                arrayList.add(person);
                                keys.add(child.getKey());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Exception : ", e.getMessage());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {

        }
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
            return arrayList.size();
        }

        @Override
        public Person getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.person_attendance,null);
            /*final Person std=arrayList.get(position);
            std.increment_attendance_total();*/
            //std.increment_attendance_total();
            TextView tv1=view.findViewById(R.id.disp_name);
            tv1.setText(arrayList.get(position).getFname()+" "+arrayList.get(position).getLname());
            TextView tv2=view.findViewById(R.id.disp_id);
            tv2.setText(arrayList.get(position).getPerson_ID());
            final CheckBox ispresent=view.findViewById(R.id.ispresent);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ispresent.isChecked()){
                        ispresent.setChecked(false);
                        //arrayList.get(position).decrement_attendance();
                    }
                    else{
                        ispresent.setChecked(true);
                        //arrayList.get(position).increment_attendance();
                    }
                }
            });
            return view;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        arrayList.clear();
    }
}
