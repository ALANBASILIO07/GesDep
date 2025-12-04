# 📁 PROPUESTA DE REORGANIZACIÓN DE LAYOUTS POR ROL

**Fecha:** 4 de Diciembre, 2025
**Objetivo:** Organizar layouts en carpetas según el rol que los utiliza

---

## 📊 ANÁLISIS ACTUAL DE LAYOUTS

### Total de Layouts: 27 archivos XML

#### Activities (19)
- activity_activities_list.xml
- activity_admin_home.xml
- activity_attendance.xml
- activity_coach_home.xml
- activity_coaches.xml
- activity_create_event.xml
- activity_create_venue.xml
- activity_detail.xml
- activity_event_detail.xml
- activity_events.xml
- activity_login.xml
- activity_maintenance.xml
- activity_manage_venues.xml
- activity_map_picker.xml
- activity_notifications.xml
- activity_participants.xml
- activity_register.xml
- activity_user_home.xml
- activity_welcome.xml

#### Items RecyclerView (7)
- item_activity.xml
- item_coach.xml
- item_event.xml
- item_notification.xml
- item_participant.xml
- item_report.xml
- item_venue.xml

#### Otros (1)
- nav_header.xml

---

## 🎯 CLASIFICACIÓN POR ROL

### 🔐 LAYOUTS GENERALES (Autenticación - Sin Rol Específico)
Estos permanecen en `layout/` raíz:

```
layout/
├── activity_welcome.xml              ✅ Pantalla inicial (todos)
├── activity_login.xml                ✅ Login (todos)
└── activity_register.xml             ✅ Registro (todos)
```

**Razón:** Son vistas compartidas por todos los roles antes de autenticarse.

---

### 👑 LAYOUTS DE ADMIN (Solo Administrador)
Propuesta: `layout-admin/` o mantener con prefijo `admin_`

```
layout-admin/
├── activity_admin_home.xml           ✅ Dashboard admin
├── activity_create_event.xml         ✅ Crear eventos (solo admin)
├── activity_create_venue.xml         ✅ Crear instalaciones (solo admin)
├── activity_manage_venues.xml        ✅ Gestionar instalaciones (solo admin)
├── activity_coaches.xml              ✅ Gestionar entrenadores (admin)
├── activity_participants.xml         ✅ Gestionar participantes (admin)
├── activity_maintenance.xml          ✅ Mantenimiento (admin)
├── item_coach.xml                    ✅ Item de lista de entrenadores
├── item_participant.xml              ✅ Item de lista de participantes
├── item_venue.xml                    ✅ Item de lista de instalaciones
└── item_report.xml                   ✅ Item de reportes de mantenimiento
```

**Acceso desde:** `AdminHomeActivity` → `drawer_admin.xml`

**Funcionalidades exclusivas:**
- ✅ Crear y gestionar eventos
- ✅ Crear y gestionar instalaciones/venues
- ✅ Ver y gestionar todos los usuarios (coaches y participantes)
- ✅ Reportes y mantenimiento
- ✅ Bandeja de entrada administrativa
- ✅ Configuración del sistema

---

### 🏋️ LAYOUTS DE COACH (Solo Entrenador)
Propuesta: `layout-coach/` o mantener con prefijo `coach_`

```
layout-coach/
├── activity_coach_home.xml           ✅ Dashboard entrenador
├── activity_activities_list.xml      ⚠️ Gestionar actividades/grupos
└── activity_detail.xml               ⚠️ Detalle de actividad (posible)
```

**Acceso desde:** `CoachHomeActivity` → `drawer_coach.xml`

**Funcionalidades exclusivas:**
- ✅ Gestionar sus grupos/equipos
- ✅ Ver horarios de entrenamientos
- ✅ Perfil del coach

**⚠️ NOTA:** El coach tiene MENOS vistas exclusivas porque su funcionalidad está limitada.

---

### 👤 LAYOUTS DE USER (Participante)
Propuesta: `layout-user/` o mantener con prefijo `user_`

```
layout-user/
└── activity_user_home.xml            ✅ Dashboard participante
```

**Acceso desde:** `UserHomeActivity` → `drawer_user.xml`

**Funcionalidades:**
- ✅ Ver eventos disponibles (compartido)
- ✅ Registrarse a eventos (compartido)
- ✅ Ver mis inscripciones
- ✅ Perfil del usuario

**⚠️ NOTA:** El user tiene MENOS vistas exclusivas, la mayoría son compartidas.

---

### 🌐 LAYOUTS COMPARTIDOS (Todos los Roles)
Estos permanecen en `layout/` raíz porque todos los roles pueden acceder:

```
layout/
├── activity_events.xml               ✅ Lista de eventos (todos pueden ver)
├── activity_event_detail.xml         ✅ Detalle de evento (todos)
├── activity_notifications.xml        ✅ Notificaciones (todos)
├── activity_attendance.xml           ✅ Asistencia/Check-in (todos registrados)
├── activity_map_picker.xml           ✅ Selector de ubicación (utilidad)
├── item_event.xml                    ✅ Item de lista de eventos
├── item_notification.xml             ✅ Item de notificación
├── item_activity.xml                 ⚠️ Item de actividad (coach/user?)
└── nav_header.xml                    ✅ Header del drawer (todos)
```

**Razón:** Estas vistas son accesibles desde múltiples roles con diferentes permisos:
- **EventsActivity:** Admin crea, User se registra, Coach también participa
- **EventDetailActivity:** Todos pueden ver detalles, cada uno con diferentes acciones
- **NotificationsActivity:** Todos reciben notificaciones
- **AttendanceActivity:** Cualquiera que esté registrado puede hacer check-in

---

## 🤔 VIABILIDAD TÉCNICA EN ANDROID

### ❌ OPCIÓN 1: Subcarpetas (layout/admin/, layout/coach/, layout/user/)

**PROBLEMA:** Android **NO SOPORTA** subcarpetas dentro de `res/layout/`

```
❌ NO FUNCIONA:
res/layout/admin/activity_admin_home.xml
res/layout/coach/activity_coach_home.xml
res/layout/user/activity_user_home.xml
```

**Error de compilación:**
```
AAPT: error: file not found: layout/admin/activity_admin_home.xml
```

Android solo reconoce:
- `res/layout/` (default)
- `res/layout-land/` (landscape orientation)
- `res/layout-sw600dp/` (tablets)
- `res/layout-v21/` (API level qualifiers)

---

### ✅ OPCIÓN 2: Resource Qualifiers con Prefijos (RECOMENDADA)

Usar **prefijos en nombres de archivo** para organización visual:

```
✅ FUNCIONA:
res/layout/
├── activity_welcome.xml              (general)
├── activity_login.xml                (general)
├── activity_register.xml             (general)
│
├── admin_home.xml                    (admin)
├── admin_create_event.xml            (admin)
├── admin_create_venue.xml            (admin)
├── admin_manage_venues.xml           (admin)
├── admin_coaches.xml                 (admin)
├── admin_participants.xml            (admin)
├── admin_maintenance.xml             (admin)
│
├── coach_home.xml                    (coach)
├── coach_activities_list.xml         (coach)
├── coach_activity_detail.xml         (coach)
│
├── user_home.xml                     (user)
│
├── shared_events.xml                 (compartido)
├── shared_event_detail.xml           (compartido)
├── shared_notifications.xml          (compartido)
├── shared_attendance.xml             (compartido)
├── shared_map_picker.xml             (compartido)
│
├── item_event.xml                    (adapter items)
├── item_notification.xml
├── item_coach.xml
├── item_participant.xml
├── item_venue.xml
└── nav_header.xml
```

**Ventajas:**
- ✅ Compatible con Android
- ✅ Organización clara por prefijo
- ✅ Fácil búsqueda en Android Studio
- ✅ No requiere cambios en código Java

**Desventajas:**
- ⚠️ Todos los archivos siguen en la misma carpeta física
- ⚠️ Solo organización visual/nominal

---

### ✅ OPCIÓN 3: Modules por Rol (Arquitectura Modular)

Crear módulos separados para cada rol:

```
app/
├── common/              (módulo compartido)
│   └── res/layout/
│       ├── activity_welcome.xml
│       ├── activity_login.xml
│       └── activity_register.xml
│
├── admin/               (módulo admin)
│   └── res/layout/
│       ├── activity_admin_home.xml
│       ├── activity_create_event.xml
│       └── ...
│
├── coach/               (módulo coach)
│   └── res/layout/
│       └── activity_coach_home.xml
│
└── user/                (módulo user)
    └── res/layout/
        └── activity_user_home.xml
```

**Ventajas:**
- ✅ Separación física real de código y recursos
- ✅ Cada módulo puede tener sus propias dependencias
- ✅ Mejor para proyectos muy grandes
- ✅ Compilación independiente por módulo

**Desventajas:**
- ⚠️ Requiere reestructuración completa del proyecto
- ⚠️ Más complejo de mantener
- ⚠️ Overhead de configuración (build.gradle por módulo)
- ⚠️ Para tu proyecto actual puede ser **overkill**

---

## 💡 RECOMENDACIÓN PARA TU PROYECTO

