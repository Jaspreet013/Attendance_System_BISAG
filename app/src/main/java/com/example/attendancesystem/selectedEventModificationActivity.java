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
    SharedPreferences preferences;
    String id="";
    String key;
    private ListView listView;
    event ev;
    EditText input;
    long count=0;
    boolean set=true;
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
        SharedPreferences.Editor edit = preferences.edit();
        input = new EditText(selectedEventModificationActivity.this);
        Gson gson = new Gson();
        String json = preferences.getString("Current event", "");
        listView=findViewById(R.id.list_view1);
        adapter=new MyPeopleAdapter(selectedEventModificationActivity.this);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.empty_message));
        current_event = gson.fromJson(json, event.class);
        Button add_person=findViewById(R.id.add_person_button);
        add_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(selectedEventModificationActivity.this,AddPerson.class));
                finish();
            }
        });
        final TextView eventview = findViewById(R.id.eventView),organisationview=findViewById(R.id.event_organisation);
        eventview.setText(current_event.getName());
        organisationview.setText(current_event.getOrganisation());
        ImageButton delete_button=findViewById(R.id.deleteButton);
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference("Persons");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot child : children) {
                            //Iterable<DataSnapshot> data = child.getChildren();
                            //for (DataSnapshot Class : data) {
                            Person person = child.getValue(Person.class);
                            if (person.getCoordinator_email().equals(current_event.getCoordinator_email()) && person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation())) {
                                arrayList.add(person);
                                count++;
                                adapter.notifyDataSetChanged();
                            }
                        }
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
                                else {
                                    final ProgressDialog waiting;
                                    databaseReference = database.getReference("events");
                                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                    waiting.setMessage("Please Wait");
                                    waiting.setCancelable(false);
                                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    waiting.show();
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            set=true;
                                            for(DataSnapshot child:children){
                                                ev=child.getValue(event.class);
                                                if(input.getText().toString().toUpperCase().equals(ev.getName()) && current_event.getOrganisation().toUpperCase().equals(ev.getOrganisation()) && current_event.getCoordinator_email().equals(ev.getCoordinator_email())){
                                                    set=false;
                                                    key="";
                                                }
                                                else if(current_event.getName().equals(ev.getName()) && current_event.getOrganisation().toUpperCase().equals(ev.getOrganisation()) && current_event.getCoordinator_email().equals(ev.getCoordinator_email())){
                                                    key=child.getKey();
                                                }
                                            }
                                            if(set){
                                                ev.setName(input.getText().toString().toUpperCase());
                                                databaseReference.child(key).setValue(ev);
                                                databaseReference=database.getReference("Persons");
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                                        for(DataSnapshot child:children){
                                                            Person person=child.getValue(Person.class);
                                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && person.getCoordinator_email().equals(current_event.getCoordinator_email())){
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
                                    /*if (!(input.getText().toString().equals(current_event.getName().toUpperCase()) || input.getText().toString().equals(""))) {
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    waiting.dismiss();
                                                    Toast.makeText(selectedEventModificationActivity.this, "Your another event with same name and organisation already exists", Toast.LENGTH_SHORT).show();

                                                    finish();
                                                } else {
                                                    //event ev = dataSnapshot.getValue(event.class);
                                                    databaseReference.getRef().removeValue();
                                                    DatabaseReference dbreference = database.getReference("events/" + current_event.getOrganisation().toUpperCase() + "/" + current_event.getName().toUpperCase() + ", " + current_event.getCoordinator_email().replace(".", "").toUpperCase());
                                                    dbreference.removeValue();
                                                    current_event.setName(input.getText().toString().toUpperCase());
                                                    databaseReference.setValue(current_event);
                                                    waiting.dismiss();
                                                    Toast.makeText(selectedEventModificationActivity.this, "Name changed successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }*/
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
                                else {
                                    final ProgressDialog waiting;
                                    databaseReference = database.getReference("events");
                                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                    waiting.setMessage("Please Wait");
                                    waiting.setCancelable(false);
                                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    waiting.show();
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            for(DataSnapshot child:children){
                                                ev=child.getValue(event.class);
                                                if(input.getText().toString().toUpperCase().equals(ev.getOrganisation()) && current_event.getName().equals(ev.getName()) && current_event.getCoordinator_email().equals(ev.getCoordinator_email())){
                                                    set=false;
                                                    key="";
                                                }
                                                else if(current_event.getOrganisation().equals(ev.getOrganisation()) && current_event.getName().equals(ev.getName()) && current_event.getCoordinator_email().equals(ev.getCoordinator_email())){
                                                    key=child.getKey();
                                                }
                                            }
                                            if(set){
                                                ev.setOrganisation(input.getText().toString().toUpperCase());
                                                databaseReference.child(key).setValue(ev);
                                                databaseReference=database.getReference("Persons");
                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                                        for(DataSnapshot child:children){
                                                            Person person=child.getValue(Person.class);
                                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && person.getCoordinator_email().equals(current_event.getCoordinator_email())){
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
                                    /*final ProgressDialog waiting;
                                    databaseReference = database.getReference("events/" + input.getText().toString().toUpperCase() + "/" + current_event.getName().toUpperCase().toUpperCase() + ", " + current_event.getCoordinator_email().replace(".", "").toUpperCase());
                                    waiting = new ProgressDialog(selectedEventModificationActivity.this);
                                    waiting.setMessage("Please Wait");
                                    waiting.setCancelable(false);
                                    waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    waiting.show();
                                    if (!(input.getText().toString().equals(current_event.getName().toUpperCase()) || input.getText().toString().equals(""))) {
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    waiting.dismiss();
                                                    Toast.makeText(selectedEventModificationActivity.this, "Your another event with same name and organisation already exists", Toast.LENGTH_SHORT).show();

                                                    finish();
                                                } else {
                                                    //event ev = dataSnapshot.getValue(event.class);
                                                    databaseReference.getRef().removeValue();
                                                    DatabaseReference dbreference = database.getReference("events/" + current_event.getOrganisation().toUpperCase() + "/" + current_event.getName().toUpperCase() + ", " + current_event.getCoordinator_email().replace(".", "").toUpperCase());
                                                    dbreference.removeValue();
                                                    current_event.setOrganisation(input.getText().toString().toUpperCase());
                                                    databaseReference.setValue(current_event);
                                                    waiting.dismiss();
                                                    Toast.makeText(selectedEventModificationActivity.this, "Orgnisation changed successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }*/
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
                            DatabaseReference databaseReference = database.getReference("events");
                            try {
                                waiting.show();
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        try {
                                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                            for (DataSnapshot child : children) {
                                                event ev = child.getValue(event.class);
                                                if (ev.getName().equals(current_event.getName()) && ev.getCoordinator_email().equals(current_event.getCoordinator_email()) && ev.getOrganisation().equals(current_event.getOrganisation())) {
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
                    final DatabaseReference databaseReference = database.getReference("Persons");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                final ArrayList<String> key=new ArrayList<>();
                                for (DataSnapshot child : children) {
                                    //Iterable<DataSnapshot> data = child.getChildren();
                                    //for (DataSnapshot Class : data) {
                                    Person person = child.getValue(Person.class);
                                    if (person.getCoordinator_email().equals(current_event.getCoordinator_email()) && person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation())) {
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
                    ArrayList<String> keys=new ArrayList<>();
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("Are you sure to delete this User?");
                    builder.setMessage("All the information regarding this user will be deleted");
                    builder.setNegativeButton("Cancel",null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            final Person temp=arrayList.get(position);
                            //String key=""
                            final DatabaseReference reference=database.getReference("Persons");
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                    for(DataSnapshot child:children){
                                        Person person=child.getValue(Person.class);
                                        if(person.getCoordinator_email().equals(current_event.getCoordinator_email()) && person.getEvent_name().equals(current_event.getName()) && person.getOrganisation().equals(current_event.getOrganisation()) && person.getPerson_ID().equals(temp.getPerson_ID())){
                                            key=child.getKey();
                                        }
                                    }
                                    reference.child(key).removeValue();
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