package com.example.attendancesystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;

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
import java.util.Set;

public class selectedEventModificationActivity extends AppCompatActivity {
    private String key,event_key;
    private ListView listView;
    private final HashMap<String,String> keys=new HashMap<>();
    private Button create_event,add_person;
    private MyPeopleAdapter adapter;
    private Event current_event;
    private final ArrayList<Person> arrayList=new ArrayList<>();
    private DatabaseReference databaseReference;
    private TextView eventview,organisationview,person_count;
    private ImageButton delete_button;
    private ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_event_modification);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        loading=findViewById(R.id.check_attendance_progress);
        event_key=getIntent().getStringExtra("Key");
        listView=findViewById(R.id.list_view1);
        eventview = findViewById(R.id.eventView);
        organisationview=findViewById(R.id.event_organisation);
        person_count = findViewById(R.id.disp_total_people);
        delete_button=findViewById(R.id.deleteButton);
        adapter=new MyPeopleAdapter(selectedEventModificationActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setVerticalScrollBarEnabled(false);
        current_event = new Gson().fromJson(getIntent().getStringExtra("Event"), Event.class);
        add_person=findViewById(R.id.add_person_button);
        create_event=findViewById(R.id.create_new_event);
        eventview.setVisibility(View.GONE);
        organisationview.setVisibility(View.GONE);
        person_count.setVisibility(View.GONE);
        delete_button.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        add_person.setVisibility(View.GONE);
        create_event.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        add_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(selectedEventModificationActivity.this,AddPerson.class);
                intent.putExtra("Event",new Gson().toJson(current_event));
                intent.putExtra("Key",event_key);
                startActivityForResult(intent,RESULT_FIRST_USER);
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
                        Intent intent=new Intent(selectedEventModificationActivity.this, AddEventActivity.class);
                        intent.putExtra("People",new Gson().toJson(arrayList));
                        startActivity(intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                    else{
                        Toast.makeText(selectedEventModificationActivity.this,"All the people are excluded from this Event",Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
        eventview.setText(current_event.getName());
        organisationview.setText(current_event.getOrganisation());
        try {
            if (!isNetworkAvailable()) {
                Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
            }
            else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+event_key);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for (DataSnapshot child : children) {
                                Person person = child.getValue(Person.class);
                                arrayList.add(person);
                                keys.put(person.getPerson_ID(),child.getKey());
                                adapter.notifyDataSetChanged();
                            }
                            Collections.sort(arrayList);
                            loading.setVisibility(View.GONE);
                            person_count.setText("Total People : "+arrayList.size());
                            eventview.setVisibility(View.VISIBLE);
                            organisationview.setVisibility(View.VISIBLE);
                            person_count.setVisibility(View.VISIBLE);
                            delete_button.setVisibility(View.VISIBLE);
                            add_person.setVisibility(View.VISIBLE);
                            listView.setEmptyView(findViewById(R.id.empty_message));
                            if(arrayList.isEmpty()) {
                                create_event.setEnabled(false);
                                create_event.setBackgroundResource(R.drawable.disabled_button);
                            }
                            create_event.setVisibility(View.VISIBLE);
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
                                else if(!input.getText().toString().trim().toUpperCase().equals(eventview.getText().toString().trim().toUpperCase())){
                                    databaseReference = FirebaseDatabase.getInstance().getReference("Events/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                            boolean set=true;
                                            Event eve=new Event();
                                            for(DataSnapshot child:children){
                                                Event ev=child.getValue(Event.class);
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
                                                current_event.setName(eve.getName());
                                                eve.setOrganisation(organisationview.getText().toString().toUpperCase());
                                                current_event.setOrganisation(eve.getOrganisation());
                                                eve.dates=current_event.dates;
                                                databaseReference.child(key).setValue(eve);
                                                Toast.makeText(selectedEventModificationActivity.this,"Event name Changed successfully",Toast.LENGTH_SHORT).show();
                                                eventview.setText(eve.getName());
                                                setResult(RESULT_OK);
                                            }
                                            else{
                                                Toast.makeText(selectedEventModificationActivity.this, "Your another Event with same name and organisation already exists", Toast.LENGTH_SHORT).show();
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
                        else if(!input.getText().toString().trim().toUpperCase().equals(organisationview.getText().toString().trim().toUpperCase())){
                            databaseReference = FirebaseDatabase.getInstance().getReference("Events/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> children=dataSnapshot.getChildren();
                                    boolean set=true;
                                    Event eve=new Event();
                                    for(DataSnapshot child:children){
                                        Event ev=child.getValue(Event.class);
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
                                        eve.setName(eventview.getText().toString());
                                        current_event.setName(eve.getName());
                                        eve.setOrganisation(input.getText().toString().trim().toUpperCase());
                                        current_event.setOrganisation(eve.getOrganisation());
                                        eve.dates=current_event.dates;
                                        databaseReference.child(key).setValue(eve);
                                        Toast.makeText(selectedEventModificationActivity.this,"Organisation Changed successfully",Toast.LENGTH_SHORT).show();
                                        organisationview.setText(eve.getOrganisation());
                                        setResult(RESULT_OK);
                                    }
                                    else{
                                        Toast.makeText(selectedEventModificationActivity.this, "Your another Event with same name and organisation already exists", Toast.LENGTH_SHORT).show();
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
                    builder.setTitle("Delete Event");
                    builder.setMessage("Are you sure you want to delete all data linked with this Event?");
                    builder.setNegativeButton("cancel",null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            eventview.setVisibility(View.GONE);
                            organisationview.setVisibility(View.GONE);
                            person_count.setVisibility(View.GONE);
                            delete_button.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                            add_person.setVisibility(View.GONE);
                            create_event.setVisibility(View.GONE);
                            TextView textView=findViewById(R.id.empty_message);
                            if(textView.getVisibility()==View.VISIBLE){
                                textView.setVisibility(View.GONE);
                            }
                            loading.setVisibility(View.VISIBLE);
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            try {
                                databaseReference = database.getReference("Events/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                databaseReference.child(event_key).removeValue();
                                databaseReference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                databaseReference.child(event_key).removeValue();
                                Toast.makeText(selectedEventModificationActivity.this,"Event Deleted Successfully",Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
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
    private class MyPeopleAdapter extends BaseAdapter {
        final Context context;
        final LayoutInflater inflater;

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
            if(std.getEnabled().equals("No")){
                tv1.setTextColor(Color.RED);
            }
            TextView tv2=view.findViewById(R.id.disp_id);
            tv2.setText(std.getPerson_ID());
            ImageButton button=view.findViewById(R.id.deleteButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(selectedEventModificationActivity.this);
                    builder.setTitle("Are you sure to delete this person?");
                    builder.setMessage(Html.fromHtml("All the information regarding this person for this Event will be deleted from records. You can exclude them by clicking on the person and disable the "+"<b>"+"Future Attendance"+"</b>"+" option so that person can be excluded without damaging the previous records of the person.", HtmlCompat.FROM_HTML_MODE_COMPACT));
                    builder.setNegativeButton("Cancel",null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isNetworkAvailable()) {
                                Toast.makeText(selectedEventModificationActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                final DatabaseReference reference = database.getReference("People/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+event_key);
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final Set<String> events=arrayList.get(position).dates.keySet();
                                        reference.child(keys.get(std.getPerson_ID())).removeValue();
                                        arrayList.remove(position);
                                        final DatabaseReference dbreference=database.getReference("Events/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        dbreference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Event ev=dataSnapshot.child(event_key).getValue(Event.class);
                                                for(String str:events){
                                                    long l=ev.dates.get(str);
                                                    l--;
                                                    if(l==0){
                                                        ev.dates.remove(str);
                                                        current_event.dates.remove(str);
                                                    }
                                                    else {
                                                        ev.dates.replace(str,l);
                                                        current_event.dates.replace(str,l);
                                                    }
                                                }
                                                dbreference.child(event_key).setValue(ev);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        Toast.makeText(selectedEventModificationActivity.this,"Person deleted successfully",Toast.LENGTH_SHORT).show();
                                        TextView person_count = findViewById(R.id.disp_total_people);
                                        person_count.setText("Total People : "+arrayList.size());
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
                    builder.setCancelable(true);
                    builder.show();
                }
            });
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(selectedEventModificationActivity.this,ModifyAttendanceActivity.class);
                    intent.putExtra("Person",new Gson().toJson(std));
                    intent.putExtra("Key",keys.get(std.getPerson_ID()));
                    intent.putExtra("Event",new Gson().toJson(current_event));
                    intent.putExtra("Event_Key",event_key);
                    startActivityForResult(intent,RESULT_FIRST_USER);
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
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}