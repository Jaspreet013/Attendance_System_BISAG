package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class selectedEventModificationActivity extends AppCompatActivity {
    SharedPreferences preferences,get_user;
    String id="";
    String key,event_key,Key;
    private ListView listView;
    ArrayList<String> keys=new ArrayList<>();
    EditText input;
    Button create_event,add_person;
    MyPeopleAdapter adapter;
    event current_event;
    ArrayList<Person> arrayList=new ArrayList<>();
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_event_modification);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        preferences = getSharedPreferences("Events", MODE_PRIVATE);
        input = new EditText(selectedEventModificationActivity.this);
        Gson gson = new Gson();
        String json = preferences.getString("Current event", "");
        event_key=preferences.getString("Key","");
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Key=get_user.getString("Key","");
        listView=findViewById(R.id.list_view1);
        adapter=new MyPeopleAdapter(selectedEventModificationActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        listView.setEmptyView(findViewById(R.id.empty_message));
        current_event = gson.fromJson(json, event.class);
        add_person=findViewById(R.id.add_person_button);
        create_event=findViewById(R.id.create_new_event);
        add_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(selectedEventModificationActivity.this,AddPerson.class));
                finish();
            }
        });
        create_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                }
                else {
                    ArrayList<Integer> list=new ArrayList<>();
                    for(int k=0;k<arrayList.size();k++){
                        if(!arrayList.get(k).getEnabled().equals("Yes")) {
                            list.add(k);
                        }
                    }
                    Collections.sort(list);
                    Collections.reverse(list);
                    if(list.size()!=arrayList.size()){
                        for(int k:list){
                            arrayList.remove(k);
                        }
                        SharedPreferences prefs = getSharedPreferences("All users", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(arrayList);
                        editor.putString("users", json);
                        editor.apply();
                        startActivity(new Intent(selectedEventModificationActivity.this, AddEventActivity.class));
                        finish();
                    }
                    else{
                        Toast.makeText(selectedEventModificationActivity.this,"All the people are excluded in this event",Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
        final TextView eventview = findViewById(R.id.eventView),organisationview=findViewById(R.id.event_organisation);
        eventview.setText(current_event.getName());
        organisationview.setText(current_event.getOrganisation());
        ImageButton delete_button=findViewById(R.id.deleteButton);
        try {
            final ProgressDialog waiting;
            waiting = new ProgressDialog(selectedEventModificationActivity.this);
            waiting.setMessage("Please Wait");
            waiting.setCancelable(false);
            waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waiting.show();
            if (!isNetworkAvailable()) {
                Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
            }
            else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = database.getReference("Persons/"+Key);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                Person person = child.getValue(Person.class);
                                if (person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation())) {
                                    arrayList.add(person);
                                    keys.add(child.getKey());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            waiting.dismiss();
                            TextView person_count = findViewById(R.id.disp_total_people);
                            person_count.setText("Total no. of People : "+arrayList.size());
                            if(arrayList.isEmpty()) {
                                create_event.setEnabled(false);
                                create_event.setBackgroundResource(R.drawable.disabled_button);
                            }
                        } catch (Exception e) {
                            Log.e("Exception : ", e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {

        }
        eventview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(selectedEventModificationActivity.this);
                alertDialog.setTitle("Rename Event");
                final EditText input = new EditText(selectedEventModificationActivity.this);
                input.setText(current_event.getName());
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isNetworkAvailable()) {
                                    Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                                }
                                else if (TextUtils.isEmpty(input.getText().toString().trim())) {
                                    Toast.makeText(selectedEventModificationActivity.this,"Event Name cannot be set as empty",Toast.LENGTH_SHORT).show();
                                }
                                else if(input.getText().toString().trim().length()>22){
                                    Toast.makeText(selectedEventModificationActivity.this,"Length cannot be more than 22",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    final ProgressDialog waiting;
                                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                    waiting.setMessage("Please Wait");
                                    waiting.setCancelable(false);
                                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    waiting.show();
                                    databaseReference = database.getReference("events/"+Key);
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            boolean set=true;
                                            event eve=new event();
                                            for(DataSnapshot child:children){
                                                event ev=child.getValue(event.class);
                                                if(input.getText().toString().trim().toUpperCase().equals(ev.getName().toUpperCase()) && current_event.getOrganisation().toUpperCase().equals(ev.getOrganisation().toUpperCase())){
                                                    set=false;
                                                    key="";
                                                    break;
                                                }
                                                else if(current_event.getName().equals(ev.getName()) && current_event.getOrganisation().toUpperCase().equals(ev.getOrganisation())){
                                                    key=child.getKey();
                                                    eve=ev;
                                                }
                                            }
                                            if(set){
                                                eve.setName(input.getText().toString().trim().toUpperCase());
                                                databaseReference.child(key).setValue(eve);
                                                databaseReference=database.getReference("Persons/"+Key);
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                                        for(DataSnapshot child:children){
                                                            Person person=child.getValue(Person.class);
                                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName())){
                                                                person.setEvent_name(input.getText().toString().trim().toUpperCase());
                                                                String key=child.getKey();
                                                                databaseReference.child(key).setValue(person);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                waiting.dismiss();
                                                Toast.makeText(selectedEventModificationActivity.this,"Event name Changed successfully",Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else{
                                                waiting.dismiss();
                                                Toast.makeText(selectedEventModificationActivity.this, "Your another event with same name and organisation already exists", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
                input.selectAll();
                input.requestFocus();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        organisationview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(selectedEventModificationActivity.this);
                alertDialog.setTitle("Rename Organisation");
                final EditText input = new EditText(selectedEventModificationActivity.this);
                input.setText(current_event.getOrganisation());
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                        }
                        else if (TextUtils.isEmpty(input.getText().toString().trim())) {
                            Toast.makeText(selectedEventModificationActivity.this,"Organisation cannot be set as empty",Toast.LENGTH_SHORT).show();
                        }
                        else if(input.getText().toString().trim().length()>22){
                            Toast.makeText(selectedEventModificationActivity.this,"Length cannot be more than 22",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            final ProgressDialog waiting;
                            waiting = new ProgressDialog(selectedEventModificationActivity.this);
                            waiting.setMessage("Please Wait");
                            waiting.setCancelable(false);
                            waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            waiting.show();
                            databaseReference = database.getReference("events/"+Key);
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                    boolean set=true;
                                    event eve=new event();
                                    for(DataSnapshot child:children){
                                        event ev=child.getValue(event.class);
                                        if(input.getText().toString().trim().toUpperCase().equals(ev.getOrganisation()) && current_event.getName().toUpperCase().equals(ev.getName().toUpperCase())){
                                            set=false;
                                            key="";
                                            break;
                                        }
                                        else if(current_event.getOrganisation().equals(ev.getOrganisation()) && current_event.getName().equals(ev.getName())){
                                            key=child.getKey();
                                            eve=ev;
                                        }
                                    }
                                    if(set){
                                        eve.setOrganisation(input.getText().toString().trim().toUpperCase());
                                        databaseReference.child(key).setValue(eve);
                                        databaseReference=database.getReference("Persons/"+Key);
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                                for(DataSnapshot child:children){
                                                    Person person=child.getValue(Person.class);
                                                    if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName())){
                                                        person.setOrganisation(input.getText().toString().trim().toUpperCase());
                                                        String key=child.getKey();
                                                        databaseReference.child(key).setValue(person);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        waiting.dismiss();
                                        Toast.makeText(selectedEventModificationActivity.this,"Organisation Changed successfully",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    else{
                                        waiting.dismiss();
                                        Toast.makeText(selectedEventModificationActivity.this, "Your another event with same name and organisation already exists", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = alertDialog.create();
                dialog.show();
                input.selectAll();
                input.requestFocus();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("Delete event");
                    builder.setMessage("Are you sure you want to delete all data linked with this event?");
                    builder.setNegativeButton("cancel",null);
                    final ProgressDialog waiting;
                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                    waiting.setMessage("Please Wait");
                    waiting.setCancelable(false);
                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            databaseReference = database.getReference("events/"+Key);
                            try {
                                waiting.show();
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        try {
                                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                            for (DataSnapshot child : children) {
                                                event ev = child.getValue(event.class);
                                                if (ev.getName().equals(current_event.getName()) && ev.getOrganisation().equals(current_event.getOrganisation())) {
                                                    child.getRef().removeValue();
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
                                databaseReference = database.getReference("Persons/"+Key);
                                for (String del : keys) {
                                    databaseReference.child(del).removeValue();
                                }
                                Toast.makeText(selectedEventModificationActivity.this,"Event Deleted Successfully",Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (Exception e) {

                            }
                        }
                    });
                    builder.show();
                }
            }
        });
    }
    public class MyPeopleAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflater;

        MyPeopleAdapter(Context context) {
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
            View view=inflater.inflate(R.layout.users_view, null);
            final Person std=arrayList.get(position);
            TextView tv1=view.findViewById(R.id.disp_name);
            tv1.setText(std.getFname()+" "+std.getLname());
            TextView tv2=view.findViewById(R.id.disp_id);
            tv2.setText(std.getPerson_ID());
            ImageButton button=view.findViewById(R.id.deleteButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("Are you sure to delete this person?");
                    builder.setMessage("All the information regarding this person for this event will be deleted from records");
                    builder.setNegativeButton("Cancel",null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isNetworkAvailable()) {
                                Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                final ProgressDialog waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                waiting.setMessage("Please Wait");
                                waiting.setCancelable(false);
                                waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                waiting.show();
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference reference = database.getReference("Persons/"+Key);
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final Set<String> events=arrayList.get(position).dates.keySet();
                                        reference.child(keys.get(position)).removeValue();
                                        arrayList.remove(position);
                                        final DatabaseReference dbreference=database.getReference("events/"+Key);
                                        dbreference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                event ev=dataSnapshot.child(event_key).getValue(event.class);
                                                for(String str:events){
                                                    long l=ev.dates.get(str);
                                                    Log.e("Key",str);
                                                    l--;
                                                    if(l==0){
                                                        ev.dates.remove(str);
                                                    }
                                                    else {
                                                        ev.dates.replace(str, l);
                                                    }
                                                }
                                                SharedPreferences get_event = getSharedPreferences("Events", MODE_PRIVATE);
                                                SharedPreferences.Editor prefsEditor = get_event.edit();
                                                Gson gson = new Gson();
                                                String json = gson.toJson(ev);
                                                prefsEditor.putString("Current event", json);
                                                prefsEditor.putString("Key",event_key);
                                                prefsEditor.apply();
                                                dbreference.child(event_key).setValue(ev);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        waiting.dismiss();
                                        TextView person_count = findViewById(R.id.disp_total_people);
                                        person_count.setText("Total no. of People : "+arrayList.size());
                                        adapter.notifyDataSetChanged();
                                        if(arrayList.isEmpty()) {
                                            create_event.setEnabled(false);
                                            create_event.setBackgroundResource(R.drawable.disabled_button);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences preferences=getSharedPreferences("Person",MODE_PRIVATE);
                    SharedPreferences.Editor pref=preferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(std);
                    pref.putString("Current Person", json);
                    pref.putString("Key",keys.get(position));
                    pref.apply();
                    startActivity(new Intent(selectedEventModificationActivity.this,ModifyAttendanceActivity.class));
                    finish();
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