# 📊 GESDEP - FASES DE IMPLEMENTACIÓN

## Sistema de Gestión de Eventos Deportivos y Culturales
**Cliente:** IMCUFIDE (Instituto Municipal de Cultura Física y Deporte)

---

## ✅ FASE 1: PREPARACIÓN Y PERMISOS - **COMPLETADA**

### Permisos agregados al AndroidManifest.xml:
- ✅ `ACCESS_FINE_LOCATION` - GPS preciso
- ✅ `ACCESS_COARSE_LOCATION` - GPS aproximado
- ✅ `INTERNET` - Conexión a internet
- ✅ `ACCESS_NETWORK_STATE` - Estado de la red
- ✅ `CAMERA` - Cámara
- ✅ `RECORD_AUDIO` - Audio (para videos)
- ✅ `READ_EXTERNAL_STORAGE` - Lectura de archivos
- ✅ `WRITE_EXTERNAL_STORAGE` - Escritura de archivos
- ✅ `READ_MEDIA_IMAGES` - Lectura de imágenes (Android 13+)
- ✅ `READ_MEDIA_VIDEO` - Lectura de videos (Android 13+)
- ✅ `POST_NOTIFICATIONS` - Notificaciones push (Android 13+)

### Dependencias agregadas al build.gradle.kts:
- ✅ **Firebase Cloud Messaging** - Notificaciones push
- ✅ **Firebase Analytics** - Analítica
- ✅ **WorkManager** (2.9.0) - Sincronización en background
- ✅ **Glide** (4.16.0) - Carga optimizada de imágenes
- ✅ **CameraX** (1.3.1) - Funcionalidad de cámara moderna
- ✅ **Navigation Component** (2.7.7) - Navegación con fragments
- ✅ **Lifecycle Components** (2.7.0) - ViewModel y LiveData

---

## ✅ FASE 2: MODELOS DE DATOS - **COMPLETADA**

### 1. **EventModel.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/models/EventModel.java`

**Características implementadas:**
- ✅ Identificación y descripción completa
- ✅ Tipos: "deportivo" o "cultural"
- ✅ Categorías: futbol, basquetbol, atletismo, danza, teatro, etc.
- ✅ Ubicación con GPS (latitud/longitud)
- ✅ **Validación de tiempo mínimo según distancia:**
  - Distancia corta (<15 min): 30 minutos de anticipación
  - Distancia media (15-30 min): 60 minutos de anticipación
  - Distancia larga (>30 min): 120 minutos de anticipación
- ✅ Registro individual o por equipos
- ✅ Capacidad mínima/máxima configurable
- ✅ **Sistema de confirmación:** evento se confirma al alcanzar mínimo de participantes (default: 2)
- ✅ Estados: active, confirmed, cancelled, rescheduled, completed
- ✅ Historial de cambios
- ✅ Multimedia (photos/videos)
- ✅ Métodos de validación: `canBeModified()`, `isFull()`, `updateConfirmationStatus()`

### 2. **TeamModel.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/models/TeamModel.java`

**Características implementadas:**
- ✅ Información del equipo y evento asociado
- ✅ **Responsable de equipo (Team Leader):**
  - ID, nombre, email, teléfono
- ✅ Lista de miembros del equipo (TeamMember)
- ✅ Capacidad mínima/máxima de miembros
- ✅ Estado del registro: confirmed, pending, cancelled
- ✅ Información adicional: uniforme, institución
- ✅ Métodos: `addMember()`, `removeMember()`, `hasMinimumMembers()`, `isFull()`

### 3. **EventRegistrationModel.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/models/EventRegistrationModel.java`

**Características implementadas:**
- ✅ **Confirmación automática:** Al registrarse, el usuario confirma automáticamente su asistencia
- ✅ Registro individual o por equipo
- ✅ **Sistema de solicitud de retraso:**
  - Usuario puede solicitar retraso con razón
  - **Requiere aprobación del rival/oponente**
  - **Requiere aprobación del administrador/organizador**
  - Estados: pending, approved_by_rival, approved_by_admin, rejected
- ✅ Check-in con GPS (asistencia verificada)
- ✅ Contacto de emergencia
- ✅ Métodos: `requestDelay()`, `approveDelayByRival()`, `approveDelayByAdmin()`, `rejectDelay()`, `checkIn()`

### 4. **UserModel.java** ✅ (ACTUALIZADO)
**Ubicación:** `app/src/main/java/com/example/gesdep/UserModel.java`

