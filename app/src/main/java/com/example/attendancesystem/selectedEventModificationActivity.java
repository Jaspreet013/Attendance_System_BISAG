package com.example.attendancesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class selectedEventModificationActivity extends AppCompatActivity {
    SharedPreferences preferences,get_user;
    String id="";
    String key;
    private ListView listView;
    User current_user;
    ArrayList<String> keys=new ArrayList<>();
    EditText input;
    long count=0;
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
        get_user = getSharedPreferences("User",MODE_PRIVATE);
        Gson gson1=new Gson();
        String json1=get_user.getString("Current User","");
        current_user=gson1.fromJson(json1,User.class);
        listView=findViewById(R.id.list_view1);
        adapter=new MyPeopleAdapter(selectedEventModificationActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setEmptyView(findViewById(R.id.empty_message));
        current_event = gson.fromJson(json, event.class);
        Button add_person=findViewById(R.id.add_person_button);
        Button create_event=findViewById(R.id.create_new_event);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else if(arrayList.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("There are currently no people in this event");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else {
                    SharedPreferences prefs = getSharedPreferences("All users", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(arrayList);
                    editor.putString("users", json);
                    editor.apply();
                    startActivity(new Intent(selectedEventModificationActivity.this, AddEventActivity.class));
                    finish();
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
            waiting.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
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
                                keys.add(child.getKey());
                                count++;
                                adapter.notifyDataSetChanged();
                            }
                        }
                        waiting.dismiss();
                        TextView person_count=findViewById(R.id.display_total_people);
                        person_count.setText(Long.toString(count));
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                                    builder.setTitle("No Internet");
                                    builder.setMessage("Please check your internet connection");
                                    builder.setPositiveButton("Ok", null);
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                                else if(input.getText().toString().length()>20){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                                    builder.setTitle("Length cannot be more than 20");
                                    builder.setPositiveButton("Ok", null);
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                                else {
                                    final ProgressDialog waiting;
                                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                    waiting.setMessage("Please Wait");
                                    waiting.setCancelable(false);
                                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    waiting.show();
                                    databaseReference = database.getReference("events/"+current_user.getEmail().replace(".",""));
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            boolean set=true;
                                            event eve=new event();
                                            for(DataSnapshot child:children){
                                                event ev=child.getValue(event.class);
                                                if(input.getText().toString().toUpperCase().equals(ev.getName().toUpperCase()) && current_event.getOrganisation().toUpperCase().equals(ev.getOrganisation().toUpperCase())){
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
                                                eve.setName(input.getText().toString().toUpperCase());
                                                databaseReference.child(key).setValue(eve);
                                                databaseReference=database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                                        for(DataSnapshot child:children){
                                                            Person person=child.getValue(Person.class);
                                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName())){
                                                                person.setEvent_name(input.getText().toString().toUpperCase());
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
                                                Toast.makeText(selectedEventModificationActivity.this,"Name Changed successfully",Toast.LENGTH_SHORT).show();
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
                alertDialog.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (!isNetworkAvailable()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                                    builder.setTitle("No Internet");
                                    builder.setMessage("Please check your internet connection");
                                    builder.setPositiveButton("Ok", null);
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                                else if(input.getText().toString().length()>20){
                                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                                    builder.setTitle("Length cannot be more than 20");
                                    builder.setPositiveButton("Ok", null);
                                    builder.setCancelable(false);
                                    builder.show();
                                }
                                else {
                                    final ProgressDialog waiting;
                                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                    waiting.setMessage("Please Wait");
                                    waiting.setCancelable(false);
                                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    waiting.show();
                                    databaseReference = database.getReference("events/"+current_user.getEmail().replace(".",""));
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            boolean set=true;
                                            event eve=new event();
                                            for(DataSnapshot child:children){
                                                event ev=child.getValue(event.class);
                                                if(input.getText().toString().toUpperCase().equals(ev.getOrganisation()) && current_event.getName().toUpperCase().equals(ev.getName().toUpperCase())){
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
                                                eve.setOrganisation(input.getText().toString().toUpperCase());
                                                databaseReference.child(key).setValue(eve);
                                                databaseReference=database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                                        for(DataSnapshot child:children){
                                                            Person person=child.getValue(Person.class);
                                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName())){
                                                                person.setOrganisation(input.getText().toString().toUpperCase());
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("No Internet");
                    builder.setMessage("Please check your internet connection");
                    builder.setPositiveButton("Ok", null);
                    builder.setCancelable(false);
                    builder.show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("Delete event?");
                    builder.setMessage("Are you sure you want to delete all data linked with this event");
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
                            DatabaseReference databaseReference = database.getReference("events/"+current_user.getEmail().replace(".",""));
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
                                                    Toast.makeText(selectedEventModificationActivity.this,"Event Deleted Successfully",Toast.LENGTH_SHORT).show();
                                                    waiting.dismiss();
                                                    finish();
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
                    });
                    builder.show();
                }
                try {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference databaseReference = database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                final ArrayList<String> key=new ArrayList<>();
                                for (DataSnapshot child : children) {
                                    Person person = child.getValue(Person.class);
                                    if (person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation())) {
                                        key.add(child.getKey());
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                for(String del:key){
                                    databaseReference.child(del).removeValue();
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
                    builder.setTitle("Are you sure to delete this User?");
                    builder.setMessage("All the information regarding this user will be deleted");
                    builder.setNegativeButton("Cancel",null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final ProgressDialog waiting = new ProgressDialog(selectedEventModificationActivity.this);
                            waiting.setMessage("Please Wait");
                            waiting.setCancelable(false);
                            waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            waiting.show();
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            final DatabaseReference reference=database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                    reference.child(keys.get(position)).removeValue();
                                    waiting.dismiss();
                                    recreate();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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