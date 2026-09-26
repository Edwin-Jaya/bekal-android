# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in getDefaultProguardFile('proguard-android-optimize.txt').

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowoptimization class * {
    @kotlinx.serialization.Serializable class *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep Data Transfer Objects (DTO)
-keep class com.edwin.bekal.data.dto.** { *; }

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# Room
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase

# Coil
-dontwarn coil.**

# Firebase Crashlytics Buildtools & relocated dependencies
-dontwarn com.google.firebase.crashlytics.buildtools.**
-dontwarn javax.servlet.**
-dontwarn org.ietf.jgss.**
