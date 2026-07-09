# ============================================================
# ProGuard / R8 Rules untuk GempaID
# ============================================================

# Keep Kotlin Serialization — jangan di-obfuscate class yang di-serialize
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep semua class DTO (Data Transfer Objects) dari network
-keep class com.gempa.id.core.data.remote.dto.** { *; }

# Keep Room entities
-keep class com.gempa.id.core.data.local.entity.** { *; }

# Retrofit — keep interface methods
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Hilt
-dontwarn com.google.dagger.**
-keep class dagger.hilt.** { *; }

# Coil
-dontwarn coil.**
