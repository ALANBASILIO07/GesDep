package com.uaemex.gesdep.repositories;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.models.EventChangeLogModel; // Asegúrate de tener este modelo o coméntalo si no
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Repositorio para gestionar operaciones CRUD de eventos
 * Actualizado para usar Getters/Setters del nuevo EventModel
 */
public class EventRepository {

    private static final String TAG = "EventRepository";
    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_CHANGELOG = "event_changelog";

    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;

    public EventRepository() {
        // Usar la instancia correcta "gesdep"
        this.db = FirebaseFirestore.getInstance("gesdep");
        this.notificationHelper = new NotificationHelper();
    }

    /**
     * Obtiene todos los eventos activos ordenados por fecha
     */
    public void getAllActiveEvents(OnEventsLoadedListener listener) {
        // Arrays.asList es mejor para versiones antiguas de Java que List.of
        db.collection(COLLECTION_EVENTS)
                .whereIn("status", Arrays.asList("ACTIVO", "CONFIRMADO"))
                .orderBy("eventDateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventModel> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event != null) {
                            event.setId(doc.getId()); // Asegurar ID
                            events.add(event);
                        }
                    }
                    Log.d(TAG, "Eventos cargados: " + events.size());
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando eventos", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Obtiene eventos por categoría (Deportivo, Cultural, etc.)
     */
    public void getEventsByType(String category, OnEventsLoadedListener listener) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("category", category)
                .whereIn("status", Arrays.asList("ACTIVO", "CONFIRMADO"))
                .orderBy("eventDateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventModel> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            events.add(event);
                        }
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando eventos por categoría", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Obtiene un evento por ID
     */
    public void getEventById(String eventId, OnEventLoadedListener listener) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        EventModel event = documentSnapshot.toObject(EventModel.class);
                        if (event != null) event.setId(documentSnapshot.getId());
                        listener.onEventLoaded(event);
                    } else {
                        listener.onError("Evento no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando evento", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Crea un nuevo evento
     */
    public void createEvent(EventModel event, String userId, String userName, OnEventSavedListener listener) {
        // Validar fecha futura (Usando GETTER)
        if (event.getEventDateTime() == null || event.getEventDateTime().toDate().getTime() < System.currentTimeMillis()) {
            listener.onError("La fecha del evento debe ser futura");
            return;
        }

        // Validar capacidades (Usando GETTERS)
        if (event.getMaxQuota() < event.getMinQuota()) {
            listener.onError("La capacidad máxima debe ser mayor o igual a la mínima");
            return;
        }

        // Generar ID
        DocumentReference docRef = db.collection(COLLECTION_EVENTS).document();
        event.setId(docRef.getId()); // Usando SETTER
        event.setOrganizerId(userId);
        event.setOrganizerName(userName);
        event.setCreatedAt(Timestamp.now());

        // Guardar evento
        docRef.set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Evento creado: " + event.getId());
                    // Opcional: Registrar en changelog si lo implementas
                    listener.onEventSaved(event);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando evento", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Cancela un evento
     */
    public void cancelEvent(String eventId, String eventName, String cancellationReason,
                            String userId, String userName, OnEventCancelledListener listener) {
        db.collection(COLLECTION_EVENTS)
                .document(eventId)
                .update("status", "CANCELADO") // Solo actualizamos estado por ahora
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Evento cancelado: " + eventId);
                    // Notificar participantes...
                    listener.onEventCancelled();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cancelando evento", e);
                    listener.onError(e.getMessage());
                });
    }

    // Interfaces para callbacks
    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<EventModel> events);
        void onError(String error);
    }

    public interface OnEventLoadedListener {
        void onEventLoaded(EventModel event);
        void onError(String error);
    }

    public interface OnEventSavedListener {
        void onEventSaved(EventModel event);
        void onError(String error);
    }

    public interface OnEventCancelledListener {
        void onEventCancelled();
        void onError(String error);
    }
}