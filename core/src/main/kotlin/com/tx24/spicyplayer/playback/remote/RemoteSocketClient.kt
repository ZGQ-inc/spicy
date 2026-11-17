package com.tx24.spicyplayer.playback.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Socket client running on the Controller (Phone) to communicate with Receiver (Speaker/TV).
 */
class RemoteSocketClient(
    private val scope: CoroutineScope,
) {
    companion object {
        private const val TAG = "SpicyRemoteClient"
    }

    private var socket: Socket? = null
    private var readJob: Job? = null
    private var heartbeatJob: Job? = null
    private var writer: PrintWriter? = null
    private val sendLock = Any()

    private val mutableState = MutableStateFlow<RemoteStatePayload?>(null)
    val state = mutableState.asStateFlow()

    private val mutableConnected = MutableStateFlow(false)
    val isConnected = mutableConnected.asStateFlow()
    val connected get() = isConnected

    private val mutableConnectionStatus = MutableStateFlow("未连接")
    val connectionStatus = mutableConnectionStatus.asStateFlow()
    val status get() = connectionStatus

    private val mutableConnectedHost = MutableStateFlow<String?>(null)
    val connectedHost = mutableConnectedHost.asStateFlow()

    private val mutableMessages = kotlinx.coroutines.flow.MutableSharedFlow<RemoteMessage>(extraBufferCapacity = 64)
    val messages = mutableMessages.asSharedFlow()

    var onMessageReceived: ((RemoteMessage) -> Unit)? = null

    @Volatile
    private var desiredHost: String? = null
    @Volatile
    private var desiredPort: Int = 8887
    @Volatile
    private var lastReceivedMs: Long = 0L

    fun connect(host: String, port: Int = 8887) {
        val cleanHost = host.trim()
        if (cleanHost.isBlank()) {
            mutableConnectionStatus.value = "请输入有效的 IP 地址"
            return
        }

        desiredHost = cleanHost
        desiredPort = port

        disconnectInternal()
        startConnectionLoop(cleanHost, port)
    }

    private fun startConnectionLoop(host: String, port: Int) {
        readJob?.cancel()
        readJob = scope.launch(Dispatchers.IO) {
            mutableConnectionStatus.value = "正在连接 $host:$port …"
            RemoteLog.i(TAG, "Attempting connection to $host:$port")
            try {
                val s = Socket()
                s.tcpNoDelay = true
                s.soTimeout = 15000 // 15s read timeout
                s.connect(InetSocketAddress(host, port), 4000)
                socket = s
                synchronized(sendLock) {
                    writer = PrintWriter(s.getOutputStream(), true)
                }
                lastReceivedMs = System.currentTimeMillis()
                mutableConnected.value = true
                mutableConnectedHost.value = "$host:$port"
                mutableConnectionStatus.value = "已连接到 $host:$port"
                RemoteLog.i(TAG, "Successfully connected to $host:$port")

                // Start heartbeat
                startHeartbeat()

                // Immediately request current full state and config
                sendMessage(RemoteMessage(type = RemoteMessageType.REQUEST_STATE))

                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                while (isActive && !s.isClosed) {
                    val line = reader.readLine() ?: break
                    lastReceivedMs = System.currentTimeMillis()
                    if (line.isNotBlank()) {
                        val msg = RemoteMessage.fromJson(line)
                        if (msg != null && msg.type != null) {
                            if (msg.type == RemoteMessageType.STATE_SYNC && msg.state != null) {
                                mutableState.value = msg.state
                            }
                            mutableMessages.tryEmit(msg)
                            try {
                                onMessageReceived?.invoke(msg)
                            } catch (e: Throwable) {
                                RemoteLog.e(TAG, "Error in onMessageReceived callback", e)
                            }
                        } else {
                            RemoteLog.w(TAG, "Failed to parse RemoteMessage: ${line.take(80)}")
                        }
                    }
                }
            } catch (e: Exception) {
                RemoteLog.w(TAG, "Connection error to $host:$port: ${e.message}")
                mutableConnectionStatus.value = "连接失败: ${e.message ?: e.javaClass.simpleName}"
            } finally {
                val wasConnected = mutableConnected.value
                disconnectInternal()
                if (wasConnected) {
                    RemoteLog.i(TAG, "Connection dropped from $host:$port")
                    mutableConnectionStatus.value = "受控端连接已断开，正在重试…"
                }

                // If user didn't explicitly disconnect, retry after delay
                if (desiredHost == host) {
                    delay(3000L)
                    if (isActive && desiredHost == host) {
                        RemoteLog.i(TAG, "Auto-reconnecting to $host:$port...")
                        startConnectionLoop(host, port)
                    }
                }
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch(Dispatchers.IO) {
            while (isActive && mutableConnected.value) {
                delay(4000L)
                if (!mutableConnected.value) break

                // Check staleness
                val elapsed = System.currentTimeMillis() - lastReceivedMs
                if (elapsed > 12000L) {
                    RemoteLog.w(TAG, "Heartbeat timeout ($elapsed ms without data from receiver), resetting socket")
                    try { socket?.close() } catch (_: Exception) {}
                    break
                }

                // Ping receiver
                sendMessage(RemoteMessage(type = RemoteMessageType.PING))
            }
        }
    }

    fun disconnect() {
        RemoteLog.i(TAG, "User requested disconnect")
        desiredHost = null
        heartbeatJob?.cancel()
        heartbeatJob = null
        readJob?.cancel()
        readJob = null
        disconnectInternal()
        mutableConnectionStatus.value = "已断开连接"
    }

    private fun disconnectInternal() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        synchronized(sendLock) {
            writer = null
        }
        mutableConnected.value = false
        mutableConnectedHost.value = null
    }

    fun sendCommand(
        action: RemoteAction,
        valueInt: Int? = null,
        valueLong: Long? = null,
        valueString: String? = null,
        valueBoolean: Boolean? = null,
    ) {
        val msg = RemoteMessage(
            type = RemoteMessageType.COMMAND,
            command = RemoteCommandPayload(action, valueInt, valueLong, valueString, valueBoolean),
        )
        sendMessage(msg)
    }

    fun sendConfig(config: RemoteConfigPayload) {
        val msg = RemoteMessage(
            type = RemoteMessageType.CONFIG_PUSH,
            config = config,
        )
        sendMessage(msg)
    }

    fun sendMessage(msg: RemoteMessage) {
        scope.launch(Dispatchers.IO) {
            try {
                val json = msg.toJson() + "\n"
                synchronized(sendLock) {
                    val w = writer ?: return@launch
                    w.print(json)
                    w.flush()
                }
            } catch (e: Exception) {
                RemoteLog.w(TAG, "Failed to send message ${msg.type}", e)
            }
        }
    }
}
