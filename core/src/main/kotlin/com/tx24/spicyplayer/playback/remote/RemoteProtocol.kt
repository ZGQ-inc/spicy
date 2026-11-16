package com.tx24.spicyplayer.playback.remote

import com.google.gson.Gson

internal object RemoteLog {
    fun d(tag: String, msg: String) = println("D/$tag: $msg")
    fun i(tag: String, msg: String) = println("I/$tag: $msg")
    fun w(tag: String, msg: String, tr: Throwable? = null) =
        System.err.println("W/$tag: $msg" + (if (tr != null) " - ${tr.message}" else ""))
    fun e(tag: String, msg: String, tr: Throwable? = null) {
        System.err.println("E/$tag: $msg" + (if (tr != null) " - ${tr.message}" else ""))
        tr?.printStackTrace()
    }
}

/**
 * Message types for local network remote control communication.
 */
enum class RemoteMessageType {
    PING,
    PONG,
    STATE_SYNC,
    COMMAND,
    CONFIG_SYNC,
    CONFIG_PUSH,
    CONFIG_ACK,
    REQUEST_STATE,
    PLAYBACK_REDIRECT,
}

/**
 * Commands executable by receiver (Speaker/TV) from controller (Phone).
 */
enum class RemoteAction {
    PLAY,
    PAUSE,
    PLAY_PAUSE,
    PREV,
    NEXT,
    SEEK,
    DELAY_DELTA,
    SET_VOLUME,
    TOGGLE_AMBIENT_VIEW,
    SET_RENDERER_STYLE,
    SET_LYRICS_SIZE,
    SET_CLOCK_SIZE,
    SET_AMBIENT_CLOCK_SIZE,
    TOGGLE_ROMANIZE,
    TOGGLE_TRANSLATE,
    TOGGLE_NIGHT_MODE,
    RELOAD_WEATHER,
    REFRESH_SPOTIFY,
    SPOTIFY_AUTH_CODE,
    TOGGLE_SHUFFLE,
    CYCLE_REPEAT,
    CUSTOM_ACTION,
}

/**
 * Envelope for all remote WebSocket/Socket messages.
 */
data class RemoteMessage(
    val type: RemoteMessageType,
    val state: RemoteStatePayload? = null,
    val command: RemoteCommandPayload? = null,
    val config: RemoteConfigPayload? = null,
    val timestamp: Long = System.currentTimeMillis(),
) {
    fun toJson(gson: Gson = Gson()): String = gson.toJson(this)

    companion object {
        fun fromJson(json: String, gson: Gson = Gson()): RemoteMessage? =
            runCatching { gson.fromJson(json, RemoteMessage::class.java) }.getOrNull()
    }
}

/**
 * Real-time playback and display state reported by the receiver.
 */
data class RemoteStatePayload(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val lyricDelayMs: Int = 0,
    val artworkUrl: String? = null,
    val currentLyricsLine: String? = null,
    val lyricsLrc: String? = null,
    val weatherCondition: String? = null,
    val weatherDescription: String? = null,
    val weatherTempC: Float? = null,
    val weatherFeelsLikeC: Float? = null,
    val weatherMinC: Float? = null,
    val weatherMaxC: Float? = null,
    val weatherHumidity: Int? = null,
    val weatherWindSpeed: Float? = null,
    val weatherEmoji: String? = null,
    val weatherIconCode: String? = null,
    val cityName: String? = null,
    val rendererStyle: String = "Spring",
    val lyricsSizePercent: Int = 100,
    val clockScalePercent: Int = 100,
    val ambientClockScalePercent: Int = 100,
    val volumePercent: Int = 100,
    val clock12h: Boolean = false,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val showWeatherDetails: Boolean = true,
    val romanize: Boolean = false,
    val translate: Boolean = false,
    val ambientViewActive: Boolean = false,
    val nightModeActive: Boolean = false,
    val deviceName: String = "Spicy Display",
    val shuffleState: Boolean = false,
    val repeatState: String = "off",
    val queue: List<RemoteQueueItem>? = null,
    val detectedSpotifyId: String? = null,
)

/**
 * Lightweight queue item for remote synchronization.
 */
data class RemoteQueueItem(
    val id: Long = 0L,
    val title: String = "",
    val subtitle: String = "",
    val artworkUrl: String? = null,
    val current: Boolean = false,
)

/**
 * Command payload sent from phone controller to receiver.
 */
data class RemoteCommandPayload(
    val action: RemoteAction,
    val valueInt: Int? = null,
    val valueLong: Long? = null,
    val valueString: String? = null,
    val valueBoolean: Boolean? = null,
)

/**
 * Remote configuration payload (e.g. pushing OWM API key, Spotify tokens, or display preferences from phone to TV).
 */
data class RemoteConfigPayload(
    val allSettings: Map<String, String>? = null,
    val settingKey: String? = null,
    val settingValue: String? = null,
    val owmApiKey: String? = null,
    val owmCity: String? = null,
    val owmUnits: String? = null,
    val owmUseCelsius: Boolean? = null,
    val weatherRefreshIntervalMinutes: Int? = null,
    val showBatteryIndicator: Boolean? = null,
    val batteryIndicatorScale: Int? = null,
    val appLanguage: String? = null,
    val spotifyClientId: String? = null,
    val spotifyClientSecret: String? = null,
    val spotifyRefreshToken: String? = null,
    val spotifyCloudSyncEnabled: Boolean? = null,
    val spotifySyncIntervalSeconds: Int? = null,
    val clock12h: Boolean? = null,
    val showSeconds: Boolean? = null,
    val showDate: Boolean? = null,
    val showWeather: Boolean? = null,
    val showWeatherDetails: Boolean? = null,
    val showClockOnLyricsScreen: Boolean? = null,
    val showWeatherOnLyricsScreen: Boolean? = null,
    val clockScalePercent: Int? = null,
    val ambientClockScalePercent: Int? = null,
    val lyricsSizePercent: Int? = null,
    val lyricsSizePortrait: Int? = null,
    val lyricsSizeLandscape: Int? = null,
    val rendererStyle: String? = null,
    val nightMode: Boolean? = null,
    val pixelShiftEnabled: Boolean? = null,
    val smartDisplayEnabled: Boolean? = null,
    val ambientIdleTimeoutSeconds: Int? = null,
    val minimalLyricsMode: Boolean? = null,
    val remoteDeviceName: String? = null,
    val nextUpCard: Boolean? = null,
    val nextUpScalePercent: Int? = null,
)
