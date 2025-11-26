package com.uaemex.gesdep.repositories;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uaemex.gesdep.models.EventChangeLogModel;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio para gestionar operaciones CRUD de eventos
 * Incluye validaciones de negocio y notificaciones
 */
public class EventRepository {

    private static final String TAG = "EventRepository";
    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_CHANGELOG = "event_changelog";

    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.notificationHelper = new NotificationHelper();
    }

    /**
     * Obtiene todos los eventos activos ordenados por fecha
     */
    public void getAllActiveEvents(OnEventsLoadedListener listener) {
        db.collection(COLLECTION_EVENTS)
                .whereIn("status", List.of("active", "confirmed"))
                .orderBy("eventDateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventModel> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event != null) {
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
     * Obtiene eventos por tipo (deportivo/cultural)
     */
    public void getEventsByType(String type, OnEventsLoadedListener listener) {
        db.collection(COLLECTION_EVENTS)
                .whereEqualTo("type", type)
                .whereIn("status", List.of("active", "confirmed"))
                .orderBy("eventDateTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EventModel> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        EventModel event = doc.toObject(EventModel.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando eventos por tipo", e);
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
        // Validar que la fecha del evento sea futura
        if (event.eventDateTime.toDate().getTime() < System.currentTimeMillis()) {
            listener.onError("La fecha del evento debe ser futura");
            return;
        }

        // Validar capacidades
        if (event.maxParticipants < event.minParticipants) {
            listener.onError("La capacidad máxima debe ser mayor o igual a la mínima");
            return;
        }

        // Calcular tiempo mínimo de cambio según distancia
        event.calculateMinimumChangeTime();

        // Generar ID
        DocumentReference docRef = db.collection(COLLECTION_EVENTS).document();
        event.id = docRef.getId();
        event.organizerId = userId;
        event.organizerName = userName;
        event.createdAt = Timestamp.now();
        event.lastModified = Timestamp.now();

        // Guardar evento
        docRef.set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Evento creado: " + event.id);

                    // Registrar en changelog
                    logChange(event.id, event.name, "created", userId, userName,
                            "status", null, "active", "Evento creado");

                    listener.onEventSaved(event);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando evento", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Actualiza un evento existente
     */
    public void updateEvent(EventModel event, String userId, String userName,
                            String changeReason, OnEventSavedListener listener) {
        // Validar que se pueda modificar
        if (!event.canBeModified()) {
            long minutesUntilEvent = event.getMinutesUntilEvent();
            listener.onError("No se puede modificar el evento. " +
                    "Requiere al menos " + event.minimumMinutesBeforeChange +
                    " minutos de anticipación. Faltan " + minutesUntilEvent + " minutos.");
            return;
        }

        event.lastModified = Timestamp.now();

        db.collection(COLLECTION_EVENTS)
                .document(event.id)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Evento actualizado: " + event.id);

                    // Registrar cambio
                    logChange(event.id, event.name, "modified", userId, userName,
                            "general", "old_value", "new_value", changeReason);

                    // Notificar a participantes
                    notificationHelper.notifyEventChanged(event.id, event.name,
                            "event_changed", changeReason, userId);

                    listener.onEventSaved(event);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando evento", e);
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
                .update("status", "cancelled",
                        "cancellationReason", cancellationReason,
                        "lastModified", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Evento cancelado: " + eventId);

                    // Registrar cambio
                    logChange(eventId, eventName, "cancelled", userId, userName,
                            "status", "active", "cancelled", cancellationReason);

                    // Notificar a todos los participantes
                    notificationHelper.notifyEventCancelled(eventId, eventName, cancellationReason);

                    listener.onEventCancelled();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cancelando evento", e);
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Registra un cambio en el changelog
     */
    private void logChange(String eventId, String eventName, String changeType,
                           String userId, String userName, String fieldChanged,
                           String oldValue, String newValue, String reason) {
        EventChangeLogModel changeLog = new EventChangeLogModel(
                db.collection(COLLECTION_CHANGELOG).document().getId(),
                eventId,
                eventName,
                changeType,
                userId,
                userName,
                fieldChanged,
                oldValue,
                newValue,
                reason
        );

        db.collection(COLLECTION_CHANGELOG)
                .add(changeLog)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Changelog registrado: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error registrando changelog", e));
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
