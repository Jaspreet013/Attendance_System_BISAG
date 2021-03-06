package com.example.attendancesystem;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

public class ModifyEventActivity extends AppCompatActivity {
    private ListView listView;
    private final HashMap<String,Event> event_data = new HashMap<>();
    private final HashMap<String,String> keys=new HashMap<>();
    private final ArrayList<String> dates=new ArrayList<>();
    private MyBaseAdapter adapter;
    private TextView total_events;
    private DatabaseReference events;
    private ProgressBar loading;
    private Button add_event;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        add_event = findViewById(R.id.add_event_button);
        listView = findViewById(R.id.list_view);
        total_events = findViewById(R.id.total_events);
        loading=findViewById(R.id.check_attendance_progress);
        adapter = new MyBaseAdapter(ModifyEventActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        loading.setVisibility(View.VISIBLE);
        total_events.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        add_event.setVisibility(View.GONE);
        DatabaseReference user_database = FirebaseDatabase.getInstance().getReference("Users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
        events=FirebaseDatabase.getInstance().getReference("Events");
        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ModifyEventActivity.this, AddEventActivity.class);
                startActivityForResult(intent,RESULT_FIRST_USER);
            }
        });
        if (!isNetworkAvailable()) {
            Toast.makeText(ModifyEventActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
        }
        else {
            try{
                user_database.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            user = dataSnapshot.getValue(User.class);
                            events.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        for (String key : user.admin_events.keySet()) {
                                            Event ev = dataSnapshot.child(user.admin_events.get(key)).getValue(Event.class);
                                            dates.add(key);
                                            event_data.put(key,ev);
                                            keys.put(ev.getName() + ", " + ev.getOrganisation(), user.admin_events.get(key));
                                            adapter.notifyDataSetChanged();
                                        }
                                        Collections.sort(dates);
                                        Collections.reverse(dates);
                                        loading.setVisibility(View.GONE);
                                        total_events.setText(total_events.getText().toString() + dates.size());
                                        total_events.setVisibility(View.VISIBLE);
                                        listView.setVisibility(View.VISIBLE);
                                        add_event.setVisibility(View.VISIBLE);
                                        listView.setEmptyView(findViewById(R.id.modification_empty_message));
                                    } catch (Exception e) {
                                        Log.e("Exception", e.getMessage());
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        catch (Exception e){
                            Log.e("Exception",e.getMessage());
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
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.event_list_view, null);
            final Event std = event_data.get(dates.get(position));
            TextView tv1 = view.findViewById(R.id.dispname);
            tv1.setText(std.getName());
            TextView tv2 = view.findViewById(R.id.disporganisation);
            tv2.setText(std.getOrganisation());
            TextView tv3 = view.findViewById(R.id.coordinator_name);
            String[] str=dates.get(position).split("-",6);
            tv3.setText("Created on "+str[2]+"/"+str[1]+"/"+str[0]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(ModifyEventActivity.this, SelectedEventModificationActivity.class);
                    intent.putExtra("Event",new Gson().toJson(std));
                    intent.putExtra("Key",keys.get(std.getName()+", "+std.getOrganisation()));
                    startActivityForResult(intent,RESULT_FIRST_USER);
                }
            });
            return view;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
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