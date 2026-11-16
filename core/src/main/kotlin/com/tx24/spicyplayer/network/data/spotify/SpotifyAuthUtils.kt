package com.tx24.spicyplayer.network.data.spotify

import java.net.URLDecoder
import java.net.URLEncoder

object SpotifyAuthUtils {
    const val DEFAULT_SCOPES = "user-read-playback-state%20user-modify-playback-state%20user-read-currently-playing"
    const val DEEP_LINK_REDIRECT = "spicyremote://callback"
    const val LOCALHOST_REDIRECT = "http://127.0.0.1:8887/callback"

    fun getLocalhostRedirect(port: Int = 8887): String = "http://127.0.0.1:$port/callback"

    /**
     * Builds standard Spotify authorization URL.
     */
    fun buildAuthorizeUrl(clientId: String, redirectUri: String): String {
        val encClientId = URLEncoder.encode(clientId.trim(), "UTF-8")
        val encRedirect = URLEncoder.encode(redirectUri.trim(), "UTF-8")
        return "https://accounts.spotify.com/authorize?client_id=$encClientId&response_type=code&redirect_uri=$encRedirect&scope=$DEFAULT_SCOPES"
    }

    /**
     * Extracts pure authorization code from either a raw code string or a full redirect URL.
     */
    fun extractCode(input: String): String {
        val trimmed = input.trim()
        if (trimmed.contains("code=")) {
            val query = if (trimmed.contains('?')) trimmed.substringAfter('?') else trimmed
            val code = query.split('&').find { it.startsWith("code=") }?.substringAfter("code=")
            if (!code.isNullOrBlank()) {
                val clean = code.substringBefore('#')
                return try {
                    URLDecoder.decode(clean, "UTF-8")
                } catch (_: Exception) {
                    clean
                }
            }
        }
        return trimmed
    }
}
