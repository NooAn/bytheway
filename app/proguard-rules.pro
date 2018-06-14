#  Uncomment this to preserve the line number information for
#  debugging stack traces.
 -keepattributes SourceFile,LineNumberTable

#  Hide the original source file name.
#-renamesourcefileattribute SourceFile


#LocalClasses
-keep public class ru.a1024bits.bytheway.model.User.**
-keep public interface ru.a1024bits.bytheway.model.User.**
-keep public enum ru.a1024bits.bytheway.model.User.**
-keepclassmembers class ru.a1024bits.bytheway.model.** { *; }

# RxJava
-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}


# VK
-keep class com.vk.* { *; }
-dontwarn org.seamless.
-dontwarn org.eclipse.jetty.**
-dontwarn org.fourthline.**
-dontshrink
-dontoptimize


# Firebase
-keep class com.firebase.** { *; }
-keep class com.google.firebase.example.fireeats.model.** { *; }


# Support library
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
-keep class android.support.v7.widget.RoundRectDrawable { *; }


# Support Design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }


# Support Constraint
-dontwarn android.support.constraint.**
-keep class android.support.constraint.** { *; }
-keep interface android.support.constraint.** { *; }
-keep public class android.support.constraint.R$* { *; }


# Maps-utils
-dontwarn com.google.maps.android.geojson.**


# Maps
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-keep class android.location.** { *; }


-keepnames class com.google.android.maps.** {*;}
-keep public class com.google.android.maps.** {*;}
-dontwarn com.google.android.maps.GeoPoint
-dontwarn com.google.android.maps.MapActivity
-dontwarn com.google.android.maps.MapView
-dontwarn com.google.android.maps.MapController
-dontwarn com.google.android.maps.Overlay


# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}


# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


# Dagger
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

-keep class dagger.* { *; }
-keep class javax.inject.* { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection


# GlaspHish Annotations
-dontwarn org.glassfish.***
-keep class org.glassfish.** {*;}
-keep interface org.glassfish.** {*;}
-keep class * implements org.glassfish.** {*;}
-keepattributes Signature,*Annotation*,EnclosingMethod

-keep class javax.** {*;}
-keep interface javax.** {*;}
-keep class * implements javax.** {*;}
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, *Annotation*, EnclosingMethod
-dontoptimize


# Android architecture components:
# Lifecycle
-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
    <init>(...);
}
# keep Lifecycle State and Event enums values
-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @android.arch.lifecycle.OnLifecycleEvent *;
}

-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}

-keep class * implements android.arch.lifecycle.LifecycleObserver {
    <init>(...);
}
-keepclassmembers class android.arch.** { *; }
-keep class android.arch.** { *; }
-dontwarn android.arch.**


## Tests
#-ignorewarnings
#
#-keepattributes *Annotation*
#
#-dontnote junit.framework.**
#-dontnote junit.runner.**
#
#-dontwarn android.test.**
#-dontwarn android.support.test.**
#-dontwarn org.junit.**
#-dontwarn org.hamcrest.**
#-dontwarn com.squareup.javawriter.JavaWriter
#-keepclassmembers class org.junit.** { public *; }
#-keepclassmembers class android.test.** { public *; }


# Mockito
-dontwarn org.mockito.**


#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}


#GMS
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

#Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**