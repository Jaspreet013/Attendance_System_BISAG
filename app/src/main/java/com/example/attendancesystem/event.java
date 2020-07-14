package com.example.attendancesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class event {
    private String name,organisation,coordinator_email;
    public event(){}
    public event(String name,String organisation,String coordinator_email)
    {
        this.name=name;
        this.organisation=organisation;
        this.coordinator_email=coordinator_email;
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
    public void setCoordinator_email(String coordinator_email) { this.coordinator_email = coordinator_email; }
    public String getCoordinator_email() {
        return coordinator_email;
    }
}
