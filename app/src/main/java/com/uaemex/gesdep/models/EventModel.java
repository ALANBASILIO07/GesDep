package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo de Evento Deportivo/Cultural
 * Actualizado con lógica de Tiempos (Inicio/Fin) y Status Dinámico.
 */
public class EventModel implements Serializable {

    // Identificación
    private String id;
    private String title;
    private String description;
    private String macroEvent;

    // Clasificación
    private String category;
    private String discipline;
    private String modality;

    // Ubicación
    private String placeName;
    private String address;
    private double latitude;
    private double longitude;

    // --- TIEMPOS Y LOGÍSTICA ---
    private Timestamp eventDateTime;      // (Legacy/Backup) Fecha de inicio general
    private Timestamp startTime;          // Hora exacta de inicio
    private Timestamp endTime;            // Hora exacta de fin
    private Timestamp registrationDeadline; // Límite para inscribirse
    private int durationMinutes;          // (Opcional) Calculado

    // Cupos
    private int minQuota;
    private int maxQuota;
    private int currentParticipants;

    // Control
    private String status; // "ACTIVO", "CANCELADO" (El estado de tiempo se calcula al vuelo)
    private String organizerId;
    private String organizerName;
    private Timestamp createdAt;

    // Multimedia
    private List<String> imageUrls;

    // Constructor vacío (Requerido por Firebase)
    public EventModel() {
        this.imageUrls = new ArrayList<>();
        this.status = "ACTIVO";
        this.currentParticipants = 0;
        this.createdAt = Timestamp.now();
    }

    // --- GETTERS Y SETTERS ---

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

    // Tiempos
    public Timestamp getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(Timestamp eventDateTime) { this.eventDateTime = eventDateTime; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public Timestamp getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(Timestamp registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    // Cupos
    public int getMinQuota() { return minQuota; }
    public void setMinQuota(int minQuota) { this.minQuota = minQuota; }

    public int getMaxQuota() { return maxQuota; }
    public void setMaxQuota(int maxQuota) { this.maxQuota = maxQuota; }

    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    // --- MÉTODOS DE LÓGICA (Calculados) ---

    /**
     * Determina el estado temporal del evento:
     * - PENDIENTE: Aún no empieza.
     * - EN VIVO: Está ocurriendo ahora mismo.
     * - FINALIZADO: Ya terminó la hora de fin.
     */
    @Exclude
    public String getTimeStatus() {
        if ("CANCELADO".equals(status)) return "CANCELADO";

        if (startTime == null || endTime == null) return "PENDIENTE";

        long now = new Date().getTime();
        long startMillis = startTime.toDate().getTime();
        long endMillis = endTime.toDate().getTime();

        if (now < startMillis) {
            return "PENDIENTE";
        } else if (now >= startMillis && now <= endMillis) {
            return "EN VIVO";
        } else {
            return "FINALIZADO";
        }
    }

    @Exclude
    public int getAvailableSpots() {
        return maxQuota - currentParticipants;
    }

    @Exclude
    public boolean isFull() {
        return currentParticipants >= maxQuota;
    }
}