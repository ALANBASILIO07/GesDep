package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;

/**
 * Modelo para registrar el historial de cambios de un evento
 * Auditoría de modificaciones, cancelaciones y reprogramaciones
 */
public class EventChangeLogModel {

    // Identificación
    public String id;
    public String eventId;
    public String eventName;

    // Tipo de cambio
    public String changeType; // "created", "modified", "cancelled", "rescheduled", "location_changed"
    public String changedBy; // userId del admin que hizo el cambio
    public String changedByName;
    public Timestamp changedAt;

    // Detalles del cambio
    public String fieldChanged; // "dateTime", "location", "capacity", "status", etc.
    public String oldValue;
    public String newValue;

    // Razón del cambio
    public String reason;

    // Notificaciones
    public boolean notificationsSent;
    public int participantsNotified;

    // Constructor vacío requerido por Firestore
    public EventChangeLogModel() {
        this.notificationsSent = false;
        this.participantsNotified = 0;
    }

    // Constructor completo
    public EventChangeLogModel(String id, String eventId, String eventName,
                               String changeType, String changedBy, String changedByName,
                               String fieldChanged, String oldValue, String newValue, String reason) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.changeType = changeType;
        this.changedBy = changedBy;
        this.changedByName = changedByName;
        this.changedAt = Timestamp.now();
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.reason = reason;
        this.notificationsSent = false;
        this.participantsNotified = 0;
    }

    /**
     * Marca las notificaciones como enviadas
     */
    public void markNotificationsSent(int count) {
        this.notificationsSent = true;
        this.participantsNotified = count;
    }
}
