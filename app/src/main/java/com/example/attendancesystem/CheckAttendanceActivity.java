package com.example.attendancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CheckAttendanceActivity extends AppCompatActivity {
    private Event current_event;
    private long present=0,absent=0,total=0;
    private ListView listView;
    private final ArrayList<Person> arrayList=new ArrayList<>();
    private final HashMap<String,String> keys=new HashMap<>();
    private MyBaseAdapter adapter;
    private String key,event_key;
    private DatabaseReference event,people;
    private TextView set_event_name,set_organisation_name,set_entry_name,presence,absence,text;
    private ProgressBar loading;
    private ImageButton delete;
    private HashMap<String, BitmapTypeRequest<String>> images=new HashMap<>();
    private long count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        key=getIntent().getStringExtra("Entry_Key");
        event_key=getIntent().getStringExtra("Event_Key");
        current_event=new Gson().fromJson(getIntent().getStringExtra("Event"), Event.class);
        set_event_name=findViewById(R.id.check_event_name);
        set_organisation_name=findViewById(R.id.check_organisation_name);
        set_entry_name = findViewById(R.id.check_entry_name);
        presence = findViewById(R.id.present_count);
        absence = findViewById(R.id.absent_count);
        text = findViewById(R.id.count);
        loading=findViewById(R.id.check_attendance_progress);
        event = FirebaseDatabase.getInstance().getReference("Events/"+event_key);
        people= FirebaseDatabase.getInstance().getReference("People/"+event_key);
        String date[]=key.split("-",5);
        String set;
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        SimpleDateFormat format1;
        if (!DateFormat.is24HourFormat(CheckAttendanceActivity.this))
        {
            format1=new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
        }
        else{
            format1=new SimpleDateFormat("dd/MM/yyyy  HH:mm");
        }
        try{
            set_entry_name.setText(format1.format(format.parse(key)).replace("am","AM").replace("pm","PM"));
        }
        catch (Exception e){}
        set_event_name.setText(current_event.getName());
        set_organisation_name.setText(current_event.getOrganisation());
        listView=findViewById(R.id.list_view2);
        adapter=new MyBaseAdapter(CheckAttendanceActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        delete=findViewById(R.id.deleteButton);
        set_entry_name.setVisibility(View.GONE);
        set_event_name.setVisibility(View.GONE);
        set_organisation_name.setVisibility(View.GONE);
        presence.setVisibility(View.GONE);
        absence.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckAttendanceActivity.this);
                builder.setTitle("Deleted data will not be recovered");
                builder.setMessage("Are you sure you want to delete this entry from this Event?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            if(!isNetworkAvailable()){
                                Toast.makeText(CheckAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                set_entry_name.setVisibility(View.GONE);
                                set_event_name.setVisibility(View.GONE);
                                set_organisation_name.setVisibility(View.GONE);
                                presence.setVisibility(View.GONE);
                                absence.setVisibility(View.GONE);
                                text.setVisibility(View.GONE);
                                delete.setVisibility(View.GONE);
                                listView.setVisibility(View.GONE);
                                loading.setVisibility(View.VISIBLE);
                                current_event.dates.remove(key);
                                event.setValue(current_event);
                                for (int i = 0; i < arrayList.size(); i++) {
                                    arrayList.get(i).dates.remove(key);
                                    arrayList.get(i).setAttendance(getPresentCount(i));
                                    arrayList.get(i).setAttendance_total(arrayList.get(i).dates.size());
                                    people.child(keys.get(arrayList.get(i).getPerson_ID())).setValue(arrayList.get(i));
                                }
                                Toast.makeText(CheckAttendanceActivity.this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent data= new Intent();
                                setResult(RESULT_OK,data.putExtra("Updated Data",new Gson().toJson(current_event)));
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
                loading.setVisibility(View.VISIBLE);
                people.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (final DataSnapshot child : children) {
                                final Person person = child.getValue(Person.class);
                                if(person.dates.containsKey(key)) {
                                    arrayList.add(person);
                                    count++;
                                    keys.put(person.getPerson_ID(),child.getKey());
                                    images.put(person.getPerson_ID(), Glide.with(CheckAttendanceActivity.this).load(person.getPhotourl()).asBitmap());
                                    adapter.notifyDataSetChanged();
                                }
                                if(count == current_event.dates.get(key)) {
                                    break;
                                }
                            }
                            for(Person person : arrayList) {
                                if(person.dates.get(key).equals("Present")) {
                                    present++;
                                }
                                else {
                                    absent++;
                                }
                                total++;
                            }
                            Collections.sort(arrayList);
                            presence.setText(presence.getText().toString() + present);
                            absence.setText(absence.getText().toString() + absent);
                            text.setText(text.getText().toString() + total);
                            loading.setVisibility(View.GONE);
                            set_event_name.setVisibility(View.VISIBLE);
                            set_organisation_name.setVisibility(View.VISIBLE);
                            set_entry_name.setVisibility(View.VISIBLE);
                            delete.setVisibility(View.VISIBLE);
                            presence.setVisibility(View.VISIBLE);
                            absence.setVisibility(View.VISIBLE);
                            text.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.VISIBLE);
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
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.attendance_view,null);
            TextView name=view.findViewById(R.id.dispname);
            name.setText(arrayList.get(position).getName());
            TextView id=view.findViewById(R.id.disporganisation);
            id.setText(arrayList.get(position).getPerson_ID());
            try {
                ImageView imageView=view.findViewById(R.id.person_image);
                images.get(arrayList.get(position).getPerson_ID()).into(imageView);
            }
            catch (Exception e){ }
            TextView status=view.findViewById(R.id.dispstatus);
            status.setText(arrayList.get(position).dates.get(key));
            if(arrayList.get(position).dates.get(key).equals("Absent")){
                status.setTextColor(Color.parseColor("#FF0000"));
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(CheckAttendanceActivity.this, AttendanceInfoActivity.class);
                    intent.putExtra("Person",new Gson().toJson(arrayList.get(position)));
                    intent.putExtra("Start_date",getIntent().getStringExtra("Start_date"));
                    intent.putExtra("End_date",getIntent().getStringExtra("End_date"));
                    startActivity(intent);
                }
            });
            return view;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private long getPresentCount (int i) {
        long count = 0;
        for (String str : arrayList.get(i).dates.keySet()) {
            if (arrayList.get(i).dates.get(str).equals("Present")) {
                count += 1;
            }
        }
        return count;
    }
}