package com.tx24.spicyplayer.playback.remote

import org.junit.Assert.assertNotNull
import org.junit.Test

class NetworkUtilsTest {
    @Test
    fun getLocalIpAddress_executesWithoutCrash() {
        val ip = NetworkUtils.getLocalIpAddress()
        // Might be null in isolated CI without network or valid IP in local environment
        if (ip != null) {
            assertNotNull(ip)
            assert(!ip.startsWith("127."))
        }
    }
}
