# Clinical Competency & Medical Board Assessment - Refined R8 / ProGuard Rules
# Optimized for maximum code shrinking in R8 full mode while preserving runtime reflection entry points.

# Preserve stack trace line numbers while normalizing source file name
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations, generic signatures, and inner class metadata needed by Room, Firestore, and Coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Strip verbose and debug logs in production release builds (preserve Log.i / Log.w / Log.e)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Preserve @Keep annotated classes and members (Firestore CloudModels, runtime hooks)
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {
    <init>(...);
    <fields>;
    <methods>;
}
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}

# Preserve Firestore POJO serialization annotations & no-arg constructors without keeping unused helper methods
-keepclassmembers class com.example.data.remote.UserAccountCloud,
    com.example.data.remote.CloudProgressDoc,
    com.example.data.remote.CloudNoteDoc {
    <init>(...);
}
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.DocumentId <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.ServerTimestamp <methods>;
}

# Preserve Room database implementations, entities, DAOs, and SQL projection tuples
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * {
    <init>(...);
}
-keep @androidx.room.Dao interface *
-keepclassmembers class com.example.data.dao.*Tuple {
    <init>(...);
    <fields>;
}

# Preserve Credential Manager & Google ID token credential reflection hooks without disabling shrinking on all Google packages
-keepnames class androidx.credentials.playservices.CredentialProviderPlayServicesImpl
-keepclassmembers class com.google.android.libraries.identity.googleid.GoogleIdTokenCredential {
    *;
}

# Preserve Coroutines exception handlers discovered via ServiceLoader
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keepclassmembers class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# Preserve Firebase App Check, reCAPTCHA Enterprise SDK & Firebase AI Logic reflection/provider hooks
-keep class com.google.firebase.appcheck.** { *; }
-keep interface com.google.firebase.appcheck.** { *; }
-keep class com.google.android.recaptcha.** { *; }
-keep class com.google.firebase.ai.** { *; }
-keepnames class com.example.data.remote.MedicalTranslationService
-keepclassmembers class com.example.data.remote.AppCheckInitializerKt { *; }

