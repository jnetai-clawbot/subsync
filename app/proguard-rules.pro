# Add project specific ProGuard rules here.

-keepattributes *Annotation*
-keep class com.jnetaol.subsync.data.model.** { *; }
-keep class com.jnetaol.subsync.engine.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
