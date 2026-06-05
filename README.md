# Circulares — Difusión de circulares e instructivos

Aplicación nativa Android para difundir circulares e instructivos a todos los
dispositivos donde esté instalada. Solo el **administrador** inicia sesión para
publicar; el resto de usuarios únicamente instala la app y visualiza el contenido.

Desarrollada en **Kotlin** con arquitectura **MVVM**, **Firebase Firestore** y
**Storage** para datos y archivos, y **WorkManager** para la repetición programada
de la visualización.

## Características

- **Acceso diferenciado:** los usuarios no se registran ni inician sesión; solo el administrador lo hace (usuario/contraseña configurables en código o en pantalla).
- **Carga de archivos:** el administrador sube imágenes (JPG/PNG) o documentos PDF.
- **Visualización automática:** cada archivo nuevo aparece automáticamente en todos los dispositivos (listener en tiempo real de Firestore).
- **Repetición programada:** la visualización se reactiva cada **4 horas** durante **2 días** desde la publicación (WorkManager + fecha de expiración).
- **Reportes:** sección exclusiva del administrador con total de circulares, fecha de envío, número de visualizaciones y estado de difusión.
- **Compatibilidad:** desde **Android 8.0 (API 26)** hasta versiones recientes.

## Estructura del proyecto

```
CircularesApp/
├── app/
│   ├── google-services.json        #  de Firebase
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/circulares/difusion/
│       │   ├── CircularesApplication.kt
│       │   ├── data/model/Circular.kt
│       │   ├── data/repository/        # CircularRepository, AuthRepository, SessionManager
│       │   ├── ui/main/                # Pantalla principal (todos los usuarios)
│       │   ├── ui/login/               # Login del administrador
│       │   ├── ui/admin/               # Panel + configuración de credenciales
│       │   ├── ui/upload/              # Carga de archivos
│       │   ├── ui/reports/             # Reportes y estadísticas
│       │   ├── ui/viewer/              # Visor de imagen / PDF
│       │   ├── worker/                 # WorkManager (repetición cada 4h)
│       │   ├── notifications/          # Canal + FCM opcional
│       │   └── util/                   # Config, Resultado, FechaUtil
│       └── res/                        # layouts, drawables, themes, strings
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/ (wrapper incluido)
```


## Tecnologías

Kotlin · MVVM (ViewModel + LiveData) · Coroutines · Firebase Firestore · Firebase Storage · Firebase Cloud Messaging (opcional) · WorkManager · Material Design 3 · Glide · DataStore · ViewBinding.

## Credenciales por defecto

- Usuario: `admin`
- Contraseña: `admin123`

Cámbialas en `util/Config.kt` o desde la pantalla de **Configuración** del panel de administrador.
