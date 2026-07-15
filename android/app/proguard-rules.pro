# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class retrofit2.** { *; }
-keepattributes Signature

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class ** { @com.google.gson.annotations.SerializedName *; }

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Keep data classes
-keep class com.astrochart.** { *; }
-keepclassmembers class com.astrochart.** { *; }

# General
-keepattributes EnclosingMethod
-keepattributes InnerClasses
