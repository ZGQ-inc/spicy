package com.tx24.spicyplayer.playback.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

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
    @SerializedName("PING") PING,
    @SerializedName("PONG") PONG,
    @SerializedName("STATE_SYNC") STATE_SYNC,
    @SerializedName("COMMAND") COMMAND,
    @SerializedName("CONFIG_SYNC") CONFIG_SYNC,
    @SerializedName("CONFIG_PUSH") CONFIG_PUSH,
    @SerializedName("CONFIG_ACK") CONFIG_ACK,
    @SerializedName("REQUEST_STATE") REQUEST_STATE,
    @SerializedName("PLAYBACK_REDIRECT") PLAYBACK_REDIRECT,
}

/**
 * Commands executable by receiver (Speaker/TV) from controller (Phone).
 */
enum class RemoteAction {
    @SerializedName("PLAY") PLAY,
    @SerializedName("PAUSE") PAUSE,
    @SerializedName("PLAY_PAUSE") PLAY_PAUSE,
    @SerializedName("PREV") PREV,
    @SerializedName("NEXT") NEXT,
    @SerializedName("SEEK") SEEK,
    @SerializedName("DELAY_DELTA") DELAY_DELTA,
    @SerializedName("SET_VOLUME") SET_VOLUME,
    @SerializedName("TOGGLE_AMBIENT_VIEW") TOGGLE_AMBIENT_VIEW,
    @SerializedName("SET_RENDERER_STYLE") SET_RENDERER_STYLE,
    @SerializedName("SET_LYRICS_SIZE") SET_LYRICS_SIZE,
    @SerializedName("SET_CLOCK_SIZE") SET_CLOCK_SIZE,
    @SerializedName("SET_AMBIENT_CLOCK_SIZE") SET_AMBIENT_CLOCK_SIZE,
    @SerializedName("TOGGLE_ROMANIZE") TOGGLE_ROMANIZE,
    @SerializedName("TOGGLE_TRANSLATE") TOGGLE_TRANSLATE,
    @SerializedName("TOGGLE_NIGHT_MODE") TOGGLE_NIGHT_MODE,
    @SerializedName("RELOAD_WEATHER") RELOAD_WEATHER,
    @SerializedName("REFRESH_SPOTIFY") REFRESH_SPOTIFY,
    @SerializedName("SPOTIFY_AUTH_CODE") SPOTIFY_AUTH_CODE,
    @SerializedName("TOGGLE_SHUFFLE") TOGGLE_SHUFFLE,
    @SerializedName("CYCLE_REPEAT") CYCLE_REPEAT,
    @SerializedName("CUSTOM_ACTION") CUSTOM_ACTION,
}

/**
 * Envelope for all remote WebSocket/Socket messages.
 */
data class RemoteMessage(
    @SerializedName("type") val type: RemoteMessageType? = null,
    @SerializedName("state") val state: RemoteStatePayload? = null,
    @SerializedName("command") val command: RemoteCommandPayload? = null,
    @SerializedName("config") val config: RemoteConfigPayload? = null,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
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
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("album") val album: String = "",
    @SerializedName("durationMs") val durationMs: Long = 0L,
    @SerializedName("positionMs") val positionMs: Long = 0L,
    @SerializedName("isPlaying") val isPlaying: Boolean = false,
    @SerializedName("lyricDelayMs") val lyricDelayMs: Int = 0,
    @SerializedName("artworkUrl") val artworkUrl: String? = null,
    @SerializedName("currentLyricsLine") val currentLyricsLine: String? = null,
    @SerializedName("lyricsLrc") val lyricsLrc: String? = null,
    @SerializedName("weatherCondition") val weatherCondition: String? = null,
    @SerializedName("weatherDescription") val weatherDescription: String? = null,
    @SerializedName("weatherTempC") val weatherTempC: Float? = null,
    @SerializedName("weatherFeelsLikeC") val weatherFeelsLikeC: Float? = null,
    @SerializedName("weatherMinC") val weatherMinC: Float? = null,
    @SerializedName("weatherMaxC") val weatherMaxC: Float? = null,
    @SerializedName("weatherHumidity") val weatherHumidity: Int? = null,
    @SerializedName("weatherWindSpeed") val weatherWindSpeed: Float? = null,
    @SerializedName("weatherEmoji") val weatherEmoji: String? = null,
    @SerializedName("weatherIconCode") val weatherIconCode: String? = null,
    @SerializedName("cityName") val cityName: String? = null,
    @SerializedName("rendererStyle") val rendererStyle: String = "Spring",
    @SerializedName("lyricsSizePercent") val lyricsSizePercent: Int = 100,
    @SerializedName("clockScalePercent") val clockScalePercent: Int = 100,
    @SerializedName("ambientClockScalePercent") val ambientClockScalePercent: Int = 100,
    @SerializedName("volumePercent") val volumePercent: Int = 100,
    @SerializedName("clock12h") val clock12h: Boolean = false,
    @SerializedName("showSeconds") val showSeconds: Boolean = false,
    @SerializedName("showDate") val showDate: Boolean = true,
    @SerializedName("showWeatherDetails") val showWeatherDetails: Boolean = true,
    @SerializedName("romanize") val romanize: Boolean = false,
    @SerializedName("translate") val translate: Boolean = false,
    @SerializedName("ambientViewActive") val ambientViewActive: Boolean = false,
    @SerializedName("nightModeActive") val nightModeActive: Boolean = false,
    @SerializedName("deviceName") val deviceName: String = "Spicy Display",
    @SerializedName("shuffleState") val shuffleState: Boolean = false,
    @SerializedName("repeatState") val repeatState: String = "off",
    @SerializedName("queue") val queue: List<RemoteQueueItem>? = null,
    @SerializedName("detectedSpotifyId") val detectedSpotifyId: String? = null,
)

