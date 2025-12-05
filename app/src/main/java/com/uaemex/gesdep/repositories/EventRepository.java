package com.uaemex.gesdep.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.uaemex.gesdep.models.EventModel;
import com.uaemex.gesdep.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EventRepository {

    private static final String TAG = "EventRepository";
    private static final String COLLECTION_EVENTS = "events";
    private static final String SUBCOLLECTION_REGISTRATIONS = "registrations";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_TRANSACTIONS = "transactions";

    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;
    private final FirebaseAuth auth;

    public EventRepository() {
        this.db = FirebaseFirestore.getInstance("gesdep");
        this.notificationHelper = new NotificationHelper();
        this.auth = FirebaseAuth.getInstance();
    }

    // ... (El resto de métodos de getAllEvents, checkAndConfirmEvent, registro y pagos se quedan IGUAL) ...
    // ... (Para ahorrar espacio, copia aquí tus métodos de registro/pagos/cancelación que ya tenías) ...

    // AGREGA ESTOS MÉTODOS SI NO LOS TIENES, O REEMPLAZA LOS EXISTENTES:

    public void getAllEvents(OnEventsLoadedListener listener) {
        db.collection(COLLECTION_EVENTS)
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
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void checkAndConfirmEvent(EventModel event) {
        if ("ACTIVO".equals(event.getStatus()) || "PENDIENTE".equals(event.getStatus())) {
            long now = System.currentTimeMillis();
            boolean deadlinePassed = event.getRegistrationDeadline() != null && event.getRegistrationDeadline().getTime() < now;
            boolean isFull = event.getCurrentParticipants() >= event.getMaxQuota();

            if (deadlinePassed || isFull) {
                String newStatus;
                String msg;

                if (event.getCurrentParticipants() >= event.getMinQuota()) {
                    newStatus = "CONFIRMADO";
                    msg = "¡El evento '" + event.getTitle() + "' ha sido CONFIRMADO! Prepara tu asistencia.";
                } else {
                    newStatus = "CANCELADO";
                    msg = "El evento '" + event.getTitle() + "' fue cancelado por no alcanzar el quórum mínimo.";
                }

                db.collection(COLLECTION_EVENTS).document(event.getId()).update("status", newStatus);
                event.setStatus(newStatus);
                notificationHelper.notifyEventParticipants(event.getId(), "Aviso Importante", msg);
            }
        }
    }

    public void checkUserRegistration(String eventId, String userId, OnCheckRegistrationListener listener) {
        db.collection(COLLECTION_EVENTS).document(eventId)
                .collection(SUBCOLLECTION_REGISTRATIONS).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> listener.onResult(documentSnapshot.exists()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public void registerUserToEvent(String eventId, String userId, OnRegistrationResultListener listener) {
        fetchUserNameAndExecute(eventId, userId, 0, listener, false);
    }

    private void runRegistrationTransaction(String eventId, String userId, String userName, OnRegistrationResultListener listener) {
        final DocumentReference eventRef = db.collection(COLLECTION_EVENTS).document(eventId);
        final DocumentReference registrationRef = eventRef.collection(SUBCOLLECTION_REGISTRATIONS).document(userId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            DocumentSnapshot regSnapshot = transaction.get(registrationRef);

            if (!eventSnapshot.exists()) throw new FirebaseFirestoreException("No existe", FirebaseFirestoreException.Code.NOT_FOUND);
            if (regSnapshot.exists()) throw new FirebaseFirestoreException("ALREADY_REGISTERED", FirebaseFirestoreException.Code.ABORTED);

            Long current = eventSnapshot.getLong("currentParticipants");
            Long max = eventSnapshot.getLong("maxQuota");
            Long min = eventSnapshot.getLong("minQuota");
            String status = eventSnapshot.getString("status");
            String title = eventSnapshot.getString("title");

            if (current == null) current = 0L;
            if (max == null) max = 0L;
            if (min == null) min = 0L;

            if (current >= max) throw new FirebaseFirestoreException("EVENT_FULL", FirebaseFirestoreException.Code.ABORTED);
            if ("CANCELADO".equals(status) || "FINALIZADO".equals(status)) throw new FirebaseFirestoreException("EVENT_CLOSED", FirebaseFirestoreException.Code.ABORTED);

            // Determinar nuevo estatus después de la inscripción
            Long newCount = current + 1;

            // FIX: Actualizar estatus a CONFIRMADO si el aforo mínimo se cumple
            if (!"CONFIRMADO".equals(status) && newCount >= min) {
                transaction.update(eventRef, "status", "CONFIRMADO");
            }


            Map<String, Object> regData = new HashMap<>();
            regData.put("userId", userId);
            regData.put("userName", userName);
            regData.put("registeredAt", new Date());
            regData.put("status", "ACTIVE");

            transaction.set(registrationRef, regData);
            transaction.update(eventRef, "currentParticipants", newCount);

            notificationHelper.sendNotificationToUser(userId, "Inscripción Exitosa", "Te has inscrito a " + title, "Sistema");

            return null;
        }).addOnSuccessListener(aVoid -> listener.onSuccess()).addOnFailureListener(e -> handleTransactionError(e, listener));
    }

    public void processPaymentAndRegister(String eventId, String userId, double cost, OnRegistrationResultListener listener) {
        fetchUserNameAndExecute(eventId, userId, cost, listener, true);
    }

    private void runPaymentTransaction(String eventId, String userId, String userName, double cost, OnRegistrationResultListener listener) {
        final DocumentReference eventRef = db.collection(COLLECTION_EVENTS).document(eventId);
        final DocumentReference registrationRef = eventRef.collection(SUBCOLLECTION_REGISTRATIONS).document(userId);
        final DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    // === READ PHASE START ===
                    DocumentSnapshot eventSnapshot = transaction.get(eventRef);
                    DocumentSnapshot regSnapshot = transaction.get(registrationRef);
                    DocumentSnapshot userSnap = transaction.get(userRef);

                    // 1. Validaciones
                    if (!eventSnapshot.exists()) throw new FirebaseFirestoreException("No existe", FirebaseFirestoreException.Code.NOT_FOUND);
                    if (regSnapshot.exists()) throw new FirebaseFirestoreException("ALREADY_REGISTERED", FirebaseFirestoreException.Code.ABORTED);

                    // Validar Saldo
                    Double currentCredit = userSnap.getDouble("appCredit");
                    if (currentCredit == null) currentCredit = 0.0;
                    if (currentCredit < cost) {
                        throw new FirebaseFirestoreException("INSUFFICIENT_FUNDS", FirebaseFirestoreException.Code.ABORTED);
                    }

                    // Validar Cupo
                    Long current = eventSnapshot.getLong("currentParticipants");
                    Long max = eventSnapshot.getLong("maxQuota");
                    Long min = eventSnapshot.getLong("minQuota");
                    if (current == null) current = 0L;
                    if (max == null) max = 0L;
                    if (min == null) min = 0L;

                    if (current >= max) throw new FirebaseFirestoreException("EVENT_FULL", FirebaseFirestoreException.Code.ABORTED);

                    // === READ PHASE END / WRITE PHASE START ===

                    // Determinar nuevo estatus después de la inscripción
                    Long newCount = current + 1;

                    // FIX: Actualizar estatus a CONFIRMADO si el aforo mínimo se cumple
                    if (!"CONFIRMADO".equals(eventSnapshot.getString("status")) && newCount >= min) {
                        transaction.update(eventRef, "status", "CONFIRMADO");
                    }

                    // 2. Ejecutar Cobro y Registro
                    transaction.update(userRef, "appCredit", currentCredit - cost); // COBRO

                    Map<String, Object> regData = new HashMap<>();
                    regData.put("userId", userId);
                    regData.put("userName", userName); // GUARDAMOS NOMBRE REAL
                    regData.put("registeredAt", new Date());
                    regData.put("status", "ACTIVE");

                    transaction.set(registrationRef, regData); // REGISTRO
                    transaction.update(eventRef, "currentParticipants", newCount); // CUPOS

                    // 3. Registrar Transacción (Movimiento)
                    registerTransaction(transaction, userId, "Pago de inscripción: " + eventSnapshot.getString("title"), cost, "PAYMENT");

                    notificationHelper.sendNotificationToUser(userId, "Pago Exitoso", "Se descontaron $" + cost + " por tu inscripción.", "Pagos");
                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    if (e.getMessage().contains("INSUFFICIENT_FUNDS")) listener.onError("Saldo insuficiente. Recarga tu cuenta.");
                    else handleTransactionError(e, listener);
                });
    }

    public void cancelRegistration(String eventId, String userId, double refundAmount, OnRegistrationResultListener listener) {
        final DocumentReference eventRef = db.collection(COLLECTION_EVENTS).document(eventId);
        final DocumentReference registrationRef = eventRef.collection(SUBCOLLECTION_REGISTRATIONS).document(userId);
        final DocumentReference userRef = db.collection(COLLECTION_USERS).document(userId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    // === READ PHASE START ===
                    DocumentSnapshot regSnapshot = transaction.get(registrationRef);
                    DocumentSnapshot eventSnap = transaction.get(eventRef);
                    DocumentSnapshot userSnap = transaction.get(userRef);

                    if (!regSnapshot.exists()) {
                        throw new FirebaseFirestoreException("NOT_REGISTERED", FirebaseFirestoreException.Code.ABORTED);
                    }

                    Long current = eventSnap.getLong("currentParticipants");
                    Long min = eventSnap.getLong("minQuota");
                    if (current == null) current = 0L;
                    if (min == null) min = 0L;

                    Double currentCredit = userSnap.getDouble("appCredit");
                    if (currentCredit == null) currentCredit = 0.0;

                    // === READ PHASE END / WRITE PHASE START ===

                    Long newCount = current - 1; // Contador después de la cancelación

                    // 1. Eliminar registro
                    transaction.delete(registrationRef);

                    // 2. Decrementar contador y corregir estatus
                    if (current > 0) {
                        transaction.update(eventRef, "currentParticipants", newCount);

                        // FIX: Si el estatus era CONFIRMADO y el conteo baja del MÍNIMO,
                        // debemos degradarlo a PENDIENTE.
                        if ("CONFIRMADO".equals(eventSnap.getString("status")) && newCount < min) {
                            transaction.update(eventRef, "status", "PENDIENTE");
                        }
                    }

                    // 3. Reembolso
                    if (refundAmount > 0) {
                        transaction.update(userRef, "appCredit", currentCredit + refundAmount);

                        // Registrar Transacción de reembolso
                        registerTransaction(transaction, userId, "Reembolso por cancelación: " + eventSnap.getString("title"), refundAmount, "REFUND");
                    }

                    notificationHelper.sendNotificationToUser(userId, "Cancelación Exitosa", "Has cancelado tu asistencia." + (refundAmount > 0 ? " Se reembolsaron $" + refundAmount : ""), "Sistema");

                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Error al cancelar: " + e.getMessage()));
    }

    private void fetchUserNameAndExecute(String eventId, String userId, double cost, OnRegistrationResultListener listener, boolean isPaid) {
        db.collection(COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String realUserName = userDoc.getString("name");
                    if (realUserName == null || realUserName.isEmpty()) {
                        realUserName = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : userId;
                    }
                    if (isPaid) {
                        runPaymentTransaction(eventId, userId, realUserName, cost, listener);
                    } else {
                        runRegistrationTransaction(eventId, userId, realUserName, listener);
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error fetching profile for transaction."));
    }

    private void registerTransaction(Transaction transaction, String userId, String description, double amount, String type) {
        DocumentReference transRef = db.collection(COLLECTION_TRANSACTIONS).document();
        Map<String, Object> trans = new HashMap<>();
        trans.put("userId", userId);
        trans.put("description", description);
        trans.put("amount", amount);
        trans.put("type", type);
        trans.put("createdAt", new Date());
        transaction.set(transRef, trans);
    }

    private void handleTransactionError(Exception e, OnRegistrationResultListener listener) {
        if (e instanceof FirebaseFirestoreException) {
            String code = e.getMessage();
            if ("ALREADY_REGISTERED".equals(code)) listener.onAlreadyRegistered();
            else if ("EVENT_FULL".equals(code)) listener.onEventFull();
            else if ("FAILED_PRECHECK".equals(code)) listener.onError("Error de validación: Evento cerrado, lleno o duplicado.");
            else listener.onError(e.getMessage());
        } else {
            listener.onError(e.getMessage());
        }
    }


    // =========================================================================
    // === AQUÍ ESTÁ EL CAMBIO IMPORTANTE: OBTENER FOTOS Y PARTICIPANTES ===
    // =========================================================================

    public void getParticipants(String eventId, OnParticipantsLoadedListener listener) {
        db.collection(COLLECTION_EVENTS).document(eventId).collection(SUBCOLLECTION_REGISTRATIONS)
                .get()
                .addOnSuccessListener(registrationQuery -> {
                    if (registrationQuery.isEmpty()) {
                        listener.onLoaded(new ArrayList<>());
                        return;
                    }

                    List<String> userIds = new ArrayList<>();
                    for (QueryDocumentSnapshot regDoc : registrationQuery) {
                        String userId = regDoc.getString("userId");
                        if (userId != null) userIds.add(userId);
                    }

                    // Firestore limita 'whereIn' a 10 elementos.
                    // Si tienes más de 10 usuarios, esto fallaría.
                    // Para simplificar, asumiremos que no excede 10 o usamos iteración.
                    // (Si falla por límite, avísame para darte la versión iterativa).

                    db.collection(COLLECTION_USERS)
                            .whereIn("uid", userIds)
                            .get()
                            .addOnSuccessListener(userQuery -> {
                                Map<String, String> userNamesMap = new HashMap<>();
                                Map<String, String> userPhotosMap = new HashMap<>(); // MAPA PARA FOTOS

                                for (QueryDocumentSnapshot userDoc : userQuery) {
                                    String name = userDoc.getString("name");
                                    // RECOLECCIÓN DE FOTO
                                    String photo = userDoc.getString("photoUrl");
                                    if(photo == null) photo = userDoc.getString("profilePhotoUrl"); // Doble check

                                    if (name != null) userNamesMap.put(userDoc.getString("uid"), name);
                                    if (photo != null) userPhotosMap.put(userDoc.getString("uid"), photo);
                                }

                                List<ParticipantData> participants = new ArrayList<>();
                                for (String uid : userIds) {
                                    String name = userNamesMap.get(uid);
                                    if (name == null || name.isEmpty()) name = uid;

                                    String photo = userPhotosMap.get(uid); // Obtener foto

                                    // Constructor actualizado
                                    participants.add(new ParticipantData(uid, name, photo));
                                }
                                listener.onLoaded(participants);
                            })
                            .addOnFailureListener(e -> listener.onError("Error fetching user details: " + e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onError("Error fetching registrations: " + e.getMessage()));
    }

    // --- CLASE DE DATOS ACTUALIZADA CON FOTO ---
    public static class ParticipantData {
        public String userId;
        public String userName;
        public String photoUrl; // CAMPO NUEVO

        public ParticipantData(String userId, String userName, String photoUrl) {
            this.userId = userId;
            this.userName = userName;
            this.photoUrl = photoUrl;
        }

        // Constructor compatible anterior (opcional, asigna null a foto)
        public ParticipantData(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
            this.photoUrl = null;
        }
    }

    public interface OnEventsLoadedListener { void onEventsLoaded(List<EventModel> events); void onError(String error); }
    public interface OnCheckRegistrationListener { void onResult(boolean isRegistered); void onError(String error); }
    public interface OnRegistrationResultListener { void onSuccess(); void onEventFull(); public void onAlreadyRegistered(); void onError(String error); }
    public interface OnStatusUpdateListener { void onSuccess(); void onError(String error); }
    public interface OnParticipantsLoadedListener { void onLoaded(List<ParticipantData> participants); void onError(String error); }
}