### ✅ USAR OPCIÓN 2: Prefijos en Nombres (Sin Cambiar Estructura)

**Por qué es la mejor opción:**

1. **Sin reestructuración:** No hay que mover archivos físicamente
2. **Compatible al 100%:** Android lo soporta nativamente
3. **Fácil de implementar:** Solo renombrar archivos
4. **Búsqueda rápida:** En Android Studio puedes filtrar por prefijo
5. **Escalable:** Si crece el proyecto, puedes migrar a módulos después

---

## 🔧 PLAN DE IMPLEMENTACIÓN

### PASO 1: Renombrar Layouts con Prefijos

#### Mantener sin cambios (Generales):
```
✅ activity_welcome.xml
✅ activity_login.xml
✅ activity_register.xml
```

#### Renombrar a prefijo "admin_":
```
activity_admin_home.xml       → admin_home.xml
activity_create_event.xml     → admin_create_event.xml
activity_create_venue.xml     → admin_create_venue.xml
activity_manage_venues.xml    → admin_manage_venues.xml
activity_coaches.xml          → admin_coaches.xml
activity_participants.xml     → admin_participants.xml
activity_maintenance.xml      → admin_maintenance.xml
```

#### Renombrar a prefijo "coach_":
```
activity_coach_home.xml       → coach_home.xml
activity_activities_list.xml  → coach_activities_list.xml
activity_detail.xml           → coach_activity_detail.xml
```

#### Renombrar a prefijo "user_":
```
activity_user_home.xml        → user_home.xml
```

#### Renombrar a prefijo "shared_":
```
activity_events.xml           → shared_events.xml
activity_event_detail.xml     → shared_event_detail.xml
activity_notifications.xml    → shared_notifications.xml
activity_attendance.xml       → shared_attendance.xml
activity_map_picker.xml       → shared_map_picker.xml
```

---

### PASO 2: Actualizar Referencias en Java

Cada Activity que use un layout renombrado debe actualizar su `setContentView()`:

#### Ejemplo AdminHomeActivity.java:
```java
// ANTES:
setContentView(R.layout.activity_admin_home);

// DESPUÉS:
setContentView(R.layout.admin_home);
```

#### Archivos Java a modificar:
- ✅ AdminHomeActivity.java → `R.layout.admin_home`
- ✅ CreateEventActivity.java → `R.layout.admin_create_event`
- ✅ CreateVenueActivity.java → `R.layout.admin_create_venue`
- ✅ ManageVenuesActivity.java → `R.layout.admin_manage_venues`
- ✅ CoachesActivity.java → `R.layout.admin_coaches`
- ✅ ParticipantsActivity.java → `R.layout.admin_participants`
- ✅ MaintenanceActivity.java → `R.layout.admin_maintenance`
- ✅ CoachHomeActivity.java → `R.layout.coach_home`
- ✅ ActivitiesListActivity.java → `R.layout.coach_activities_list`
- ✅ ActivityDetailActivity.java → `R.layout.coach_activity_detail`
- ✅ UserHomeActivity.java → `R.layout.user_home`
- ✅ EventsActivity.java → `R.layout.shared_events`
- ✅ EventDetailActivity.java → `R.layout.shared_event_detail`
- ✅ NotificationsActivity.java → `R.layout.shared_notifications`
- ✅ AttendanceActivity.java → `R.layout.shared_attendance`
- ✅ MapPickerActivity.java → `R.layout.shared_map_picker`

---

### PASO 3: Actualizar Referencias en Adapters

Los adapters que inflan layouts también necesitan actualización:

```java
// EventsAdapter.java
LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);

// CoachAdapter.java
LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach, parent, false);
```

**⚠️ Estos NO se renombran porque son items genéricos.**

---

### PASO 4: Verificar AndroidManifest.xml

El `AndroidManifest.xml` **NO necesita cambios** porque solo referencia las Activities por nombre de clase, no por layout.

```xml
<!-- ESTO NO CAMBIA -->
<activity android:name=".AdminHomeActivity" />
```

---

## ⏱️ ESTIMACIÓN DE TIEMPO

| Tarea | Tiempo Estimado |
|-------|-----------------|
| Renombrar archivos XML (16 archivos) | 15 minutos |
| Actualizar referencias Java (16 Activities) | 30 minutos |
| Sincronizar Gradle + Build | 5 minutos |
| Testing básico (abrir cada vista) | 20 minutos |
| **TOTAL** | **~70 minutos** |

---

## ⚖️ PROS Y CONTRAS

### ✅ VENTAJAS