**Nuevos campos agregados:**
- ✅ `userType` - "admin", "user", "team_leader"
- ✅ `phone` - Teléfono de contacto
- ✅ `profilePhotoUrl` - URL de foto de perfil
- ✅ `institution` - Institución que representa
- ✅ `createdAt` / `lastLogin` - Timestamps
- ✅ `fcmToken` - Token para notificaciones push
- ✅ Estadísticas:
  - `eventsRegistered` - Eventos registrados
  - `eventsCompleted` - Eventos completados
  - `teamsLeading` - Equipos que lidera
- ✅ Métodos: `isAdmin()`, `isTeamLeader()`, `incrementEventsRegistered()`, etc.

### 5. **EventChangeLogModel.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/models/EventChangeLogModel.java`

**Características implementadas:**
- ✅ Auditoría completa de cambios
- ✅ Tipos de cambio: created, modified, cancelled, rescheduled, location_changed
- ✅ Registro de quién hizo el cambio y cuándo
- ✅ Valores antiguos vs nuevos
- ✅ Razón del cambio
- ✅ Seguimiento de notificaciones enviadas

---

## 📋 CONFIGURACIÓN DE EVENTOS

### **EventConfig.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/utils/EventConfig.java`

**Eventos Deportivos - Equipos:**
- ✅ **Fútbol:** 2-16 equipos, 5-11 jugadores, 90 min
- ✅ **Basquetbol:** 2-8 equipos, 5-12 jugadores, 60 min
- ✅ **Voleibol:** 2-8 equipos, 6-12 jugadores, 60 min

**Eventos Deportivos - Individuales:**
- ✅ **Atletismo:** 2-20 participantes, 30 min
- ✅ **Salto de longitud:** 2-15 participantes, 45 min
- ✅ **Ajedrez:** 2-32 participantes, 90 min
- ✅ **Natación:** 2-8 participantes, 30 min
- ✅ **Ciclismo:** 2-50 participantes, 120 min

**Eventos Culturales - Individuales:**
- ✅ **Danza Individual:** 2-30 participantes, 60 min
- ✅ **Teatro Individual:** 2-20 participantes, 90 min
- ✅ **Música Solista:** 2-25 participantes, 90 min
- ✅ **Arte/Pintura:** 2-40 participantes, 120 min

**Eventos Culturales - Grupos:**
- ✅ **Danza Grupal:** 2-15 grupos, 4-20 integrantes, 90 min
- ✅ **Teatro Grupal:** 2-10 grupos, 3-15 actores, 120 min
- ✅ **Música - Bandas:** 2-12 bandas, 3-10 integrantes, 120 min

**Eventos Especiales:**
- ✅ **Semana Deportiva:** 4-20 equipos, 10-30 miembros, multi-día
- ✅ **Festival Cultural:** 5-30 grupos, 3-25 integrantes, 6 horas

**Métodos disponibles:**
- `getConfig(category)` - Obtener configuración específica
- `getAllConfigs()` - Todas las categorías
- `getSportsConfigs()` - Solo deportivos
- `getCulturalConfigs()` - Solo culturales
- `getIndividualConfigs()` - Solo individuales
- `getTeamConfigs()` - Solo equipos

---

## 📐 ESTRUCTURA DE LA BASE DE DATOS FIRESTORE

### Colecciones principales:

```
firestore/
├── users/{userId}
│   ├── uid, name, email, role, userType
│   ├── phone, profilePhotoUrl, institution
│   ├── fcmToken (para notificaciones)
│   └── estadísticas (eventsRegistered, eventsCompleted, teamsLeading)
│
├── events/{eventId}
│   ├── Información básica (id, name, description, type, category)
│   ├── Ubicación (placeName, latitude, longitude, distanceFromCenterMinutes)
│   ├── Fecha/hora (eventDateTime, registrationDeadline, durationMinutes)
│   ├── Registro (registrationType, min/maxParticipants, currentParticipants)
│   ├── Estado (status, isConfirmed, cancellationReason)
│   ├── Validación (minimumMinutesBeforeChange)
│   ├── Organizador (organizerId, organizerName, organizerEmail)
│   └── Multimedia (photoUrls[], videoUrls[], thumbnailUrl)
│
├── registrations/{registrationId}
│   ├── eventId, userId, userName
│   ├── registrationType ("individual" o "team")
│   ├── teamId, teamName (si aplica)
│   ├── status (confirmed, cancelled, delayed, completed)
│   ├── Sistema de retraso:
│   │   ├── hasDelayRequest, delayReason, delayMinutes
│   │   ├── rivalApproved, adminApproved
│   │   └── delayStatus
│   └── Check-in (attended, checkInTime, GPS coords)
│
├── teams/{teamId}
│   ├── teamName, eventId, eventName
│   ├── Team Leader (leaderId, leaderName, leaderEmail, leaderPhone)
│   ├── members[] (array de TeamMember)
│   ├── min/maxMembers, currentMembers
│   └── status, uniformColor, institution
│
└── event_changelog/{changeId}
    ├── eventId, eventName
    ├── changeType (created, modified, cancelled, rescheduled)
    ├── changedBy, changedByName, changedAt
    ├── fieldChanged, oldValue, newValue
    ├── reason
    └── notificationsSent, participantsNotified
```

