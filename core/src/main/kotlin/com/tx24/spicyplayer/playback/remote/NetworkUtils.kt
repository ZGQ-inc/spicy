package com.tx24.spicyplayer.playback.remote

import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Utility functions for local networking inspection.
 */
object NetworkUtils {
    /**
     * Resolves the primary local IPv4 address (e.g. 192.168.x.x or 10.x.x.x) of the device.
     * Returns null if no active non-loopback network interface is found.
     */
    fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            val addresses = mutableListOf<String>()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (host != null && !host.startsWith("127.")) {
                            addresses.add(host)
                        }
                    }
                }
            }
            // Prefer common Wi-Fi/LAN private subnets
            addresses.firstOrNull { it.startsWith("192.168.") }
                ?: addresses.firstOrNull { it.startsWith("10.") }
                ?: addresses.firstOrNull { it.startsWith("172.") }
                ?: addresses.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }
}
