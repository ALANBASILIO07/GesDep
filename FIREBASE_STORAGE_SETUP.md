# Firebase Storage - Solución de Configuración

## Problema Actual
Firebase Storage no se pudo crear debido a problemas de conexión.

## Solución: Configurar Storage Manualmente

### Opción 1: Reintentar desde Firebase Console (Recomendado)

1. Ir a Firebase Console: https://console.firebase.google.com/project/gesdep-90334
2. Click en **Compilación → Storage**
3. Click en **Comenzar**
4. Seleccionar modo:
   - **Modo de producción** (recomendado para seguridad)
   - Ubicación: **us-central1** (o la más cercana a México)
5. Click en **Listo**

### Opción 2: Si persiste el error de conexión

**Storage NO es crítico para la Fase 4 actual**, solo será necesario para:
- Fase 8: Subir fotos y videos de eventos
- Perfiles de usuario con fotos

**Puedes continuar sin Storage por ahora** y configurarlo más adelante.

---

## Reglas de Seguridad para Storage

Cuando logres crear Storage, usa estas reglas:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Fotos de eventos
    match /events/{eventId}/{fileName} {
      allow read: if true;  // Todos pueden ver
      allow write: if request.auth != null;  // Solo usuarios autenticados pueden subir
    }
    
    // Fotos de perfil
    match /users/{userId}/profile/{fileName} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Videos de eventos
    match /events/{eventId}/videos/{fileName} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

---

## Verificar que Storage funciona

Una vez configurado, prueba con este código en Android Studio:

```java
// En cualquier Activity
FirebaseStorage storage = FirebaseStorage.getInstance();
StorageReference storageRef = storage.getReference();

// Esto debe funcionar sin errores
Log.d("Storage", "Storage configurado correctamente: " + storageRef.toString());
```

---

## Estado Actual del Proyecto

✅ **Funcionando SIN Storage:**
- Autenticación (Firebase Auth)
- Base de datos (Firestore)
- Notificaciones (FCM)
- Lista de eventos
- Filtros y navegación

⏳ **Necesitará Storage en el futuro:**
- Subir fotos de eventos
- Fotos de perfil
- Videos de eventos

**Conclusión:** Puedes seguir desarrollando sin problemas. Storage se configurará cuando tengas mejor conexión.

---

Fecha: 26 de Noviembre, 2025