---

## 🎯 REGLAS DE NEGOCIO IMPLEMENTADAS

### 1. **Sistema de Confirmación de Eventos:**
- ✅ Todos los eventos requieren **mínimo 2 participantes/equipos** para confirmarse
- ✅ El campo `isConfirmed` se actualiza automáticamente con `updateConfirmationStatus()`
- ✅ Si no se alcanza el mínimo, el evento permanece "active" pero no confirmado

### 2. **Validación de Tiempo Mínimo para Cambios:**
- ✅ **Distancia corta (<15 min):** 30 minutos de anticipación
- ✅ **Distancia media (15-30 min):** 1 hora de anticipación
- ✅ **Distancia larga (>30 min):** 2 horas de anticipación
- ✅ Método `canBeModified()` valida si aún hay tiempo para cambios

### 3. **Sistema de Aprobación de Retrasos:**
- ✅ Usuario solicita retraso con razón
- ✅ **Paso 1:** Rival debe aprobar (`approveDelayByRival()`)
- ✅ **Paso 2:** Administrador debe aprobar (`approveDelayByAdmin()`)
- ✅ Solo si ambos aprueban, el retraso es efectivo
- ✅ Estados: pending → approved_by_rival → approved_by_admin

### 4. **Registro Automático con Confirmación:**
- ✅ Al registrarse, `isConfirmed = true` automáticamente
- ✅ Usuario confirma su asistencia desde el inicio
- ✅ Puede solicitar cambios posteriores (retraso/cancelación)

---

## 📱 PRÓXIMAS FASES A IMPLEMENTAR

### **FASE 3: Firebase Cloud Messaging (Notificaciones Push)**
- Servicio `MyFirebaseMessagingService.java`
- Enviar notificaciones en cambios de eventos
- Recordatorios automáticos 24h antes

### **FASE 4: Sistema de Eventos - Backend**
- `EventsActivity.java` - Listar eventos
- `EventDetailActivity.java` - Detalle completo
- `CreateEventActivity.java` - Crear (admin only)
- `EventRepository.java` - Lógica de negocio

### **FASE 5: Sistema de Registro**
- `EventRegistrationActivity.java` - Formulario
- `RegistrationRepository.java` - Validaciones
- `MyRegistrationsActivity.java` - Mis eventos

### **FASE 6: Mapa Interactivo**
- `EventsMapActivity.java` - Mapa con múltiples eventos
- Marcadores personalizados por tipo
- Filtros (fecha, tipo, estado)

### **FASE 7: Cambios y Reprogramaciones**
- `EditEventActivity.java` - Editar eventos
- `EventChangeNotifier.java` - Notificar cambios
- Validación de tiempo mínimo

### **FASE 8: Cámara y Multimedia**
- `CameraActivity.java` - Capturar fotos/videos
- `MediaUploadService.java` - Subir a Firebase Storage
- Galería en detalle de eventos

### **FASE 9: Sincronización Offline**
- `SyncWorker.java` - WorkManager
- Banner de "Sin conexión"
- Sincronizar al volver internet

### **FASE 10: Fragments y Navegación**
- Convertir a arquitectura con Fragments
- BottomNavigationView
- Navigation Component

---

## 📊 RESUMEN DE ARCHIVOS CREADOS/MODIFICADOS

### **Archivos Modificados (5):**
1. ✅ `app/src/main/AndroidManifest.xml` - Permisos + Servicio FCM
2. ✅ `app/build.gradle.kts` - Dependencias (FCM, WorkManager, Glide, CameraX)
3. ✅ `app/src/main/java/com/example/gesdep/UserModel.java` - Campos adicionales + token FCM
4. ✅ `app/src/main/java/com/example/gesdep/LoginActivity.java` - Registro de token FCM
5. ✅ `FASES_IMPLEMENTADAS.md` - Documentación actualizada

