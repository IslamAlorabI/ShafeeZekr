# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# -- GSON Rules --
# Gson uses generic type information stored in a class file when working with fields. ProGuard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
# Adjust this to match your package name for model/data classes
-keep class islamalorabi.shafeezekr.pbuh.data.model.** { *; }
-keep class islamalorabi.shafeezekr.pbuh.data.remote.** { *; }

# Prevent ProGuard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# -- Retrofit Rules --
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod to recreate the codebase.
-keepattributes InnerClasses, EnclosingMethod

# Retrofit main classes (though usually included in consumer proguard rules of the library)
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# -- Android/Compose General Rules --
# Often redundant if using consumer rules, but good for safety
-keep class androidx.appcompat.widget.** { *; }

# -- WorkManager / Room (required by Glance) --
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}
-keep class **_Impl { *; }

# -- Glance --
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }

# -- App widget & audio classes --
-keep class islamalorabi.shafeezekr.pbuh.widget.** { *; }
-keep class islamalorabi.shafeezekr.pbuh.util.AudioHelper { *; }