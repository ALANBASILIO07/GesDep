# RESUMEN DE CAMBIOS - SINCRONIZACION FIREBASE

## CAMBIOS REALIZADOS AUTOMATICAMENTE

### 1. Package Name
- Anterior: com.example.gesdep
- Nuevo: com.uaemex.gesdep
- Actualizado en: build.gradle.kts y todos los archivos Java

### 2. google-services.json
- Copiado a: app/google-services.json
- Project ID: gesdep-90334
- Project Number: 479735260140

### 3. Application ID
- Actualizado en app/build.gradle.kts
- applicationId = "com.uaemex.gesdep"
- namespace = "com.uaemex.gesdep"

## VERIFICAR EN FIREBASE CONSOLE

1. Cloud Messaging - HABILITAR
2. Firestore Database - CREAR
3. Storage - CONFIGURAR
4. Authentication - Email/Password HABILITADO

## QUE HACER EN ANDROID STUDIO

1. File -> Sync Project with Gradle Files
2. Build -> Clean Project
3. Build -> Rebuild Project
4. Verificar que compile sin errores

## ARCHIVOS MODIFICADOS
- app/build.gradle.kts
- Todos los .java (package declarations)
- app/google-services.json (nuevo)
