package com.example.attendancesystem;
import java.util.HashMap;

public class Event implements Comparable<Event>{
    private String name,organisation,admin;
    public HashMap<String,Long> dates = new HashMap<>();
    //public HashMap<String,Integer> people=new HashMap<>();
    public Event(){}
    public Event(String name, String organisation,String admin)
    {
        this.name=name;
        this.organisation=organisation;
        this.admin=admin;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }
    public String getOrganisation() {
        return organisation;
    }
    public void setAdmin(String admin) { this.admin = admin; }
    public String getAdmin() { return admin; }
    public void setDates(HashMap<String, Long> dates) { this.dates = dates; }
    public HashMap<String, Long> getDates() { return dates; }

    @Override
    public int compareTo(Event o) {
        if(!this.getName().equalsIgnoreCase(o.getName())){
            return this.getName().compareToIgnoreCase(o.getName());
        }
        else{
            return this.getOrganisation().compareToIgnoreCase(o.getOrganisation());
        }
    }
}
