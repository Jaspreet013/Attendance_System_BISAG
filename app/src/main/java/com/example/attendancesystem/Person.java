package com.example.attendancesystem;

import java.util.HashMap;

public class Person {
    private String person_email, person_ID, fname, lname, event_name, organisation, enabled = "Yes";
    private long attendance = 0, attendance_total = 0;
    private Boolean ispresent = null;
    public HashMap<String, String> dates = new HashMap<>();

    public Person() {
    }

    public Person(String fname, String lname, String person_email, String person_ID, String event_name, String organisation) {
        this.fname = fname;
        this.lname = lname;
        this.person_email = person_email;
        this.person_ID = person_ID;
        this.event_name = event_name;
        this.organisation = organisation;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getFname() {
        return fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getLname() {
        return lname;
    }

    public void setPerson_email(String person_email) {
        this.person_email = person_email;
    }

    public String getPerson_email() {
        return person_email;
    }

    public void setPerson_ID(String person_ID) {
        this.person_ID = person_ID;
    }

    public String getPerson_ID() {
        return person_ID;
    }

    public long getAttendance() {
        return attendance;
    }

    public long getAttendance_total() {
        return attendance_total;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public void setAttendance(long attendance) {
        this.attendance = attendance;
    }

    public void setAttendance_total(long attendance_total) { this.attendance_total = attendance_total; }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getOrganisation() {
        return organisation;
    }

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
}