### **Archivos Creados (10):**

**Modelos (5):**
1. ✅ `app/src/main/java/com/example/gesdep/models/EventModel.java`
2. ✅ `app/src/main/java/com/example/gesdep/models/TeamModel.java`
3. ✅ `app/src/main/java/com/example/gesdep/models/EventRegistrationModel.java`
4. ✅ `app/src/main/java/com/example/gesdep/models/EventChangeLogModel.java`
5. ✅ `app/src/main/java/com/example/gesdep/models/NotificationModel.java`

**Utilidades (2):**
6. ✅ `app/src/main/java/com/example/gesdep/utils/EventConfig.java`
7. ✅ `app/src/main/java/com/example/gesdep/utils/NotificationHelper.java`

**Servicios (1):**
8. ✅ `app/src/main/java/com/example/gesdep/services/MyFirebaseMessagingService.java`

**Documentación (2):**
9. ✅ `FASES_IMPLEMENTADAS.md` (este archivo)
10. ✅ `CONFIGURACION_FCM.md`

**Total:** **5 archivos modificados + 10 archivos nuevos = 15 archivos**

---

---

## ✅ FASE 3: FIREBASE CLOUD MESSAGING (NOTIFICACIONES PUSH) - **COMPLETADA**

### Archivos creados:

#### 1. **MyFirebaseMessagingService.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/services/MyFirebaseMessagingService.java`

**Funcionalidades implementadas:**
- ✅ Recepción de notificaciones push en tiempo real
- ✅ Manejo de tokens FCM (onNewToken)
- ✅ Guardado automático de token en Firestore
- ✅ Canal de notificaciones para Android 8.0+
- ✅ Notificaciones con acciones (abrir HomeActivity)
- ✅ Método estático `registerFCMToken()` para registrar token desde cualquier Activity
- ✅ Soporte para 9 tipos de notificaciones:
  - `event_changed` - Cambio general en evento
  - `event_cancelled` - Cancelación de evento
  - `event_rescheduled` - Reprogramación de evento
  - `location_changed` - Cambio de ubicación
  - `delay_request` - Solicitud de retraso
  - `delay_approved` - Retraso aprobado
  - `delay_rejected` - Retraso rechazado
  - `event_reminder` - Recordatorio 24h antes
  - `event_confirmed` - Evento confirmado (mínimo alcanzado)

#### 2. **NotificationHelper.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/utils/NotificationHelper.java`

**Métodos implementados:**
- ✅ `notifyEventChanged()` - Notificación genérica de cambio
- ✅ `notifyEventCancelled()` - Notificación de cancelación
- ✅ `notifyEventRescheduled()` - Notificación de reprogramación
- ✅ `notifyLocationChanged()` - Notificación de cambio de ubicación
- ✅ `sendEventReminder()` - Recordatorio 24 horas antes
- ✅ `notifyDelayRequest()` - Notificar solicitud de retraso a rival y admin
- ✅ `notifyDelayApprovedByRival()` - Rival aprobó el retraso
- ✅ `notifyDelayApproved()` - Admin aprobó el retraso (aprobación final)
- ✅ `notifyDelayRejected()` - Retraso rechazado
- ✅ `notifyEventConfirmed()` - Evento alcanzó mínimo de participantes

**Características:**
- ✅ Obtiene automáticamente tokens FCM de usuarios registrados
- ✅ Guarda notificaciones en `pending_notifications` para Cloud Functions
- ✅ Maneja límite de Firestore (10 usuarios por query con whereIn)
- ✅ Títulos y mensajes personalizados con emojis
- ✅ Logs detallados para debugging

#### 3. **NotificationModel.java** ✅
**Ubicación:** `app/src/main/java/com/example/gesdep/models/NotificationModel.java`

**Campos:**
- ✅ `id`, `eventId`, `eventName`
- ✅ `title`, `message`, `type`
- ✅ `tokens[]` - Array de tokens FCM
- ✅ `userIds[]` - Array de IDs de usuarios
- ✅ `sent`, `sentAt`, `status`
- ✅ `senderUserId`, `senderUserName`
- ✅ Métodos: `markAsSent()`, `markAsFailed()`

### Actualizaciones en archivos existentes:

#### 4. **AndroidManifest.xml** ✅
**Agregado:**
```xml
<!-- Servicio de Firebase Cloud Messaging -->
<service
    android:name=".services.MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Canal de notificaciones predeterminado -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_channel_id"
    android:value="gesdep_events_channel" />
```

