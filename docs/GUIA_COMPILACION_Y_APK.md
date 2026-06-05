# Guía de configuración, compilación y generación del APK

Aplicación **Circulares** — difusión de circulares e instructivos.
Kotlin + MVVM + Firebase (Firestore y Storage) + WorkManager.

---

## 1. Requisitos previos

| Herramienta | Versión recomendada |
|---|---|
| Android Studio | Ladybug (2024.2) o más reciente |
| JDK | 17 (incluido en Android Studio) |
| Android SDK | API 34 instalada + API 26 mínima |
| Cuenta Google | Para Firebase (gratis, plan Spark) |

Min SDK: **26 (Android 8.0)**. Target SDK: **34**.

---

## 2. Crear el proyecto en Firebase

1. Entra a https://console.firebase.google.com y pulsa **Agregar proyecto**.
2. Nombre sugerido: `circulares` (acepta los términos, Analytics es opcional).
3. Dentro del proyecto, pulsa el icono de **Android** para registrar la app:
   - **Nombre del paquete:** `com.circulares.difusion` (debe coincidir EXACTAMENTE con el `applicationId` del proyecto).
   - Apodo y SHA-1: opcionales para esta app (no usamos login de Google).
4. Pulsa **Registrar app** y **Descarga `google-services.json`**.
5. Copia ese archivo dentro de la carpeta **`app/`** del proyecto, **reemplazando** el `google-services.json` de ejemplo que viene incluido.

> Sin el `google-services.json` real, la app compila pero **no conecta** con Firebase.

### 2.1 Habilitar Firestore

