package com.example.attendancesystem;

public class User {
    private String fname,lname,email;

    public User(){}

    public User(String fname,String lname,String email){
        this.fname=fname;
        this.lname=lname;
        this.email=email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail(){
        return this.email;
    }

    public void setFname(String fname){
        this.fname=fname;
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
}
