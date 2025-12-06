# Instrucciones para Agregar Reporte de Prueba

## Opción 1: Desde la Consola de Firebase (Más Fácil)

1. Ir a: https://console.firebase.google.com/
2. Seleccionar el proyecto **gesdep**
3. En el menú lateral, ir a **Firestore Database**
4. Click en **"Iniciar colección"** (o abrir colección existente `reports`)
5. ID de colección: `reports`
6. ID de documento: `TEST_REPORT_001`
7. Agregar los siguientes campos:

```
reportId: "TEST_REPORT_001" (string)
eventId: "GENERAL" (string)
eventName: "Aviso General" (string)
createdByUid: "admin_test" (string)
createdByName: "Administrador de Prueba" (string)
createdByRole: "admin" (string)
subject: "Mantenimiento de Cancha Principal" (string)
description: "Se requiere pintar las líneas de la cancha de básquetbol principal. Las marcas están desgastadas y dificultan el juego." (string)
photoUrls: [] (array vacío)
category: "Cancha sin pintar" (string)
priority: "BAJA" (string)
status: "Pendiente" (string)
adminResponse: null
respondedByUid: null
respondedByName: null
createdAt: 1733450400000 (number) - timestamp actual
updatedAt: 1733450400000 (number) - mismo timestamp
```

8. Click en **Guardar**

## Opción 2: Desde la App

1. Abrir la app GESDEP
2. Iniciar sesión como **Administrador**
3. En el Dashboard, click en **"Enviar Aviso General"** (Acciones Rápidas)
4. Llenar el formulario:
   - Asunto: "Mantenimiento de Cancha Principal"
   - Categoría: "Cancha sin pintar"
   - Descripción: "Se requiere pintar las líneas..."
5. Click en **"Enviar Reporte"**

## Opción 3: Usando Script Node.js

1. Asegurarse de tener Node.js instalado
2. Instalar Firebase Admin SDK:
   ```bash
   npm install firebase-admin
   ```
3. Descargar Service Account Key desde Firebase Console
4. Ejecutar el script:
   ```bash
   node add_test_report.js
   ```

## Verificación

Después de agregar el reporte:

1. Abrir la app GESDEP
2. Iniciar sesión como Admin
3. Ir a **Menú Lateral → Reportes y Mantenimiento**
4. Deberías ver el reporte de prueba listado
5. Click en el reporte para ver los detalles

---

## Ejemplo de JSON Completo para Copiar/Pegar

```json
{
  "reportId": "TEST_REPORT_001",
  "eventId": "GENERAL",
  "eventName": "Aviso General",
  "createdByUid": "admin_test",
  "createdByName": "Administrador de Prueba",
  "createdByRole": "admin",
  "subject": "Mantenimiento de Cancha Principal",
  "description": "Se requiere pintar las líneas de la cancha de básquetbol principal. Las marcas están desgastadas y dificultan el juego.",
  "photoUrls": [],
  "category": "Cancha sin pintar",
  "priority": "BAJA",
  "status": "Pendiente",
  "adminResponse": null,
  "respondedByUid": null,
  "respondedByName": null,
  "createdAt": 1733450400000,
  "updatedAt": 1733450400000
}
```

Simplemente copiar este JSON y pegarlo en la consola de Firebase al crear un nuevo documento.
