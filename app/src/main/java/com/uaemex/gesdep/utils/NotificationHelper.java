package com.uaemex.gesdep.utils;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uaemex.gesdep.models.NotificationModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private final FirebaseFirestore db;

    public NotificationHelper() {
        this.db = FirebaseFirestore.getInstance("gesdep");
    }

    // --- MÉTODO QUE FALTABA (Solución del Error) ---
    public void sendNotificationToUser(String userId, String title, String message, String type) {
        String id = db.collection("notifications").document().getId();

        // Crear objeto compatible con tu NotificationModel
        NotificationModel notification = new NotificationModel();
        notification.setId(id);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTimestamp(Timestamp.now());
        notification.setRead(false);

        // Guardamos un campo extra 'targetUserId' para filtrar después si es necesario
        Map<String, Object> notifMap = new HashMap<>();
        notifMap.put("id", id);
        notifMap.put("title", title);
        notifMap.put("message", message);
        notifMap.put("type", type);
        notifMap.put("timestamp", Timestamp.now());
        notifMap.put("read", false);
        notifMap.put("targetUserId", userId); // Para saber a quién pertenece

        db.collection("notifications").document(id).set(notifMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notificación enviada a: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Error enviando notificación", e));
    }

    /**
     * Notifica a los participantes de un evento
     */
    public void notifyEventParticipants(String eventId, String title, String message) {
        db.collection("events").document(eventId).collection("registrations")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            sendNotificationToUser(userId, title, message, "Aviso");
                        }
                    }
                });
    }

    // --- Métodos de Negocio (Llamados desde la app) ---

    public void notifyEventCancelled(String eventId, String eventName, String reason) {
        notifyEventParticipants(eventId, "⚠️ Evento Cancelado",
                "El evento \"" + eventName + "\" ha sido cancelado.\nMotivo: " + reason);
    }

    public void notifyEventConfirmed(String eventId, String eventName) {
        notifyEventParticipants(eventId, "✅ Evento Confirmado",
                "El evento \"" + eventName + "\" ha sido confirmado. ¡Te esperamos!");
    }

    public void notifyEventRescheduled(String eventId, String eventName, String newDate) {
        notifyEventParticipants(eventId, "📅 Cambio de Fecha",
                "El evento \"" + eventName + "\" se movió al: " + newDate);
    }
}