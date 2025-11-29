package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de Evento Deportivo/Cultural
 * Sistema de gestión de eventos para IMCUFIDE
 * Actualizado para soportar Macro Eventos, Modalidades y Disciplinas.
 */
public class EventModel implements Serializable {

    // Identificación
    private String id;
    private String title;           // Antes 'name'. Título del evento.
    private String description;
    private String macroEvent;      // NUEVO: Ej: "Juegos Universitarios 2025" (Opcional)

    // Clasificación
    private String category;        // "Deportivo", "Cultural", "Académico", "Social"
    private String discipline;      // NUEVO: "Fútbol", "Ajedrez", "Danza", "Conferencia", etc.
    private String modality;        // NUEVO: "Torneo", "Amistoso", "Liga", "Clase", "Presentación"

    // Ubicación y Logística
    private String placeName;       // Nombre del lugar (ej: "Cancha 1")
    private String address;         // Dirección legible
    private double latitude;        // Coordenada GPS
    private double longitude;       // Coordenada GPS

    // Fecha y Hora
    private Timestamp eventDateTime;
    private Timestamp registrationDeadline; // Fecha límite para inscribirse
    private int durationMinutes;

    // Registro y Cupos
    private int minQuota;           // Mínimo para que el evento no se cancele
    private int maxQuota;           // Capacidad máxima
    private int currentParticipants;

    // Estado y Control
    private String status;          // "ACTIVO", "CANCELADO", "FINALIZADO", "LLENO"
    private String organizerId;     // UID del creador
    private String organizerName;
    private Timestamp createdAt;

    // Multimedia
    private List<String> imageUrls; // Galería de hasta 5 fotos (la 0 es el banner)

    // Constructor vacío requerido por Firestore
    public EventModel() {
        this.imageUrls = new ArrayList<>();
        this.status = "ACTIVO";
        this.currentParticipants = 0;
        this.createdAt = Timestamp.now();
    }

    // --- GETTERS Y SETTERS (Necesarios para Firebase) ---

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

    public Timestamp getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(Timestamp eventDateTime) { this.eventDateTime = eventDateTime; }

    public Timestamp getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(Timestamp registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

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

    // --- MÉTODOS DE UTILIDAD ---

    /**
     * Calcula los espacios disponibles restantes.
     */
    @Exclude // Excluir para que no se guarde como campo en BD, se calcula al vuelo
    public int getAvailableSpots() {
        return maxQuota - currentParticipants;
    }

    /**
     * Verifica si el evento ha alcanzado el mínimo requerido.
     */
    @Exclude
    public boolean isMinimumMet() {
        return currentParticipants >= minQuota;
    }

    /**
     * Verifica si el evento está lleno.
     */
    @Exclude
    public boolean isFull() {
        return currentParticipants >= maxQuota;
    }

    /**
     * Valida si el mínimo de participantes es congruente con la modalidad.
     * (Ej: Un torneo no puede ser de 1 persona).
     * @return true si es válido, false si hay error de lógica.
     */
    @Exclude
    public boolean validateModalityConstraints() {
        if (modality == null) return true; // Si no hay modalidad, asumimos válido

        switch (modality.toLowerCase()) {
            case "torneo":
                return minQuota >= 4; // Mínimo 4 para un torneo
            case "amistoso":
            case "liga":
                return minQuota >= 2; // Mínimo 2 para competir
            case "clase":
            case "presentación":
                return minQuota >= 1; // Al menos 1 interesado
            default:
                return true;
        }
    }
}