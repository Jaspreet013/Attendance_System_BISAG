package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AttendanceActivity extends AppCompatActivity {
    private final ArrayList<Person> arrayList=new ArrayList<>();
    private final ArrayList<String> keys=new ArrayList<>();
    private String key;
    private ListView listView;
    private Event current_event;
    private MyBaseAdapter adapter;
    private TextView selectall,total;
    private int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        current_event=new Gson().fromJson(getIntent().getStringExtra("Event"), Event.class);
        key=getIntent().getStringExtra("Key");
        final TextView set_event_name=findViewById(R.id.message_event_name);
        final TextView set_organisation_name=findViewById(R.id.message_organisation_name);
        set_event_name.setText(current_event.getName());
        set_organisation_name.setText(current_event.getOrganisation());
        adapter=new MyBaseAdapter(AttendanceActivity.this);
        listView=findViewById(R.id.list_view3);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        final Button submit=findViewById(R.id.attendance_submit_button);
        selectall=findViewById(R.id.select_all);
        total=findViewById(R.id.total_people);
        set_event_name.setVisibility(View.GONE);
        set_organisation_name.setVisibility(View.GONE);
        total.setVisibility(View.GONE);
        selectall.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        submit.setVisibility(View.GONE);
        selectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i;
                if(selectall.getText().equals("Select All")) {
                    for (i = 0; i < arrayList.size(); i++) {
                        arrayList.get(i).setIspresent(true);
                    }
                    selectall.setText("Clear All");
                    count=arrayList.size();
                    adapter.notifyDataSetChanged();
                }
                else{
                    for (i = 0; i < arrayList.size(); i++) {
                        arrayList.get(i).setIspresent(false);
                    }
                    selectall.setText("Select All");
                    count=0;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(AttendanceActivity.this);
                alertDialog.setTitle("Save this Entry?");
                alertDialog.setNegativeButton("Cancel",null);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            Toast.makeText(AttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            try {
                                final ProgressDialog waiting;
                                waiting = new ProgressDialog(AttendanceActivity.this);
                                waiting.setMessage("Please Wait");
                                waiting.setCancelable(false);
                                waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                waiting.show();
                                Date date=new Date();
                                SimpleDateFormat datef=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Events/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                                current_event.dates.put(datef.format(date),Long.parseLong(Integer.toString(arrayList.size())));
                                databaseReference.child(key).setValue(current_event);
                                for (int i = 0; i<arrayList.size(); i++) {
                                    if (arrayList.get(i).getIspresent()) {
                                        arrayList.get(i).dates.put(datef.format(date),"Present");
                                    }
                                    else{
                                        arrayList.get(i).dates.put(datef.format(date),"Absent");
                                    }
                                    arrayList.get(i).setAttendance(getPresentCount(i));
                                    arrayList.get(i).setnull();
                                    arrayList.get(i).setAttendance_total(arrayList.get(i).dates.size());
                                }
                                if(!current_event.dates.containsKey(datef.format(date))){
                                    Toast.makeText(AttendanceActivity.this,"Entry cannot be saved because you took attendance recently", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    DatabaseReference dbreference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+key);
                                    for (int i = 0; i < keys.size(); i++) {
                                        dbreference.child(keys.get(i)).setValue(arrayList.get(i));
                                    }
                                    Toast.makeText(AttendanceActivity.this,"Entry saved successfully",Toast.LENGTH_SHORT).show();
                                }
                                waiting.dismiss();
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    }
                });
                alertDialog.show();
            }
        });
        if (!isNetworkAvailable()) {
            Toast.makeText(AttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            final ProgressDialog waiting;
            waiting = new ProgressDialog(AttendanceActivity.this);
            waiting.setMessage("Please Wait");
            waiting.setCancelable(false);
            waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waiting.show();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+key);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            Person person = child.getValue(Person.class);
                            if(person.getEnabled().equals("Yes")) {
                                person.setIspresent(false);
                                arrayList.add(person);
                                keys.add(child.getKey());
                                adapter.notifyDataSetChanged();
                            }
                        }
                        total.setText(total.getText()+Integer.toString(arrayList.size()));
                        waiting.dismiss();
                        if(arrayList.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AttendanceActivity.this);
                            builder.setMessage("Please go to manage events -> (click on this Event) -> add new person to add people or include people if you have excluded them");
                            builder.setTitle("No people are in this Event");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            builder.show();
                        }
                        else{
                            listView.setAdapter(adapter);
                            set_event_name.setVisibility(View.VISIBLE);
                            set_organisation_name.setVisibility(View.VISIBLE);
                            selectall.setVisibility(View.VISIBLE);
                            total.setVisibility(View.VISIBLE);
                            submit.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.VISIBLE);
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
    private long getPresentCount(int i) {
        long pcount = 0;
        for (String str : arrayList.get(i).dates.keySet()) {
            if (arrayList.get(i).dates.get(str).equals("Present")) {
                pcount += 1;
            }
        }
        return pcount;
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
            View view = inflater.inflate(R.layout.person_attendance,null);
            TextView tv1 = view.findViewById(R.id.disp_name);
            tv1.setText(arrayList.get(position).getFname() + " " + arrayList.get(position).getLname());
            TextView tv2 = view.findViewById(R.id.disp_id);
            tv2.setText(arrayList.get(position).getPerson_ID());
            final CheckBox ispresent = view.findViewById(R.id.ispresent);
            if(arrayList.get(position).getIspresent()){
                ispresent.setChecked(true);
            }
            if(count==arrayList.size()){
                selectall.setText("Clear All");
            }
            ispresent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrayList.get(position).getIspresent()) {
                        ispresent.setChecked(false);
                        arrayList.get(position).setIspresent(false);
                        count--;
                        selectall.setText("Select All");
                    }
                    else {
                        ispresent.setChecked(true);
                        arrayList.get(position).setIspresent(true);
                        count++;
                        if(count==arrayList.size()){
                            selectall.setText("Clear All");
                        }
                    }
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (arrayList.get(position).getIspresent()) {
                        ispresent.setChecked(false);
                        arrayList.get(position).setIspresent(false);
                        count--;
                        selectall.setText("Select All");
                    }
                    else {
                        ispresent.setChecked(true);
                        arrayList.get(position).setIspresent(true);
                        count++;
                        if(count==arrayList.size()){
                            selectall.setText("Clear All");
                        }
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
        AlertDialog.Builder builder=new AlertDialog.Builder(AttendanceActivity.this);
        builder.setMessage("Entry will not be saved");
        builder.setTitle("Are you sure to go back?");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
    }
}