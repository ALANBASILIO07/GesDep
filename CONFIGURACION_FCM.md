# 🔔 Configuración de Firebase Cloud Messaging (FCM)

## Guía completa para habilitar notificaciones push en GESDEP

---

## 📋 PREREQUISITOS

1. Proyecto de Firebase creado
2. App Android conectada a Firebase
3. Archivo `google-services.json` en la carpeta `app/`

---

## 🚀 PASOS DE CONFIGURACIÓN

### 1. **Habilitar Cloud Messaging en Firebase Console**

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto **GESDEP**
3. En el menú lateral, ve a **Compilación → Cloud Messaging**
4. Habilita la API de Cloud Messaging si está deshabilitada

### 2. **Verificar dependencias (YA IMPLEMENTADO)**

El archivo `app/build.gradle.kts` ya incluye:

```kotlin
implementation("com.google.firebase:firebase-messaging") // Notificaciones Push
```

### 3. **Verificar permisos (YA IMPLEMENTADO)**

El `AndroidManifest.xml` ya incluye:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 4. **Servicio FCM (YA IMPLEMENTADO)**

El servicio `MyFirebaseMessagingService.java` está configurado y registrado en el Manifest.

---

## 📱 FUNCIONAMIENTO DEL SISTEMA DE NOTIFICACIONES

### **Flujo de Notificaciones:**

```
1. Usuario inicia sesión
   ↓
2. LoginActivity registra token FCM
   ↓
3. Token se guarda en Firestore (users/{uid}/fcmToken)
   ↓
4. Admin modifica evento
   ↓
5. NotificationHelper obtiene tokens de participantes
   ↓
6. Notificación se guarda en pending_notifications
   ↓
7. Cloud Function detecta y envía FCM
   ↓
8. MyFirebaseMessagingService recibe notificación
   ↓
9. Usuario ve notificación
```

---

## 🔧 TIPOS DE NOTIFICACIONES IMPLEMENTADAS

### 1. **Cambio de Evento**
```java
NotificationHelper helper = new NotificationHelper();
helper.notifyEventChanged(eventId, eventName, "event_changed", reason, adminId);
```

### 2. **Cancelación de Evento**
```java
helper.notifyEventCancelled(eventId, eventName, cancellationReason);
```

### 3. **Reprogramación de Evento**
```java
helper.notifyEventRescheduled(eventId, eventName, newDateTime, reason);
```

### 4. **Cambio de Ubicación**
```java
helper.notifyLocationChanged(eventId, eventName, newLocation, reason);
```

### 5. **Recordatorio 24h antes**
```java
helper.sendEventReminder(eventId, eventName, dateTime, location);
```

### 6. **Solicitud de Retraso**
```java
helper.notifyDelayRequest(eventId, eventName, requesterId, requesterName,
                           rivalId, adminId, delayReason, delayMinutes);
```

### 7. **Retraso Aprobado**
```java
helper.notifyDelayApproved(eventId, eventName, requesterId, rivalId, delayMinutes);
```

### 8. **Retraso Rechazado**
```java
helper.notifyDelayRejected(eventId, eventName, requesterId, rejectedBy, rejectionReason);
```

### 9. **Evento Confirmado**
```java
helper.notifyEventConfirmed(eventId, eventName);
```

---

## 🔥 IMPLEMENTAR CLOUD FUNCTIONS (OPCIONAL PERO RECOMENDADO)

Para enviar notificaciones de manera eficiente, se recomienda usar Cloud Functions:

### **Crear Cloud Function:**

1. Instala Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Inicializa Functions:
```bash
firebase init functions
```

3. Crea función en `functions/index.js`:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendEventNotification = functions.firestore
    .document('pending_notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        const notification = snap.data();

        if (notification.sent) {
            return null; // Ya fue enviada
        }

        const message = {
            notification: {
                title: notification.title,
                body: notification.message,
            },
            data: {
                eventId: notification.eventId,
                type: notification.type,
            },
            tokens: notification.tokens, // Array de tokens FCM
        };

        try {
            const response = await admin.messaging().sendMulticast(message);
            console.log('Notificación enviada:', response.successCount, 'exitosas');

            // Marcar como enviada
            await snap.ref.update({
                sent: true,
                sentAt: admin.firestore.FieldValue.serverTimestamp(),
                successCount: response.successCount,
                failureCount: response.failureCount,
            });

            return response;
        } catch (error) {
            console.error('Error enviando notificación:', error);
            await snap.ref.update({
                status: 'failed',
                errorMessage: error.message,
            });
            return null;
        }
    });