1. En la consola, menú lateral → **Compilación → Firestore Database → Crear base de datos**.
2. Elige modo **producción** y una ubicación cercana (ej. `us-central` o `southamerica-east1`).
3. Ve a la pestaña **Reglas** y pega estas reglas (lectura pública, escritura solo autenticada-por-app; ajústalas a tu política de seguridad):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /circulares/{doc} {
      // Todos pueden leer las circulares (los usuarios no inician sesión)
      allow read: if true;
      // La escritura la realiza el administrador desde la app.
      // Para una app interna controlada, puedes permitir escritura:
      allow write: if true;
    }
  }
}
```

> **Nota de seguridad:** `allow write: if true` permite escribir a cualquiera con la app. Es aceptable para una app interna/piloto. Para producción real, integra Firebase Authentication y restringe la escritura a un usuario administrador. La guía de mejoras está al final.

### 2.2 Habilitar Storage

1. Menú lateral → **Compilación → Storage → Comenzar**.
2. Acepta las reglas iniciales y elige la misma ubicación.
3. En **Reglas**, usa algo equivalente:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /circulares/{archivo} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

### 2.3 (Opcional) Cloud Messaging

No es necesario para el funcionamiento básico. La difusión automática ya funciona con el listener en tiempo real de Firestore (app abierta) y con WorkManager (segundo plano, cada 4 h). Si quieres push instantáneo con la app cerrada, habilita **Cloud Messaging** y revisa la sección 8.

---

## 3. Abrir e importar el proyecto en Android Studio

1. Descomprime `CircularesApp.zip`.
2. Android Studio → **File → Open** → selecciona la carpeta `CircularesApp`.
3. Espera a que Gradle sincronice (**Gradle Sync**). La primera vez descargará dependencias; necesita internet.
4. Si aparece "Gradle wrapper", acéptalo. El proyecto ya incluye `gradle-wrapper.jar`, por lo que la sincronización debería ser directa.

> Si la sincronización falla por el wrapper, ejecuta en una terminal dentro del proyecto: `gradle wrapper --gradle-version 8.7` (requiere Gradle instalado) y vuelve a abrir.

---

## 4. Credenciales del administrador

Por defecto:

- **Usuario:** `admin`
- **Contraseña:** `admin123`

Para cambiarlas tienes dos opciones:

1. **En el código:** edita `app/src/main/java/com/circulares/difusion/util/Config.kt`, campos `ADMIN_USER` y `ADMIN_PASSWORD`, y recompila.
2. **En la app:** inicia sesión, abre **Configuración** en el panel del administrador y guarda nuevas credenciales (se almacenan localmente con DataStore y tienen prioridad sobre las del código).

---

## 5. Ejecutar la app en un dispositivo o emulador

1. Conecta un dispositivo con **depuración USB** activada, o crea un emulador (**Device Manager**) con API 26+.
2. Pulsa el botón **Run ▶** (Shift+F10).
3. La app abre en la pantalla principal. Sin circulares aún, verás el mensaje de "no hay circulares vigentes".
4. Pulsa el botón **Administrador** → inicia sesión → **Subir circular** → elige una imagen o PDF y publica.
5. La circular aparecerá automáticamente en la pantalla principal de todos los dispositivos con la app instalada (mientras tengan internet).

---

## 6. Generar el APK de depuración (rápido, para pruebas)

**Desde Android Studio:**
- Menú **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
- Al terminar, aparece una notificación con enlace **locate**; el archivo está en:
  `app/build/outputs/apk/debug/app-debug.apk`

**Desde terminal:**
```bash
# Linux/macOS
./gradlew assembleDebug
# Windows
gradlew.bat assembleDebug
```

Este APK es instalable directamente para pruebas internas.

---

## 7. Generar el APK de release firmado (para distribuir)

Un APK de release debe ir **firmado**.

### 7.1 Crear un keystore (una sola vez)

```bash
keytool -genkey -v -keystore circulares.jks -keyalg RSA -keysize 2048 -validity 10000 -alias circulares
```
Responde a las preguntas y **guarda la contraseña**. Guarda `circulares.jks` fuera del control de versiones.

### 7.2 Firmar desde Android Studio (recomendado)

1. Menú **Build → Generate Signed Bundle / APK**.
2. Elige **APK** → **Next**.
3. **Key store path:** selecciona `circulares.jks`, escribe contraseñas y alias.
4. **Next** → marca **release** → marca las casillas V1 y V2 → **Finish**.
5. El APK firmado queda en `app/release/app-release.apk`.

### 7.3 Firmar por línea de comandos (alternativa)

Añade en `app/build.gradle.kts` dentro de `android { }`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../circulares.jks")
        storePassword = "TU_PASSWORD"
        keyAlias = "circulares"
        keyPassword = "TU_PASSWORD"
    }
}
buildTypes {
    getByName("release") {
        signingConfig = signingConfigs.getByName("release")
    }
}
```
Luego:
```bash
./gradlew assembleRelease
```
APK en `app/build/outputs/apk/release/app-release.apk`.

> Para subir a Google Play se recomienda un **AAB**: `./gradlew bundleRelease` (genera `app/build/outputs/bundle/release/app-release.aab`).

---

## 8. Push instantáneo con FCM (ya incluido — requiere plan Blaze)

El proyecto **ya trae implementado** el push instantáneo: cuando el administrador
publica una circular, todos los dispositivos reciben una notificación al instante,
incluso con la app cerrada. Esto lo logra una **Cloud Function** que escucha la
colección `circulares` y envía un mensaje FCM al topic `circulares`.

Lo que ya está en el código (no tienes que escribirlo):
- **Cliente:** `CircularesApplication` suscribe cada dispositivo al topic `circulares`, y `CircularMessagingService` recibe y muestra la notificación.
- **Servidor:** la carpeta `functions/` contiene la función `notificarCircular` (`functions/index.js`), su `package.json` y la configuración `firebase.json`.

Solo necesitas **desplegar** la función una vez. Como ya tienes el plan **Blaze**, estás listo.

### 8.1 Requisitos

- Plan **Blaze** activo (lo tienes).
- **Node.js 20** instalado en tu computadora: https://nodejs.org
- **Firebase CLI**:
  ```bash
  npm install -g firebase-tools
  firebase login
  ```

### 8.2 Habilitar la API de Cloud Messaging

En la consola de Firebase → **⚙️ Configuración del proyecto → Cloud Messaging**, asegúrate de que la **Firebase Cloud Messaging API (V1)** esté **habilitada**. Si aparece deshabilitada, actívala desde el enlace que lleva a Google Cloud.

