# -------------------------------------------------------
# Debugging: preserve line numbers in crash stack traces
# -------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# -------------------------------------------------------
# Kotlin
# -------------------------------------------------------
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# -------------------------------------------------------
# Kotlinx Serialization
# -------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class com.subnetik.unlock.**$$serializer { *; }
-keepclassmembers class com.subnetik.unlock.** {
    *** Companion;
}
-keepclasseswithmembers class com.subnetik.unlock.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }

# -------------------------------------------------------
# Data / DTO classes — never obfuscate (used in JSON)
# -------------------------------------------------------
-keep class com.subnetik.unlock.data.remote.dto.** { *; }
-keep class com.subnetik.unlock.data.local.** { *; }

# -------------------------------------------------------
# Retrofit
# -------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# -------------------------------------------------------
# OkHttp
# -------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# -------------------------------------------------------
# Hilt / Dagger
# -------------------------------------------------------
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }

# -------------------------------------------------------
# Room
# -------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# -------------------------------------------------------
# Firebase / FCM
# -------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# -------------------------------------------------------
# Coil
# -------------------------------------------------------
-dontwarn coil.**

# -------------------------------------------------------
# ZXing (QR code)
# -------------------------------------------------------
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**