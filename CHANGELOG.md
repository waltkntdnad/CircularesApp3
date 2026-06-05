# Historial de versiones

## Versión final 1 (base)
Estado base entregado y validado:
- Acceso diferenciado: solo el administrador inicia sesión (`admin` / `admin123`, configurable).
- Carga de circulares en imagen (JPG/PNG) o PDF a Firebase Storage.
- Visualización automática en tiempo real para todos los dispositivos (listener de Firestore).
- Repetición de la visualización cada 4 horas durante 2 días (WorkManager + fecha de expiración).
- Reportes y estadísticas del administrador (total, visualizaciones, estado, fechas).
- Push instantáneo con FCM mediante Cloud Function (`functions/`), apto para plan Blaze.
- Compatible desde Android 8.0 (API 26).

## Versión 1.1 (mejoras sobre la v1)
Añadido a partir de la versión final 1:

1. **Eliminar publicaciones.** Desde Reportes, cada circular tiene el botón *Eliminar*,
   con diálogo de confirmación. Borra el documento en Firestore y el archivo en Storage.
   (Repositorio: `eliminarCircular`; UI: `ReportsActivity` + `ReportAdapter`.)

2. **Desactivar / activar publicaciones.** Nuevo campo `activa` en el modelo `Circular`.
   El botón *Desactivar/Activar* en Reportes alterna el estado. Una circular desactivada
   deja de mostrarse a los usuarios aunque siga dentro de sus 2 días, y se puede reactivar.
   (Repositorio: `establecerActiva`; el feed filtra por `activa`.)

3. **Previsualizar antes de publicar.** En la pantalla de carga, al seleccionar el archivo
   se muestra una vista previa en línea (imagen real o icono de PDF) y un botón
   *Abrir archivo* que lo abre a pantalla completa con el visor del dispositivo.
   También se puede previsualizar cualquier circular ya publicada desde Reportes.
   (UI: `UploadActivity`.)

4. **Zoom para los usuarios.** El visor (`ViewerActivity`) ahora permite hacer zoom con
   pellizco (pinch) y doble toque, tanto en imágenes como en PDF, usando el zoom nativo
   del WebView (sin librerías externas).

### Nota de migración (importante)
El nuevo campo `activa` tiene valor por defecto `true`, por lo que las circulares creadas
antes de esta versión (que no tienen el campo en Firestore) se interpretan como activas y
seguirán mostrándose con normalidad. No se requiere ninguna migración manual de datos.
