# Configuracion del Sistema de Autenticacion - GESDEP

## Descripcion General

El sistema implementa autenticacion con 3 tipos de roles de usuario:
1. **Participante (user)** - Usuarios regulares que participan en eventos
2. **Entrenador (coach)** - Instructores que gestionan grupos y horarios
3. **Organizador (admin)** - Administradores que gestionan eventos y usuarios

## Estructura de Firebase

### 1. Firebase Authentication
Ya configurado en el proyecto. Los usuarios se crean con email y contraseña.

### 2. Firestore Database

Crear la coleccion **users** con la siguiente estructura de documento:

```
users (coleccion)
  └── {uid} (documento)
      ├── uid: string
      ├── name: string
      ├── email: string
      ├── role: string ("user" | "coach" | "admin")
      ├── createdAt: number (timestamp)
      ├── active: boolean
      ├── eventsOrganized: number
      └── eventsParticipated: number
```

### 3. Reglas de Seguridad de Firestore

En Firebase Console > Firestore Database > Rules, configurar:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Permitir lectura/escritura del documento de usuario solo al usuario autenticado
    match /users/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow create: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null && request.auth.uid == userId;
    }
    
    // Permitir a admins leer todos los usuarios
    match /users/{userId} {
      allow read: if request.auth != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
  }
}
```

## Flujo de Autenticacion

### Registro de Usuario

1. Usuario abre la app → **WelcomeActivity**
2. Click en "Crear Cuenta" → **RegisterActivity**
3. Completa formulario:
   - Nombre completo
   - Email
   - Contraseña
   - Confirmar contraseña
   - Tipo de usuario (Participante/Entrenador/Organizador)
   - Codigo de organizacion (solo para Entrenador y Organizador)

4. Validaciones:
   - Campos no vacios
   - Contraseña minimo 6 caracteres
   - Contraseñas coinciden
   - Codigo correcto para coach/admin: **IMCUFIDE2025**

5. Si todo es correcto:
   - Se crea usuario en Firebase Authentication
   - Se crea documento en Firestore coleccion "users"
   - Se redirige automaticamente a la pantalla home segun su rol

### Login de Usuario

1. Usuario abre la app → **WelcomeActivity**
2. Si ya esta autenticado, se redirige automaticamente
3. Si no, click en "Iniciar Sesion" → **LoginActivity**
4. Ingresa email y contraseña
5. Sistema valida credenciales con Firebase Auth
6. Lee documento del usuario en Firestore para obtener el rol
7. Redirige a la pantalla correspondiente:
   - role="admin" → **AdminHomeActivity**
   - role="coach" → **CoachHomeActivity**
   - role="user" → **UserHomeActivity**

## Pantallas por Rol

### Admin Home (Organizador)
- Gestion de eventos
- Creacion de eventos
- Lista de participantes
- Lista de entrenadores
- Reportes
- Configuracion

### Coach Home (Entrenador)
- Mis grupos
- Mi horario
- Mi perfil
- Cerrar sesion

### User Home (Participante)
- Explorar eventos
- Mis inscripciones
- Mi perfil
- Cerrar sesion

## Configuracion Necesaria en Firebase Console

### Paso 1: Habilitar Authentication
1. Ir a Firebase Console > Authentication
2. Click en "Get Started"
3. En "Sign-in method", habilitar "Email/Password"
4. Guardar cambios

### Paso 2: Crear Firestore Database
1. Ir a Firebase Console > Firestore Database
2. Click en "Create database"
3. Seleccionar modo "Production" (usaremos reglas personalizadas)
4. Elegir ubicacion (preferiblemente us-central1 para Latinoamerica)
5. Click en "Enable"

### Paso 3: Configurar Reglas de Seguridad
1. En Firestore Database, ir a tab "Rules"
2. Reemplazar con las reglas mostradas arriba
3. Click en "Publish"

### Paso 4: Verificar google-services.json
El archivo ya existe en `app/google-services.json`
Si necesitas actualizarlo:
1. Ir a Project Settings en Firebase Console
2. Descargar google-services.json
3. Reemplazar el archivo en app/google-services.json

## Testing del Sistema

### Crear Usuario de Prueba

#### Participante
```
Email: participante@test.com
Contraseña: test123
Tipo: Participante
Codigo: (no requerido)
```

#### Entrenador
```
Email: coach@test.com
Contraseña: test123
Tipo: Entrenador
Codigo: IMCUFIDE2025
```

#### Organizador
```
Email: admin@test.com
Contraseña: test123
Tipo: Organizador
Codigo: IMCUFIDE2025
```

### Verificar Creacion en Firebase
1. Ir a Firebase Console > Authentication
2. Verificar que aparece el usuario creado
3. Ir a Firestore Database
4. Verificar que existe documento en users/{uid} con los campos correctos

## Archivos Principales del Sistema

### Activities
- **WelcomeActivity.java** - Pantalla inicial con login/registro
- **RegisterActivity.java** - Formulario de registro
- **LoginActivity.java** - Formulario de login
- **AdminHomeActivity.java** - Panel de administrador
- **CoachHomeActivity.java** - Panel de entrenador
- **UserHomeActivity.java** - Panel de participante

### Layouts
- **activity_welcome.xml** - Layout de bienvenida
- **activity_register.xml** - Formulario de registro
- **activity_login.xml** - Formulario de login
- **activity_admin_home.xml** - Dashboard admin
- **activity_coach_home.xml** - Dashboard coach
- **activity_user_home.xml** - Dashboard user

### Menus
- **drawer_admin.xml** - Menu de navegacion para admin
- **drawer_coach.xml** - Menu de navegacion para coach
- **drawer_user.xml** - Menu de navegacion para user

## Notas Importantes

1. **Codigo de Organizacion**: Solo los usuarios con codigo IMCUFIDE2025 pueden registrarse como Entrenador u Organizador

2. **Sesion Persistente**: La sesion se mantiene activa hasta que el usuario cierre sesion manualmente

3. **Validacion de Email**: Firebase valida automaticamente el formato del email

4. **Seguridad**: Las contraseñas son hasheadas por Firebase Authentication

5. **Estructura Escalable**: El sistema esta diseñado para agregar mas roles en el futuro

## Proximos Pasos

1. Implementar funcionalidad completa de EventsActivity
2. Implementar funcionalidad completa de CreateEventActivity
3. Agregar edicion de perfil
4. Implementar sistema de grupos para coaches
5. Implementar sistema de horarios
6. Agregar notificaciones push con FCM
