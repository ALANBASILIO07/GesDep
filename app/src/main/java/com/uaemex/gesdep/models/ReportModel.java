package com.uaemex.gesdep.models;

import java.util.ArrayList;
import java.util.List;

public class ReportModel {
    private String reportId;
    private String eventId;
    private String eventName;

    // Creator info
    private String createdByUid;
    private String createdByName;
    private String createdByRole;

    // Report content
    private String subject;
    private String description;
    private List<String> photoUrls;

    // Classification
    private String category;
    private String priority; // ALTA, MEDIA, BAJA
    private String status; // Pendiente, En Proceso, Resuelto

    // Admin response
    private String adminResponse;
    private String respondedByUid;
    private String respondedByName;

    // Timestamps
    private long createdAt;
    private long updatedAt;

    // Category constants
    public static final String CATEGORY_VENUE_DAMAGE = "Daño en instalación";
    public static final String CATEGORY_WEATHER = "Reprogramación por clima";
    public static final String CATEGORY_ACCIDENT = "Accidente";
    public static final String CATEGORY_PARTICIPANT_ABSENCE = "Participante/rival no puede llegar";
    public static final String CATEGORY_REFEREE_ABSENCE = "Ausencia de árbitro";
    public static final String CATEGORY_COURT_MAINTENANCE = "Cancha sin pintar";
    public static final String CATEGORY_SOUND_DELAY = "Retraso en sonido";
    public static final String CATEGORY_OTHER = "Otro";

    // Priority constants
    public static final String PRIORITY_HIGH = "ALTA";
    public static final String PRIORITY_MEDIUM = "MEDIA";
    public static final String PRIORITY_LOW = "BAJA";

    // Status constants
    public static final String STATUS_PENDING = "Pendiente";
    public static final String STATUS_IN_PROCESS = "En Proceso";
    public static final String STATUS_RESOLVED = "Resuelto";

    public ReportModel() {
        // Default constructor required for Firestore
        this.photoUrls = new ArrayList<>();
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public ReportModel(String eventId, String eventName, String createdByUid,
                      String createdByName, String createdByRole, String subject,
                      String description, String category) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.createdByUid = createdByUid;
        this.createdByName = createdByName;
        this.createdByRole = createdByRole;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.photoUrls = new ArrayList<>();
        this.priority = assignPriorityByCategory(category);
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * Auto-assign priority based on category
     */
    public static String assignPriorityByCategory(String category) {
        switch (category) {
            case CATEGORY_VENUE_DAMAGE:
            case CATEGORY_WEATHER:
            case CATEGORY_ACCIDENT:
                return PRIORITY_HIGH;

            case CATEGORY_PARTICIPANT_ABSENCE:
            case CATEGORY_REFEREE_ABSENCE:
                return PRIORITY_MEDIUM;

            case CATEGORY_COURT_MAINTENANCE:
            case CATEGORY_SOUND_DELAY:
            case CATEGORY_OTHER:
            default:
                return PRIORITY_LOW;
        }
    }

    /**
     * Get all available categories
     */
    public static String[] getCategories() {
        return new String[] {
            CATEGORY_VENUE_DAMAGE,
            CATEGORY_WEATHER,
            CATEGORY_ACCIDENT,
            CATEGORY_PARTICIPANT_ABSENCE,
            CATEGORY_REFEREE_ABSENCE,
            CATEGORY_COURT_MAINTENANCE,
            CATEGORY_SOUND_DELAY,
            CATEGORY_OTHER
        };
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getCreatedByUid() {
        return createdByUid;
    }

    public void setCreatedByUid(String createdByUid) {
        this.createdByUid = createdByUid;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByRole() {
        return createdByRole;
    }

    public void setCreatedByRole(String createdByRole) {
        this.createdByRole = createdByRole;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        // Auto-update priority when category changes
        this.priority = assignPriorityByCategory(category);
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getRespondedByUid() {
        return respondedByUid;
    }

    public void setRespondedByUid(String respondedByUid) {
        this.respondedByUid = respondedByUid;
    }

    public String getRespondedByName() {
        return respondedByName;
    }

    public void setRespondedByName(String respondedByName) {
        this.respondedByName = respondedByName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
