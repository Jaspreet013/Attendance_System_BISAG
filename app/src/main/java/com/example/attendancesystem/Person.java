package com.example.attendancesystem;

import java.util.HashMap;

public class Person implements Comparable<Person>{
    private String person_ID,enabled = "Yes",name,email,photourl;
    private long attendance = 0, attendance_total = 0;
    private Boolean ispresent = null;
    public HashMap<String, String> dates = new HashMap<>();

    public Person() {
    }

    public Person(String person_ID,String name,String email,String photourl) {
        this.person_ID = person_ID;
        this.name = name;
        this.email=email;
        this.photourl=photourl;
    }

    public void setName(String name) { this.name = name; }

    public String getName() { return name; }

    public void setEmail(String email) { this.email = email; }

    public String getEmail() { return email; }

    public void setPerson_ID(String person_ID) {
        this.person_ID = person_ID;
    }

    public String getPerson_ID() {
        return person_ID;
    }

    public void setPhotourl(String photourl) { this.photourl = photourl; }

    public String getPhotourl() { return photourl; }

    public long getAttendance() {
        return attendance;
    }

    public long getAttendance_total() {
        return attendance_total;
    }

    public void setAttendance(long attendance) {
        this.attendance = attendance;
    }

    public void setAttendance_total(long attendance_total) { this.attendance_total = attendance_total; }

    public Boolean getIspresent() {
        return ispresent;
    }

    public void setIspresent(Boolean ispresent) {
        this.ispresent = ispresent;
    }

    public void setnull() {
        this.ispresent = null;
    }

    public void setDates(HashMap<String, String> dates) {
        this.dates = dates;
    }

    public HashMap<String, String> getDates() {
        return dates;
    }

    public void setEnabled(String enabled) { this.enabled = enabled; }

    public String getEnabled() { return enabled; }

    @Override
    public int compareTo(Person o) { return this.getPerson_ID().compareTo(o.getPerson_ID()); }
}