#### 5. **LoginActivity.java** ✅
**Agregado en método `irHome()`:**
```java
// Registrar token FCM para notificaciones push
MyFirebaseMessagingService.registerFCMToken(this);
```

### Documentación creada:

#### 6. **CONFIGURACION_FCM.md** ✅
**Ubicación:** `C:\AndroidProjects\GesDep\CONFIGURACION_FCM.md`

**Contenido:**
- ✅ Guía completa de configuración de FCM
- ✅ Pasos para habilitar Cloud Messaging en Firebase Console
- ✅ Flujo de notificaciones explicado
- ✅ Ejemplos de uso para cada tipo de notificación
- ✅ Código de Cloud Functions (opcional)
- ✅ Solicitud de permisos para Android 13+
- ✅ Troubleshooting común
- ✅ Testing con Firebase Console, Postman y código

---

## 🔔 FLUJO DE NOTIFICACIONES IMPLEMENTADO

```
1. Usuario inicia sesión
   ↓
2. LoginActivity.irHome() llama a registerFCMToken()
   ↓
3. Token FCM se guarda en users/{uid}/fcmToken
   ↓
4. Admin modifica/cancela evento
   ↓
5. NotificationHelper obtiene registros del evento
   ↓
6. NotificationHelper obtiene tokens FCM de participantes
   ↓
7. Notificación se guarda en pending_notifications (Firestore)
   ↓
8. Cloud Function detecta documento nuevo (opcional)
   ↓
9. Cloud Function envía FCM a todos los tokens
   ↓
10. MyFirebaseMessagingService.onMessageReceived()
   ↓
11. Notificación se muestra en barra de estado
   ↓
12. Usuario hace clic → abre HomeActivity con datos del evento
```

---

## 📊 NUEVAS COLECCIONES EN FIRESTORE

### **pending_notifications** (nueva)
```javascript
{
  id: "notif123",
  eventId: "event456",
  eventName: "Torneo de Fútbol",
  title: "⚠️ Evento Cancelado",
  message: "El evento ha sido cancelado por mal clima.",
  type: "event_cancelled",
  tokens: ["token1", "token2", ...],
  userIds: ["user1", "user2", ...],
  recipientCount: 15,
  sent: false,
  status: "pending",
  createdAt: Timestamp,
  sentAt: null,
  senderUserId: "admin123",
  senderUserName: "Admin GESDEP"
}
```

---

## 🚀 ESTADO DEL PROYECTO

| Fase | Estado | Progreso |
|------|--------|----------|
| Fase 1: Permisos y Dependencias | ✅ COMPLETADA | 100% |
| Fase 2: Modelos de Datos | ✅ COMPLETADA | 100% |
| **Fase 3: Notificaciones FCM** | ✅ **COMPLETADA** | **100%** |
| Fase 4: Sistema de Eventos | ⏳ PENDIENTE | 0% |
| Fase 5: Registro | ⏳ PENDIENTE | 0% |
| Fase 6: Mapa Interactivo | ⏳ PENDIENTE | 0% |
| Fase 7: Cambios/Reprogramación | ⏳ PENDIENTE | 0% |
| Fase 8: Cámara/Multimedia | ⏳ PENDIENTE | 0% |
| Fase 9: Sincronización Offline | ⏳ PENDIENTE | 0% |
| Fase 10: Fragments | ⏳ PENDIENTE | 0% |

**Progreso Total del Proyecto:** **30%** (3/10 fases completadas)

---

## 📝 NOTAS IMPORTANTES

1. **Sincronizar Gradle:** Después de modificar `build.gradle.kts`, ejecutar:
   ```
   File → Sync Project with Gradle Files
   ```

2. **Firebase Console:** Configurar FCM en la consola de Firebase para habilitar notificaciones.

3. **Google Maps API Key:** Actualizar el API key en `res/values/strings.xml` con una clave válida.

4. **Permisos en Runtime:** Implementar solicitud de permisos en runtime para Android 6.0+:
   - GPS: `ACCESS_FINE_LOCATION`
   - Cámara: `CAMERA`
   - Notificaciones: `POST_NOTIFICATIONS` (Android 13+)

5. **Testing:** Cada modelo incluye métodos de validación que deben ser probados:
   - `EventModel.canBeModified()`
   - `EventModel.updateConfirmationStatus()`
   - `EventRegistrationModel.isDelayFullyApproved()`
   - `TeamModel.hasMinimumMembers()`

---

**Fecha de última actualización:** 26 de Noviembre, 2025
**Versión:** 1.0
**Desarrollado con Claude Code** 🤖
