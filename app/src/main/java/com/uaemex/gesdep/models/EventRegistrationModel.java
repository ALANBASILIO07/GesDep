package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;

/**
 * Modelo de Registro a Evento
 * Gestiona el registro de usuarios/equipos a eventos con sistema de confirmación
 */
public class EventRegistrationModel {

    // Identificación
    public String id;
    public String eventId;
    public String eventName;
    public String userId;
    public String userName;

    // Tipo de registro
    public String registrationType; // "individual" o "team"
    public String teamId; // Solo si es registro de equipo
    public String teamName; // Nombre del equipo (si aplica)

    // Estado del registro
    public String status; // "confirmed", "pending", "cancelled", "completed", "delayed"
    public boolean isConfirmed; // true cuando usuario confirma su asistencia
    public Timestamp registeredAt;
    public Timestamp confirmedAt;
    public Timestamp lastModified;

    // Solicitud de retraso/reprogramación
    public boolean hasDelayRequest;
    public String delayReason;
    public int delayMinutes; // Minutos de retraso solicitados
    public Timestamp delayRequestedAt;
    public String delayStatus; // "pending", "approved_by_rival", "approved_by_admin", "rejected"

    // Aprobaciones para cambios (sistema de confirmación)
    public boolean rivalApproved; // true si el rival/oponente aprobó el cambio
    public boolean adminApproved; // true si el admin aprobó el cambio
    public String rivalId; // ID del rival (si es un enfrentamiento 1v1 o equipo vs equipo)

    // Asistencia
    public boolean attended; // true si el usuario/equipo asistió al evento
    public Timestamp checkInTime; // Hora de check-in (llegada al evento)
    public double checkInLatitude; // Ubicación del check-in
    public double checkInLongitude;

    // Información adicional
    public String notes;
    public String emergencyContact;
    public String emergencyPhone;

    // Constructor vacío requerido por Firestore
    public EventRegistrationModel() {
        this.isConfirmed = true; // Por defecto se confirma automáticamente
        this.status = "confirmed";
        this.hasDelayRequest = false;
        this.attended = false;
        this.rivalApproved = false;
        this.adminApproved = false;
    }

    // Constructor para registro individual
    public EventRegistrationModel(String id, String eventId, String eventName,
                                   String userId, String userName) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.registrationType = "individual";
        this.status = "confirmed";
        this.isConfirmed = true;
        this.registeredAt = Timestamp.now();
        this.confirmedAt = Timestamp.now();
        this.lastModified = Timestamp.now();
        this.hasDelayRequest = false;
        this.attended = false;
        this.rivalApproved = false;
        this.adminApproved = false;
    }

    // Constructor para registro de equipo
    public EventRegistrationModel(String id, String eventId, String eventName,
                                   String userId, String userName,
                                   String teamId, String teamName) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.userName = userName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.registrationType = "team";
        this.status = "confirmed";
        this.isConfirmed = true;
        this.registeredAt = Timestamp.now();
        this.confirmedAt = Timestamp.now();
        this.lastModified = Timestamp.now();
        this.hasDelayRequest = false;
        this.attended = false;
        this.rivalApproved = false;
        this.adminApproved = false;
    }

    /**
     * Solicita un retraso para el evento
     * Requiere aprobación del rival y del administrador
     */
    public void requestDelay(String reason, int minutes) {
        this.hasDelayRequest = true;
        this.delayReason = reason;
        this.delayMinutes = minutes;
        this.delayRequestedAt = Timestamp.now();
        this.delayStatus = "pending";
        this.status = "delayed";
        this.lastModified = Timestamp.now();
    }

    /**
     * Aprueba el retraso por parte del rival
     */
    public void approveDelayByRival() {
        this.rivalApproved = true;
        this.lastModified = Timestamp.now();

        // Si ambos aprobaron, cambiar estado
        if (rivalApproved && adminApproved) {
            this.delayStatus = "approved_by_admin";
        } else {
            this.delayStatus = "approved_by_rival";
        }
    }

    /**
     * Aprueba el retraso por parte del administrador
     */
    public void approveDelayByAdmin() {
        this.adminApproved = true;
        this.lastModified = Timestamp.now();

        // Si ambos aprobaron, cambiar estado
        if (rivalApproved && adminApproved) {
            this.delayStatus = "approved_by_admin";
            this.status = "confirmed"; // Vuelve a estar confirmado con el nuevo horario
        }
    }

    /**
     * Rechaza la solicitud de retraso
     */
    public void rejectDelay() {
        this.delayStatus = "rejected";
        this.hasDelayRequest = false;
        this.status = "confirmed";
        this.lastModified = Timestamp.now();
    }

    /**
     * Verifica si la solicitud de retraso fue completamente aprobada
     */
    public boolean isDelayFullyApproved() {
        return rivalApproved && adminApproved && delayStatus.equals("approved_by_admin");
    }

    /**
     * Registra la asistencia del usuario/equipo
     */
    public void checkIn(double latitude, double longitude) {
        this.attended = true;
        this.checkInTime = Timestamp.now();
        this.checkInLatitude = latitude;
        this.checkInLongitude = longitude;
        this.status = "completed";
        this.lastModified = Timestamp.now();
    }

    /**
     * Cancela el registro
     */
    public void cancel() {
        this.status = "cancelled";
        this.isConfirmed = false;
        this.lastModified = Timestamp.now();
    }
}
