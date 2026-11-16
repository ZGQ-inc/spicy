package com.tx24.spicyplayer.playback.remote

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

data class DiscoveredDisplay(
    val name: String,
    val host: InetAddress,
    val port: Int,
)

/**
 * mDNS / NSD discovery helper for zero-configuration local networking.
 * Implements sequential resolve queue and Wi-Fi MulticastLock for maximum stability.
 */
class RemoteNsdHelper(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private var multicastLock: WifiManager.MulticastLock? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val mutableDiscovered = MutableStateFlow<List<DiscoveredDisplay>>(emptyList())
    val discovered = mutableDiscovered.asStateFlow()
    val discoveredDisplays = discovered

    private val resolveQueue = ConcurrentLinkedQueue<NsdServiceInfo>()
    @Volatile
    private var isResolving = false

    fun discoverServices() = startDiscovery()

    fun registerService(serviceName: String, port: Int) {
        unregisterService()
        acquireMulticastLock()

        val info = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {}
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {}
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}
        }

        try {
            nsdManager?.registerService(info, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (_: Exception) {}
    }

    fun unregisterService() {
        registrationListener?.let {
            try { nsdManager?.unregisterService(it) } catch (_: Exception) {}
            registrationListener = null
        }
        releaseMulticastLockIfIdle()
    }

    fun startDiscovery() {
        stopDiscovery()
        acquireMulticastLock()
        mutableDiscovered.value = emptyList()

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String?) {}
            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo?.serviceType?.contains(SERVICE_TYPE_CLEAN) == true) {
                    enqueueResolve(serviceInfo)
                }
            }
            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                val lostName = serviceInfo?.serviceName ?: return
                mutableDiscovered.value = mutableDiscovered.value.filter { it.name != lostName }
            }
            override fun onDiscoveryStopped(serviceType: String?) {}
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {}
        }

        try {
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (_: Exception) {}
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try { nsdManager?.stopServiceDiscovery(it) } catch (_: Exception) {}
            discoveryListener = null
        }
        resolveQueue.clear()
        isResolving = false
        releaseMulticastLockIfIdle()
    }

    private fun enqueueResolve(serviceInfo: NsdServiceInfo) {
        resolveQueue.offer(serviceInfo)
        processNextResolve()
    }

    @Synchronized
    private fun processNextResolve() {
        if (isResolving) return
        val next = resolveQueue.poll() ?: return
        isResolving = true

        val resolver = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                isResolving = false
                processNextResolve()
            }

            override fun onServiceResolved(resolved: NsdServiceInfo?) {
                isResolving = false
                val host = resolved?.host
                val port = resolved?.port ?: 0
                val name = resolved?.serviceName.orEmpty()
                if (host != null && port > 0) {
                    val ipStr = host.hostAddress.orEmpty()
                    val isIpv4 = host is java.net.Inet4Address
                    // Filter out link-local and loopback addresses
                    if (!host.isLinkLocalAddress && !host.isLoopbackAddress && !ipStr.startsWith("fe80:")) {
                        val display = DiscoveredDisplay(name, host, port)
                        val existing = mutableDiscovered.value.firstOrNull { it.name == name }
                        // Prefer IPv4 over IPv6 for reliable LAN connection
                        if (existing != null && existing.host is java.net.Inet4Address && !isIpv4) {
                            // Keep existing IPv4
                        } else {
                            val filtered = mutableDiscovered.value.filter { it.name != name && it.host.hostAddress != ipStr }
                            mutableDiscovered.value = filtered + display
                        }
                    }
                }
                processNextResolve()
            }
        }

        try {
            nsdManager?.resolveService(next, resolver)
        } catch (_: Exception) {
            isResolving = false
            processNextResolve()
        }
    }

    private fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("SpicyRemoteLock")?.apply {
                    setReferenceCounted(false)
                    acquire()
                }
            }
        } catch (_: Exception) {}
    }

    private fun releaseMulticastLockIfIdle() {
        if (registrationListener == null && discoveryListener == null) {
            try {
                multicastLock?.release()
                multicastLock = null
            } catch (_: Exception) {}
        }
    }

    companion object {
        const val SERVICE_TYPE = "_spicy-remote._tcp."
        const val SERVICE_TYPE_CLEAN = "_spicy-remote._tcp"
    }
}
