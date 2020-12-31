package com.example.attendancesystem;

import java.util.HashMap;

public class User {
    private String Fname,Lname,email,photourl;
    public HashMap<String,Integer> events=new HashMap<>(),admin_events=new HashMap<>();
    public User(){}
    public User(String Fname,String Lname,String email,String photourl){
        this.Fname=Fname;
        this.Lname=Lname;
        this.email=email;
        this.photourl=photourl;
    }

    public String getFname() { return Fname; }

    public void setFname(String fname) { Fname = fname; }

    public String getLname() { return Lname; }

    public void setLname(String lname) { Lname = lname; }

    public void setEmail(String email) { this.email = email; }

    public String getEmail() { return email; }

    public void setPhotourl(String photourl) { this.photourl = photourl; }

    public String getPhotourl() { return photourl; }

    public void setEvents(HashMap<String, Integer> events) { this.events = events; }

    public HashMap<String, Integer> getEvents() { return events; }

    public void setAdmin_events(HashMap<String, Integer> admin_events) { this.admin_events = admin_events; }

    public HashMap<String, Integer> getAdmin_events() { return admin_events; }

    /*public User getUser(){
        DatabaseReference user_database= FirebaseDatabase.getInstance().getReference("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        final User user=new User();
        user_database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    User temp=dataSnapshot.getValue(User.class);
                    user.setFname(temp.getFname());
                    user.setLname(temp.getLname());
                    user.setEmail(temp.getEmail());
                    user.events=temp.events;
                    user.admin_events=temp.admin_events;
                }
                catch (Exception e){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return user;
    }
    public User getUser(String key){
        DatabaseReference user_database= FirebaseDatabase.getInstance().getReference("Users/"+key);
        final User user=new User();
        user_database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    User temp=dataSnapshot.getValue(User.class);
                    user.setFname(temp.getFname());
                    user.setLname(temp.getLname());
                    user.setEmail(temp.getEmail());
                    user.events=temp.events;
                    user.admin_events=temp.admin_events;
                }
                catch (Exception e){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return user;
    }*/
    /*public void setUser(User user){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.setValue(user);
    }
    public void setUser(User user,String key){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users/"+key);
        databaseReference.setValue(user);
    }*/
}
