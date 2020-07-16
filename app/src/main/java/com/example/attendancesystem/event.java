package com.example.attendancesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class event {
    private String name,organisation;
    public event(){}
    public event(String name,String organisation)
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
