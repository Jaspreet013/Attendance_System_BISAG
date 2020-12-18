package com.example.attendancesystem;
import java.util.HashMap;

public class Event {
    private String name,organisation;
    public final HashMap<String,Long> dates = new HashMap<>();
    public Event(){}
    public Event(String name, String organisation)
    {
        this.name=name;
        this.organisation=organisation;
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
}
