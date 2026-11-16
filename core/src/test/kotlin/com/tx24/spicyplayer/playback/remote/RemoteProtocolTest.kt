package com.tx24.spicyplayer.playback.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteProtocolTest {

    @Test
    fun testSerializationAndDeserialization() {
        val state = RemoteStatePayload(
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            durationMs = 354000L,
            positionMs = 120000L,
            isPlaying = true,
            lyricDelayMs = -50,
            currentLyricsLine = "Is this the real life? Is this just fantasy?",
            weatherCondition = "Clear",
            weatherTempC = 22.5f,
            cityName = "Tokyo",
            rendererStyle = "Bloom",
            lyricsSizePercent = 110,
        )

        val message = RemoteMessage(
            type = RemoteMessageType.STATE_SYNC,
            state = state,
        )

        val json = message.toJson()
        assertTrue(json.contains("Bohemian Rhapsody"))
        assertTrue(json.contains("Bloom"))

        val restored = RemoteMessage.fromJson(json)
        assertNotNull(restored)
        assertEquals(RemoteMessageType.STATE_SYNC, restored!!.type)
        assertEquals("Queen", restored.state?.artist)
        assertEquals(120000L, restored.state?.positionMs)
        assertEquals(-50, restored.state?.lyricDelayMs)
    }

    @Test
    fun testCommandSerialization() {
        val cmd = RemoteCommandPayload(
            action = RemoteAction.DELAY_DELTA,
            valueInt = 50,
        )
        val message = RemoteMessage(
            type = RemoteMessageType.COMMAND,
            command = cmd,
        )
        val json = message.toJson()
        val restored = RemoteMessage.fromJson(json)
        assertNotNull(restored)
        assertEquals(RemoteAction.DELAY_DELTA, restored!!.command?.action)
        assertEquals(50, restored.command?.valueInt)
    }

    @Test
    fun testConfigSerialization() {
        val config = RemoteConfigPayload(
            owmApiKey = "test_owm_key",
            owmCity = "Beijing",
            spotifyRefreshToken = "test_refresh_token",
            spotifyClientId = "test_client_id",
        )
        val message = RemoteMessage(
            type = RemoteMessageType.CONFIG_SYNC,
            config = config,
        )
        val json = message.toJson()
        val restored = RemoteMessage.fromJson(json)
        assertNotNull(restored)
        assertEquals("test_owm_key", restored!!.config?.owmApiKey)
        assertEquals("Beijing", restored.config?.owmCity)
        assertEquals("test_refresh_token", restored.config?.spotifyRefreshToken)
    }
}
