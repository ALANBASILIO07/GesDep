package com.uaemex.gesdep.repositories;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRepository {

    private static final String TAG = "EventRepository";
    private static final String COLLECTION_EVENTS = "events";
    private static final String SUBCOLLECTION_REGISTRATIONS = "registrations";

    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance("gesdep");
        this.notificationHelper = new NotificationHelper();
    }

    /**
     * MODIFICADO: Obtiene TODOS los eventos (sin filtrar por status en el servidor)
     * Esto permite filtrar localmente por Activos, Cancelados, Finalizados, etc.
     */
    public void getAllEvents(OnEventsLoadedListener listener) {
        db.collection(COLLECTION_EVENTS)
                .orderBy("eventDateTime", Query.Direction.ASCENDING) // Orden cronológico
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
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ... (Mantén el resto de métodos: checkUserRegistration, registerUserToEvent, cancelEvent igual que antes) ...
    // Solo pego los necesarios para que compile, asegúrate de mantener los de inscripción que te di antes.

    public void checkUserRegistration(String eventId, String userId, OnCheckRegistrationListener listener) {
        db.collection(COLLECTION_EVENTS).document(eventId)
                .collection(SUBCOLLECTION_REGISTRATIONS).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> listener.onResult(documentSnapshot.exists()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void registerUserToEvent(String eventId, String userId, String userName, OnRegistrationResultListener listener) {
        final DocumentReference eventRef = db.collection(COLLECTION_EVENTS).document(eventId);
        final DocumentReference registrationRef = eventRef.collection(SUBCOLLECTION_REGISTRATIONS).document(userId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            DocumentSnapshot regSnapshot = transaction.get(registrationRef);

            if (!eventSnapshot.exists()) throw new FirebaseFirestoreException("El evento no existe", FirebaseFirestoreException.Code.NOT_FOUND);
            if (regSnapshot.exists()) throw new FirebaseFirestoreException("ALREADY_REGISTERED", FirebaseFirestoreException.Code.ABORTED);

            Long current = eventSnapshot.getLong("currentParticipants");
            Long max = eventSnapshot.getLong("maxQuota");
            String status = eventSnapshot.getString("status");

            if (current == null) current = 0L;
            if (max == null) max = 0L;

            if (current >= max) throw new FirebaseFirestoreException("EVENT_FULL", FirebaseFirestoreException.Code.ABORTED);
            if ("CANCELADO".equals(status) || "FINALIZADO".equals(status)) throw new FirebaseFirestoreException("EVENT_CLOSED", FirebaseFirestoreException.Code.ABORTED);

            Map<String, Object> regData = new HashMap<>();
            regData.put("userId", userId);
            regData.put("userName", userName);
            regData.put("registeredAt", new Date());
            regData.put("status", "ACTIVE");

            transaction.set(registrationRef, regData);
            transaction.update(eventRef, "currentParticipants", current + 1);
            return null;
        }).addOnSuccessListener(aVoid -> listener.onSuccess()).addOnFailureListener(e -> {
            if (e instanceof FirebaseFirestoreException) {
                String code = e.getMessage();
                if ("ALREADY_REGISTERED".equals(code)) listener.onAlreadyRegistered();
                else if ("EVENT_FULL".equals(code)) listener.onEventFull();
                else listener.onError(e.getMessage());
            } else {
                listener.onError(e.getMessage());
            }
        });
    }

    public void cancelEvent(String eventId, String eventName, String cancellationReason, String userId, String userName, OnEventCancelledListener listener) {
        db.collection(COLLECTION_EVENTS).document(eventId).update("status", "CANCELADO")
                .addOnSuccessListener(aVoid -> listener.onEventCancelled())
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // Interfaces
    public interface OnEventsLoadedListener { void onEventsLoaded(List<EventModel> events); void onError(String error); }
    public interface OnCheckRegistrationListener { void onResult(boolean isRegistered); void onError(String error); }
    public interface OnRegistrationResultListener { void onSuccess(); void onEventFull(); void onAlreadyRegistered(); void onError(String error); }
    public interface OnEventCancelledListener { void onEventCancelled(); void onError(String error); }
}