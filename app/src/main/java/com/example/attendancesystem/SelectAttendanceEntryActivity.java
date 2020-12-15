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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class SelectAttendanceEntryActivity extends AppCompatActivity {
    SharedPreferences get_event;
    event current_event;
    TextView textView;
    ListView listView;
    ArrayList<String> arrayList=new ArrayList<>();
    ArrayList<Person> persons=new ArrayList<>();
    MyBaseAdapter adapter;
    DatePickerDialog picker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_attendance_entry);
        StrictMode.VmPolicy.Builder builders = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builders.build());
        builders.detectFileUriExposure();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        textView=findViewById(R.id.select_subject_text);
        get_event=getSharedPreferences("Events",MODE_PRIVATE);
        Gson gson=new Gson();
        String json=get_event.getString("Current event","");
        current_event=gson.fromJson(json,event.class);
        textView.setText("Total Entries : "+current_event.dates.size());
        listView=findViewById(R.id.list_view3);
        adapter=new MyBaseAdapter(SelectAttendanceEntryActivity.this);
        listView.setAdapter(adapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setVerticalScrollBarEnabled(false);
        listView.setBackgroundResource(R.drawable.rounded_corners);
        listView.setEmptyView(findViewById(R.id.select_empty_message));
        if (!isNetworkAvailable()) {
            Toast.makeText(SelectAttendanceEntryActivity.this,"Please check your internet connection and try again",Toast.LENGTH_SHORT).show();
        }
        else{
            try{
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = database.getReference("Persons/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for(DataSnapshot child:children){
                            Person person=child.getValue(Person.class);
                            if(current_event.getName().equals(person.getEvent_name()) && current_event.getOrganisation().equals(person.getOrganisation())){
                                persons.add(person);
                            }
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
        for(String i:current_event.dates.keySet()) {
            arrayList.add(i);
        }
        Collections.sort(arrayList);
        Collections.reverse(arrayList);
        adapter.notifyDataSetChanged();
        final Button download=findViewById(R.id.download_pdf);
        if(arrayList.isEmpty()){
            download.setVisibility(View.INVISIBLE);
        }
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(ActivityCompat.shouldShowRequestPermissionRationale(SelectAttendanceEntryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) && !(SelectAttendanceEntryActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
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
                else {
                    ActivityCompat.requestPermissions(SelectAttendanceEntryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
                    final ArrayList<String> selected_keys = new ArrayList<>();
                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                    String date[] = format.format(new Date()).split("-", 3);
                    if (date[1] == "1") {
                        date[2] = Integer.toString(Integer.parseInt(date[2]) - 1);
                        date[1] = "12";
                    }
                    picker = new DatePickerDialog(SelectAttendanceEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            PrintReport report = new PrintReport();
                            month += 1;
                            for (String key : arrayList) {
                                String str[] = key.split("-", 5);
                                int d = Integer.parseInt(str[2]);
                                int m = Integer.parseInt(str[1]);
                                int y = Integer.parseInt(str[0]);
                                if (month == 12) {
                                    if (m == 1 && y == year + 1 && dayOfMonth > d) {
                                        selected_keys.add(key);
                                    } else if (m == 12 && year == y && dayOfMonth <= d) {
                                        selected_keys.add(key);
                                    }
                                } else {
                                    if (year == y && month == m && d >= dayOfMonth) {
                                        selected_keys.add(key);
                                    } else if (year == y && m == (month + 1) && d < dayOfMonth) {
                                        selected_keys.add(key);
                                    }
                                }
                            }
                            if (!selected_keys.isEmpty()) {
                                int end_day = 0, end_month = 0, end_year = 0;
                                if (dayOfMonth == 1) {
                                    if (month >= 1 && month <= 7) {
                                        end_month = month;
                                        end_year = year;
                                        if (month != 2) {
                                            if (month % 2 == 1) {
                                                end_day = 31;
                                            } else {
                                                end_day = 30;
                                            }
                                        } else {
                                            if (year % 2 == 0) {
                                                end_day = 29;
                                            } else {
                                                end_day = 28;
                                            }
                                        }
                                    } else {
                                        end_month = month;
                                        end_year = year;
                                        if (month % 2 == 0) {
                                            end_day = 31;
                                        } else {
                                            end_day = 30;
                                        }
                                    }
                                } else {
                                    end_day = dayOfMonth - 1;
                                    if (month == 12) {
                                        end_month = 1;
                                        end_year = year + 1;
                                    } else {
                                        end_month = month + 1;
                                        end_year = year;
                                    }
                                }
                                if (end_month == 2 && (end_day == 30 || end_day == 29)) {
                                    if (end_year % 4 == 0) {
                                        end_day = 29;
                                    } else {
                                        end_day = 28;
                                    }
                                }

                                File file = new File(report.createPDF(current_event, persons, selected_keys, dayOfMonth, month, year, end_day, end_month, end_year));
                                AlertDialog.Builder dialog = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
                                dialog.setCancelable(false);
                                dialog.setTitle("File Saved Successfully");
                                dialog.setMessage("File has been successfully saved in " + file.getPath());
                                dialog.setPositiveButton("Ok", null);
                                dialog.show();
                                addNotification(file.getName());
                            } else {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(SelectAttendanceEntryActivity.this);
                                dialog.setCancelable(false);
                                dialog.setTitle("No Entry found during given duration");
                                dialog.setPositiveButton("Ok", null);
                                dialog.show();
                            }
                        }
                    }, Integer.parseInt(date[2]), Integer.parseInt(date[1]) - 2, Integer.parseInt(date[0]));
                    picker.setMessage("Select your start of month");
                    if (SelectAttendanceEntryActivity.this.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        picker.show();
                    }
                }
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
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.event_list_view,null);
            TextView tv=view.findViewById(R.id.dispname);
            String str[]=arrayList.get(position).split("-",5);
            String set="";
            if (!DateFormat.is24HourFormat(SelectAttendanceEntryActivity.this))
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
                tv.setText(str[2] + "/" + str[1] + "/" + str[0] + "  " + str[3] + ":" + str[4]+" "+set);
            }
            else {
                tv.setText(str[2]+"/"+str[1]+"/"+str[0]+"  "+str[3]+":"+str[4]);
            }
            TextView tv2=view.findViewById(R.id.disporganisation);
            tv2.setText("Total People : "+current_event.dates.get(arrayList.get(position)));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor=get_event.edit();
                    editor.putString("Key",arrayList.get(position));
                    editor.apply();
                    startActivity(new Intent(SelectAttendanceEntryActivity.this,CheckAttendanceActivity.class));
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
    class PrintReport {
        HashMap<String,Long> present=new HashMap<>();
        HashMap<String,Long> absent=new HashMap<>();
        HashMap<String,Long> total=new HashMap<>();
        public String createPDF (final event ev,ArrayList<Person> persons,ArrayList<String> selected_keys,int start_day,int start_month,int start_year,int end_day,int end_month,int end_year){
            Collections.sort(selected_keys);
            Document doc = new Document();
            PdfWriter docWriter = null;
            String path="";
            try {
                Font bfBold12 = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, new BaseColor(0, 0, 0));
                Font bf12 = new Font(Font.FontFamily.TIMES_ROMAN, 12);
                Date date=new Date();
                java.text.SimpleDateFormat sdf=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Attendance_"+sdf.format(date)+".pdf";
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
                for(int i=0;i<persons.size();i++){
                    insertCell(table,Integer.toString(count),Element.ALIGN_CENTER,1,bf12);
                    insertCell(table,persons.get(i).getLname().toUpperCase()+"  "+persons.get(i).getFname().toUpperCase(),Element.ALIGN_LEFT,1,bf12);
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
            picker.show();
        }
        return;
    }
}