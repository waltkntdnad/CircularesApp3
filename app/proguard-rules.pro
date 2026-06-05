# Reglas ProGuard del proyecto Circulares

# Mantener modelos usados por Firestore (deserialización por reflexión)
-keep class com.circulares.difusion.data.model.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
