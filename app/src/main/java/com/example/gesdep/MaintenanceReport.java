package com.example.gesdep;

public class MaintenanceReport {
    private String id;
    private String title;
    private String description;
    private String location;
    private String status; // Pendiente, En proceso, Resuelto

    public MaintenanceReport() { }

    public MaintenanceReport(String id, String title, String description,
                             String location, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }
}

