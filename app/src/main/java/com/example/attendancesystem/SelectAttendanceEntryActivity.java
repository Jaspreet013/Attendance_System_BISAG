package com.example.attendancesystem;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class SelectAttendanceEntryActivity extends AppCompatActivity {
    SharedPreferences get_event,get_user;
    event current_event;
    ListView listView;
    ArrayList<String> arrayList=new ArrayList<>();
    MyBaseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_attendance_entry);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        get_event=getSharedPreferences("Events",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_event.getString("Current event","");
        current_event=gson.fromJson(json,event.class);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Gson gson1=new Gson();
        String json1=get_user.getString("Current User","");
        User current_user=gson1.fromJson(json1,User.class);
        /*TextView set_event_name=findViewById(R.id.check_event_name);
        TextView set_organisation_name=findViewById(R.id.check_organisation_name);
        set_event_name.setText(current_event.getName());
        set_organisation_name.setText(current_event.getOrganisation());*/
        listView=findViewById(R.id.list_view3);
        adapter=new MyBaseAdapter(SelectAttendanceEntryActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setEmptyView(findViewById(R.id.select_empty_message));
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
            builder.setTitle("No Internet");
            builder.setMessage("Please check your internet connection");
            builder.setPositiveButton("Ok", null);
            builder.setCancelable(false);
            builder.show();
        }
        for(String i:current_event.dates.keySet()) {
            arrayList.add(i);
        }
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        adapter.notifyDataSetChanged();
        /*try {
            final ProgressDialog waiting;
            waiting = new ProgressDialog(SelectAttendanceEntryActivity.this);
            waiting.setMessage("Please Wait");
            waiting.setCancelable(false);
            waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waiting.show();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference("Persons/"+current_user.getEmail().replace(".",""));
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            Person person = child.getValue(Person.class);
                            if (person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation())) {
                                arrayList.add(person);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        waiting.dismiss();
                        Log.e(Integer.toString(listView.getChildCount()),Integer.toString(arrayList.size()));
                        if(arrayList.size()==0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
                            builder.setMessage("Please go to modify events -> (click on this event) -> add new person to add people");
                            builder.setTitle("No people are in this event");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            builder.show();
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

        }*/
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
        public String getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.event_list_view,null);
            TextView tv=view.findViewById(R.id.dispname);
            String str[]=arrayList.get(position).split("-",5);
            tv.setText(str[2]+"/"+str[1]+"/"+str[0]+"  "+str[3]+":"+str[4]);
            TextView tv2=view.findViewById(R.id.disporganisation);
            tv2.setText("Total People : "+current_event.dates.get(arrayList.get(position)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor=get_event.edit();
                    editor.putString("Key",arrayList.get(position));
                    editor.apply();
                    startActivity(new Intent(SelectAttendanceEntryActivity.this,CheckAttendanceActivity.class));
                    finish();
                }
            });
            /*final Person std=arrayList.get(position);
            float percent;
            TextView check_name=view.findViewById(R.id.check_name);
            TextView check_id=view.findViewById(R.id.check_id);
            TextView check_email=view.findViewById(R.id.check_email);
            TextView check_attendance=view.findViewById(R.id.check_attendance);
            TextView check_total_attendance=view.findViewById(R.id.check_total_attendance);
            TextView attendance_percent=view.findViewById(R.id.attendance_percent);
            if(std.getAttendance()==0){
                percent=(float)0.00;
            }
            else {
                percent = ((float) std.getAttendance() / (float) std.getAttendance_total()) * 100;
            }
            check_name.setText(std.getFname()+" "+std.getLname());
            check_id.setText(std.getPerson_ID());
            check_email.setText(std.getPerson_email());
            check_attendance.setText(
                    "Attendance Present(no): "+Long.toString(std.getAttendance()));
            check_total_attendance.setText(
                    "Total Attendance taken : "+Long.toString(std.getAttendance_total()));
            attendance_percent.setText(
                    "Attendance (in Percent): "+String.format("%.2f",percent)+"%");
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(std.getAttendance_total()==0){
                        Toast.makeText(SelectAttendanceEntryActivity.this,"No attendance is taken for this user",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        SharedPreferences sharedPreferences = getSharedPreferences("Person", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(std);
                        editor.putString("Current Person", json);
                        editor.apply();
                        startActivity(new Intent(SelectAttendanceEntryActivity.this, AttendanceInfoActivity.class));
                    }
                }
            });*/
            return view;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
