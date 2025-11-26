package com.uaemex.gesdep;

public class ParticipantModel {
    private String id;
    private String name;
    private int age;
    private String activity;
    private String phone;

    public ParticipantModel() { }

    public ParticipantModel(String id, String name, int age, String activity, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.activity = activity;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getActivity() { return activity; }
    public String getPhone() { return phone; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setActivity(String activity) { this.activity = activity; }
    public void setPhone(String phone) { this.phone = phone; }
}

