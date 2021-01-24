package com.example.attendancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class ModifyAttendanceActivity extends AppCompatActivity {
    private final ArrayList<String> arrayList = new ArrayList<>();
    private Person current_person;
    private String key;
    private String event_key;
    private Switch enable;
    private DatabaseReference person;
    private TextView userfname,userlname,id,email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        current_person = new Gson().fromJson(getIntent().getStringExtra("Person"),Person.class);
        key = getIntent().getStringExtra("Key");
        event_key=getIntent().getStringExtra("Event_Key");
        enable=findViewById(R.id.option);
        enable.setChecked(current_person.getEnabled().equals("Yes"));
        MyBaseAdapter adapter = new MyBaseAdapter(ModifyAttendanceActivity.this);
        ListView listView = findViewById(R.id.list_view);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.empty_entry));
        arrayList.addAll(current_person.dates.keySet());
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        adapter.notifyDataSetChanged();
        person = FirebaseDatabase.getInstance().getReference("People/"+event_key+"/"+key);
        userfname = findViewById(R.id.disp_user_fname);
        userlname  = findViewById(R.id.disp_user_lname);
        id = findViewById(R.id.disp_user_id);
        email = findViewById(R.id.disp_user_email);
        String str[]=current_person.getName().split(" ",2);
        userfname.setText(str[0]);
        userlname.setText(str[1]);
        email.setText(email.getText().toString() + current_person.getEmail());
        id.setText(id.getText().toString() + current_person.getPerson_ID());
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
                                person.setValue(current_person);
                                Toast.makeText(ModifyAttendanceActivity.this, "This person has been excluded from future entries", Toast.LENGTH_SHORT).show();
                                enable.setChecked(false);
                                setResult(RESULT_OK);
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
                                person.setValue(current_person);
                                Toast.makeText(ModifyAttendanceActivity.this, "This person has been enabled to future entries", Toast.LENGTH_SHORT).show();
                                enable.setChecked(true);
                                setResult(RESULT_OK);
                            }
                        }
                    }
                }
            }
        });
        id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyAttendanceActivity.this);
                View view=getLayoutInflater().inflate(R.layout.alert_dialog_text_input_layout,null);
                TextInputLayout layout = view.findViewById(R.id.border7);
                final TextInputEditText input = view.findViewById(R.id.input);
                layout.setHint("Rename ID");
                input.setText(current_person.getPerson_ID());
                alertDialog.setView(view);
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isNetworkAvailable()) {
                            Toast.makeText(ModifyAttendanceActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                        }
                        else if(TextUtils.isEmpty(input.getText().toString().trim())){
                            Toast.makeText(ModifyAttendanceActivity.this,"ID cannot be left blank",Toast.LENGTH_SHORT).show();
                        }
                        else if(!input.getText().toString().trim().equals(current_person.getPerson_ID())){
                            try {
                                Toast.makeText(ModifyAttendanceActivity.this,"Please Wait....",Toast.LENGTH_SHORT).show();
                                person.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                        boolean set=true;
                                        for(DataSnapshot child:children){
                                            Person person=child.getValue(Person.class);
                                            if(person.getPerson_ID().equals(input.getText().toString().trim())){
                                                set=false;
                                                Toast.makeText(ModifyAttendanceActivity.this,"This ID is already registered to this Event",Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                        if(set) {
                                            current_person.setPerson_ID(input.getText().toString().trim());
                                            person.setValue(current_person);
                                            Toast.makeText(ModifyAttendanceActivity.this,"ID changed successfully",Toast.LENGTH_SHORT).show();
                                            id.setText("ID : "+input.getText().toString());
                                            setResult(RESULT_OK);
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
                            person.setValue(current_person);
                            Toast.makeText(ModifyAttendanceActivity.this, "Attendance Updated Successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
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
        final Context context;
        final LayoutInflater inflater;

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
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            SimpleDateFormat format1;
            if (!DateFormat.is24HourFormat(ModifyAttendanceActivity.this))
            {
                format1=new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
            }
            else{
                format1=new SimpleDateFormat("dd/MM/yyyy  HH:mm");
            }
            try{
                tv1.setText(format1.format(format.parse(arrayList.get(position))));
            }
            catch (Exception e){}
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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