```

4. Despliega:
```bash
firebase deploy --only functions
```

---

## 📊 ESTRUCTURA DE DATOS EN FIRESTORE

### **Colección: `users`**
```javascript
{
  uid: "user123",
  name: "Juan Pérez",
  email: "juan@example.com",
  role: "user",
  fcmToken: "dA3kF7... (token FCM)", // ← Token para notificaciones
  // ... otros campos
}
```

### **Colección: `pending_notifications`** (usada por Cloud Functions)
```javascript
{
  id: "notif123",
  eventId: "event456",
  eventName: "Torneo de Fútbol",
  title: "⚠️ Evento Cancelado",
  message: "El evento ha sido cancelado por mal clima.",
  type: "event_cancelled",
  tokens: ["token1", "token2", "token3"], // Tokens de destinatarios
  userIds: ["user1", "user2", "user3"],
  sent: false,
  createdAt: Timestamp,
  sentAt: null,
  status: "pending"
}
```

---

## 🧪 PROBAR NOTIFICACIONES

### **Método 1: Desde Firebase Console**

1. Ve a **Cloud Messaging → Nueva campaña**
2. Ingresa título y mensaje
3. Selecciona "Usuarios en segmentos" → Todos los usuarios
4. Enviar

### **Método 2: Desde código (Testing)**

En cualquier Activity, puedes probar manualmente:

```java
NotificationHelper helper = new NotificationHelper();
helper.notifyEventCancelled(
    "test_event_123",
    "Evento de Prueba",
    "Prueba de notificaciones push"
);
```

### **Método 3: Con Postman (API REST)**

Endpoint: `https://fcm.googleapis.com/fcm/send`

Headers:
```
Content-Type: application/json
Authorization: key=YOUR_SERVER_KEY
```

Body:
```json
{
  "to": "USER_FCM_TOKEN",
  "notification": {
    "title": "Prueba GESDEP",
    "body": "Notificación de prueba"
  },
  "data": {
    "eventId": "event123",
    "type": "test"
  }
}
```

**Server Key:** Lo obtienes en Firebase Console → Project Settings → Cloud Messaging → Server Key

---

## 🛡️ SOLICITAR PERMISO DE NOTIFICACIONES (Android 13+)

Para Android 13 (API 33) y superiores, debes solicitar permiso explícito:

### **Agregar en HomeActivity.java:**

```java
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

private static final int NOTIFICATION_PERMISSION_CODE = 101;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ... código existente

    // Solicitar permiso de notificaciones (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
        }
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == NOTIFICATION_PERMISSION_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notificaciones habilitadas", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Se necesita permiso para notificaciones", Toast.LENGTH_SHORT).show();
        }
    }
}
```

---

## 📝 EJEMPLO DE USO COMPLETO

### **Escenario: Admin cancela un evento**

```java
// En EditEventActivity.java (cuando admin cancela)

// 1. Actualizar estado del evento
EventModel event = obtenerEvento(eventId);
event.status = "cancelled";
event.cancellationReason = "Mal clima";
event.lastModified = Timestamp.now();

FirebaseFirestore db = FirebaseFirestore.getInstance();
db.collection("events").document(eventId)
    .set(event)
    .addOnSuccessListener(aVoid -> {
        // 2. Registrar cambio en changelog
        EventChangeLogModel changeLog = new EventChangeLogModel(
            db.collection("event_changelog").document().getId(),
            eventId,
            event.name,
            "cancelled",
            currentUserId,
            currentUserName,
            "status",
            "active",
            "cancelled",
            event.cancellationReason
        );

        db.collection("event_changelog").add(changeLog);

        // 3. Enviar notificaciones a todos los participantes
        NotificationHelper notificationHelper = new NotificationHelper();
        notificationHelper.notifyEventCancelled(
            eventId,
            event.name,
            event.cancellationReason
        );

        Toast.makeText(this, "Evento cancelado y participantes notificados",
                       Toast.LENGTH_LONG).show();
    });
```

---

## 🐛 TROUBLESHOOTING

### **Problema: No recibo notificaciones**

✅ Verificar:
1. Token FCM está guardado en Firestore
2. Permisos de notificaciones otorgados
3. App no está en modo "No molestar"
4. Firebase Cloud Messaging está habilitado en console
5. `google-services.json` está actualizado

### **Problema: Token FCM es null**

✅ Solución:
- Verifica que Google Play Services esté instalado en el dispositivo
- Ejecuta `MyFirebaseMessagingService.registerFCMToken(context)` manualmente

### **Problema: Notificaciones no se muestran en primer plano**

✅ Solución:
- `MyFirebaseMessagingService.onMessageReceived()` debe llamar a `showNotification()`
- Canal de notificaciones debe estar creado (Android 8.0+)

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [x] Dependencias agregadas (`firebase-messaging`)
- [x] Permisos en `AndroidManifest.xml`
- [x] Servicio `MyFirebaseMessagingService` creado
- [x] Servicio registrado en `AndroidManifest.xml`
- [x] Canal de notificaciones creado
- [x] `NotificationHelper` implementado
- [x] Token FCM se registra en login
- [ ] Cloud Functions desplegadas (opcional)
- [ ] Solicitud de permiso en Android 13+ (pendiente)
- [ ] Pruebas de notificaciones realizadas

---

## 📚 RECURSOS ADICIONALES

- [Documentación oficial FCM](https://firebase.google.com/docs/cloud-messaging)
- [Guía de notificaciones Android](https://developer.android.com/develop/ui/views/notifications)
- [Cloud Functions para FCM](https://firebase.google.com/docs/functions/use-cases#cloud-messaging)

---

**Última actualización:** 26 de Noviembre, 2025
**Versión:** 1.0 (Fase 3)
