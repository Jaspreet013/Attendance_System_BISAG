package com.example.attendancesystem;

public class event {
    private String person_email,name,person_id,organisation,coordinator_email;
    private int attendance,total_attendance;
    public event(String person_email,String name,String person_id,String organisation,String coordinator_email,int attendance)
    {
        this.person_email=person_email;
        this.name=name;
        this.person_id=person_id;
        this.organisation=organisation;
        this.coordinator_email=coordinator_email;
        this.attendance=attendance;
    }

    public void setPerson_email(String person_email) {
        this.person_email = person_email;
    }

    public String getPerson_email() {
        return person_email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public String getPerson_id() {
        return person_id;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setCoordinator_email(String coordinator_email) {
        this.coordinator_email = coordinator_email;
    }

    public String getCoordinator_email() {
        return coordinator_email;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    public int getAttendance() {
        return attendance;
    }
}
