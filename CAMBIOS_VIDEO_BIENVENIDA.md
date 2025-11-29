# Cambios Realizados - Video de Fondo en Pantalla de Bienvenida

## Descripcion

Se ha restaurado y mejorado el diseño de la pantalla de bienvenida con video de fondo animado.

## Archivos Modificados

### 1. FullScreenVideoView.java
**Ubicacion:** app/src/main/java/com/uaemex/gesdep/FullScreenVideoView.java

**Cambios:**
- Implementado calculo de escala para cubrir toda la pantalla
- El video se ajusta automaticamente sin dejar espacios negros
- Se recorta proporcionalmente si es necesario
- Metodo setVideoSize() para actualizar dimensiones dinamicamente

**Codigo clave:**
```java
// Calcula si el video debe ajustarse por ancho o alto
float videoRatio = (float) videoWidth / videoHeight;
float screenRatio = (float) widthSize / heightSize;

if (videoRatio > screenRatio) {
    // Video mas ancho, ajustar por altura
    width = (int) (heightSize * videoRatio);
    height = heightSize;
} else {
    // Video mas alto, ajustar por ancho
    width = widthSize;
    height = (int) (widthSize / videoRatio);
}
```

### 2. activity_welcome.xml
**Ubicacion:** app/src/main/res/layout/activity_welcome.xml

**Cambios:**
- Estructura cambiada de LinearLayout a FrameLayout para capas
- Agregado FullScreenVideoView como capa de fondo
- Agregado filtro oscuro transparente (80% opacidad negro: #80000000)
- Textos cambiados a color blanco para contraste
- Botones actualizados con borde blanco

**Estructura de capas:**
1. FullScreenVideoView (fondo)
2. View con filtro oscuro (#80000000)
3. LinearLayout con contenido (textos y botones)

### 3. WelcomeActivity.java
**Ubicacion:** app/src/main/java/com/uaemex/gesdep/WelcomeActivity.java

**Cambios:**
- Agregado campo FullScreenVideoView
- Implementado setupBackgroundVideo() para configurar reproduccion
- Video en loop continuo sin sonido
- Manejo de errores para continuar sin video si falla
- Refactorizado onCreate() para evitar duplicacion
- Agregado setupWelcomeScreen() para inicializar UI
- Ciclo de vida: onResume() retoma video, onPause() pausa video

**Configuracion del video:**
```java
// Video en loop sin sonido
mp.setLooping(true);
mp.setVolume(0f, 0f);

// Configurar dimensiones para escala correcta
videoView.setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
```

## Características Implementadas

### Video de Fondo
- Reproduce video1.mp4 de la carpeta res/raw
- Loop infinito sin sonido
- Escala automática para cubrir pantalla completa
- Sin espacios negros (crop proporcional si es necesario)

### Filtro Oscuro
- Opacidad: 50% (valor #80 en hexadecimal)
- Color: Negro (#000000)
- Codigo completo: #80000000
- Permite leer texto blanco sobre el video

### Responsive
- Se adapta a cualquier tamaño de pantalla
- Mantiene proporcion del video
- Recorta inteligentemente para llenar pantalla

### Manejo de Errores
- Si el video falla al cargar, la app continua funcionando
- Fallback gracioso sin crashes

## Videos Disponibles

Carpeta: app/src/main/res/raw/

- video1.mp4 (26 MB) - Actualmente en uso
- video2.mp4 (2.8 MB)
- video3.mp4 (20 MB)
- video4.mp4 (53 MB)
- video5.mp4 (12 MB)
- video6.mp4 (34 MB)

Para cambiar el video, modificar en WelcomeActivity.java linea 59:
```java
Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video1);
```

Cambiar R.raw.video1 por R.raw.video2, video3, etc.

## Flujo de la Aplicacion

1. Usuario abre app
2. WelcomeActivity verifica autenticacion
3. Si esta autenticado:
   - Consulta rol en Firestore
   - Redirige a home correspondiente
4. Si no esta autenticado:
   - Muestra pantalla de bienvenida
   - Inicia reproduccion de video
   - Usuario puede Login o Registro

## Diagnostico de Problemas

### La app se cierra inmediatamente

**Posibles causas:**
1. Firebase no configurado correctamente
2. Error en google-services.json
3. Firestore no habilitado

**Solucion:**
- Verificar google-services.json en app/
- Habilitar Authentication en Firebase Console
- Habilitar Firestore Database

### El video no se reproduce

**Posibles causas:**
1. Archivo de video corrupto
2. Formato no soportado
3. Permisos insuficientes

**Solucion:**
- Verificar que el archivo existe en res/raw/
- Intentar con otro video (video2, video3, etc.)
- La app continua funcionando sin el video

### Texto no visible

**Causa:** Filtro muy oscuro o video muy claro

**Solucion:** Ajustar opacidad del filtro en activity_welcome.xml linea 18:
```xml
android:background="#80000000"
```
- #80 = 50% opacidad
- #B0 = 69% opacidad (mas oscuro)
- #60 = 38% opacidad (mas claro)

## Compilacion

Proyecto compila exitosamente:
```
BUILD SUCCESSFUL in 2s
37 actionable tasks: 15 executed, 22 up-to-date
```

## Testing Recomendado

1. Abrir app en dispositivo/emulador
2. Verificar que el video se reproduce
3. Verificar que los textos son legibles
4. Verificar que los botones funcionan
5. Probar rotacion de pantalla
6. Verificar que el video se pausa/retoma correctamente

## Notas Tecnicas

- El filtro oscuro usa alpha channel en hexadecimal (#AARRGGBB)
- VideoView se extiende para crear FullScreenVideoView personalizado
- El video se centra y escala usando gravity="center"
- La reproduccion usa MediaPlayer con callbacks
- El ciclo de vida de Android se maneja correctamente (onPause/onResume)
