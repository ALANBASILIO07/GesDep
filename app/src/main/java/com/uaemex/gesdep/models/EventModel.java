package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de Evento Deportivo/Cultural
 * Sistema de gestión de eventos para IMCUFIDE
 */
public class EventModel {

    // Identificación
    public String id;
    public String name;
    public String description;

    // Tipo de evento
    public String type; // "deportivo" o "cultural"
    public String category; // "futbol", "basquetbol", "atletismo", "danza", "teatro", etc.

    // Ubicación
    public String placeName;
    public double latitude;
    public double longitude;
    public int distanceFromCenterMinutes; // Tiempo estimado desde centro (para calcular tiempo mínimo de cambio)

    // Fecha y hora
    public Timestamp eventDateTime;
    public Timestamp registrationDeadline;
    public int durationMinutes;

    // Registro
    public String registrationType; // "individual" o "team"
    public int minParticipants; // Mínimo 2 para confirmar evento
    public int maxParticipants; // Capacidad máxima
    public int currentParticipants; // Participantes actuales
    public int currentTeams; // Equipos actuales (si es tipo team)

    // Estado
    public String status; // "active", "confirmed", "cancelled", "rescheduled", "completed"
    public boolean isConfirmed; // true si ya alcanzó el mínimo de participantes
    public String cancellationReason;
    public Timestamp lastModified;
    public Timestamp createdAt;

    // Validación de cambios - tiempo mínimo en minutos según distancia
    public int minimumMinutesBeforeChange; // 30 min, 60 min, 120 min

    // Organizador
    public String organizerId;
    public String organizerName;
    public String organizerEmail;

    // Multimedia
    public List<String> photoUrls;
    public List<String> videoUrls;
    public String thumbnailUrl;

    // Información adicional
    public String rules; // Reglas específicas del evento
    public String requirements; // Requisitos para participar
    public String prizes; // Premios o reconocimientos

    // Constructor vacío requerido por Firestore
    public EventModel() {
        this.photoUrls = new ArrayList<>();
        this.videoUrls = new ArrayList<>();
        this.minParticipants = 2; // Por defecto mínimo 2
        this.status = "active";
        this.isConfirmed = false;
        this.currentParticipants = 0;
        this.currentTeams = 0;
    }

    // Constructor completo
    public EventModel(String id, String name, String description, String type, String category,
                      String placeName, double latitude, double longitude,
                      int distanceFromCenterMinutes, Timestamp eventDateTime,
                      Timestamp registrationDeadline, int durationMinutes,
                      String registrationType, int minParticipants, int maxParticipants,
                      String organizerId, String organizerName, String organizerEmail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromCenterMinutes = distanceFromCenterMinutes;
        this.eventDateTime = eventDateTime;
        this.registrationDeadline = registrationDeadline;
        this.durationMinutes = durationMinutes;
        this.registrationType = registrationType;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 0;
        this.currentTeams = 0;
        this.status = "active";
        this.isConfirmed = false;
        this.createdAt = Timestamp.now();
        this.lastModified = Timestamp.now();
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.organizerEmail = organizerEmail;
        this.photoUrls = new ArrayList<>();
        this.videoUrls = new ArrayList<>();

        // Calcular tiempo mínimo según distancia
        calculateMinimumChangeTime();
    }

    /**
     * Calcula el tiempo mínimo para hacer cambios según la distancia
     * Distancia corta (<15 min): 30 minutos
     * Distancia media (15-30 min): 60 minutos
     * Distancia larga (>30 min): 120 minutos
     */
    public void calculateMinimumChangeTime() {
        if (distanceFromCenterMinutes <= 15) {
            this.minimumMinutesBeforeChange = 30;
        } else if (distanceFromCenterMinutes <= 30) {
            this.minimumMinutesBeforeChange = 60;
        } else {
            this.minimumMinutesBeforeChange = 120;
        }
    }

    /**
     * Verifica si el evento puede ser modificado
     * @return true si aún hay tiempo suficiente para hacer cambios
     */
    public boolean canBeModified() {
        if (eventDateTime == null) return false;

        long currentTimeMillis = System.currentTimeMillis();
        long eventTimeMillis = eventDateTime.toDate().getTime();
        long minutesUntilEvent = (eventTimeMillis - currentTimeMillis) / (1000 * 60);

        return minutesUntilEvent >= minimumMinutesBeforeChange;
    }

    /**
     * Obtiene el tiempo restante en minutos hasta el evento
     */
    public long getMinutesUntilEvent() {
        if (eventDateTime == null) return 0;

        long currentTimeMillis = System.currentTimeMillis();
        long eventTimeMillis = eventDateTime.toDate().getTime();
        return (eventTimeMillis - currentTimeMillis) / (1000 * 60);
    }

    /**
     * Verifica si el evento está confirmado (alcanzó el mínimo de participantes)
     */
    public void updateConfirmationStatus() {
        if (registrationType.equals("team")) {
            this.isConfirmed = currentTeams >= minParticipants;
        } else {
            this.isConfirmed = currentParticipants >= minParticipants;
        }
    }

    /**
     * Verifica si el evento está lleno
     */
    public boolean isFull() {
        if (registrationType.equals("team")) {
            return currentTeams >= maxParticipants;
        } else {
            return currentParticipants >= maxParticipants;
        }
    }

    /**
     * Obtiene el número de espacios disponibles
     */
    public int getAvailableSpots() {
        if (registrationType.equals("team")) {
            return maxParticipants - currentTeams;
        } else {
            return maxParticipants - currentParticipants;
        }
    }
}
