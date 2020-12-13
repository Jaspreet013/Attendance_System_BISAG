package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.util.ArrayList;

public class CheckAttendanceActivity extends AppCompatActivity {
    SharedPreferences get_event,get_user;
    event current_event;
    long present=0,absent=0,total=0;
    ListView listView;
    ArrayList<Person> arrayList=new ArrayList<>();
    ArrayList<String> keys=new ArrayList<>();
    MyBaseAdapter adapter;
    String key,event_key,Key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        get_event=getSharedPreferences("Events",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_event.getString("Current event","");
        key=get_event.getString("Key","");
        event_key=get_event.getString("Event key","");
        current_event=gson.fromJson(json,event.class);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Key=get_user.getString("Key","");
        TextView set_event_name=findViewById(R.id.check_event_name);
        TextView set_organisation_name=findViewById(R.id.check_organisation_name);
        set_event_name.setText(current_event.getName());
        set_organisation_name.setText(current_event.getOrganisation());
        listView=findViewById(R.id.list_view2);
        adapter=new MyBaseAdapter(CheckAttendanceActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        ImageButton delete=findViewById(R.id.deleteButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckAttendanceActivity.this);
                builder.setTitle("Deleted data will not be recovered");
                builder.setMessage("Are you sure you want to delete this entry from this event?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            if(!isNetworkAvailable()){
                                Toast.makeText(CheckAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = database.getReference("events/" + Key);
                                current_event.dates.remove(key);
                                databaseReference.child(event_key).setValue(current_event);
                                databaseReference = database.getReference("Persons/" + Key);
                                for (int i = 0; i < arrayList.size(); i++) {
                                    arrayList.get(i).dates.remove(key);
                                    arrayList.get(i).setAttendance(getPresentCount(i));
                                    arrayList.get(i).setAttendance_total(arrayList.get(i).dates.size());
                                    databaseReference.child(keys.get(i)).setValue(arrayList.get(i));
                                }
                                Toast.makeText(CheckAttendanceActivity.this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        catch (Exception e){

                        }
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.setCancelable(true);
                builder.show();
            }
        });
        if (!isNetworkAvailable()) {
            Toast.makeText(CheckAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            try {
                final ProgressDialog waiting;
                waiting = new ProgressDialog(CheckAttendanceActivity.this);
                waiting.setMessage("Please Wait");
                waiting.setCancelable(false);
                waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                waiting.show();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = database.getReference("Persons/" + Key);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            long count = 0;
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                Person person = child.getValue(Person.class);
                                if (person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation()) && person.dates.containsKey(key)) {
                                    arrayList.add(person);
                                    count++;
                                    keys.add(child.getKey());
                                    adapter.notifyDataSetChanged();
                                }
                                if (count == current_event.dates.get(key)) {
                                    break;
                                }
                            }
                            for (Person person : arrayList) {
                                if (person.dates.get(key).equals("Present")) {
                                    present++;
                                } else {
                                    absent++;
                                }
                                total++;
                            }
                            TextView presence = findViewById(R.id.present_count);
                            presence.setText(presence.getText().toString() + present);
                            TextView absence = findViewById(R.id.absent_count);
                            absence.setText(absence.getText().toString() + absent);
                            TextView text = findViewById(R.id.count);
                            text.setText(text.getText().toString() + total);
                            waiting.dismiss();
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
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.attendance_view,null);
            TextView name=view.findViewById(R.id.dispname);
            name.setText(arrayList.get(position).getFname()+" "+arrayList.get(position).getLname());
            TextView id=view.findViewById(R.id.disporganisation);
            id.setText(arrayList.get(position).getPerson_ID());
            TextView status=view.findViewById(R.id.dispstatus);
            status.setText(arrayList.get(position).dates.get(key));
            if(arrayList.get(position).dates.get(key).equals("Absent")){
                status.setTextColor(Color.parseColor("#FF0000"));
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = getSharedPreferences("Person", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(arrayList.get(position));
                    editor.putString("Current Person", json);
                    editor.apply();
                    startActivity(new Intent(CheckAttendanceActivity.this, AttendanceInfoActivity.class));
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
    public long getPresentCount (int i) {
        long count = 0;
        for (String str : arrayList.get(i).dates.keySet()) {
            if (arrayList.get(i).dates.get(str).equals("Present")) {
                count += 1;
            }
        }
        return count;
    }
}