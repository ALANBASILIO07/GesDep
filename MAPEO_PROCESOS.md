# 🗺️ MAPEO DE PROCESOS - GESDEP

**Fecha de creación:** 1 de Diciembre, 2025
**Versión:** 1.0

---

## 📚 ÍNDICE

1. [Proceso de Autenticación](#1-proceso-de-autenticación)
2. [Proceso de Registro](#2-proceso-de-registro)
3. [Proceso de Creación de Eventos](#3-proceso-de-creación-de-eventos)
4. [Proceso de Registro a Eventos](#4-proceso-de-registro-a-eventos)
5. [Proceso de Gestión de Venues](#5-proceso-de-gestión-de-venues)
6. [Proceso de Notificaciones](#6-proceso-de-notificaciones)
7. [Proceso de Check-in](#7-proceso-de-check-in)
8. [Diagramas de Flujo](#8-diagramas-de-flujo)

---

## 1. PROCESO DE AUTENTICACIÓN

### 1.1 Flujo Principal: Inicio de Sesión

```
┌─────────────────────────────────────────────────────────────┐
│                    WelcomeActivity                           │
│                    (Pantalla inicial)                        │
└─────────────────────────────────────────────────────────────┘
                           ↓
           ┌───────────────┴───────────────┐
           │                               │
   [Hay usuario autenticado?]              │
           │                               │
         ✅ SÍ                            ❌ NO
           │                               │
           ↓                               ↓
  checkUserRoleAndRedirect()      Mostrar botones
           │                       Login / Register
           ↓                               │
    Consultar Firestore                    │
    users/{uid}/role                   [Usuario toca Login]
           │                               │
           ↓                               ↓
    ┌─────┴──────┐               ┌─────────────────┐
    │            │               │  LoginActivity  │
  "admin"     "coach"            └─────────────────┘
    │            │    "user"              │
    ↓            ↓       ↓                ↓
AdminHome   CoachHome  UserHome   1. Ingresar email
                                   2. Ingresar password
                                          ↓
                                   Firebase Auth
                                   signInWithEmailAndPassword()
                                          ↓
                                   ┌──────┴───────┐
                                   │              │
                               ✅ ÉXITO      ❌ ERROR
                                   │              │
                                   ↓              ↓
                          loadUserAndRedirect()  Mostrar Toast
                                   │           "Error al iniciar sesión"
                                   ↓
                          Consultar Firestore
                          users/{uid}
                                   │
                           ┌───────┴────────┐
                           │                │
                    Doc existe?      Doc NO existe
                           │                │
                           ↓                ↓
                    Obtener role    Crear perfil básico
                           │         role = "user"
                           │                │
                           └────────┬───────┘
                                    ↓
                         Registrar token FCM
                         (para notificaciones)
                                    ↓
                         redirectToHome(role)
                                    ↓
                         ┌──────────┼──────────┐
                         │          │          │
                      "admin"    "coach"    "user"
                         │          │          │
                         ↓          ↓          ↓
                   AdminHome  CoachHome  UserHome
```

### 1.2 Código Relevante

**WelcomeActivity.java:42**
```java
db = FirebaseFirestore.getInstance("gesdep"); // ⚠️ Instancia personalizada
```

**LoginActivity.java:105-129**
```java
auth.signInWithEmailAndPassword(email, pass)
    .addOnSuccessListener(result -> {
        loadUserAndRedirect(user.getUid());
    })
    .addOnFailureListener(e -> {
        // Manejo de errores específicos
        if (e.getMessage().contains("no user record")) {
            errorMessage = "Usuario no encontrado";
        } else if (e.getMessage().contains("password is invalid")) {
            errorMessage = "Contraseña incorrecta";
        }
    });
```

---

## 2. PROCESO DE REGISTRO

### 2.1 Flujo Principal: Creación de Cuenta

```
┌─────────────────────────────────────────────────────────────┐
│                 RegisterActivity                             │
│              (Formulario de registro)                        │
└─────────────────────────────────────────────────────────────┘
                           ↓
                    Usuario completa:
                    • Nombre completo
                    • Email
                    • Contraseña
                    • Confirmar contraseña
                    • Foto de perfil (opcional)
                    • Tipo de usuario
                           ↓
              ┌────────────┴────────────┐
              │                         │
    [Tipo de usuario seleccionado]     │
              │                         │
     ┌────────┼─────────┐              │
     │        │         │              │
Participante Coach  Organizador        │
     │        │         │              │
     │        └────┬────┘              │
     │             ↓                   │
     │    Mostrar campo                │
     │    "Código de Organización"     │
     │             ↓                   │
     │    ┌────────┴────────┐         │
     │    │                 │         │
     │  Coach          Organizador    │
     │    │                 │         │
     │ ENTRENADOR2025   ADMIN2025     │
     │    │                 │         │
     └────┴─────────────────┘         │
              │                       │
              ↓                       │
    [Validar todos los campos]       │
              ↓                       │
         ✅ Válido                    │
              ↓                       │
    ┌─────────────────────┐          │
    │ Crear usuario en    │          │
    │ Firebase Auth       │          │
    └─────────────────────┘          │
              ↓                       │
         ✅ Éxito                     │
              ↓                       │
    [¿Usuario seleccionó foto?]      │
              │                       │
         ┌────┴────┐                 │
         │         │                 │
       SÍ         NO                 │
         │         │                 │
         ↓         └────┐            │
  uploadImageAndSaveData() │         │
         │              │            │
  Upload a Storage     │            │
  /profile_images/     │            │
  {uid}.jpg            │            │
         │              │            │
  Obtener URL          │            │
         │              │            │
         └──────┬───────┘            │
                ↓                    │
    saveUserToFirestore()           │
                ↓                    │
    Firestore("gesdep")/users/{uid} │
    {                               │
      uid: string,                  │
      name: string,                 │
      email: string,                │
      role: string,                 │
      photoUrl: string? (opcional), │
      createdAt: timestamp,         │
      active: true,                 │
      eventsOrganized: 0,          │
      eventsParticipated: 0        │
    }                               │
                ↓                    │
         ✅ Guardado                 │
                ↓                    │
      auth.signOut()                │
      (Cerrar sesión automática)    │
                ↓                    │
      Intent → LoginActivity        │
      (Para que inicie sesión)      │
                ↓                    │
         Toast: "Cuenta creada       │
          exitosamente"              │
```

### 2.2 Validaciones Implementadas

**RegisterActivity.java:167-194**
```java
// Validaciones
if (fullName.isEmpty()) {
    etName.setError("Requerido");
    return;
}

if (email.isEmpty()) {
    etEmail.setError("Requerido");
    return;
}

if (password.length() < 6) {
    etPassword.setError("Mínimo 6 caracteres");
    return;
}

if (!password.equals(confirmPassword)) {
    etConfirmPassword.setError("No coinciden");
    return;
}

// Validar código de organización
if (selectedRole == R.id.rbOrganizer) {
    if (!orgCode.equals(CODE_ORGANIZER)) {
        etOrgCode.setError("Código incorrecto");
        return;
    }
}
```

### 2.3 Códigos de Acceso Actuales

| Rol | Código | Variable |
|-----|--------|----------|
| Organizador (admin) | `ADMIN2025` | `CODE_ORGANIZER` |
| Entrenador (coach) | `ENTRENADOR2025` | `CODE_COACH` |
| Participante (user) | *(sin código)* | N/A |

---

## 3. PROCESO DE CREACIÓN DE EVENTOS

### 3.1 Flujo Principal (Solo Admin)

```
┌─────────────────────────────────────────────────────────────┐
│               AdminHomeActivity                              │
│            (Dashboard del administrador)                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
                [Admin toca "Eventos"]
                           ↓
              ┌─────────────────────┐
              │  EventsActivity     │
              │  (Lista de eventos) │
              └─────────────────────┘
                           ↓
              [Admin toca botón FAB "+"]
                           ↓
         ┌──────────────────────────────┐
         │  CreateEventActivity         │
         │  (Formulario de evento)      │
         └──────────────────────────────┘
                           ↓
              Admin completa:
              • Nombre del evento
              • Descripción
              • Tipo: Deportivo / Cultural
              • Categoría: fútbol, basquetbol, etc.
              • Fecha y hora
              • Deadline de registro
              • Tipo de registro: Individual / Equipos
              • Capacidad mín/máx
              • Ubicación (GPS)
                           ↓
         [Validar todos los campos]
                           ↓
              ✅ Todos válidos
                           ↓
         Crear objeto EventModel
                           ↓
    Firestore("gesdep")/events/{eventId}
    {
      id: auto-generated,
      name: string,
      description: string,
      type: "deportivo" | "cultural",
      category: string,
      eventDateTime: timestamp,
      registrationDeadline: timestamp,
      registrationType: "individual" | "team",
      minParticipants: int,
      maxParticipants: int,
      currentParticipants: 0,
      placeName: string,
      latitude: double,
      longitude: double,
      organizerId: currentUser.uid,
      organizerName: currentUser.name,
      organizerEmail: currentUser.email,
      status: "active",
      isConfirmed: false,
      createdAt: timestamp
    }
                           ↓
              ✅ Guardado en Firestore
                           ↓
         Notificar usuarios interesados
         (opcional, si hay suscriptores)
                           ↓
              Intent → EventsActivity
              Toast: "Evento creado"
```

### 3.2 Estados de un Evento

```
Estado del Evento (Lifecycle):

  [CREADO]
     ↓
  active (esperando registros)
     ↓
     ├─> currentParticipants >= minParticipants
     │   ↓
     │   isConfirmed = true
     │   status = "confirmed"
     │
     ├─> Admin cancela evento
     │   ↓
     │   status = "cancelled"
     │   cancellationReason = "..."
     │
     ├─> Admin reprograma evento
     │   ↓
     │   status = "rescheduled"
     │   nueva fecha guardada
     │
     └─> Evento finaliza
         ↓
         status = "completed"
```

### 3.3 Validaciones de Negocio

**EventModel.java**
```java
// Validar si evento puede ser modificado
public boolean canBeModified() {
    long now = System.currentTimeMillis();
    long timeDifference = this.eventDateTime - now;
    long minimumTime = this.minimumMinutesBeforeChange * 60 * 1000L;

    return timeDifference >= minimumTime;
}

// Validar si está lleno
public boolean isFull() {
    return this.currentParticipants >= this.maxParticipants;
}

// Actualizar confirmación según participantes
public void updateConfirmationStatus() {
    this.isConfirmed = this.currentParticipants >= this.minParticipants;
}
```

---

## 4. PROCESO DE REGISTRO A EVENTOS

### 4.1 Flujo Principal (Usuario)

```
┌─────────────────────────────────────────────────────────────┐
│                UserHomeActivity                              │
│           (Dashboard del participante)                       │
└─────────────────────────────────────────────────────────────┘
                           ↓
              [Usuario toca "Eventos"]
                           ↓
         ┌──────────────────────────────┐
         │  EventsActivity               │
         │  (Lista de eventos activos)   │
         └──────────────────────────────┘
                           ↓
         Usuario selecciona un evento
                           ↓
         ┌──────────────────────────────┐
         │  EventDetailActivity          │
         │  (Detalle completo)           │
         └──────────────────────────────┘
                           ↓
         Mostrar información:
         • Nombre, descripción
         • Fecha y hora
         • Ubicación (mapa)
         • Participantes actuales/máximo
         • Estado de confirmación
                           ↓
         [¿Ya está registrado?]
                ↓
         ┌──────┴──────┐
         │             │
       NO             SÍ
         │             │
         ↓             ↓
  [Botón: Registrarse]  [Botón: Cancelar Registro]
         │             │
         ↓             │
  [¿Evento lleno?]     │
         │             │
   ┌─────┴─────┐       │
   │           │       │
 NO           SÍ       │
   │           │       │
   ↓           ↓       │
Registrar   Lista      │
            espera     │
   │                   │
   ↓                   │
Crear registro        │
en Firestore          │
   │                   │
registrations/        │
{registrationId}      │
{                     │
  eventId: string,    │
  userId: string,     │
  userName: string,   │
  registrationType: string,
  status: "confirmed",
  isConfirmed: true,  // Auto-confirmado
  registeredAt: timestamp
}                     │
   │                   │
   ↓                   │
Actualizar evento:    │
currentParticipants++ │
   │                   │
   ↓                   │
updateConfirmationStatus()
   │                   │
   ↓                   │
Toast: "Registrado    │
exitosamente"         │
                      │
                      ↓
              [Cancelar registro]
                      ↓
              Actualizar registro:
              status = "cancelled"
                      ↓
              currentParticipants--
                      ↓
              updateConfirmationStatus()
                      ↓
              Toast: "Registro cancelado"
```

### 4.2 Tipos de Registro

#### A) Registro Individual
```
Usuario → EventDetailActivity
         ↓
   [Botón: Registrarse]
         ↓
   Crear EventRegistrationModel
   {
     registrationType: "individual",
     userId: currentUser.uid,
     userName: currentUser.name,
     teamId: null,
     teamName: null
   }
```

#### B) Registro por Equipo
```
Líder de Equipo → EventDetailActivity
         ↓
   [Botón: Registrar Equipo]
         ↓
   Mostrar formulario:
   • Nombre del equipo
   • Miembros del equipo
   • Uniforme
   • Institución
         ↓
   Crear TeamModel
   {
     teamName: string,
     leaderId: currentUser.uid,
     leaderName: currentUser.name,
     leaderEmail: string,
     leaderPhone: string,
     members: [{
       userId, name, email, phone, role
     }],
     minMembers: int,
     maxMembers: int,
     currentMembers: int
   }
         ↓
   Crear EventRegistrationModel
   {
     registrationType: "team",
     userId: leaderId,
     userName: leaderName,
     teamId: team.id,
     teamName: team.name
   }
```

---

## 5. PROCESO DE GESTIÓN DE VENUES

### 5.1 Flujo Principal (Admin)

```
┌─────────────────────────────────────────────────────────────┐
│               AdminHomeActivity                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
            [Admin toca "Instalaciones"]
                           ↓
         ┌──────────────────────────────┐
         │  ManageVenuesActivity         │
         │  (Lista de instalaciones)     │
         └──────────────────────────────┘
                           ↓
         Cargar venues desde Firestore
         venues/{venueId}
                           ↓
         Mostrar RecyclerView
         (item_venue.xml)
                           ↓
         ┌────────────────┴────────────────┐
         │                                 │
    [Ver/Editar]                    [Crear Nueva]
         │                                 │
         ↓                                 ↓
   EventDetailActivity        CreateVenueActivity
   (editar venue)                         ↓
                                 Formulario:
                                 • Nombre
                                 • Tipo (deportiva/cultural/mixta)
                                 • Categorías
                                 • Capacidad
                                 • Ubicación (GPS)
                                 • Horarios
                                 • Servicios
                                 • Fotos
                                          ↓
                                 [Seleccionar ubicación]
                                          ↓
                                 MapPickerActivity
                                 (seleccionar punto GPS)
                                          ↓
                                 Guardar lat/lng
                                          ↓
                                 Validar campos
                                          ↓
                                 Crear VenueModel
                                          ↓
                           Firestore/venues/{venueId}
                           {
                             id: auto-generated,
                             name: string,
                             type: string,
                             categories: [string],
                             capacity: int,
                             location: {
                               address: string,
                               latitude: double,
                               longitude: double
                             },
                             availability: {
                               isAvailable: boolean,
                               schedule: {
                                 "lunes": "8:00-18:00",
                                 "martes": "8:00-18:00",
                                 ...
                               }
                             },
                             services: [string],
                             photoUrls: [string],
                             createdBy: adminId,
                             createdAt: timestamp,
                             maintenanceStatus: "operativo"
                           }
                                          ↓
                                 Toast: "Instalación creada"
                                          ↓
                                 Intent → ManageVenuesActivity
```

### 5.2 Estados de Venue

```
Venue Status (maintenanceStatus):

operativo
   └─> La instalación está disponible para reservas

en_mantenimiento
   └─> Temporalmente no disponible

fuera_de_servicio
   └─> Cerrada permanentemente o por tiempo prolongado
```

---

## 6. PROCESO DE NOTIFICACIONES

### 6.1 Flujo de Firebase Cloud Messaging

```
┌─────────────────────────────────────────────────────────────┐
│            MyFirebaseMessagingService                        │
│         (Servicio de notificaciones FCM)                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
        [Evento importante ocurre]
        Ejemplo: Admin cancela evento
                           ↓
         ┌──────────────────────────────┐
         │  NotificationHelper          │
         │  .notifyEventCancelled()     │
         └──────────────────────────────┘
                           ↓
    1. Obtener registros del evento
       registrations WHERE eventId = X
                           ↓
    2. Obtener tokens FCM de usuarios
       users WHERE uid IN [userId1, userId2...]
                           ↓
    3. Crear NotificationModel
       {
         eventId: string,
         eventName: string,
         title: "⚠️ Evento Cancelado",
         message: "El evento X ha sido cancelado",
         type: "event_cancelled",
         tokens: [token1, token2...],
         userIds: [userId1, userId2...],
         sent: false,
         status: "pending",
         createdAt: timestamp
       }
                           ↓
    4. Guardar en Firestore
       pending_notifications/{notifId}
                           ↓
    5. Cloud Function detecta nuevo doc
       (o enviar directamente desde app)
                           ↓
    6. Enviar FCM a todos los tokens
       https://fcm.googleapis.com/fcm/send
                           ↓
    7. MyFirebaseMessagingService
       .onMessageReceived()
                           ↓
    8. Crear notificación local
       NotificationManager.notify()
                           ↓
    9. Usuario ve notificación
       en barra de estado
                           ↓
   [Usuario toca notificación]
                           ↓
    10. Abrir EventDetailActivity
        con eventId del intent
```

### 6.2 Tipos de Notificaciones

| Tipo | Descripción | Ícono | Acción |
|------|-------------|-------|--------|
| `event_created` | Nuevo evento disponible | 📢 | Abrir EventDetail |
| `event_changed` | Cambio general en evento | 📝 | Abrir EventDetail |
| `event_cancelled` | Evento cancelado | ⚠️ | Abrir EventDetail |
| `event_rescheduled` | Evento reprogramado | 📅 | Abrir EventDetail |
| `location_changed` | Cambio de ubicación | 📍 | Abrir Mapa |
| `event_reminder` | Recordatorio 24h antes | ⏰ | Abrir EventDetail |
| `event_confirmed` | Evento confirmado (mínimo alcanzado) | ✅ | Abrir EventDetail |
| `registration_confirmed` | Registro confirmado | ✅ | Abrir Mis Eventos |
| `registration_cancelled` | Registro cancelado | ❌ | Ver Eventos |

---

## 7. PROCESO DE CHECK-IN

### 7.1 Flujo de Asistencia con GPS

```
┌─────────────────────────────────────────────────────────────┐
│          EventDetailActivity                                 │
│      (Usuario en evento registrado)                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
    [Día del evento, hora de inicio cercana]
                           ↓
         Botón: "Confirmar Asistencia"
                           ↓
    [¿Usuario cerca del evento?]
    Validar ubicación GPS
                           ↓
         Calcular distancia entre:
         • GPS actual del usuario
         • GPS del evento
                           ↓
              ┌────────────┴────────────┐
              │                         │
       Dentro del rango          Fuera del rango
       (<500 metros)             (>500 metros)
              │                         │
              ↓                         ↓
       ✅ Check-in OK          ❌ Mostrar error
              │                "Debes estar cerca
              │                 del evento"
              ↓
    Actualizar registro:
    registrations/{regId}
    {
      attended: true,
      checkInTime: timestamp,
      checkInLatitude: double,
      checkInLongitude: double
    }
              ↓
    Incrementar contador:
    events/{eventId}/attendedCount++
              ↓
    Toast: "✅ Asistencia confirmada"
              ↓
    [Opcional: Desbloquear funciones]
    • Subir fotos del evento
    • Calificar evento
    • Ver resultados
```

### 7.2 Sistema de Retrasos (Futuro)

```
Usuario solicita retraso
         ↓
EventRegistrationModel
.requestDelay(minutos, razón)
         ↓
Actualizar registro:
{
  hasDelayRequest: true,
  delayReason: string,
  delayMinutes: int,
  delayStatus: "pending",
  rivalApproved: false,
  adminApproved: false
}
         ↓
Notificar a rival/oponente
         ↓
[Rival aprueba?]
         ↓
   ┌─────┴─────┐
   │           │
  SÍ          NO
   │           │
   ↓           ↓
rivalApproved  delayStatus
= true         = "rejected"
   │
   ↓
Notificar a admin
   ↓
[Admin aprueba?]
   ↓
   ┌─────┴─────┐
   │           │
  SÍ          NO
   │           │
   ↓           ↓
adminApproved  delayStatus
= true         = "rejected"
   │
   ↓
delayStatus = "approved"
   ↓
Actualizar hora del evento
   ↓
Notificar a todos los participantes
```

---

## 8. DIAGRAMAS DE FLUJO

### 8.1 Diagrama de Navegación General

```
                    ┌──────────────┐
                    │   Welcome    │
                    │   Activity   │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         ┌────▼───┐   ┌───▼────┐  ┌───▼────┐
         │ Login  │   │Register│  │ (Skip) │
         └────┬───┘   └───┬────┘  └───┬────┘
              │            │           │
              └────────┬───┴───────────┘
                       │
              ┌────────┴────────┐
              │                 │
         [Verificar rol]        │
              │                 │
    ┌─────────┼─────────┐       │
    │         │         │       │
┌───▼────┐ ┌──▼──┐ ┌───▼───┐   │
│ Admin  │ │Coach│ │ User  │   │
│ Home   │ │Home │ │ Home  │   │
└───┬────┘ └──┬──┘ └───┬───┘   │
    │         │        │        │
    │    ┌────┴────────┴────┐   │
    │    │                  │   │
    │    ▼                  ▼   │
    │  Eventos          Perfil  │
    │    │                      │
    │    ├─> EventsActivity     │
    │    │                      │
    │    ├─> EventDetail        │
    │    │                      │
    │    └─> CreateEvent (admin)│
    │                           │
    ├─> Instalaciones (admin)   │
    │    │                      │
    │    ├─> ManageVenues       │
    │    │                      │
    │    └─> CreateVenue        │
    │                           │
    ├─> Notificaciones          │
    │    │                      │
    │    └─> NotificationsActivity
    │                           │
    └─> Configuración           │
         │                      │
         └─> Cerrar Sesión ─────┘
```

### 8.2 Ciclo de Vida de un Evento

```
     ┌─────────────────┐
     │  EVENTO CREADO  │
     │  status:active  │
     └────────┬────────┘
              │
    Usuarios se registran
              │
              ▼
   ┌──────────────────────┐
   │ currentParticipants  │
   │        < min         │
   │ isConfirmed: false   │
   └──────────┬───────────┘
              │
              ├─> Tiempo límite alcanzado
              │   No alcanzó mínimo
              │   ↓
              │   status: "cancelled"
              │   reason: "Insuficientes participantes"
              │
              ├─> currentParticipants >= min
              │   ↓
              │   ┌────────────────────┐
              │   │ status: "confirmed"│
              │   │ isConfirmed: true  │
              │   └─────────┬──────────┘
              │             │
              │   Notificar a todos
              │   "Evento confirmado"
              │             │
              │             ▼
              │   ┌─────────────────┐
              │   │  DÍA DEL EVENTO │
              │   └─────────┬───────┘
              │             │
              │   Usuarios hacen check-in
              │   (GPS validation)
              │             │
              │             ▼
              │   ┌─────────────────────┐
              │   │ status: "completed" │
              │   └─────────────────────┘
              │
              └─> Admin cancela
                  ↓
                  status: "cancelled"
                  cancellationReason: "..."
```

### 8.3 Flujo de Datos Firebase

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE (Android App)                     │
└──────────────┬──────────────────────────────────────────────┘
               │
               ├─> Firebase Authentication
               │   • signInWithEmailAndPassword()
               │   • createUserWithEmailAndPassword()
               │   • signOut()
               │
               ├─> Firestore("gesdep")
               │   │
               │   ├─> users/
               │   │   • get() - Obtener perfil
               │   │   • set() - Crear/actualizar
               │   │   • update() - Actualizar campos
               │   │
               │   ├─> events/
               │   │   • addSnapshotListener() - Tiempo real
               │   │   • where() - Filtros
               │   │   • orderBy() - Ordenar
               │   │
               │   ├─> registrations/
               │   │   • where("eventId", "==", X)
               │   │   • where("userId", "==", Y)
               │   │
               │   ├─> venues/
               │   │   • get() - Lista de instalaciones
               │   │   • add() - Crear nueva
               │   │
               │   └─> pending_notifications/
               │       • add() - Encolar notificación
               │
               ├─> Firebase Storage
               │   │
               │   ├─> profile_images/{uid}.jpg
               │   │   • putFile() - Subir foto
               │   │   • getDownloadUrl() - Obtener URL
               │   │
               │   └─> event_images/{eventId}/{timestamp}.jpg
               │
               └─> Firebase Cloud Messaging
                   • getToken() - Obtener token FCM
                   • onMessageReceived() - Recibir notificación
```

---

## 9. GLOSARIO DE TÉRMINOS

| Término | Definición |
|---------|------------|
| **FCM** | Firebase Cloud Messaging - Sistema de notificaciones push de Google |
| **Firestore** | Base de datos NoSQL en tiempo real de Firebase |
| **UID** | User ID - Identificador único de usuario en Firebase Auth |
| **Token FCM** | Identificador único del dispositivo para recibir notificaciones |
| **Check-in** | Confirmación de asistencia presencial con validación GPS |
| **Venue** | Instalación deportiva o cultural (cancha, auditorio, etc.) |
| **Event Lifecycle** | Ciclo de vida del evento (active → confirmed → completed) |
| **Role** | Rol del usuario (admin, coach, user) |
| **GPS Validation** | Validación de ubicación para check-in |
| **Delay Request** | Solicitud de retraso con aprobación dual (rival + admin) |

---

## 10. CÓDIGOS DE ESTADO Y ERRORES

### 10.1 Estados de Eventos
```java
"active"      // Evento activo, esperando registros
"confirmed"   // Evento confirmado (mínimo alcanzado)
"cancelled"   // Evento cancelado
"rescheduled" // Evento reprogramado
"completed"   // Evento finalizado
```

### 10.2 Estados de Registros
```java
"confirmed"   // Registro confirmado
"cancelled"   // Registro cancelado
"delayed"     // Retraso solicitado
"completed"   // Asistió al evento
```

### 10.3 Códigos de Error Firebase Auth
```java
"no user record"        → "Usuario no encontrado"
"password is invalid"   → "Contraseña incorrecta"
"already in use"        → "Email ya registrado"
"invalid email"         → "Email inválido"
"network"               → "Error de conexión"
```

---

## 11. CONFIGURACIÓN Y CONSTANTES

### 11.1 Tiempos de Validación
```java
// EventModel.java
distanceFromCenterMinutes < 15  → 30 min anticipación
distanceFromCenterMinutes 15-30 → 60 min anticipación
distanceFromCenterMinutes > 30  → 120 min anticipación
```

### 11.2 Capacidades por Defecto
```java
// EventConfig.java
minParticipants: 2 (default para confirmación)
maxParticipants: Variable según categoría
checkInRadius: 500 metros
```

### 11.3 Firebase Paths
```java
Firestore Instance: "gesdep"
Storage: gs://gesdep-uaemex.firebasestorage.app
Auth: Firebase Authentication (default)
FCM: Cloud Messaging (default)
```

---

**Documento creado por:** Claude Code
**Para:** Proyecto GESDEP - IMCUFIDE
**Última actualización:** 1 de Diciembre, 2025
