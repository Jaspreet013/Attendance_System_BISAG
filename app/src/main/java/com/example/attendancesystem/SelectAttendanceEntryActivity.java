package com.example.attendancesystem;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class SelectAttendanceEntryActivity extends AppCompatActivity {
    private Event current_event;
    private String event_key;
    private ArrayList<String> arrayList=new ArrayList<>();
    private final ArrayList<Person> persons=new ArrayList<>();
    private final HashMap<String,String> keys=new HashMap<>();
    private ProgressBar loading;
    private TextView textView;
    private ListView listView;
    private MyBaseAdapter adapter;
    private Button download;
    private DatabaseReference people;
    private String start_date,end_date;
    private AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_attendance_entry);
        StrictMode.VmPolicy.Builder builders = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builders.build());
        builders.detectFileUriExposure();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        textView=findViewById(R.id.select_subject_text);
        current_event=new Gson().fromJson(getIntent().getStringExtra("Event"), Event.class);
        textView.setText("Total Entries : "+current_event.dates.size());
        event_key=getIntent().getStringExtra("Event_Key");
        listView=findViewById(R.id.list_view3);
        loading=findViewById(R.id.check_attendance_progress);
        textView.setVisibility(View.GONE);
        people=FirebaseDatabase.getInstance().getReference("People/"+event_key);
        adapter=new MyBaseAdapter(SelectAttendanceEntryActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        arrayList.addAll(current_event.dates.keySet());
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        adapter.notifyDataSetChanged();
        listView.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        download=findViewById(R.id.download_pdf);
        download.setVisibility(View.GONE);
        if (!isNetworkAvailable()) {
            Toast.makeText(SelectAttendanceEntryActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
        }
        else{
            try{
                people.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for(DataSnapshot child:children){
                            Person person=child.getValue(Person.class);
                            persons.add(person);
                            keys.put(person.getPerson_ID(),child.getKey());
                        }
                        Collections.sort(persons);
                        loading.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        listView.setEmptyView(findViewById(R.id.select_empty_message));
                        if(!arrayList.isEmpty()){
                            download.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            catch(Exception e){
                Log.e("Database exception is",e.getMessage());
            }
        }
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder=new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
                builder.setTitle("Select Duration");
                LinearLayout rl =new LinearLayout(SelectAttendanceEntryActivity.this);
                rl.setOrientation(LinearLayout.HORIZONTAL);
                rl.setGravity(Gravity.CENTER);
                final TextView start=new TextView(SelectAttendanceEntryActivity.this);
                start.setText("Start Date");
                start.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f);
                start.setTextColor(getResources().getColor(R.color.colorAccent,null));
                start.setTypeface(start.getTypeface(), Typeface.BOLD);
                start.setGravity(View.TEXT_ALIGNMENT_CENTER);
                if(!TextUtils.isEmpty(start_date)){
                    start.setText(start_date);
                }
                final TextView end=new TextView(SelectAttendanceEntryActivity.this);
                end.setPadding(50,0,0,0);
                end.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f);
                end.setText("End Date");
                end.setTextColor(getResources().getColor(R.color.colorAccent,null));
                end.setTypeface(end.getTypeface(),Typeface.BOLD);
                end.setGravity(View.TEXT_ALIGNMENT_CENTER);
                if(!TextUtils.isEmpty(end_date)) {
                    end.setText(end_date);
                }
                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar set=Calendar.getInstance();
                        DatePickerDialog picker = new DatePickerDialog(SelectAttendanceEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month += 1;
                                start_date=dayOfMonth+"/"+month+"/"+year;
                                start.setText(start_date);
                                try {
                                    if (TextUtils.isEmpty(end_date)) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(start_date));
                                        calendar.add(Calendar.MONTH, 1);
                                        calendar.add(Calendar.DAY_OF_MONTH, -1);
                                        end_date=new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());
                                    }
                                }
                                catch (Exception e){}
                            }
                        },set.get(Calendar.YEAR),set.get(Calendar.MONTH),set.get(Calendar.DAY_OF_MONTH));
                        picker.show();
                    }
                });
                rl.addView(start);
                end.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar set=Calendar.getInstance();
                        DatePickerDialog picker = new DatePickerDialog(SelectAttendanceEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                month += 1;
                                end_date=dayOfMonth+"/"+month+"/"+year;
                                end.setText(end_date);
                                try {
                                    if (TextUtils.isEmpty(start_date)) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(end_date));
                                        calendar.add(Calendar.MONTH, -1);
                                        calendar.add(Calendar.DAY_OF_MONTH,1);
                                        start_date=new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());
                                    }
                                }
                                catch (Exception e){}
                            }
                        },set.get(Calendar.YEAR),set.get(Calendar.MONTH),set.get(Calendar.DAY_OF_MONTH));
                        picker.show();
                    }
                });
                rl.addView(end);
                builder.setMessage("Select Duration of at most one month by clicking on start date and end date");
                builder.setView(rl);
                builder.setCancelable(false);
                builder.setPositiveButton("Download PDF", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Date start,end;
                        ArrayList<String> selected_keys=new ArrayList<>();
                        if(TextUtils.isEmpty(start_date) || TextUtils.isEmpty(end_date)){
                            Toast.makeText(SelectAttendanceEntryActivity.this,"You have not selected any date",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            try {
                                start = new SimpleDateFormat("dd/MM/yyyy").parse(start_date);
                                end = new SimpleDateFormat("dd/MM/yyyy").parse(end_date);
                                Calendar calendar=Calendar.getInstance();
                                calendar.setTime(end);
                                calendar.add(Calendar.MONTH,-1);
                                calendar.add(Calendar.DAY_OF_MONTH,1);
                                Date month_back=calendar.getTime();
                                calendar.setTime(start);
                                calendar.add(Calendar.MONTH,1);
                                calendar.add(Calendar.DAY_OF_MONTH,-1);
                                Date month_ahead=calendar.getTime();
                                if(start.compareTo(end)>0){
                                    Toast.makeText(SelectAttendanceEntryActivity.this,"Start Date is more than end date",Toast.LENGTH_SHORT).show();
                                }
                                else if(month_back.compareTo(start)>0 || month_ahead.compareTo(end)<0){
                                    Toast.makeText(SelectAttendanceEntryActivity.this,"You can select only one month duration",Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    for (String key : arrayList) {
                                        String str[] = key.split("-", 5);
                                        int d = Integer.parseInt(str[2]);
                                        int m = Integer.parseInt(str[1]);
                                        int y = Integer.parseInt(str[0]);
                                        Date temp = new SimpleDateFormat("yyyy-MM-dd").parse(y + "-" + m + "-" + d);
                                        if (temp.compareTo(start) >= 0 && temp.compareTo(end) <= 0) {
                                            selected_keys.add(key);
                                        }
                                    }
                                    if (selected_keys.isEmpty()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("No Entry found during given duration");
                                        builder.setPositiveButton("Ok", null);
                                        builder.show();
                                    } else {
                                        PrintReport printReport = new PrintReport();
                                        String[] date0 = start_date.split("/", 3);
                                        String[] date1 = end_date.split("/", 3);
                                        File file = new File(printReport.createPDF(current_event, persons, selected_keys, Integer.parseInt(date0[0]), Integer.parseInt(date0[1]), Integer.parseInt(date0[2]), Integer.parseInt(date1[0]), Integer.parseInt(date1[1]), Integer.parseInt(date1[2])));
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
                                        builder.setCancelable(false);
                                        builder.setTitle("File Saved Successfully");
                                        builder.setMessage("File has been successfully saved in " + file.getPath());
                                        builder.setPositiveButton("Ok", null);
                                        builder.show();
                                        addNotification(file.getName());
                                    }
                                }
                            } catch (Exception e) { }
                        }
                    }
                });
                builder.setNegativeButton("Cancel",null);
                if(SelectAttendanceEntryActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    builder.show();
                }
                else{
                    ActivityCompat.requestPermissions(SelectAttendanceEntryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                }
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
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.event_list_view,null);
            TextView tv=view.findViewById(R.id.dispname);
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            SimpleDateFormat format1;
            if (!DateFormat.is24HourFormat(SelectAttendanceEntryActivity.this))
            {
                format1=new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
            }
            else{
                format1=new SimpleDateFormat("dd/MM/yyyy  HH:mm");
            }
            try{
                tv.setText(format1.format(format.parse(arrayList.get(position))));
            }
            catch (Exception e){}
            TextView tv2=view.findViewById(R.id.disporganisation);
            tv2.setText("Total People : "+current_event.dates.get(arrayList.get(position)));
            TextView tv3=view.findViewById(R.id.coordinator_name);
            tv3.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(SelectAttendanceEntryActivity.this,CheckAttendanceActivity.class);
                    intent.putExtra("Event",new Gson().toJson(current_event));
                    intent.putExtra("Event_Key",event_key);
                    intent.putExtra("Entry_Key",arrayList.get(position));
                    startActivityForResult(intent,RESULT_FIRST_USER);
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
    private class PrintReport {
        final HashMap<String,Long> present=new HashMap<>();
        final HashMap<String,Long> absent=new HashMap<>();
        final HashMap<String,Long> total=new HashMap<>();
        String createPDF (final Event ev, ArrayList<Person> persons, ArrayList<String> selected_keys, int start_day, int start_month, int start_year, int end_day, int end_month, int end_year){
            Collections.sort(selected_keys);
            Document doc = new Document();
            PdfWriter docWriter = null;
            String path="";
            try {
                int count=1,test;
                long p,a,t;
                ArrayList<Integer> list=new ArrayList<>();
                for(int k=0;k<persons.size();k++){
                    test=0;
                    for(String l:selected_keys) {
                        if(!persons.get(k).dates.containsKey(l)) {
                            test++;
                            if(test==selected_keys.size()){
                                list.add(k);
                            }
                        }
                    }
                }
                Collections.sort(list);
                Collections.reverse(list);
                for(int k:list){
                    persons.remove(k);
                }
                Font bfBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, new BaseColor(0, 0, 0));
                Font bf12 = new Font(Font.FontFamily.TIMES_ROMAN, 12);
                Date date=new Date();
                java.text.SimpleDateFormat sdf=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Attendance_"+sdf.format(date)+".pdf";
                if(!(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).exists() && Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).isDirectory())){
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
                }
                docWriter = PdfWriter.getInstance(doc , new FileOutputStream(path));
                if(selected_keys.size()<=27) {
                    doc.setPageSize(PageSize.A3.rotate());
                }
                else{
                    doc.setPageSize(PageSize.A2.rotate());
                }
                doc.open();
                Font f,f1;
                if(selected_keys.size()<=30) {
                    f = new Font(Font.FontFamily.TIMES_ROMAN, 20.0f, Font.BOLD, BaseColor.BLACK);
                    f1=new Font(Font.FontFamily.TIMES_ROMAN,15.0f,Font.BOLD,BaseColor.BLACK);
                }
                else{
                    f = new Font(Font.FontFamily.TIMES_ROMAN, 30.0f, Font.BOLD, BaseColor.BLACK);
                    f1=new Font(Font.FontFamily.TIMES_ROMAN,20.0f,Font.BOLD,BaseColor.BLACK);
                }
                Paragraph paragraph1=new Paragraph("Date : "+start_day+"-"+start_month+"-"+start_year+" to "+end_day+"-"+end_month+"-"+end_year+"\nTotal Entries : "+selected_keys.size()+"\nTotal People : "+persons.size(),f1);
                paragraph1.setAlignment(Element.ALIGN_LEFT);
                Paragraph paragraph=new Paragraph(ev.getName().toUpperCase()+", "+ev.getOrganisation().toUpperCase(),f);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                float columnWidths[]=new float[selected_keys.size()+3];
                columnWidths[0]=0.5f;
                columnWidths[1]=3f;
                columnWidths[2]=2f;
                for (int i = 3; i < selected_keys.size() + 3; i++) {
                    columnWidths[i] = 0.3f;
                }
                for(String key:selected_keys){
                    present.put(key,(long)0);
                    absent.put(key,(long)0);
                    total.put(key,(long)0);
                }
                PdfPTable table = new PdfPTable(columnWidths);
                table.setWidthPercentage(90f);
                insertCell(table,"No.",Element.ALIGN_CENTER,1,bfBold12);
                insertCell(table,"Name",Element.ALIGN_CENTER,1,bfBold12);
                insertCell(table,"ID",Element.ALIGN_CENTER,1,bfBold12);
                for(int i=0;i<selected_keys.size();i++){
                    String str[]=selected_keys.get(i).split("-",5);
                    insertCell(table,str[2],Element.ALIGN_CENTER,1,bfBold12);
                }
                table.setHeadersInEvent(true);
                table.setHeaderRows(1);
                for(int i=0;i<persons.size();i++){
                    insertCell(table,Integer.toString(count),Element.ALIGN_CENTER,1,bf12);
                    insertCell(table,persons.get(i).getName().toUpperCase(),Element.ALIGN_LEFT,1,bf12);
                    insertCell(table,persons.get(i).getPerson_ID(),Element.ALIGN_LEFT,1,bf12);
                    for(String key:selected_keys) {
                        if (persons.get(i).dates.containsKey(key)){
                            insertCell(table, persons.get(i).dates.get(key).charAt(0) + "", Element.ALIGN_CENTER, 1, bf12);
                            if(persons.get(i).dates.get(key).equals("Present")){
                                p=present.get(key);
                                p++;
                                present.replace(key,p);
                            }
                            else{
                                a=absent.get(key);
                                a++;
                                absent.replace(key,a);
                            }
                            t=total.get(key);
                            t++;
                            total.replace(key,t);
                        }
                        else{
                            insertCell(table,"",Element.ALIGN_CENTER,1,bf12);
                        }
                    }
                    count++;
                }
                insertCell(table,"Present",Element.ALIGN_RIGHT,3,bfBold12);
                for(String key:selected_keys){
                    insertCell(table,Long.toString(present.get(key)),Element.ALIGN_CENTER,1,bfBold12);
                }
                insertCell(table,"Absent",Element.ALIGN_RIGHT,3,bfBold12);
                for(String key:selected_keys){
                    insertCell(table,Long.toString(absent.get(key)),Element.ALIGN_CENTER,1,bfBold12);
                }
                insertCell(table,"Total",Element.ALIGN_RIGHT,3,bfBold12);
                for(String key:selected_keys){
                    insertCell(table,Long.toString(total.get(key)),Element.ALIGN_CENTER,1,bfBold12);
                }
                paragraph1.add(table);
                doc.add(paragraph);
                doc.add(paragraph1);
            }
            catch (DocumentException dex)
            {
                Log.e("Doc",dex.getMessage());
            }
            catch (Exception ex)
            {
                Log.e("Other",ex.getMessage());
            }
            finally
            {
                if (doc != null){
                    doc.close();
                }
                if (docWriter != null){
                    docWriter.close();
                }
            }
            return path;
        }
        private void insertCell(PdfPTable table, String text, int align, int colspan, Font font){
            PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
            cell.setBorderWidth(1);
            cell.setHorizontalAlignment(align);
            cell.setColspan(colspan);
            if(text.trim().equalsIgnoreCase("")){
                cell.setMinimumHeight(10f);
                cell.setBackgroundColor(new BaseColor(53,53,233,150));
            }
            else if(text.equals("A")){
                cell.setBackgroundColor(new BaseColor(255,0,0,150));
            }
            else if(text.equals("Present") || text.equals("Absent") || text.equals("Total")){
                cell.setBorderWidthBottom(0);
                cell.setBorderWidthLeft(0);
                if(text.equals("Absent") || text.equals("Total")){
                    cell.setBorderWidthTop(0);
                }
            }
            table.addCell(cell);
        }
    }
    private void addNotification(String name) {
        createNotificationChannel();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+name);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "0");
        builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        builder.setContentText("Download Complete");
        builder.setContentTitle(file.getName());
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE), builder.build());
    }
    private void createNotificationChannel(){
        CharSequence name = "Personal";
        String description = "";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("0", name, importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            builder.show();
        }
        else if(!(ActivityCompat.shouldShowRequestPermissionRationale(SelectAttendanceEntryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) && !(SelectAttendanceEntryActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
            alertDialogBuilder.setTitle("Permission needed");
            alertDialogBuilder.setMessage("Storage permission needed for storing pdf");
            alertDialogBuilder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", SelectAttendanceEntryActivity.this.getPackageName(),
                            null);
                    intent.setData(uri);
                    SelectAttendanceEntryActivity.this.startActivity(intent);
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", null);
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            current_event=new Gson().fromJson(data.getStringExtra("Updated Data"),Event.class);
            arrayList=new ArrayList<>();
            arrayList.addAll(current_event.dates.keySet());
            Collections.sort(arrayList);
            Collections.reverse(arrayList);
            textView.setText("Total Entries : "+current_event.dates.size());
            adapter.notifyDataSetChanged();
            if(arrayList.isEmpty()){
                download.setVisibility(View.GONE);
            }
            setResult(RESULT_OK);
        }
    }
}