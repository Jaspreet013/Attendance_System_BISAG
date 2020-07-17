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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
    ListView listView;
    ArrayList<Person> arrayList=new ArrayList<>();
    MyBaseAdapter adapter;
    Person std;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        get_event=getSharedPreferences("Events",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_event.getString("Current event","");
        current_event=gson.fromJson(json,event.class);
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Gson gson1=new Gson();
        String json1=get_user.getString("Current User","");
        User current_user=gson1.fromJson(json1,User.class);
        TextView set_event_name=findViewById(R.id.check_event_name);
        TextView set_organisation_name=findViewById(R.id.check_organisation_name);
        set_event_name.setText(current_event.getName());
        set_organisation_name.setText(current_event.getOrganisation());
        listView=findViewById(R.id.list_view2);
        adapter=new MyBaseAdapter(CheckAttendanceActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CheckAttendanceActivity.this);
            builder.setTitle("No Internet");
            builder.setMessage("Please check your internet connection");
            builder.setPositiveButton("Ok", null);
            builder.setCancelable(false);
            builder.show();
        }
        try {
            final ProgressDialog waiting;
            waiting = new ProgressDialog(CheckAttendanceActivity.this);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(CheckAttendanceActivity.this);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.attendance_schema,null);
            std=arrayList.get(position);
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
        finish();
    }
}
