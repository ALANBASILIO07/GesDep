package com.uaemex.gesdep.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.uaemex.gesdep.HomeActivity;
import com.uaemex.gesdep.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Servicio de Firebase Cloud Messaging
 * Maneja la recepción de notificaciones push y actualiza el token FCM del usuario
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "gesdep_events_channel";
    private static final String CHANNEL_NAME = "Eventos GESDEP";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones de cambios en eventos deportivos y culturales";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Se llama cuando se recibe un nuevo token FCM
     * Actualiza el token en Firestore para el usuario actual
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);

        // Guardar el token en Firestore para el usuario actual
        saveTokenToFirestore(token);
    }

    /**
     * Se llama cuando se recibe una notificación push
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Verificar si el mensaje contiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Datos del mensaje: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Verificar si el mensaje contiene una notificación
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Título de notificación: " + title);
            Log.d(TAG, "Cuerpo de notificación: " + body);

            showNotification(title, body, remoteMessage.getData());
        }
    }

    /**
     * Maneja los mensajes de datos personalizados
     */
    private void handleDataMessage(Map<String, String> data) {
        String notificationType = data.get("type");
        String eventId = data.get("eventId");
        String eventName = data.get("eventName");
        String title = data.get("title");
        String message = data.get("message");

        // Mostrar notificación según el tipo
        if (title != null && message != null) {
            showNotification(title, message, data);
        }

        // Aquí puedes agregar lógica adicional según el tipo de notificación
        switch (notificationType != null ? notificationType : "") {
            case "event_changed":
                Log.d(TAG, "Evento modificado: " + eventName);
                // Actualizar datos locales si es necesario
                break;
            case "event_cancelled":
                Log.d(TAG, "Evento cancelado: " + eventName);
                break;
            case "event_rescheduled":
                Log.d(TAG, "Evento reprogramado: " + eventName);
                break;
            case "delay_request":
                Log.d(TAG, "Solicitud de retraso para: " + eventName);
                break;
            case "delay_approved":
                Log.d(TAG, "Retraso aprobado para: " + eventName);
                break;
            case "delay_rejected":
                Log.d(TAG, "Retraso rechazado para: " + eventName);
                break;
            case "event_reminder":
                Log.d(TAG, "Recordatorio de evento: " + eventName);
                break;
            default:
                Log.d(TAG, "Tipo de notificación desconocido: " + notificationType);
                break;
        }
    }

    /**
     * Muestra una notificación en la barra de estado
     */
    private void showNotification(String title, String message, Map<String, String> data) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para abrir la app al hacer clic en la notificación
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Agregar datos extra si existen
        if (data != null && data.containsKey("eventId")) {
            intent.putExtra("eventId", data.get("eventId"));
            intent.putExtra("notificationType", data.get("type"));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir la notificación
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher) // Usar icono de la app
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Mostrar la notificación
        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    /**
     * Crea el canal de notificaciones (requerido para Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Guarda el token FCM en Firestore para el usuario actual
     */
    private void saveTokenToFirestore(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Token FCM actualizado en Firestore"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error actualizando token FCM", e));
        } else {
            Log.w(TAG, "Usuario no autenticado, no se puede guardar el token");
        }
    }

    /**
     * Método estático para obtener el token actual y guardarlo
     * Llamar desde LoginActivity o HomeActivity
     */
    public static void registerFCMToken(Context context) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Error obteniendo token FCM", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "Token FCM obtenido: " + token);

                    // Guardar token en Firestore
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        String userId = auth.getCurrentUser().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("users").document(userId)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Token FCM registrado exitosamente"))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error registrando token FCM", e));
                    }
                });
    }
}
