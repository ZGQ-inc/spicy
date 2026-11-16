package com.tx24.spicyplayer.playback

import com.tx24.spicyplayer.i18n.t
import com.tx24.spicyplayer.network.data.spotify.CurrentlyPlayingResult
import com.tx24.spicyplayer.network.data.spotify.SpotifyCloudPlayback
import com.tx24.spicyplayer.network.data.spotify.SpotifyWebApiClient
import com.tx24.spicyplayer.ui.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SpotifyCloudStatus {
    DISABLED,
    CONNECTING,
    ACTIVE_PLAYBACK,
    NO_PLAYBACK,
    AUTH_ERROR,
    NETWORK_ERROR,
}

/**
 * Manages background synchronization with Spotify Web API for cloud playback tracking.
 */
class SpotifyCloudManager(
    private val settings: AppSettings,
    private val scope: CoroutineScope,
    private val client: SpotifyWebApiClient = SpotifyWebApiClient(),
) {
    private val mutablePlayback = MutableStateFlow<SpotifyCloudPlayback?>(null)
    val playback = mutablePlayback.asStateFlow()

    val status = MutableStateFlow(SpotifyCloudStatus.DISABLED)
    val statusMessage = MutableStateFlow(t("Disabled"))
    val lastSyncTimeMs = MutableStateFlow(0L)
    val lastError = MutableStateFlow<String?>(null)

    init {
        client.logger = { android.util.Log.d("SpotifyCloud", it) }
    }

    private var pollJob: Job? = null
    var onPlaybackReceived: ((SpotifyCloudPlayback?) -> Unit)? = null
    var isLocalPlaybackActive: (() -> Boolean)? = null

    private var tokenExpiresAtMs: Long = 0L
    private val cachedQueue = mutableListOf<com.tx24.spicyplayer.network.data.spotify.SpotifyCloudTrack>()
    private var lastQueueTrackId: String? = null
    private var lastQueueFetchAt = 0L

    /**
     * Starts or restarts polling based on settings.
     */
    fun start() {
        pollJob?.cancel()
        if (!settings.spotifyCloudSyncEnabled) {
            mutablePlayback.value = null
            status.value = SpotifyCloudStatus.DISABLED
            statusMessage.value = t("Spotify Cloud Sync Disabled")
            return
        }

        status.value = SpotifyCloudStatus.CONNECTING
        statusMessage.value = t("Connecting to Spotify API…")
        android.util.Log.i("SpotifyCloud", "start() called. enabled=${settings.spotifyCloudSyncEnabled}, hasToken=${settings.spotifyAccessToken.isNotBlank()}, hasRefresh=${settings.spotifyRefreshToken.isNotBlank()}")

        pollJob = scope.launch(Dispatchers.IO) {
            var consecutiveNetworkErrors = 0
            var consecutiveNoPlayback = 0
            while (isActive) {
                if (!settings.spotifyCloudSyncEnabled) {
                    status.value = SpotifyCloudStatus.DISABLED
                    statusMessage.value = t("Spotify Cloud Sync Disabled")
                    delay(3000L)
                    continue
                }

                if (isLocalPlaybackActive?.invoke() == true) {
                    delay(4000L)
                    continue
                }

                ensureValidToken()
                var token = settings.spotifyAccessToken
                if (token.isBlank()) {
                    status.value = SpotifyCloudStatus.AUTH_ERROR
                    statusMessage.value = t("Access Token not configured, please authorize")
                    delay(5000L)
                    continue
                }

                var result = client.fetchCurrentlyPlaying(token)
                // If 401 AuthError and we have a refresh token, refresh and retry once
                if (result is CurrentlyPlayingResult.AuthError && settings.spotifyRefreshToken.isNotBlank()) {
                    android.util.Log.w("SpotifyCloud", "Token returned 401 AuthError, attempting refresh…")
                    val refreshed = refreshTokenInternal()
                    if (refreshed != null && refreshed.accessToken.isNotBlank()) {
                        token = refreshed.accessToken
                        result = client.fetchCurrentlyPlaying(token)
                    }
                }

                val syncIntervalMs = (settings.spotifySyncIntervalSeconds.coerceIn(1, 60)) * 1000L

                when (result) {
                    is CurrentlyPlayingResult.Success -> {
                        consecutiveNetworkErrors = 0
                        consecutiveNoPlayback = 0
                        val current = result.playback
                        val currentTrack = current.track
                        val trackChanged = currentTrack?.id != lastQueueTrackId
                        val now = android.os.SystemClock.elapsedRealtime()
                        if (trackChanged || cachedQueue.isEmpty() || (now - lastQueueFetchAt > 20_000L)) {
                            val q = client.fetchQueue(token)
                            if (q != null && q.isNotEmpty()) {
                                cachedQueue.clear()
                                cachedQueue.addAll(q)
                                lastQueueTrackId = currentTrack?.id
                                lastQueueFetchAt = now
                            }
                        }
                        val currentWithQueue = current.copy(queue = cachedQueue.toList())
                        mutablePlayback.value = currentWithQueue
                        withContext(Dispatchers.Main) {
                            onPlaybackReceived?.invoke(currentWithQueue)
                        }

                        status.value = if (current.isPlaying) SpotifyCloudStatus.ACTIVE_PLAYBACK else SpotifyCloudStatus.NO_PLAYBACK
                        val playLabel = if (current.isPlaying) t("Playing") else t("Paused")
                        statusMessage.value = "$playLabel: ${currentTrack?.title ?: t("Unknown Track")} · ${currentTrack?.artist ?: ""}"
                        lastSyncTimeMs.value = System.currentTimeMillis()
                        lastError.value = null

                        val remainingMs = (current.track?.durationMs ?: 0L) - current.progressMs
                        val pollDelay = if (current.isPlaying) {
                            if (remainingMs in 0L..syncIntervalMs) minOf(1500L, syncIntervalMs) else syncIntervalMs
                        } else (syncIntervalMs * 2).coerceAtLeast(8000L)
                        delay(pollDelay)
                    }
                    is CurrentlyPlayingResult.NoPlayback -> {
                        consecutiveNetworkErrors = 0
                        status.value = SpotifyCloudStatus.NO_PLAYBACK
                        statusMessage.value = t("Connected to Spotify API, no active device playing")
                        lastSyncTimeMs.value = System.currentTimeMillis()
                        lastError.value = null

                        val wasPlaying = mutablePlayback.value?.isPlaying == true
                        if (wasPlaying && consecutiveNoPlayback < 2) {
                            consecutiveNoPlayback++
                            delay(1500L)
                        } else {
                            consecutiveNoPlayback = 0
                            cachedQueue.clear()
                            lastQueueTrackId = null
                            mutablePlayback.value = null
                            withContext(Dispatchers.Main) {
                                onPlaybackReceived?.invoke(null)
                            }
                            delay((syncIntervalMs * 3).coerceAtLeast(15000L))
                        }
                    }
                    is CurrentlyPlayingResult.RateLimited -> {
                        val waitSec = result.retryAfterSeconds.coerceAtLeast(5)
                        val isQuota = result.isQuotaExceeded || waitSec > 300
                        status.value = SpotifyCloudStatus.NETWORK_ERROR
                        val msg = if (isQuota) {
                            val hours = String.format(java.util.Locale.US, "%.1f", waitSec / 3600f)
                            t("Spotify Daily Quota Exceeded (429), cooling down ~%s hrs. Change Client ID or use Remote mode.", hours)
                        } else {
                            val backoff = minOf(waitSec, 120)
                            t("Spotify Rate Limited (429), waiting %d s to retry…", backoff)
                        }
                        statusMessage.value = msg
                        lastError.value = if (isQuota) "QUOTA_EXCEEDED (${waitSec}s)" else "RateLimited (${waitSec}s)"
                        android.util.Log.w("SpotifyCloud", "Spotify 429: $msg")
                        val actualWaitSec = if (isQuota) maxOf(waitSec.toLong(), 300L) else minOf(waitSec.toLong(), 120L)
                        delay(actualWaitSec * 1000L)
                    }
                    is CurrentlyPlayingResult.NetworkError -> {
                        consecutiveNetworkErrors++
                        val err = result.error.message ?: result.error.javaClass.simpleName
                        status.value = SpotifyCloudStatus.NETWORK_ERROR
                        statusMessage.value = t("Network error: %s", err)
                        lastError.value = err
                        android.util.Log.e("SpotifyCloud", "Network error in fetch: $err")

                        val backoff = minOf(1500L * consecutiveNetworkErrors, 5000L)
                        delay(backoff)
                    }
                    is CurrentlyPlayingResult.AuthError -> {
                        status.value = SpotifyCloudStatus.AUTH_ERROR
                        statusMessage.value = t("Spotify authentication expired (HTTP %d)", result.code)
                        lastError.value = "AuthError HTTP ${result.code}"
                        android.util.Log.e("SpotifyCloud", "Auth error in fetch (HTTP ${result.code})")
                        delay(5000L)
                    }
                }
            }
        }
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
        mutablePlayback.value = null
        status.value = SpotifyCloudStatus.DISABLED
        statusMessage.value = t("Sync stopped")
    }

    fun play() {
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.play(settings.spotifyAccessToken)
            triggerImmediatePoll()
        }
    }

    fun pause() {
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.pause(settings.spotifyAccessToken)
            triggerImmediatePoll()
        }
    }

    fun next() {
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.next(settings.spotifyAccessToken)
            triggerImmediatePoll()
        }
    }

    fun previous() {
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.previous(settings.spotifyAccessToken)
            triggerImmediatePoll()
        }
    }

    fun seekTo(positionMs: Long) {
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.seek(settings.spotifyAccessToken, positionMs)
            triggerImmediatePoll()
        }
    }

    fun setVolume(volumePercent: Int) {
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.setVolume(settings.spotifyAccessToken, volumePercent)
        }
    }

    fun toggleShuffle() {
        val current = mutablePlayback.value?.shuffleState ?: false
        val next = !current
        val updated = mutablePlayback.value?.copy(shuffleState = next)
        mutablePlayback.value = updated
        if (updated != null) {
            scope.launch(Dispatchers.Main) { onPlaybackReceived?.invoke(updated) }
        }
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.setShuffle(settings.spotifyAccessToken, next)
            triggerImmediatePoll()
        }
    }

    fun cycleRepeat() {
        val current = mutablePlayback.value?.repeatState ?: "off"
        val next = when (current) {
            "off" -> "context"
            "context" -> "track"
            else -> "off"
        }
        val updated = mutablePlayback.value?.copy(repeatState = next)
        mutablePlayback.value = updated
        if (updated != null) {
            scope.launch(Dispatchers.Main) { onPlaybackReceived?.invoke(updated) }
        }
        scope.launch(Dispatchers.IO) {
            ensureValidToken()
            client.setRepeat(settings.spotifyAccessToken, next)
            triggerImmediatePoll()
        }
    }

    fun triggerImmediatePoll() {
        scope.launch(Dispatchers.IO) {
            delay(200L)
            val token = settings.spotifyAccessToken
            if (token.isNotBlank()) {
                val res = client.fetchCurrentlyPlaying(token)
                if (res is CurrentlyPlayingResult.Success) {
                    mutablePlayback.value = res.playback
                    withContext(Dispatchers.Main) { onPlaybackReceived?.invoke(res.playback) }
                }
            }
        }
    }

    private fun ensureValidToken() {
        val refreshToken = settings.spotifyRefreshToken
        val clientId = settings.spotifyClientId
        val now = System.currentTimeMillis()
        if (refreshToken.isNotBlank() && clientId.isNotBlank()) {
            if (settings.spotifyAccessToken.isBlank() || now >= tokenExpiresAtMs) {
                refreshTokenInternal()
            }
        }
    }

    private fun refreshTokenInternal(): com.tx24.spicyplayer.network.data.spotify.SpotifyTokenResult? {
        val refreshToken = settings.spotifyRefreshToken
        val clientId = settings.spotifyClientId
        if (refreshToken.isBlank() || clientId.isBlank()) return null
        val secret = settings.spotifyClientSecret.ifBlank { null }
        val res = client.refreshAccessToken(clientId, refreshToken, secret)
        if (res != null && res.accessToken.isNotBlank()) {
            settings.spotifyAccessToken = res.accessToken
            tokenExpiresAtMs = System.currentTimeMillis() + (res.expiresInSeconds.coerceAtLeast(300) - 60) * 1000L
        }
        return res
    }
}
