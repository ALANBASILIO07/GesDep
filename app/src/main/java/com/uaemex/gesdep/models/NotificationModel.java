package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private String type; // "event", "report", "system"
    private Timestamp timestamp;
    private boolean read;

    // Navigation fields for smart routing
    private String eventId;
    private String reportId;
    private String targetActivity; // "EventDetailActivity" or "ReportDetailActivity"

    // User targeting
    private String userId; // The user who should receive this notification

    // Type constants
    public static final String TYPE_EVENT = "event";
    public static final String TYPE_REPORT = "report";
    public static final String TYPE_SYSTEM = "system";

    public NotificationModel() {} // Constructor vacío para Firebase

    /**
     * Constructor for event notifications
     */
    public NotificationModel(String title, String message, String eventId, String userId) {
        this.title = title;
        this.message = message;
        this.type = TYPE_EVENT;
        this.eventId = eventId;
        this.userId = userId;
        this.targetActivity = "EventDetailActivity";
        this.timestamp = Timestamp.now();
        this.read = false;
    }

    /**
     * Constructor for report notifications
     */
    public NotificationModel(String title, String message, String eventId, String reportId, String userId) {
        this.title = title;
        this.message = message;
        this.type = TYPE_REPORT;
        this.eventId = eventId;
        this.reportId = reportId;
        this.userId = userId;
        this.targetActivity = "ReportDetailActivity";
        this.timestamp = Timestamp.now();
        this.read = false;
    }

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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getTargetActivity() { return targetActivity; }
    public void setTargetActivity(String targetActivity) { this.targetActivity = targetActivity; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}