package com.uaemex.gesdep.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.uaemex.gesdep.models.NotificationModel;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    /**
     * Save notification to Firestore
     */
    public static void saveNotification(FirebaseFirestore db, NotificationModel notification) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", notification.getTitle());
        notificationData.put("message", notification.getMessage());
        notificationData.put("type", notification.getType());
        notificationData.put("timestamp", notification.getTimestamp());
        notificationData.put("read", notification.isRead());
        notificationData.put("userId", notification.getUserId());
        notificationData.put("eventId", notification.getEventId());
        notificationData.put("reportId", notification.getReportId());
        notificationData.put("targetActivity", notification.getTargetActivity());

        db.collection("notifications").add(notificationData);
    }

    /**
     * Create event change notification
     */
    public static NotificationModel createEventChangeNotification(
            String eventId, String eventName, String changeDescription, String userId) {
        String title = "Cambios en el evento " + eventName;
        return new NotificationModel(title, changeDescription, eventId, userId);
    }

    /**
     * Create report notification for admin
     */
    public static NotificationModel createReportNotificationForAdmin(
            String eventId, String eventName, String reportId,
            String subject, String priority, String adminUid) {
        String title = "Nuevo Reporte: " + eventName;
        String message = subject + " - Prioridad: " + priority;
        return new NotificationModel(title, message, eventId, reportId, adminUid);
    }

    /**
     * Create report notification for participants
     */
    public static NotificationModel createReportNotificationForParticipants(
            String eventId, String eventName, String reportId,
            String subject, String participantUid) {
        String title = "Reporte en: " + eventName;
        return new NotificationModel(title, subject, eventId, reportId, participantUid);
    }

    /**
     * Create status change notification
     */
    public static NotificationModel createStatusChangeNotification(
            String eventId, String eventName, String reportId,
            String newStatus, String userId) {
        String title = "Actualización de Reporte: " + eventName;
        String message = "El estado cambió a: " + newStatus;
        return new NotificationModel(title, message, eventId, reportId, userId);
    }

    // Legacy methods for backwards compatibility

    /**
     * Send notification to a single user (legacy method)
     */
    public void sendNotificationToUser(String userId, String title, String message, String type) {
        FirebaseFirestore db = FirebaseFirestore.getInstance("gesdep");
        NotificationModel notification = new NotificationModel();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setTimestamp(com.google.firebase.Timestamp.now());
        saveNotification(db, notification);
    }

    /**
     * Notify all participants of an event (legacy method)
     */
    public void notifyEventParticipants(String eventId, String title, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance("gesdep");
        db.collection("eventParticipants")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> {
                        String participantUid = doc.getString("userId");
                        if (participantUid != null) {
                            NotificationModel notification = new NotificationModel(
                                    title, message, eventId, participantUid);
                            saveNotification(db, notification);
                        }
                    });
                });
    }
}
