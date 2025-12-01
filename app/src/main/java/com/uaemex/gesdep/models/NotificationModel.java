package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private String type; // "Aviso", "Reporte", "Sistema"
    private Timestamp timestamp;
    private boolean read;

    public NotificationModel() {} // Constructor vacío para Firebase

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}