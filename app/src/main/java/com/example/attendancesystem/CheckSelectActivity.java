package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CheckSelectActivity extends AppCompatActivity {
    private ListView listView;
    private final ArrayList<String> dates=new ArrayList<>();
    private final HashMap<String,Event> event_data=new HashMap<>();
    private final HashMap<String,String> keys=new HashMap<>();
    private MyBaseAdapter adapter;
    private TextView textView;
    private ProgressBar loading;
    private DatabaseReference events,users_database;
    private User user=new User();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_select);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        listView=findViewById(R.id.list_view3);
        textView=findViewById(R.id.select_subject_text);
        adapter=new MyBaseAdapter(CheckSelectActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        events = FirebaseDatabase.getInstance().getReference("Events");
        users_database=FirebaseDatabase.getInstance().getReference("Users");
        loading=findViewById(R.id.check_attendance_progress);
        textView.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        if (!isNetworkAvailable()) {
            Toast.makeText(CheckSelectActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                users_database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(User.class);
                        events.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    for(String key:user.admin_events.keySet()){
                                        Event ev = dataSnapshot.child(user.admin_events.get(key)).getValue(Event.class);
                                        event_data.put(key,ev);
                                        dates.add(key);
                                        keys.put(ev.getName()+", "+ev.getOrganisation(),user.admin_events.get(key));
                                        adapter.notifyDataSetChanged();
                                    }
                                    loading.setVisibility(View.GONE);
                                    Collections.sort(dates);
                                    Collections.reverse(dates);
                                    textView.setText("Total Events : "+dates.size());
                                    textView.setVisibility(View.VISIBLE);
                                    listView.setVisibility(View.VISIBLE);
                                    listView.setEmptyView(findViewById(R.id.select_empty_message));
                                } catch (Exception e) {
                                    Log.e("Exception : ", e.getMessage());
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
            return dates.size();
        }

        @Override
        public String getItem(int position) {
            return dates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.event_list_view, null);
            final Event std=event_data.get(dates.get(position));
            TextView tv1=view.findViewById(R.id.dispname);
            tv1.setText(std.getName());
            TextView tv2=view.findViewById(R.id.disporganisation);
            tv2.setText(std.getOrganisation());
            TextView tv3=view.findViewById(R.id.coordinator_name);
            tv3.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(CheckSelectActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent=new Intent(CheckSelectActivity.this, SelectAttendanceEntryActivity.class);
                        intent.putExtra("Event",new Gson().toJson(std));
                        intent.putExtra("Event_Key",keys.get(std.getName()+", "+std.getOrganisation()));
                        startActivityForResult(intent,RESULT_FIRST_USER);
                    }
                }
            });
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            recreate();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}