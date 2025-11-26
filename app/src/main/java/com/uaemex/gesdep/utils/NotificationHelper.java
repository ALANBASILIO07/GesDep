package com.uaemex.gesdep.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper para enviar notificaciones push a través de Firebase Cloud Messaging
 * Gestiona el envío de notificaciones a usuarios registrados en eventos
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private final FirebaseFirestore db;

    public NotificationHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Envía notificación de cambio de evento a todos los participantes
     */
    public void notifyEventChanged(String eventId, String eventName, String changeType,
                                    String reason, String changedBy) {
        Log.d(TAG, "Enviando notificación de cambio para evento: " + eventName);

        // Obtener todos los registros del evento
        db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> userIds = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            userIds.add(userId);
                        }
                    });

                    if (!userIds.isEmpty()) {
                        String title = getNotificationTitle(changeType);
                        String message = getNotificationMessage(changeType, eventName, reason);
                        sendNotificationToUsers(userIds, title, message, eventId, changeType);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error obteniendo registros", e));
    }

    /**
     * Envía notificación de cancelación de evento
     */
    public void notifyEventCancelled(String eventId, String eventName, String cancellationReason) {
        String title = "⚠️ Evento Cancelado";
        String message = "El evento \"" + eventName + "\" ha sido cancelado.\n" +
                "Motivo: " + cancellationReason;

        notifyEventChanged(eventId, eventName, "event_cancelled", cancellationReason, "admin");
    }

    /**
     * Envía notificación de reprogramación de evento
     */
    public void notifyEventRescheduled(String eventId, String eventName, String newDateTime, String reason) {
        String title = "📅 Evento Reprogramado";
        String message = "El evento \"" + eventName + "\" ha sido reprogramado.\n" +
                "Nueva fecha: " + newDateTime + "\n" +
                "Motivo: " + reason;

        notifyEventChanged(eventId, eventName, "event_rescheduled", reason, "admin");
    }

    /**
     * Envía notificación de cambio de ubicación
     */
    public void notifyLocationChanged(String eventId, String eventName, String newLocation, String reason) {
        String title = "📍 Cambio de Ubicación";
        String message = "El evento \"" + eventName + "\" cambió de ubicación.\n" +
                "Nueva ubicación: " + newLocation + "\n" +
                "Motivo: " + reason;

        notifyEventChanged(eventId, eventName, "location_changed", reason, "admin");
    }

    /**
     * Envía recordatorio 24 horas antes del evento
     */
    public void sendEventReminder(String eventId, String eventName, String dateTime, String location) {
        Log.d(TAG, "Enviando recordatorio para evento: " + eventName);

        db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "confirmed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> userIds = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            userIds.add(userId);
                        }
                    });

                    if (!userIds.isEmpty()) {
                        String title = "⏰ Recordatorio de Evento";
                        String message = "¡No olvides! El evento \"" + eventName + "\" es mañana.\n" +
                                "Fecha: " + dateTime + "\n" +
                                "Lugar: " + location;
                        sendNotificationToUsers(userIds, title, message, eventId, "event_reminder");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error obteniendo registros para recordatorio", e));
    }

    /**
     * Notifica solicitud de retraso al rival y al admin
     */
    public void notifyDelayRequest(String eventId, String eventName, String requesterId,
                                    String requesterName, String rivalId, String adminId,
                                    String delayReason, int delayMinutes) {
        List<String> recipientIds = new ArrayList<>();
        recipientIds.add(rivalId);
        recipientIds.add(adminId);

        String title = "⏱️ Solicitud de Retraso";
        String message = requesterName + " solicita un retraso de " + delayMinutes +
                " minutos para el evento \"" + eventName + "\".\n" +
                "Motivo: " + delayReason;

        sendNotificationToUsers(recipientIds, title, message, eventId, "delay_request");
    }

    /**
     * Notifica aprobación de retraso por el rival
     */
    public void notifyDelayApprovedByRival(String eventId, String eventName, String requesterId,
                                            String rivalName, String adminId) {
        List<String> recipientIds = new ArrayList<>();
        recipientIds.add(requesterId);
        recipientIds.add(adminId);

        String title = "✅ Retraso Aprobado por Rival";
        String message = rivalName + " aprobó la solicitud de retraso para \"" + eventName + "\".\n" +
                "Esperando aprobación del administrador.";

        sendNotificationToUsers(recipientIds, title, message, eventId, "delay_approved_by_rival");
    }

    /**
     * Notifica aprobación final del retraso por el admin
     */
    public void notifyDelayApproved(String eventId, String eventName, String requesterId,
                                     String rivalId, int delayMinutes) {
        List<String> recipientIds = new ArrayList<>();
        recipientIds.add(requesterId);
        recipientIds.add(rivalId);

        String title = "✅ Retraso Aprobado";
        String message = "Tu solicitud de retraso de " + delayMinutes +
                " minutos para \"" + eventName + "\" ha sido aprobada.";

        sendNotificationToUsers(recipientIds, title, message, eventId, "delay_approved");
    }

    /**
     * Notifica rechazo de solicitud de retraso
     */
    public void notifyDelayRejected(String eventId, String eventName, String requesterId,
                                     String rejectedBy, String rejectionReason) {
        List<String> recipientIds = new ArrayList<>();
        recipientIds.add(requesterId);

        String title = "❌ Retraso Rechazado";
        String message = "Tu solicitud de retraso para \"" + eventName + "\" fue rechazada.\n" +
                "Motivo: " + rejectionReason;

        sendNotificationToUsers(recipientIds, title, message, eventId, "delay_rejected");
    }

    /**
     * Notifica confirmación de evento (se alcanzó el mínimo de participantes)
     */
    public void notifyEventConfirmed(String eventId, String eventName) {
        String title = "✅ Evento Confirmado";
        String message = "¡Buenas noticias! El evento \"" + eventName +
                "\" alcanzó el número mínimo de participantes y está confirmado.";

        notifyEventChanged(eventId, eventName, "event_confirmed", "Mínimo de participantes alcanzado", "system");
    }

    /**
     * Envía notificación a una lista de usuarios
     */
    private void sendNotificationToUsers(List<String> userIds, String title, String message,
                                          String eventId, String notificationType) {
        if (userIds.isEmpty()) {
            Log.w(TAG, "No hay usuarios a los cuales notificar");
            return;
        }

        // Obtener tokens FCM de los usuarios
        db.collection("users")
                .whereIn("uid", userIds.subList(0, Math.min(userIds.size(), 10))) // Firestore limit: 10
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> tokens = new ArrayList<>();
                    querySnapshot.forEach(doc -> {
                        String token = doc.getString("fcmToken");
                        if (token != null && !token.isEmpty()) {
                            tokens.add(token);
                        }
                    });

                    if (!tokens.isEmpty()) {
                        // Guardar notificación en Firestore para envío por Cloud Function
                        saveNotificationForSending(tokens, title, message, eventId, notificationType);
                    } else {
                        Log.w(TAG, "No se encontraron tokens FCM para los usuarios");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error obteniendo tokens de usuarios", e));
    }

    /**
     * Guarda la notificación en Firestore para que sea enviada por Cloud Functions
     * Nota: Requiere implementar Cloud Functions en Firebase
     */
    private void saveNotificationForSending(List<String> tokens, String title, String message,
                                             String eventId, String notificationType) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("tokens", tokens);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("eventId", eventId);
        notification.put("type", notificationType);
        notification.put("createdAt", Timestamp.now());
        notification.put("sent", false);

        db.collection("pending_notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notificación guardada para envío: " + documentReference.getId());
                    // Aquí una Cloud Function detectará este documento y enviará la notificación
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error guardando notificación", e));
    }

    /**
     * Obtiene el título según el tipo de cambio
     */
    private String getNotificationTitle(String changeType) {
        switch (changeType) {
            case "event_cancelled":
                return "⚠️ Evento Cancelado";
            case "event_rescheduled":
                return "📅 Evento Reprogramado";
            case "location_changed":
                return "📍 Cambio de Ubicación";
            case "event_confirmed":
                return "✅ Evento Confirmado";
            case "event_changed":
                return "📝 Cambio en Evento";
            default:
                return "🔔 Notificación GESDEP";
        }
    }

    /**
     * Obtiene el mensaje según el tipo de cambio
     */
    private String getNotificationMessage(String changeType, String eventName, String reason) {
        String baseMessage = "El evento \"" + eventName + "\" ";
        switch (changeType) {
            case "event_cancelled":
                return baseMessage + "ha sido cancelado.\nMotivo: " + reason;
            case "event_rescheduled":
                return baseMessage + "ha sido reprogramado.\nMotivo: " + reason;
            case "location_changed":
                return baseMessage + "cambió de ubicación.\nMotivo: " + reason;
            case "event_confirmed":
                return baseMessage + "está confirmado. ¡Nos vemos allá!";
            case "event_changed":
                return baseMessage + "tuvo cambios.\nMotivo: " + reason;
            default:
                return "Hubo una actualización en el evento \"" + eventName + "\".";
        }
    }
}
