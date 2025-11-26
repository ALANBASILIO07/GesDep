package com.uaemex.gesdep.models;

import com.google.firebase.Timestamp;
import java.util.List;

/**
 * Modelo de Notificación
 * Representa una notificación pendiente de envío o un historial de notificaciones enviadas
 */
public class NotificationModel {

    // Identificación
    public String id;
    public String eventId;
    public String eventName;

    // Contenido de la notificación
    public String title;
    public String message;
    public String type; // "event_changed", "event_cancelled", "event_rescheduled", etc.

    // Destinatarios
    public List<String> tokens; // Tokens FCM de los destinatarios
    public List<String> userIds; // IDs de los usuarios destinatarios
    public int recipientCount;

    // Estado de envío
    public boolean sent;
    public Timestamp createdAt;
    public Timestamp sentAt;
    public String status; // "pending", "sent", "failed"
    public String errorMessage;

    // Datos adicionales
    public String senderUserId;
    public String senderUserName;

    // Constructor vacío requerido por Firestore
    public NotificationModel() {
        this.sent = false;
        this.status = "pending";
        this.recipientCount = 0;
    }

    // Constructor completo
    public NotificationModel(String id, String eventId, String eventName,
                             String title, String message, String type,
                             List<String> tokens, List<String> userIds,
                             String senderUserId, String senderUserName) {
        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.title = title;
        this.message = message;
        this.type = type;
        this.tokens = tokens;
        this.userIds = userIds;
        this.recipientCount = tokens != null ? tokens.size() : 0;
        this.senderUserId = senderUserId;
        this.senderUserName = senderUserName;
        this.sent = false;
        this.status = "pending";
        this.createdAt = Timestamp.now();
    }

    /**
     * Marca la notificación como enviada
     */
    public void markAsSent() {
        this.sent = true;
        this.status = "sent";
        this.sentAt = Timestamp.now();
    }

    /**
     * Marca la notificación como fallida
     */
    public void markAsFailed(String error) {
        this.sent = false;
        this.status = "failed";
        this.errorMessage = error;
        this.sentAt = Timestamp.now();
    }
}