/**
 * Lightweight queue item for remote synchronization.
 */
data class RemoteQueueItem(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("artworkUrl") val artworkUrl: String? = null,
    @SerializedName("current") val current: Boolean = false,
)

/**
 * Command payload sent from phone controller to receiver.
 */
data class RemoteCommandPayload(
    @SerializedName("action") val action: RemoteAction? = null,
    @SerializedName("valueInt") val valueInt: Int? = null,
    @SerializedName("valueLong") val valueLong: Long? = null,
    @SerializedName("valueString") val valueString: String? = null,
    @SerializedName("valueBoolean") val valueBoolean: Boolean? = null,
)

/**
 * Remote configuration payload (e.g. pushing OWM API key, Spotify tokens, or display preferences from phone to TV).
 */
data class RemoteConfigPayload(
    @SerializedName("allSettings") val allSettings: Map<String, String>? = null,
    @SerializedName("settingKey") val settingKey: String? = null,
    @SerializedName("settingValue") val settingValue: String? = null,
    @SerializedName("owmApiKey") val owmApiKey: String? = null,
    @SerializedName("owmCity") val owmCity: String? = null,
    @SerializedName("owmUnits") val owmUnits: String? = null,
    @SerializedName("owmUseCelsius") val owmUseCelsius: Boolean? = null,
    @SerializedName("weatherRefreshIntervalMinutes") val weatherRefreshIntervalMinutes: Int? = null,
    @SerializedName("showBatteryIndicator") val showBatteryIndicator: Boolean? = null,
    @SerializedName("batteryIndicatorScale") val batteryIndicatorScale: Int? = null,
    @SerializedName("appLanguage") val appLanguage: String? = null,
    @SerializedName("spotifyClientId") val spotifyClientId: String? = null,
    @SerializedName("spotifyClientSecret") val spotifyClientSecret: String? = null,
    @SerializedName("spotifyRefreshToken") val spotifyRefreshToken: String? = null,
    @SerializedName("spotifyCloudSyncEnabled") val spotifyCloudSyncEnabled: Boolean? = null,
    @SerializedName("spotifySyncIntervalSeconds") val spotifySyncIntervalSeconds: Int? = null,
    @SerializedName("clock12h") val clock12h: Boolean? = null,
    @SerializedName("showSeconds") val showSeconds: Boolean? = null,
    @SerializedName("showDate") val showDate: Boolean? = null,
    @SerializedName("showWeather") val showWeather: Boolean? = null,
    @SerializedName("showWeatherDetails") val showWeatherDetails: Boolean? = null,
    @SerializedName("showClockOnLyricsScreen") val showClockOnLyricsScreen: Boolean? = null,
    @SerializedName("showWeatherOnLyricsScreen") val showWeatherOnLyricsScreen: Boolean? = null,
    @SerializedName("clockScalePercent") val clockScalePercent: Int? = null,
    @SerializedName("ambientClockScalePercent") val ambientClockScalePercent: Int? = null,
    @SerializedName("lyricsSizePercent") val lyricsSizePercent: Int? = null,
    @SerializedName("lyricsSizePortrait") val lyricsSizePortrait: Int? = null,
    @SerializedName("lyricsSizeLandscape") val lyricsSizeLandscape: Int? = null,
    @SerializedName("rendererStyle") val rendererStyle: String? = null,
    @SerializedName("nightMode") val nightMode: Boolean? = null,
    @SerializedName("pixelShiftEnabled") val pixelShiftEnabled: Boolean? = null,
    @SerializedName("smartDisplayEnabled") val smartDisplayEnabled: Boolean? = null,
    @SerializedName("ambientIdleTimeoutSeconds") val ambientIdleTimeoutSeconds: Int? = null,
    @SerializedName("minimalLyricsMode") val minimalLyricsMode: Boolean? = null,
    @SerializedName("remoteDeviceName") val remoteDeviceName: String? = null,
    @SerializedName("nextUpCard") val nextUpCard: Boolean? = null,
    @SerializedName("nextUpScalePercent") val nextUpScalePercent: Int? = null,
)
