package com.example.attendancesystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import java.util.ArrayList;

public class SelectSubjectActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<event> arrayList=new ArrayList<>();
    private ArrayList<String> keys=new ArrayList<>();
    MyBaseAdapter adapter;
    SharedPreferences get_event;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_subject);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        listView=findViewById(R.id.list_view3);
        adapter=new MyBaseAdapter(SelectSubjectActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        listView.setEmptyView(findViewById(R.id.select_empty_message));
        if (!isNetworkAvailable()) {
            Toast.makeText(SelectSubjectActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                final ProgressDialog waiting;
                waiting = new ProgressDialog(SelectSubjectActivity.this);
                waiting.setMessage("Please Wait");
                waiting.setCancelable(false);
                waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                waiting.show();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = database.getReference("events/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                event ev = child.getValue(event.class);
                                arrayList.add(ev);
                                keys.add(child.getKey());
                                adapter.notifyDataSetChanged();
                            }
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
        public event getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.event_list_view, null);
            final event std=arrayList.get(position);
            TextView tv1=view.findViewById(R.id.dispname);
            tv1.setText(std.getName());
            TextView tv2=view.findViewById(R.id.disporganisation);
            tv2.setText(std.getOrganisation());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(SelectSubjectActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        get_event = getSharedPreferences("Events", MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = get_event.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(std);
                        prefsEditor.putString("Current event", json);
                        prefsEditor.putString("Key",keys.get(position));
                        prefsEditor.apply();
                        startActivity(new Intent(SelectSubjectActivity.this, AttendanceActivity.class));
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
}