### 8.3 Ajustar el ID del proyecto y la región

1. Abre `.firebaserc` (en la raíz del proyecto) y reemplaza `REEMPLAZAR_CON_TU_PROJECT_ID` por el **Project ID** real (lo ves en Configuración del proyecto, ej. `circulares-1a2b3`).
2. Abre `functions/index.js` y confirma que la región coincide con la de tu Firestore. Por defecto está `southamerica-east1`. Si creaste Firestore en otra región (ej. `us-central1`), cámbiala en la línea:
   ```javascript
   setGlobalOptions({ region: "southamerica-east1", maxInstances: 5 });
   ```

### 8.4 Instalar dependencias y desplegar

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

La primera vez, Firebase puede pedirte habilitar APIs adicionales (Cloud Build, Artifact Registry, Cloud Run): acepta. El despliegue tarda 1–3 minutos. Al terminar verás algo como `✔ functions[notificarCircular] deployed`.

### 8.5 (Opcional) Desplegar también las reglas desde la CLI

El proyecto incluye `firestore.rules` y `storage.rules`. Puedes publicarlas sin entrar a la consola:
```bash
firebase deploy --only firestore:rules,storage
```

### 8.6 Probar

1. Instala la app en uno o dos dispositivos y ábrela al menos una vez (para que se suscriban al topic; acepta el permiso de notificaciones en Android 13+).
2. Como administrador, sube una circular.
3. En segundos, los dispositivos reciben la notificación **aunque la app esté cerrada**. Al tocarla, se abre la app.

### 8.7 Ver registros / depurar

```bash
firebase functions:log
```
O en la consola: **Functions → notificarCircular → Registros**. Si no llega la notificación, revisa que la API de Cloud Messaging esté habilitada (8.2) y que los dispositivos se hayan suscrito (busca en Logcat el mensaje `FCM: Suscrito al topic 'circulares'`).

> **Costo:** con Blaze, esta función entra en la capa gratuita en uso normal (las primeras 2 millones de invocaciones/mes son sin costo). Para una app de circulares el costo será prácticamente cero.

---

## 9. Cómo se cumple cada requisito

| Requisito | Implementación |
|---|---|
| Solo el admin inicia sesión | `LoginActivity` + `AuthRepository`; el resto entra directo a `MainActivity`. |
| Credenciales en código o pantalla | `Config.kt` (código) + diálogo de Configuración (DataStore). |
| Carga de JPG/PNG/PDF | `UploadActivity` con `OpenDocument` filtrando esos MIME; sube a Storage. |
| Visualización automática | Listener en tiempo real de Firestore en `MainViewModel`; abre la circular más reciente al entrar. |
| Repetición cada 4 h por 2 días | `WorkScheduler` (periódico 4 h) + `fechaExpiracion` = publicación + 2 días. |
| Reportes y estadísticas | `ReportsActivity`: total, visualizaciones, activas/expiradas, fecha de envío y estado. |
| Firestore + Storage | `CircularRepository`. |
| WorkManager + notificaciones | `CircularDisplayWorker` + `NotificationHelper`. |
| Push instantáneo (app cerrada) | Cloud Function `notificarCircular` (`functions/`) + topic FCM `circulares` (ver sección 8). |
| Compatible Android 8.0+ | `minSdk = 26`. |

---

## 10. Solución de problemas

- **Gradle Sync falla:** revisa conexión a internet y que tengas el SDK API 34. **File → Invalidate Caches / Restart**.
- **La app no muestra circulares:** confirma que reemplazaste `google-services.json`, que Firestore y Storage están habilitados y que el dispositivo tiene internet.
- **No llegan notificaciones (Android 13+):** acepta el permiso de notificaciones que pide la app al abrir.
- **WorkManager no se ejecuta exacto a las 4 h:** es normal; el sistema agrupa tareas para ahorrar batería (mínimo garantizado, no exacto). El periodo de 2 días sí se respeta por fecha de expiración.
- **PDF no se ve:** el visor usa Google Docs y requiere internet; verifica la URL del archivo en Storage.
