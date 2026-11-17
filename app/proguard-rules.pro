# R8 rules for the release build. Libraries bring their own rules (OkHttp, Retrofit, Coil,
# Compose); these cover what this app does by reflection.

# Gson reads and writes these by field name: the disk cache of picked lyrics and romanizations,
# and LRCLIB's responses.
-keepclassmembers class com.tx24.spicyplayer.lyrics.NextLyricsBackend$StoredPick { <fields>; }
-keepclassmembers class com.tx24.spicyplayer.lyrics.NextLyricsBackend$StoredAttempt { <fields>; }
-keepclassmembers class com.tx24.spicyplayer.lyrics.NextLyricsBackend$StoredRoman { <fields>; }
-keepclassmembers class com.tx24.spicyplayer.network.data.RemoteLyricsPayload { <fields>; }
-keepclassmembers class com.tx24.spicyplayer.network.data.LyricsAttribution { <fields>; }
-keepclassmembers class com.tx24.spicyplayer.network.data.LyricsContributor { <fields>; }
-keep class com.tx24.spicyplayer.network.model.** { <fields>; <init>(...); }

# Translation cache entries are also serialized by field name, including their provider enum.
-keep class com.tx24.spicyplayer.translation.DiskTranslationCache$Entry { <fields>; <init>(...); }
-keep class com.tx24.spicyplayer.translation.TranslationKey { <fields>; <init>(...); }
-keep class com.tx24.spicyplayer.translation.TranslationResult { <fields>; <init>(...); }
-keep enum com.tx24.spicyplayer.translation.TranslationProvider { *; }
-keep enum com.tx24.spicyplayer.translation.TranslationOrigin { *; }
-keep class com.tx24.spicyplayer.translation.DiskGeniusTranslationCache$Entry { <fields>; <init>(...); }
-keep class com.tx24.spicyplayer.translation.GeniusTranslationPair { <fields>; <init>(...); }

# Retrofit builds the LRCLIB client from this interface's annotations and generic signatures.
-keep interface com.tx24.spicyplayer.network.service.LyricsService { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Kuromoji finds its dictionary next to its own classes, so their package names must stay.
-keep class com.atilika.kuromoji.** { *; }

# Google ML Kit (GenAI Prompt API, Common, AICore IPC bindings & Firebase Components)
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-keep enum com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-keep interface com.google.android.gms.internal.mlkit_** { *; }
-keep class com.google.firebase.components.** { *; }
-keep interface com.google.firebase.components.** { *; }
-keep public class * implements com.google.firebase.components.ComponentRegistrar {
    public <init>();
    public java.util.List getComponents();
}
-keep class com.google.android.gms.tasks.** { *; }
-keep interface com.google.android.gms.tasks.** { *; }
-keep enum com.tx24.spicyplayer.translation.OnDeviceModel$Status { *; }

# Crash traces stay readable with the mapping file.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile


# Remote protocol and LAN sync payloads serialized via Gson
-keep class com.tx24.spicyplayer.playback.remote.** { *; }
-keepclassmembers class com.tx24.spicyplayer.playback.remote.** { <fields>; <init>(...); }
-keep enum com.tx24.spicyplayer.playback.remote.** { *; }
-keepclassmembers enum com.tx24.spicyplayer.playback.remote.** { *; }
