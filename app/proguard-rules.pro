# ProGuard rules for RecyScan

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ZXing
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }

# Material Components
-keep class com.google.android.material.** { *; }

# Keep model classes
-keep class com.recyscan.models.** { *; }
