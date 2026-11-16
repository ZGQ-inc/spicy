package com.tx24.spicyplayer.network.data.spotify

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Normalized cloud playback state fetched from Spotify Web API.
 */
data class SpotifyCloudPlayback(
    val isPlaying: Boolean,
    val progressMs: Long,
    val timestamp: Long,
    val track: SpotifyCloudTrack?,
    val deviceName: String? = null,
    val volumePercent: Int? = null,
    val shuffleState: Boolean = false,
    val repeatState: String = "off", // "off", "context", "track"
    val queue: List<SpotifyCloudTrack>? = null,
)

/**
 * Normalized cloud track metadata.
 */
data class SpotifyCloudTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artworkUrl: String?,
)

/**
 * Result of Spotify OAuth token exchange or refresh.
 */
data class SpotifyTokenResult(
    val accessToken: String,
    val expiresInSeconds: Int,
    val refreshToken: String?,
    val tokenType: String = "Bearer",
)

/**
 * Lightweight Spotify Web API client for cloud playback synchronization and remote transport.
 */
sealed class CurrentlyPlayingResult {
    data class Success(val playback: SpotifyCloudPlayback) : CurrentlyPlayingResult()
    data object NoPlayback : CurrentlyPlayingResult()
    data class AuthError(val code: Int) : CurrentlyPlayingResult()
    data class RateLimited(val retryAfterSeconds: Int, val isQuotaExceeded: Boolean = false) : CurrentlyPlayingResult()
    data class NetworkError(val error: Throwable) : CurrentlyPlayingResult()
}

class SpotifyWebApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson(),
) {
    var logger: ((String) -> Unit)? = null

    private fun log(msg: String) {
        logger?.invoke(msg) ?: println("[SpotifyWebApiClient] $msg")
    }

    /**
     * Retrieves currently active playback from Spotify Web API (`GET /v1/me/player`) with detailed result status.
     */
    fun fetchCurrentlyPlaying(accessToken: String): CurrentlyPlayingResult {
        if (accessToken.isBlank()) return CurrentlyPlayingResult.AuthError(401)
        val req = Request.Builder()
            .url("https://api.spotify.com/v1/me/player")
            .header("Authorization", "Bearer $accessToken")
            .build()

        return try {
            client.newCall(req).execute().use { resp ->
                log("fetchCurrentlyPlaying HTTP ${resp.code}")
                if (resp.code == 204) {
                    return CurrentlyPlayingResult.NoPlayback
                }
                if (resp.code == 401) {
                    return CurrentlyPlayingResult.AuthError(401)
                }
                if (resp.code == 429) {
                    val retryAfter = resp.header("Retry-After")?.toIntOrNull() ?: 10
                    val errBody = resp.body?.string().orEmpty()
                    val isQuota = errBody.contains("QUOTA_EXCEEDED", ignoreCase = true)
                    log("Spotify 429 Rate limited! Retry-After: $retryAfter s, isQuota: $isQuota, body: $errBody")
                    return CurrentlyPlayingResult.RateLimited(retryAfter, isQuota)
                }
                if (!resp.isSuccessful) {
                    return CurrentlyPlayingResult.NetworkError(java.io.IOException("HTTP ${resp.code}"))
                }
                val body = resp.body?.string() ?: return CurrentlyPlayingResult.NoPlayback
                val raw = gson.fromJson(body, RawPlaybackResponse::class.java) ?: return CurrentlyPlayingResult.NoPlayback

                val item = raw.item
                val track = if (item != null) {
                    val artistsStr = item.artists?.joinToString(", ") { it.name.orEmpty() } ?: "Unknown Artist"
                    val bestImage = item.album?.images?.maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }?.url
                        ?: item.album?.images?.firstOrNull()?.url
                    SpotifyCloudTrack(
                        id = item.id.orEmpty(),
                        title = item.name.orEmpty().ifBlank { "Unknown Track" },
                        artist = artistsStr,
                        album = item.album?.name.orEmpty(),
                        durationMs = item.durationMs ?: 0L,
                        artworkUrl = bestImage,
                    )
                } else null

                CurrentlyPlayingResult.Success(
                    SpotifyCloudPlayback(
                        isPlaying = raw.isPlaying == true,
                        progressMs = raw.progressMs ?: 0L,
                        timestamp = raw.timestamp ?: System.currentTimeMillis(),
                        track = track,
                        deviceName = raw.device?.name,
                        volumePercent = raw.device?.volumePercent,
                        shuffleState = raw.shuffleState ?: false,
                        repeatState = raw.repeatState ?: "off",
                    )
                )
            }
        } catch (e: Exception) {
            log("fetchCurrentlyPlaying exception: ${e.message}")
            CurrentlyPlayingResult.NetworkError(e)
        }
    }

    /**
     * Retrieves currently active playback from Spotify Web API (`GET /v1/me/player`).
     */
    fun getCurrentlyPlaying(accessToken: String): SpotifyCloudPlayback? =
        (fetchCurrentlyPlaying(accessToken) as? CurrentlyPlayingResult.Success)?.playback

    /**
     * Resumes playback on active device.
     */
    fun play(accessToken: String): Boolean = sendCommand(accessToken, "PUT", "https://api.spotify.com/v1/me/player/play")

    /**
     * Pauses playback on active device.
     */
    fun pause(accessToken: String): Boolean = sendCommand(accessToken, "PUT", "https://api.spotify.com/v1/me/player/pause")

    /**
     * Skips to next track.
     */
    fun next(accessToken: String): Boolean = sendCommand(accessToken, "POST", "https://api.spotify.com/v1/me/player/next")

    /**
     * Skips to previous track.
     */
    fun previous(accessToken: String): Boolean = sendCommand(accessToken, "POST", "https://api.spotify.com/v1/me/player/previous")

    /**
     * Seeks to target position in milliseconds.
     */
    fun seek(accessToken: String, positionMs: Long): Boolean =
        sendCommand(accessToken, "PUT", "https://api.spotify.com/v1/me/player/seek?position_ms=$positionMs")

    /**
     * Sets playback volume on active device (0..100).
     */
    fun setVolume(accessToken: String, volumePercent: Int): Boolean =
        sendCommand(accessToken, "PUT", "https://api.spotify.com/v1/me/player/volume?volume_percent=${volumePercent.coerceIn(0, 100)}")

    /**
     * Toggles shuffle mode (PUT /v1/me/player/shuffle?state={true|false}).
     */
    fun setShuffle(accessToken: String, state: Boolean): Boolean =
        sendCommand(accessToken, "PUT", "https://api.spotify.com/v1/me/player/shuffle?state=$state")

    /**
     * Sets repeat mode: track (repeat one), context (repeat all), off (PUT /v1/me/player/repeat?state={track|context|off}).
     */
    fun setRepeat(accessToken: String, state: String): Boolean =
        sendCommand(accessToken, "PUT", "https://api.spotify.com/v1/me/player/repeat?state=$state")

    /**
     * Fetches current queue from Spotify Web API (`GET /v1/me/player/queue`).
     * Returns a list with currently playing track at index 0 (if available), followed by upcoming queued tracks.
     */
    fun fetchQueue(accessToken: String): List<SpotifyCloudTrack>? {
        if (accessToken.isBlank()) return null
        val req = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/queue")
            .header("Authorization", "Bearer $accessToken")
            .build()
        return try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val raw = gson.fromJson(body, RawQueueResponse::class.java) ?: return null
                val out = ArrayList<SpotifyCloudTrack>()
                raw.currentlyPlaying?.let { item ->
                    val artistsStr = item.artists?.joinToString(", ") { it.name.orEmpty() } ?: "Unknown Artist"
                    val bestImage = item.album?.images?.maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }?.url
                        ?: item.album?.images?.firstOrNull()?.url
                    out.add(
                        SpotifyCloudTrack(
                            id = item.id.orEmpty(),
                            title = item.name.orEmpty().ifBlank { "Unknown Track" },
                            artist = artistsStr,
                            album = item.album?.name.orEmpty(),
                            durationMs = item.durationMs ?: 0L,
                            artworkUrl = bestImage,
                        )
                    )
                }
                raw.queue?.forEach { item ->
                    val artistsStr = item.artists?.joinToString(", ") { it.name.orEmpty() } ?: "Unknown Artist"
                    val bestImage = item.album?.images?.maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }?.url
                        ?: item.album?.images?.firstOrNull()?.url
                    out.add(
                        SpotifyCloudTrack(
                            id = item.id.orEmpty(),
                            title = item.name.orEmpty().ifBlank { "Unknown Track" },
                            artist = artistsStr,
                            album = item.album?.name.orEmpty(),
                            durationMs = item.durationMs ?: 0L,
                            artworkUrl = bestImage,
                        )
                    )
                }
                out
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Builds standard Spotify OAuth authorization URL.
     */
    fun getAuthorizeUrl(clientId: String, redirectUri: String): String {
        val scopes = "user-read-playback-state%20user-modify-playback-state%20user-read-currently-playing"
        val encClientId = java.net.URLEncoder.encode(clientId.trim(), "UTF-8")
        val encRedirect = java.net.URLEncoder.encode(redirectUri.trim(), "UTF-8")
        return "https://accounts.spotify.com/authorize?client_id=$encClientId&response_type=code&redirect_uri=$encRedirect&scope=$scopes"
    }

    /**
     * Exchanges OAuth authorization code for initial access and refresh tokens.
     * Supports both standard HTTP Basic Auth header and form body parameters.
     */
    fun exchangeAuthCode(clientId: String, clientSecret: String, code: String, redirectUri: String): SpotifyTokenResult? {
        val cleanClientId = clientId.trim()
        val cleanSecret = clientSecret.trim()
        val cleanCode = code.trim()
        val cleanRedirect = redirectUri.trim()

        if (cleanClientId.isBlank() || cleanCode.isBlank() || cleanRedirect.isBlank()) {
            System.err.println("[SpotifyWebApiClient] exchangeAuthCode aborted: empty params (id=$cleanClientId, codeLen=${cleanCode.length}, redirect=$cleanRedirect)")
            return null
        }

        // Try standard Basic Auth header first if clientSecret is present
        val token = doExchangeToken(cleanClientId, cleanSecret, cleanCode, cleanRedirect, useBasicAuth = cleanSecret.isNotBlank())
            ?: if (cleanSecret.isNotBlank()) {
                // Fallback to sending client_id and client_secret in form body
                doExchangeToken(cleanClientId, cleanSecret, cleanCode, cleanRedirect, useBasicAuth = false)
            } else null
        return token
    }

    private fun doExchangeToken(
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String,
        useBasicAuth: Boolean
    ): SpotifyTokenResult? {
        val formBuilder = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", redirectUri)

        val reqBuilder = Request.Builder().url("https://accounts.spotify.com/api/token")

        if (useBasicAuth && clientSecret.isNotBlank()) {
            reqBuilder.header("Authorization", Credentials.basic(clientId, clientSecret))
        } else {
            formBuilder.add("client_id", clientId)
            if (clientSecret.isNotBlank()) {
                formBuilder.add("client_secret", clientSecret)
            }
        }

        val req = reqBuilder.post(formBuilder.build()).build()
        return try {
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful) {
                    System.err.println("[SpotifyWebApiClient] exchangeAuthCode HTTP ${resp.code}: $body (redirectUri=$redirectUri, useBasicAuth=$useBasicAuth)")
                    return null
                }
                if (body.isNullOrBlank()) return null
                val raw = gson.fromJson(body, RawTokenResponse::class.java)
                if (raw?.accessToken != null) {
                    println("[SpotifyWebApiClient] Successfully exchanged token! (expiresIn=${raw.expiresIn})")
                    SpotifyTokenResult(
                        accessToken = raw.accessToken,
                        expiresInSeconds = raw.expiresIn ?: 3600,
                        refreshToken = raw.refreshToken,
                        tokenType = raw.tokenType ?: "Bearer",
                    )
                } else null
            }
        } catch (e: Exception) {
            System.err.println("[SpotifyWebApiClient] exchangeAuthCode exception: ${e.message}")
            null
        }
    }

    /**
     * Refreshes access token using OAuth refresh token.
     */
    fun refreshAccessToken(clientId: String, refreshToken: String, clientSecret: String? = null): SpotifyTokenResult? {
        val cleanClientId = clientId.trim()
        val cleanToken = refreshToken.trim()
        val cleanSecret = clientSecret?.trim().orEmpty()

        if (cleanClientId.isBlank() || cleanToken.isBlank()) return null

        val formBuilder = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", cleanToken)

        val reqBuilder = Request.Builder().url("https://accounts.spotify.com/api/token")

        if (cleanSecret.isNotBlank()) {
            reqBuilder.header("Authorization", Credentials.basic(cleanClientId, cleanSecret))
        } else {
            formBuilder.add("client_id", cleanClientId)
        }

        val req = reqBuilder.post(formBuilder.build()).build()
        return try {
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (!resp.isSuccessful) {
                    System.err.println("[SpotifyWebApiClient] refreshAccessToken HTTP ${resp.code}: $body")
                    return null
                }
                if (body.isNullOrBlank()) return null
                val raw = gson.fromJson(body, RawTokenResponse::class.java)
                if (raw?.accessToken != null) {
                    SpotifyTokenResult(
                        accessToken = raw.accessToken,
                        expiresInSeconds = raw.expiresIn ?: 3600,
                        refreshToken = raw.refreshToken ?: refreshToken,
                        tokenType = raw.tokenType ?: "Bearer",
                    )
                } else null
            }
        } catch (e: Exception) {
            System.err.println("[SpotifyWebApiClient] refreshAccessToken exception: ${e.message}")
            null
        }
    }

    private fun sendCommand(accessToken: String, method: String, url: String): Boolean {
        if (accessToken.isBlank()) return false
        val emptyBody = "".toRequestBody(null)
        val builder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")

        when (method) {
            "PUT" -> builder.put(emptyBody)
            "POST" -> builder.post(emptyBody)
            else -> builder.get()
        }

        return try {
            client.newCall(builder.build()).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    private data class RawPlaybackResponse(
        @SerializedName("is_playing") val isPlaying: Boolean?,
        @SerializedName("progress_ms") val progressMs: Long?,
        val timestamp: Long?,
        val item: RawTrackItem?,
        val device: RawDeviceItem?,
        @SerializedName("shuffle_state") val shuffleState: Boolean? = null,
        @SerializedName("repeat_state") val repeatState: String? = null,
    )

    private data class RawQueueResponse(
        @SerializedName("currently_playing") val currentlyPlaying: RawTrackItem? = null,
        @SerializedName("queue") val queue: List<RawTrackItem>? = null,
    )

    private data class RawTrackItem(
        val id: String?,
        val name: String?,
        val artists: List<RawArtistItem>?,
        val album: RawAlbumItem?,
        @SerializedName("duration_ms") val durationMs: Long?,
    )

    private data class RawArtistItem(val name: String?)

    private data class RawAlbumItem(
        val name: String?,
        val images: List<RawImageItem>?,
    )

    private data class RawImageItem(
        val url: String?,
        val width: Int?,
        val height: Int?,
    )

    private data class RawDeviceItem(
        val name: String?,
        @SerializedName("volume_percent") val volumePercent: Int?,
    )

    private data class RawTokenResponse(
        @SerializedName("access_token") val accessToken: String?,
        @SerializedName("token_type") val tokenType: String?,
        @SerializedName("expires_in") val expiresIn: Int?,
        @SerializedName("refresh_token") val refreshToken: String?,
    )
}
