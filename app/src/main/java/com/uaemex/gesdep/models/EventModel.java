package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventModel implements Serializable {

    private String id;
    private String title;
    private String description;
    private String macroEvent;
    private String category;
    private String discipline;
    private String modality;
    private String placeName;
    private String address;
    private double latitude;
    private double longitude;

    // CAMBIO IMPORTANTE: Usar Date en lugar de Timestamp para Serializable
    private Date eventDateTime;
    private Date startTime;
    private Date endTime;
    private Date registrationDeadline;

    private int durationMinutes;
    private int minQuota;
    private int maxQuota;
    private int currentParticipants;

    private boolean paid;
    private double cost;
    private String paymentConcept;

    private String status;
    private String organizerId;
    private String organizerName;

    @ServerTimestamp
    private Date createdAt;

    private List<String> imageUrls;

    public EventModel() {
        this.imageUrls = new ArrayList<>();
        this.status = "ACTIVO";
        this.currentParticipants = 0;
        this.paid = false;
        this.cost = 0.0;
    }

    // --- GETTERS Y SETTERS (Tipo Date) ---

    public Date getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(Date eventDateTime) { this.eventDateTime = eventDateTime; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Date getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(Date registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // ... (Resto de Getters y Setters estándar) ...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMacroEvent() { return macroEvent; }
    public void setMacroEvent(String macroEvent) { this.macroEvent = macroEvent; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDiscipline() { return discipline; }
    public void setDiscipline(String discipline) { this.discipline = discipline; }
    public String getModality() { return modality; }
    public void setModality(String modality) { this.modality = modality; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getMinQuota() { return minQuota; }
    public void setMinQuota(int minQuota) { this.minQuota = minQuota; }
    public int getMaxQuota() { return maxQuota; }
    public void setMaxQuota(int maxQuota) { this.maxQuota = maxQuota; }
    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public String getPaymentConcept() { return paymentConcept; }
    public void setPaymentConcept(String paymentConcept) { this.paymentConcept = paymentConcept; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    // Lógica con Dates
    @Exclude
    public String getTimeStatus() {
        if ("CANCELADO".equals(status)) return "CANCELADO";
        if (startTime == null || endTime == null) return "PENDIENTE";
        long now = new Date().getTime();
        long startMillis = startTime.getTime();
        long endMillis = endTime.getTime();

        if (now < startMillis) return "PENDIENTE";
        else if (now >= startMillis && now <= endMillis) return "EN VIVO";
        else return "FINALIZADO";
    }
    @Exclude public int getAvailableSpots() { return maxQuota - currentParticipants; }
    @Exclude public boolean isFull() { return currentParticipants >= maxQuota; }
}