package com.uaemex.gesdep.models;

public class CoachModel {
    private String id;
    private String name;
    private String sport;
    private String phone;
    private String email;

    public CoachModel() { }

    public CoachModel(String id, String name, String sport, String phone, String email) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.phone = phone;
        this.email = email;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSport() { return sport; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSport(String sport) { this.sport = sport; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
}

