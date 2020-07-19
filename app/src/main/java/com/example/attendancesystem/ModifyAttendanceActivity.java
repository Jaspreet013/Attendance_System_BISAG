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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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

public class ModifyAttendanceActivity extends AppCompatActivity {
    SharedPreferences get_person, get_user;
    ArrayList<String> arrayList = new ArrayList<>();
    Person current_person;
    String key;
    ListView listView;
    MyBaseAdapter adapter;
    User current_user;
    event current_event;
    String event_key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        get_person = getSharedPreferences("Person", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = get_person.getString("Current Person", "");
        current_person = gson.fromJson(json, Person.class);
        get_user = getSharedPreferences("User", MODE_PRIVATE);
        Gson gson1=new Gson();
        String json1=get_user.getString("Current User", "");
        current_user=gson1.fromJson(json1, User.class);
        key = get_person.getString("Key", "");
        SharedPreferences preferences = getSharedPreferences("Events", MODE_PRIVATE);
        Gson gson2 = new Gson();
        String json2 = preferences.getString("Current event", "");
        event_key=preferences.getString("Key","");
        current_event = gson2.fromJson(json2, event.class);
        adapter = new MyBaseAdapter(ModifyAttendanceActivity.this);
        listView = findViewById(R.id.list_view);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.modification_empty_message));
        for (String i : current_person.dates.keySet()) {
            arrayList.add(i);
        }
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        adapter.notifyDataSetChanged();
        final TextView userfname = findViewById(R.id.disp_user_fname);
        final TextView userlname = findViewById(R.id.disp_user_lname);
        final TextView id = findViewById(R.id.disp_user_id);
        final TextView email = findViewById(R.id.disp_user_email);
        userfname.setText(current_person.getFname());
        userlname.setText(current_person.getLname());
        id.setText(id.getText().toString() + current_person.getPerson_ID());
        email.setText(email.getText().toString() + current_person.getPerson_email());
        userfname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                alertDialog.setTitle("Rename Fname");
                final EditText input = new EditText(ModifyAttendanceActivity.this);
                input.setText(current_person.getFname());
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(input.getText().toString().length()>15){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("Fname length cannot be more than 15");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(!input.getText().toString().equals(current_person.getFname())){
                            current_person.setFname(input.getText().toString());
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Persons/"+current_user.getEmail().replace(".",""));
                            databaseReference.child(key).setValue(current_person);
                            userfname.setText(input.getText().toString());
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
        userlname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                alertDialog.setTitle("Rename Lname");
                final EditText input = new EditText(ModifyAttendanceActivity.this);
                input.setText(current_person.getLname());
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(input.getText().toString().length()>15){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("Lname length cannot be more than 15");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(!input.getText().toString().equals(current_person.getLname())){
                            current_person.setLname(input.getText().toString());
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Persons/"+current_user.getEmail().replace(".",""));
                            databaseReference.child(key).setValue(current_person);
                            userlname.setText(input.getText().toString());
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
        id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                alertDialog.setTitle("Rename ID");
                final EditText input = new EditText(ModifyAttendanceActivity.this);
                input.setText(current_person.getPerson_ID());
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(input.getText().toString().length()>20){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("ID length cannot be more than 15");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(!input.getText().toString().equals(current_person.getPerson_ID())){
                            final ProgressDialog progressDialog=new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            try {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference databaseReference = database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                        boolean set=true;
                                        AlertDialog.Builder builder=new AlertDialog.Builder(ModifyAttendanceActivity.this);
                                        for(DataSnapshot child:children){
                                            Person person=child.getValue(Person.class);
                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && person.getPerson_ID().equals(input.getText().toString()) && !person.getPerson_email().equals(current_person.getPerson_email())){
                                                set=false;
                                                builder.setTitle("This ID is already registered to this event");
                                            }
                                        }
                                        if(set) {
                                            current_person.setPerson_ID(input.getText().toString());
                                            databaseReference.child(key).setValue(current_person);
                                            progressDialog.dismiss();
                                            builder.setTitle("ID Changed Successfully");
                                            id.setText(input.getText().toString());
                                        }
                                        else{
                                            progressDialog.dismiss();
                                        }
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("Ok",null);
                                        builder.show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception ex) {
                                Log.e("Super exception",ex.getMessage());
                            }
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
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                alertDialog.setTitle("Rename Email");
                final EditText input = new EditText(ModifyAttendanceActivity.this);
                input.setText(current_person.getPerson_email());
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(!input.getText().toString().equals(current_person.getPerson_ID())){
                            final ProgressDialog progressDialog=new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            try {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference databaseReference = database.getReference("Persons/"+current_user.getEmail().replace(".",""));
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                        boolean set=true;
                                        AlertDialog.Builder builder=new AlertDialog.Builder(ModifyAttendanceActivity.this);
                                        for(DataSnapshot child:children){
                                            Person person=child.getValue(Person.class);
                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && !person.getPerson_ID().equals(current_person.getPerson_ID()) && person.getPerson_email().equals(input.getText().toString())){
                                                set=false;
                                                builder.setTitle("This Email is already registered to this event");
                                            }
                                        }
                                        if(set) {
                                            current_person.setPerson_email(input.getText().toString());
                                            databaseReference.child(key).setValue(current_person);
                                            progressDialog.dismiss();
                                            builder.setTitle("Email Changed Successfully");
                                            email.setText(input.getText().toString());
                                        }
                                        else{
                                            progressDialog.dismiss();
                                        }
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("Ok",null);
                                        builder.show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception ex) {
                                Log.e("Super exception",ex.getMessage());
                            }
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
        Button submit = findViewById(R.id.update_attendance);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                builder.setTitle("Are you sure you want to update attendance?");
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setTitle("No Internet");
                            builder.setMessage("Please check your internet connection");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else if(arrayList.isEmpty()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                            builder.setMessage("There is currently no attendance entry available for modification of this person");
                            builder.setTitle("No Entry");
                            builder.setPositiveButton("Ok", null);
                            builder.setCancelable(false);
                            builder.show();
                        }
                        else {
                            current_person.setAttendance(getPresentCount());
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Persons/" + current_user.getEmail().replace(".", ""));
                            databaseReference.child(key).setValue(current_person);
                            Toast.makeText(ModifyAttendanceActivity.this, "Attendance Updated Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });
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
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.disp_modify_attendance, null);
            final TextView tv1=view.findViewById(R.id.dispname);
            String str[]=arrayList.get(position).split("-",5);
            tv1.setText(str[2]+"/"+str[1]+"/"+str[0]+"  "+str[3]+":"+str[4]);
            final TextView tv2=view.findViewById(R.id.dispstatus);
            final CheckBox ispresent=view.findViewById(R.id.ispresent);
            final ImageButton delete=view.findViewById(R.id.ModificationDeleteButton);
            if(current_person.dates.get(arrayList.get(position)).equals("Present")){
                ispresent.setChecked(true);
                tv2.setText("Present");
            }
            else{
                ispresent.setChecked(false);
                tv2.setText("Absent");
            }
            ispresent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (current_person.dates.get(arrayList.get(position)).equals("Present")) {
                        ispresent.setChecked(false);
                        current_person.dates.replace(arrayList.get(position),"Absent");
                        tv2.setText("Absent");
                    } else {
                        ispresent.setChecked(true);
                        current_person.dates.replace(arrayList.get(position),"Present");
                        tv2.setText("Present");
                    }
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ispresent.isChecked()) {
                        ispresent.setChecked(false);
                        current_person.dates.replace(arrayList.get(position),"Absent");
                        tv2.setText("Absent");
                    } else {
                        ispresent.setChecked(true);
                        current_person.dates.replace(arrayList.get(position),"Present");
                        tv2.setText("Present");
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                    builder.setTitle("Delete Entry");
                    builder.setMessage("Are you sure to delete this entry for this user?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProgressDialog waiting=new ProgressDialog(ModifyAttendanceActivity.this);
                            waiting.setMessage("Please Wait");
                            waiting.setCancelable(false);
                            waiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            waiting.show();
                            current_person.dates.remove(arrayList.get(position));
                            current_person.setAttendance(getPresentCount());
                            current_person.setAttendance_total(current_person.dates.size());
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Persons/"+current_user.getEmail().replace(".",""));
                            databaseReference.child(key).setValue(current_person);
                            DatabaseReference dbreference=FirebaseDatabase.getInstance().getReference("events/"+current_user.getEmail().replace(".",""));
                            long l=current_event.dates.get(arrayList.get(position));
                            l--;
                            if(l==0){
                                current_event.dates.remove(arrayList.get(position));
                            }
                            else{
                                current_event.dates.replace(arrayList.get(position),l);
                            }
                            dbreference.child(event_key).setValue(current_event);
                            /*dbreference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    event ev=dataSnapshot.child(event_key).getValue(event.class);
                                    Log.e(arrayList.get(position),event_key);
                                    long l=ev.dates.get(arrayList.get(position));
                                    l--;
                                    if(l==0){
                                        ev.dates.remove(arrayList.get(position));
                                    }
                                    else{
                                        ev.dates.replace(arrayList.get(position),l);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });*/
                            //dbreference.child(event_key).setValue(ev);
                            waiting.dismiss();
                            Toast.makeText(ModifyAttendanceActivity.this,"Attendance Record successfully removed for specified person",Toast.LENGTH_SHORT).show();
                            arrayList.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel",null);
                    builder.setCancelable(false);
                    builder.show();
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
    public long getPresentCount () {
        long count = 0;
        for (String str : current_person.dates.keySet()) {
            if (current_person.dates.get(str).equals("Present")) {
                count += 1;
            }
        }
        return count;
    }
}