1. **Organización Clara:**
   - Al ver `admin_create_event.xml` sabes inmediatamente que es del admin
   - Fácil de encontrar archivos relacionados

2. **Sin Romper Nada:**
   - Los layouts siguen en `res/layout/`
   - Android Studio sigue funcionando normalmente

3. **Búsqueda Mejorada:**
   - Filtro en Android Studio: "admin_*" muestra solo vistas de admin
   - Más fácil para nuevos desarrolladores

4. **Escalabilidad:**
   - Base sólida para futura modularización
   - Puedes agregar más roles fácilmente

5. **Mantenimiento:**
   - Cambios en vistas de admin no afectan user/coach
   - Menos conflictos en Git

### ❌ DESVENTAJAS

1. **Trabajo Manual:**
   - Hay que renombrar 16 archivos
   - Actualizar 16 referencias en Java
   - Riesgo de olvidar alguna referencia

2. **Misma Carpeta Física:**
   - Todos los archivos siguen en `res/layout/`
   - No hay separación física real

3. **Backwards Compatibility:**
   - Si alguien tiene el proyecto clonado, necesitará hacer pull y rebuild
   - Posibles conflictos en Git si hay trabajo en paralelo

4. **Nombres Más Largos:**
   - `admin_create_event.xml` vs `activity_create_event.xml`
   - Puede ser más verboso

---

## 🎯 CONCLUSIÓN

### ¿ES CONVENIENTE HACERLO?

**✅ SÍ, ES CONVENIENTE** por las siguientes razones:

1. **Mejora la organización** sin romper nada
2. **Facilita el desarrollo** futuro con nombres claros
3. **Tiempo de implementación razonable** (~70 minutos)
4. **No requiere cambios arquitectónicos** complejos
5. **Base para futura modularización** si el proyecto crece

### ¿CUÁNDO HACERLO?

**MEJOR MOMENTO:**
- ✅ Ahora, antes de que el proyecto crezca más
- ✅ Cuando no haya trabajo en progreso en múltiples branches
- ✅ Al inicio de un nuevo sprint/fase

**NO HACERLO SI:**
- ❌ Hay cambios importantes sin commitear
- ❌ Múltiples personas trabajando en layouts simultáneamente
- ❌ Estás en medio de una release crítica

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### Antes de Empezar:
- [ ] Hacer commit de todos los cambios actuales
- [ ] Crear branch nuevo: `git checkout -b feature/reorganize-layouts`
- [ ] Hacer backup del proyecto

### Renombrar Layouts:
- [ ] Admin (7 archivos)
- [ ] Coach (3 archivos)
- [ ] User (1 archivo)
- [ ] Shared (5 archivos)

### Actualizar Código Java:
- [ ] AdminHomeActivity.java
- [ ] CreateEventActivity.java
- [ ] CreateVenueActivity.java
- [ ] ManageVenuesActivity.java
- [ ] CoachesActivity.java
- [ ] ParticipantsActivity.java
- [ ] MaintenanceActivity.java
- [ ] CoachHomeActivity.java
- [ ] ActivitiesListActivity.java
- [ ] ActivityDetailActivity.java
- [ ] UserHomeActivity.java
- [ ] EventsActivity.java
- [ ] EventDetailActivity.java
- [ ] NotificationsActivity.java
- [ ] AttendanceActivity.java
- [ ] MapPickerActivity.java

### Testing:
- [ ] ./gradlew clean build
- [ ] Instalar en emulador/dispositivo
- [ ] Probar login como Admin
- [ ] Probar login como Coach
- [ ] Probar login como User
- [ ] Verificar que todas las vistas cargan correctamente

### Finalizar:
- [ ] Commit: `git commit -m "refactor: Reorganize layouts with role-based prefixes"`
- [ ] Push: `git push origin feature/reorganize-layouts`
- [ ] Crear Pull Request
- [ ] Merge a main después de testing

---

## 📝 ALTERNATIVA: Solo Documentar Sin Renombrar

Si prefieres **NO hacer cambios**, puedes simplemente:

1. **Crear un documento** `LAYOUT_ORGANIZATION.md` que liste qué layout pertenece a qué rol
2. **Agregar comentarios** en cada Activity indicando el rol
3. **Usar tags en Git** para marcar layouts por rol

**Ejemplo de comentario:**
```java
/**
 * AdminHomeActivity
 *
 * Pantalla principal del ADMINISTRADOR.
 * Layout: activity_admin_home.xml
 * Roles permitidos: admin
 */
public class AdminHomeActivity extends AppCompatActivity {
    // ...
}
```

---

**¿Quieres que proceda con el renombrado? Si sí, puedo hacerlo automáticamente con un script.**
