# Add project specific ProGuard rules here.
-keep class com.sony.braviaremote.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
