# 📐 DIAGRAMAS UML - SISTEMA GESDEP

## Sistema de Gestión de Eventos Deportivos y Culturales

---

## 📋 ÍNDICE

1. [Diagrama de Casos de Uso](#diagrama-de-casos-de-uso)
2. [Diagrama de Clases](#diagrama-de-clases)
3. [Diagrama de Secuencia - Registro de Usuario](#diagrama-de-secuencia---registro-de-usuario)
4. [Diagrama de Secuencia - Crear Evento](#diagrama-de-secuencia---crear-evento)
5. [Diagrama de Secuencia - Inscripción a Evento](#diagrama-de-secuencia---inscripción-a-evento)
6. [Diagrama de Actividades - Flujo de Autenticación](#diagrama-de-actividades---flujo-de-autenticación)
7. [Diagrama de Actividades - Recarga de Saldo](#diagrama-de-actividades---recarga-de-saldo)
8. [Diagrama de Actividades - Crear Reporte](#diagrama-de-actividades---crear-reporte)
9. [Diagrama de Estados - Evento](#diagrama-de-estados---evento)
10. [Diagrama de Componentes](#diagrama-de-componentes)

---

## 1. DIAGRAMA DE CASOS DE USO

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor "Usuario Regular" as User
actor "Administrador" as Admin
actor "Coach" as Coach
actor "Sistema Firebase" as Firebase

rectangle "Sistema GESDEP" {
  ' Casos de uso de Autenticación
  (Registrarse) as UC1
  (Iniciar Sesión) as UC2
  (Cerrar Sesión) as UC3

  ' Casos de uso de Eventos
  (Ver Eventos) as UC4
  (Crear Evento) as UC5
  (Inscribirse en Evento) as UC6
  (Cancelar Inscripción) as UC7
  (Solicitar Retraso) as UC8
  (Aprobar Retraso) as UC9

  ' Casos de uso de Equipos
  (Crear Equipo) as UC10
  (Agregar Miembros) as UC11
  (Ver Mis Equipos) as UC12

  ' Casos de uso de Saldo
  (Recargar Saldo) as UC13
  (Ver Saldo) as UC14

  ' Casos de uso de Reportes
  (Crear Reporte) as UC15
  (Ver Reportes) as UC16
  (Gestionar Reportes) as UC17

  ' Casos de uso del Coach
  (Ver Grupos) as UC18
  (Ver Horarios) as UC19

  ' Notificaciones
  (Enviar Notificaciones) as UC20
}

' Relaciones Usuario Regular
User --> UC1
User --> UC2
User --> UC3
User --> UC4
User --> UC6
User --> UC7
User --> UC8
User --> UC13
User --> UC14
User --> UC15

' Relaciones Administrador
Admin --> UC2
Admin --> UC3
Admin --> UC4
Admin --> UC5
Admin --> UC9
Admin --> UC16
Admin --> UC17
Admin --> UC20

' Relaciones Coach
Coach --> UC2
Coach --> UC3
Coach --> UC10
Coach --> UC11
Coach --> UC12
Coach --> UC13
Coach --> UC14
Coach --> UC18
Coach --> UC19

' Extends y Includes
UC6 ..> UC10 : <<extends>>
UC8 ..> UC9 : <<requires>>
UC5 ..> UC20 : <<includes>>

' Sistema externo
Firebase ..> UC1 : <<validates>>
Firebase ..> UC2 : <<authenticates>>

@enduml
```

---

## 2. DIAGRAMA DE CLASES

```plantuml
@startuml
skinparam class {
  BackgroundColor PaleGreen
  ArrowColor SeaGreen
  BorderColor SpringGreen
}

class UserModel {
  + String uid
  + String name
  + String email
  + String phone
  + String role
  + String userType
  + double balance
  + String profilePhotoUrl
  + String institution
  + long createdAt
  + long lastLogin
  + String fcmToken
  + int eventsRegistered
  + int eventsCompleted
  + int teamsLeading
  --
  + boolean isAdmin()
  + boolean isTeamLeader()
  + void incrementEventsRegistered()
  + void incrementEventsCompleted()
}

class EventModel {
  + String id
  + String name
  + String description
  + String type
  + String category
  + String placeName
  + double latitude
  + double longitude
  + int distanceFromCenterMinutes
  + Timestamp eventDateTime
  + Timestamp registrationDeadline
  + int durationMinutes
  + String registrationType
  + int minParticipants
  + int maxParticipants
  + int currentParticipants
  + int currentTeams
  + String status
  + boolean isConfirmed
  + String organizerId
  + List<String> photoUrls
  + List<String> videoUrls
  --
  + boolean canBeModified()
  + boolean isFull()
  + void updateConfirmationStatus()
  + int getMinimumMinutesBeforeChange()
}

class TeamModel {
  + String id
  + String teamName
  + String eventId
  + String eventName
  + String leaderId
  + String leaderName
  + String leaderEmail
  + String leaderPhone
  + List<TeamMember> members
  + int minMembers
  + int maxMembers
  + int currentMembers
  + String status
  + String uniformColor
  + String institution
  --
  + void addMember(TeamMember member)
  + void removeMember(String memberId)
  + boolean hasMinimumMembers()
  + boolean isFull()
}

class TeamMember {
  + String memberId
  + String memberName
  + String memberEmail
  + String memberPhone
  + String position
  + int number
  + long joinedAt
}

class EventRegistrationModel {
  + String id
  + String eventId
  + String eventName
  + String userId
  + String userName
  + String userEmail
  + String registrationType
  + String teamId
  + String teamName
  + String status
  + boolean isConfirmed
  + boolean hasDelayRequest
  + String delayReason
  + int delayMinutes
  + boolean rivalApproved
  + boolean adminApproved
  + String delayStatus
  + boolean attended
  + Timestamp checkInTime
  + double checkInLatitude
  + double checkInLongitude
  + String emergencyContactName
  + String emergencyContactPhone
  --
  + void requestDelay(String reason, int minutes)
  + void approveDelayByRival()
  + void approveDelayByAdmin()
  + void rejectDelay()
  + boolean isDelayFullyApproved()
  + void checkIn(double lat, double lng)
}

class EventChangeLogModel {
  + String id
  + String eventId
  + String eventName
  + String changeType
  + String changedBy
  + String changedByName
  + Timestamp changedAt
  + String fieldChanged
  + String oldValue
  + String newValue
  + String reason
  + boolean notificationsSent
  + int participantsNotified
}

class ReportModel {
  + String id
  + String eventId
  + String eventName
  + String reportType
  + String subject
  + String description
  + String priority
  + String status
  + String reporterId
  + String reporterEmail
  + long createdAt
  + long resolvedAt
  + String resolution
}

class EventConfig {
  + static class EventCategoryConfig
  --
  + static EventCategoryConfig getConfig(String category)
  + static Map<String, EventCategoryConfig> getAllConfigs()
  + static Map<String, EventCategoryConfig> getSportsConfigs()
  + static Map<String, EventCategoryConfig> getCulturalConfigs()
}

' Relaciones
UserModel "1" -- "*" EventModel : organiza >
UserModel "1" -- "*" EventRegistrationModel : se inscribe >
UserModel "1" -- "*" TeamModel : lidera >
UserModel "1" -- "*" ReportModel : reporta >

EventModel "1" -- "*" EventRegistrationModel : tiene >
EventModel "1" -- "*" TeamModel : participa >
EventModel "1" -- "*" EventChangeLogModel : registra >
EventModel "1" -- "*" ReportModel : afecta >

TeamModel "1" -- "*" TeamMember : contiene >
TeamModel "1" -- "1" EventRegistrationModel : inscribe >

EventConfig "1" -- "*" EventModel : configura >

@enduml
```

---

## 3. DIAGRAMA DE SECUENCIA - REGISTRO DE USUARIO

```plantuml
@startuml
actor Usuario as user
participant "RegisterActivity" as reg
participant "FirebaseAuth" as auth
participant "Firestore" as db
participant "UserHomeActivity" as home

user -> reg : Ingresa datos de registro
activate reg

reg -> reg : validateAndRegister()
reg -> auth : createUserWithEmailAndPassword()
activate auth

auth --> reg : FirebaseUser
deactivate auth

reg -> auth : updateProfile(displayName)
activate auth
auth --> reg : Success
deactivate auth

reg -> db : saveUserToFirestore()
activate db

db -> db : Crear documento en users/
note right: {\n  uid, name, email,\n  phone, role, userType,\n  balance: 0.0\n}

db --> reg : Success
deactivate db

reg -> home : startActivity()
activate home
home -> db : loadUserData()
home --> user : Pantalla de inicio
deactivate home
deactivate reg

@enduml
```

---

## 4. DIAGRAMA DE SECUENCIA - CREAR EVENTO

```plantuml
@startuml
actor Administrador as admin
participant "CreateEventActivity" as create
participant "EventConfig" as config
participant "Firestore" as db
participant "FCM" as fcm

admin -> create : Completa formulario de evento
activate create

create -> create : Validar datos
create -> config : getConfig(category)
activate config
config --> create : EventCategoryConfig
deactivate config

create -> create : Crear EventModel
note right: {\n  name, description,\n  type, category,\n  location, dateTime,\n  minParticipants,\n  maxParticipants,\n  status: "active"\n}

create -> db : collection("events").add(event)
activate db
db --> create : DocumentReference
deactivate db

create -> fcm : Enviar notificación\n"Nuevo evento creado"
activate fcm
fcm --> create : Success
deactivate fcm

create --> admin : Toast "Evento creado exitosamente"
deactivate create

@enduml
```

---

## 5. DIAGRAMA DE SECUENCIA - INSCRIPCIÓN A EVENTO

```plantuml
@startuml
actor Usuario as user
participant "EventsActivity" as events
participant "EventDetailActivity" as detail
participant "Firestore" as db

user -> events : Ver lista de eventos
activate events

events -> db : collection("events")\n.whereEqualTo("status", "active")\n.get()
activate db
db --> events : List<EventModel>
deactivate db

events --> user : Muestra eventos
user -> events : Seleccionar evento
events -> detail : startActivity(eventId)
activate detail

detail -> db : get event data
activate db
db --> detail : EventModel
deactivate db

detail --> user : Muestra detalles del evento

user -> detail : Clic en "Inscribirse"
detail -> detail : Validar capacidad\nevent.isFull()

alt Evento lleno
  detail --> user : "Evento lleno"
else Hay espacio
  detail -> detail : Crear EventRegistrationModel
  note right: {\n    eventId, userId,\n    status: "confirmed",\n    isConfirmed: true\n  }

  detail -> db : collection("registrations").add()
  activate db
  db --> detail : Success
  deactivate db

  detail -> db : Update event.currentParticipants++
  activate db
  db --> detail : Success
  deactivate db

  detail -> db : Update event.isConfirmed\nif (currentParticipants >= minParticipants)
  activate db
  db --> detail : Success
  deactivate db

  detail --> user : "Inscripción exitosa"
end

deactivate detail
deactivate events

@enduml
```

---

## 6. DIAGRAMA DE ACTIVIDADES - FLUJO DE AUTENTICACIÓN

```plantuml
@startuml
start

:Usuario abre la app;

if (¿Usuario autenticado?) then (Sí)
  :Obtener rol del usuario;

  if (¿Rol es Admin?) then (Sí)
    :Ir a AdminHomeActivity;
  else if (¿Rol es Coach?) then (Sí)
    :Ir a CoachHomeActivity;
  else (Usuario Regular)
    :Ir a UserHomeActivity;
  endif

else (No)
  :Mostrar WelcomeActivity;

  if (¿Selecciona Login?) then (Sí)
    :Mostrar LoginActivity;
    :Ingresar credenciales;
    :Validar con Firebase Auth;

    if (¿Credenciales válidas?) then (Sí)
      :Obtener datos de Firestore;
      :Redirigir según rol;
    else (No)
      :Mostrar error;
      stop
    endif

  else (Registro)
    :Mostrar RegisterActivity;
    :Ingresar datos;
    :Validar formulario;

    if (¿Datos válidos?) then (Sí)
      :Crear usuario en Firebase Auth;
      :Guardar datos en Firestore;
      :Ir a UserHomeActivity;
    else (No)
      :Mostrar error;
      stop
    endif
  endif
endif

:Cargar datos del usuario;
:Mostrar pantalla principal;

stop
@enduml
```

---

## 7. DIAGRAMA DE ACTIVIDADES - RECARGA DE SALDO

```plantuml
@startuml
start

:Usuario selecciona\n"Recargar Saldo";

:Abrir RechargeActivity;

:Cargar saldo actual\ndesde Firestore;

:Mostrar saldo actual;

partition "Selección de Monto" {
  fork
    :Usuario ingresa monto\nmanualmente;
  fork again
    :Usuario selecciona\nmonto rápido\n($50, $100, $200);
  endfork
}

:Usuario confirma recarga;

if (¿Monto válido?) then (Sí)
  :Calcular nuevo saldo\n= saldo actual + monto;

  :Actualizar saldo en\nFirestore users/{uid};

  if (¿Actualización exitosa?) then (Sí)
    :Actualizar vista con\nnuevo saldo;
    :Mostrar mensaje:\n"Recarga exitosa";
  else (No)
    :Mostrar mensaje:\n"Error al procesar recarga";
  endif

else (No)
  :Mostrar mensaje:\n"Monto inválido";
endif

stop
@enduml
```

---

## 8. DIAGRAMA DE ACTIVIDADES - CREAR REPORTE

```plantuml
@startuml
start

:Usuario selecciona\n"Crear Reporte";

:Abrir CreateReportActivity;

:Cargar eventos activos\ndesde Firestore;

:Mostrar combo de eventos;

partition "Completar Formulario" {
  :Seleccionar evento afectado;
  :Seleccionar tipo de reporte;
  :Ingresar asunto;
  :Ingresar descripción;
  :Seleccionar prioridad\n(Baja/Media/Alta);
}

:Usuario envía reporte;

if (¿Todos los campos completos?) then (Sí)
  :Crear objeto Report;
  note right
    {
      eventId, eventName,
      reportType, subject,
      description, priority,
      status: "pendiente",
      reporterId
    }
  end note

  :Guardar en Firestore\ncollection("reports");

  if (¿Guardado exitoso?) then (Sí)
    :Mostrar mensaje:\n"Reporte enviado exitosamente";
    :Cerrar actividad;
  else (No)
    :Mostrar mensaje:\n"Error al enviar reporte";
  endif

else (No)
  :Mostrar mensaje:\n"Complete todos los campos";
endif

stop
@enduml
```

---

## 9. DIAGRAMA DE ESTADOS - EVENTO

```plantuml
@startuml
[*] --> Active : Evento creado

Active --> Confirmed : currentParticipants\n>= minParticipants

Confirmed --> Active : Participante\ncancela inscripción\n(participants < min)

Active --> Cancelled : Administrador\ncancela evento

Confirmed --> Cancelled : Administrador\ncancela evento

Active --> Rescheduled : Administrador\nreprograma evento

Confirmed --> Rescheduled : Administrador\nreprograma evento

Rescheduled --> Active : Nuevas inscripciones

Rescheduled --> Confirmed : Confirmado de nuevo

Active --> Completed : Evento finaliza

Confirmed --> Completed : Evento finaliza

Rescheduled --> Completed : Evento finaliza

Cancelled --> [*]
Completed --> [*]

note right of Active
  Estado inicial del evento.
  Esperando confirmación
  de participantes mínimos.
end note

note right of Confirmed
  Evento confirmado.
  Tiene el mínimo de
  participantes requeridos.
end note

note right of Rescheduled
  Evento reprogramado.
  Nueva fecha/hora asignada.
  Se notifica a participantes.
end note

note right of Completed
  Evento finalizado.
  Se registra asistencia.
end note

@enduml
```

---

## 10. DIAGRAMA DE COMPONENTES

```plantuml
@startuml
package "Presentation Layer" {
  [WelcomeActivity]
  [LoginActivity]
  [RegisterActivity]
  [AdminHomeActivity]
  [CoachHomeActivity]
  [UserHomeActivity]
  [EventsActivity]
  [CreateEventActivity]
  [RechargeActivity]
  [CreateReportActivity]
}

package "Business Logic" {
  [EventConfig]
  [Validators]
  [Utils]
}

package "Data Layer" {
  [Models] as models
  [Adapters] as adapters
}

package "Firebase Services" {
  [Firebase Auth]
  [Cloud Firestore]
  [Firebase Storage]
  [Firebase Messaging]
}

package "External Services" {
  [Google Maps API]
  [Payment Gateway]
}

' Relaciones Presentation -> Business Logic
[WelcomeActivity] --> [Firebase Auth]
[LoginActivity] --> [Firebase Auth]
[RegisterActivity] --> [Firebase Auth]
[RegisterActivity] --> [Cloud Firestore]

[AdminHomeActivity] --> [Cloud Firestore]
[CoachHomeActivity] --> [Cloud Firestore]
[UserHomeActivity] --> [Cloud Firestore]

[EventsActivity] --> [Cloud Firestore]
[EventsActivity] --> [adapters]
[CreateEventActivity] --> [EventConfig]
[CreateEventActivity] --> [Cloud Firestore]
[CreateEventActivity] --> [Firebase Messaging]

[RechargeActivity] --> [Cloud Firestore]
[RechargeActivity] --> [Payment Gateway]

[CreateReportActivity] --> [Cloud Firestore]

' Business Logic -> Data
[EventConfig] ..> [models]
[Validators] ..> [models]

' Adapters -> Models
[adapters] ..> [models]

' Models -> Firebase
[models] ..> [Cloud Firestore]

' Google Maps
[EventsActivity] ..> [Google Maps API]
[CreateEventActivity] ..> [Google Maps API]

note right of [Firebase Auth]
  Autenticación de usuarios
  con email/contraseña
end note

note right of [Cloud Firestore]
  Base de datos NoSQL
  en tiempo real
end note

note right of [EventConfig]
  Configuraciones de
  categorías de eventos
end note

@enduml
```

---

## 📊 DESCRIPCIÓN DE DIAGRAMAS

### 1. Diagrama de Casos de Uso
Muestra las interacciones principales entre los actores (Usuario Regular, Administrador, Coach) y el sistema GESDEP. Incluye casos de uso para:
- Autenticación (registro, login, logout)
- Gestión de eventos (crear, ver, inscribirse)
- Gestión de equipos (crear, agregar miembros)
- Gestión de saldo (recargar, consultar)
- Reportes (crear, gestionar)

### 2. Diagrama de Clases
Representa la estructura de clases del modelo de datos del sistema:
- **UserModel**: Gestión de usuarios
- **EventModel**: Gestión de eventos
- **TeamModel**: Gestión de equipos
- **EventRegistrationModel**: Inscripciones a eventos
- **EventChangeLogModel**: Auditoría de cambios
- **ReportModel**: Sistema de reportes
- **EventConfig**: Configuraciones de eventos

### 3-5. Diagramas de Secuencia
Muestran la interacción temporal entre objetos para:
- Registro de nuevos usuarios
- Creación de eventos por administradores
- Proceso de inscripción a eventos

### 6-8. Diagramas de Actividades
Representan el flujo de procesos:
- Autenticación y redirección por roles
- Proceso de recarga de saldo
- Creación de reportes

### 9. Diagrama de Estados
Muestra los diferentes estados por los que puede pasar un evento:
- Active → Confirmed → Completed
- Transiciones a Cancelled o Rescheduled

### 10. Diagrama de Componentes
Arquitectura del sistema organizada en capas:
- **Presentation Layer**: Activities y UI
- **Business Logic**: Lógica de negocio y validaciones
- **Data Layer**: Modelos y adaptadores
- **Firebase Services**: Servicios de backend
- **External Services**: APIs externas

---

## 🎯 PATRONES DE DISEÑO IDENTIFICADOS

1. **MVC (Model-View-Controller)**
   - Models: EventModel, UserModel, etc.
   - Views: XML Layouts
   - Controllers: Activities

2. **Singleton**
   - Firebase instances (Auth, Firestore)

3. **Adapter Pattern**
   - EventsAdapter, TeamsAdapter
   - Para RecyclerViews

4. **Observer Pattern**
   - Firebase Realtime Listeners
   - LiveData para cambios en tiempo real

5. **Strategy Pattern**
   - EventConfig para diferentes configuraciones de eventos

---

**Generado:** 6 de Diciembre, 2025
**Herramienta:** PlantUML
**Desarrollado con Claude Code** 🤖
