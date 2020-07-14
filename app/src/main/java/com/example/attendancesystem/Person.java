package com.example.attendancesystem;

public class Person {
        private String person_email,person_ID,fname,lname,event_name,organisation,coordinator_email;
        private long attendance=0,total_attendance=0;
        public Person(){}
        public Person(String fname,String lname,String person_email,String person_ID,String event_name,String organisation,String coordinator_email){
            this.fname=fname;
            this.lname=lname;
            this.person_email=person_email;
            this.person_ID=person_ID;
            this.event_name=event_name;
            this.organisation=organisation;
            this.coordinator_email=coordinator_email;
        }
    public void setFname(String fname) { this.fname = fname; }
    public String getFname() { return fname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getLname() { return lname; }
    public void setPerson_email(String person_email) { this.person_email = person_email; }
    public String getPerson_email() { return person_email; }
    public void setPerson_ID(String person_ID) { this.person_ID = person_ID; }
    public String getPerson_ID() { return person_ID; }
    public long getAttendance() { return attendance; }
    public void increment_attendance(){ attendance+=1; }
    public void decrement_attendance(){ attendance-=1; }
    public long getTotalAttendance(){ return total_attendance; }
    public String getEvent_name() { return event_name; }
    public void setEvent_name(String event_name) { this.event_name = event_name; }
    public void increment_total_attendance(){ total_attendance+=1; }
    public void setOrganisation(String organisation) { this.organisation = organisation; }
    public String getOrganisation() { return organisation; }
    public String getCoordinator_email() { return coordinator_email; }
    public void setCoordinator_email(String coordinator_email) { this.coordinator_email = coordinator_email; }
}