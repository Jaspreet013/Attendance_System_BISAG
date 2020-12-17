package com.example.attendancesystem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;

public class ModifyAttendanceActivity extends AppCompatActivity {
    private ArrayList<String> arrayList = new ArrayList<>();
    private Person current_person;
    private String key;
    private ListView listView;
    private MyBaseAdapter adapter;
    private Event current_event;
    private String event_key;
    private Switch enable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        current_person = new Gson().fromJson(getIntent().getStringExtra("Person"),Person.class);
        key = getIntent().getStringExtra("Key");
        event_key=getIntent().getStringExtra("Event_Key");
        current_event = new Gson().fromJson(getIntent().getStringExtra("Event"), Event.class);
        enable=findViewById(R.id.option);
        enable.setChecked(current_person.getEnabled().equals("Yes"));
        adapter = new MyBaseAdapter(ModifyAttendanceActivity.this);
        listView = findViewById(R.id.list_view);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.empty_entry));
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
        enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(ModifyAttendanceActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                    enable.setChecked(!isChecked);
                }
                else {
                    if(!isChecked) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                        alertDialog.setTitle("Exclude this person from future entries?");
                        alertDialog.setMessage("This will prevent from including this person into future entries but any previous records of the person will not be affected");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                current_person.setEnabled("No");
                                final ProgressDialog progressDialog = new ProgressDialog(ModifyAttendanceActivity.this);
                                progressDialog.setMessage("Please Wait");
                                progressDialog.setCancelable(false);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.show();
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference("People/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                                database.child(key).setValue(current_person);
                                progressDialog.dismiss();
                                Toast.makeText(ModifyAttendanceActivity.this, "This person has been excluded from future entries", Toast.LENGTH_SHORT).show();
                                enable.setChecked(false);
                            }
                        });
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                enable.setChecked(true);
                                dialog.cancel();
                            }
                        });
                        AlertDialog dialog = alertDialog.create();
                        dialog.show();
                    }
                    else {
                        if (current_person.getEnabled().equals("No")) {
                            if (!isNetworkAvailable()) {
                                Toast.makeText(ModifyAttendanceActivity.this, "Please check your internet connection and try again", Toast.LENGTH_SHORT).show();
                                enable.setChecked(false);
                            } else {
                                current_person.setEnabled("Yes");
                                final ProgressDialog progressDialog = new ProgressDialog(ModifyAttendanceActivity.this);
                                progressDialog.setMessage("Please Wait");
                                progressDialog.setCancelable(false);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.show();
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                database.child(key).setValue(current_person);
                                progressDialog.dismiss();
                                Toast.makeText(ModifyAttendanceActivity.this, "This person has been enabled to future entries", Toast.LENGTH_SHORT).show();
                                enable.setChecked(true);
                            }
                        }
                    }
                }
            }
        });
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
                            Toast.makeText(ModifyAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                        }
                        else if(input.getText().toString().trim().length()>15){
                            Toast.makeText(ModifyAttendanceActivity.this,"Fname length cannot be more than 15",Toast.LENGTH_SHORT).show();
                        }
                        else if(!(input.getText().toString().trim().matches("^[a-zA-Z]*$")) || TextUtils.isEmpty(input.getText().toString().trim())){
                            Toast.makeText(ModifyAttendanceActivity.this,"Please provide a proper fname",Toast.LENGTH_SHORT).show();
                        }
                        else if(!input.getText().toString().trim().equals(current_person.getFname())){
                            final ProgressDialog progressDialog=new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            current_person.setFname(input.getText().toString().trim());
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                            databaseReference.child(key).setValue(current_person);
                            userfname.setText(input.getText().toString().trim());
                            progressDialog.dismiss();
                            Toast.makeText(ModifyAttendanceActivity.this,"Person fname changed successfully",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ModifyAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                        }
                        else if(input.getText().toString().trim().length()>15){
                            Toast.makeText(ModifyAttendanceActivity.this,"Lname length cannot be more than 15",Toast.LENGTH_SHORT).show();
                        }
                        else if(!(input.getText().toString().trim().matches("^[a-zA-Z]*$")) || TextUtils.isEmpty(input.getText().toString().trim())){
                            Toast.makeText(ModifyAttendanceActivity.this,"Please provide a proper lname",Toast.LENGTH_SHORT).show();
                        }
                        else if(!input.getText().toString().trim().equals(current_person.getLname())){
                            final ProgressDialog progressDialog=new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            current_person.setLname(input.getText().toString().trim());
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                            databaseReference.child(key).setValue(current_person);
                            userlname.setText(input.getText().toString().trim());
                            progressDialog.dismiss();
                            Toast.makeText(ModifyAttendanceActivity.this,"Person lname changed successfully",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ModifyAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                        }
                        else if(input.getText().toString().trim().length()>20){
                            Toast.makeText(ModifyAttendanceActivity.this,"ID length cannot be more than 15",Toast.LENGTH_SHORT).show();
                        }
                        else if(TextUtils.isEmpty(input.getText().toString().trim())){
                            Toast.makeText(ModifyAttendanceActivity.this,"ID cannot be left blank",Toast.LENGTH_SHORT).show();
                        }
                        else if(!input.getText().toString().trim().equals(current_person.getPerson_ID())){
                            final ProgressDialog progressDialog=new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            try {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference databaseReference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                        boolean set=true;
                                        for(DataSnapshot child:children){
                                            Person person=child.getValue(Person.class);
                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && person.getPerson_ID().equals(input.getText().toString().trim()) && !person.getPerson_email().equals(current_person.getPerson_email())){
                                                set=false;
                                                Toast.makeText(ModifyAttendanceActivity.this,"This ID is already registered to this Event",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        if(set) {
                                            current_person.setPerson_ID(input.getText().toString().trim());
                                            databaseReference.child(key).setValue(current_person);
                                            progressDialog.dismiss();
                                            Toast.makeText(ModifyAttendanceActivity.this,"ID changed successfully",Toast.LENGTH_SHORT).show();
                                            id.setText(input.getText().toString());
                                        }
                                        else{
                                            progressDialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception ex) {
                                Log.e("Exception",ex.getMessage());
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
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            Toast.makeText(ModifyAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();

                        }
                        else if(!input.getText().toString().trim().matches("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                            Toast.makeText(ModifyAttendanceActivity.this,"Please provide a valid email",Toast.LENGTH_SHORT).show();

                        }
                        else if(!input.getText().toString().trim().equals(current_person.getPerson_ID())){
                            final ProgressDialog progressDialog=new ProgressDialog(ModifyAttendanceActivity.this);
                            progressDialog.setMessage("Please Wait");
                            progressDialog.setCancelable(false);
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            try {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference databaseReference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                        boolean set=true;
                                        for(DataSnapshot child:children){
                                            Person person=child.getValue(Person.class);
                                            if(person.getOrganisation().equals(current_event.getOrganisation()) && person.getEvent_name().equals(current_event.getName()) && !person.getPerson_ID().equals(current_person.getPerson_ID()) && person.getPerson_email().equals(input.getText().toString().trim())){
                                                set=false;
                                                Toast.makeText(ModifyAttendanceActivity.this,"This email is already registered to this Event",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        if(set) {
                                            current_person.setPerson_email(input.getText().toString().trim());
                                            databaseReference.child(key).setValue(current_person);
                                            progressDialog.dismiss();
                                            Toast.makeText(ModifyAttendanceActivity.this,"Email changed successfully",Toast.LENGTH_SHORT).show();
                                            email.setText(input.getText().toString().trim());
                                        }
                                        else{
                                            progressDialog.dismiss();
                                        }
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
        if(arrayList.isEmpty()){
            submit.setVisibility(View.GONE);
        }
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
                            Toast.makeText(ModifyAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            current_person.setAttendance(getPresentCount());
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
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
    private class MyBaseAdapter extends BaseAdapter {
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
            String set="";
            if (!DateFormat.is24HourFormat(ModifyAttendanceActivity.this))
            {
                if(Integer.parseInt(str[3])>12){
                    if(Integer.parseInt(str[3])-12<10) {
                        str[3]="0"+(Integer.parseInt(str[3])-12);
                    }
                    else{
                        str[3]=Integer.toString(Integer.parseInt(str[3])-12);
                    }
                    set="PM";
                }
                else if(str[3].equals("00")){
                    str[3]="12";
                    set="AM";
                }
                else if(str[3].equals("12")){
                    set="PM";
                }
                else{
                    set="AM";
                }
                tv1.setText(str[2] + "/" + str[1] + "/" + str[0] + "  " + str[3] + ":" + str[4]+" "+set);
            }
            else {
                tv1.setText(str[2]+"/"+str[1]+"/"+str[0]+"  "+str[3]+":"+str[4]);
            }
            final CheckBox ispresent=view.findViewById(R.id.ispresent);
            if(current_person.dates.get(arrayList.get(position)).equals("Present")){
                ispresent.setChecked(true);
            }
            else{
                ispresent.setChecked(false);
            }
            ispresent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (current_person.dates.get(arrayList.get(position)).equals("Present")) {
                        ispresent.setChecked(false);
                        current_person.dates.replace(arrayList.get(position),"Absent");
                    } else {
                        ispresent.setChecked(true);
                        current_person.dates.replace(arrayList.get(position),"Present");
                    }
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ispresent.isChecked()) {
                        ispresent.setChecked(false);
                        current_person.dates.replace(arrayList.get(position),"Absent");
                    } else {
                        ispresent.setChecked(true);
                        current_person.dates.replace(arrayList.get(position),"Present");
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
    private long getPresentCount () {
        long count = 0;
        for (String str : current_person.dates.keySet()) {
            if (current_person.dates.get(str).equals("Present")) {
                count += 1;
            }
        }
        return count;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder=new AlertDialog.Builder(ModifyAttendanceActivity.this);
        builder.setMessage("Any selections you edited in the entries will not be saved");
        builder.setTitle("Are you sure you want to go back?");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.show();
    }
}