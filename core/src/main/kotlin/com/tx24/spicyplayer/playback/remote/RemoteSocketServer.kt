package com.tx24.spicyplayer.playback.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Embedded socket server running on the Receiver (Speaker / TV) to handle remote commands and config.
 */
class RemoteSocketServer(
    private val scope: CoroutineScope,
    val port: Int = 8887,
) {
    companion object {
        private const val TAG = "SpicyRemoteServer"
    }

    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null
    private val activeClients = CopyOnWriteArrayList<Socket>()
    private val clientWriters = ConcurrentHashMap<Socket, PrintWriter>()

    private val mutableClientsCount = MutableStateFlow(0)
    val clientsCount = mutableClientsCount.asStateFlow()

    private val mutableLatestClient = MutableStateFlow<String?>(null)
    val latestClient = mutableLatestClient.asStateFlow()

    private val mutableConnectedClients = MutableStateFlow<List<String>>(emptyList())
    val connectedClients = mutableConnectedClients.asStateFlow()

    var onMessageReceived: ((RemoteMessage) -> Unit)? = null
    var onSpotifyAuthCodeReceived: ((String) -> Unit)? = null
    var onClientConnected: ((String) -> Unit)? = null
    var onClientDisconnected: ((String) -> Unit)? = null

    var getInitialState: (() -> RemoteStatePayload)? = null
    var getInitialConfig: (() -> RemoteConfigPayload)? = null

    @Volatile
    private var isStarting = false

    fun isRunning(): Boolean {
        val s = serverSocket
        return s != null && !s.isClosed && s.isBound && acceptJob?.isActive == true
    }

    fun start() {
        if (isRunning()) {
            RemoteLog.d(TAG, "Server already running on port $port, skipping start")
            return
        }
        if (isStarting) return
        isStarting = true

        stop()
        acceptJob = scope.launch(Dispatchers.IO) {
            try {
                var server: ServerSocket? = null
                for (attempt in 1..4) {
                    try {
                        val s = ServerSocket().apply {
                            reuseAddress = true
                            bind(InetSocketAddress(port))
                        }
                        server = s
                        break
                    } catch (e: java.net.BindException) {
                        RemoteLog.w(TAG, "Port $port already in use (attempt $attempt/4), waiting to retry...", e)
                        if (attempt < 4) delay(500L) else throw e
                    }
                }

                serverSocket = server
                RemoteLog.i(TAG, "RemoteSocketServer listening on port $port")
                isStarting = false

                while (isActive) {
                    try {
                        val client = server?.accept() ?: break
                        client.tcpNoDelay = true
                        client.soTimeout = 0 // Keep connection open
                        activeClients.add(client)
                        val writer = PrintWriter(client.getOutputStream(), true)
                        clientWriters[client] = writer

                        val clientIp = client.inetAddress?.hostAddress ?: "Unknown"
                        RemoteLog.i(TAG, "Client connected: $clientIp, total active clients: ${activeClients.size}")
                        mutableClientsCount.value = activeClients.size
                        mutableLatestClient.value = clientIp
                        mutableConnectedClients.value = activeClients.mapNotNull { it.inetAddress?.hostAddress }
                        try {
                            onClientConnected?.invoke(clientIp)
                        } catch (e: Throwable) {
                            RemoteLog.e(TAG, "Error in onClientConnected callback", e)
                        }

                        // Immediately push initial full state and config to the newly connected client
                        scope.launch(Dispatchers.IO) {
                            try {
                                getInitialState?.invoke()?.let {
                                    sendToClient(client, RemoteMessage(type = RemoteMessageType.STATE_SYNC, state = it))
                                }
                                getInitialConfig?.invoke()?.let {
                                    sendToClient(client, RemoteMessage(type = RemoteMessageType.CONFIG_SYNC, config = it))
                                }
                            } catch (e: Exception) {
                                RemoteLog.w(TAG, "Failed to send initial state/config to $clientIp", e)
                            }
                        }

                        handleClient(client)
                    } catch (e: Exception) {
                        if (server?.isClosed == true || !isActive) {
                            RemoteLog.i(TAG, "ServerSocket closed, stopping accept loop")
                            break
                        }
                        RemoteLog.w(TAG, "Error accepting client connection", e)
                        delay(200L)
                    }
                }
            } catch (e: Exception) {
                RemoteLog.e(TAG, "Failed to start or maintain ServerSocket on port $port", e)
            } finally {
                isStarting = false
            }
        }
    }

    fun stop() {
        RemoteLog.i(TAG, "Stopping RemoteSocketServer on port $port")
        acceptJob?.cancel()
        acceptJob = null
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        for (client in activeClients) {
            try { client.close() } catch (_: Exception) {}
        }
        activeClients.clear()
        clientWriters.clear()
        mutableClientsCount.value = 0
        mutableLatestClient.value = null
        mutableConnectedClients.value = emptyList()
        isStarting = false
    }

    private fun removeClient(client: Socket) {
        val clientIp = client.inetAddress?.hostAddress ?: "Unknown"
        RemoteLog.i(TAG, "Removing client $clientIp")
        try { client.close() } catch (_: Exception) {}
        clientWriters.remove(client)
        if (activeClients.remove(client)) {
            mutableClientsCount.value = activeClients.size
            mutableConnectedClients.value = activeClients.mapNotNull { it.inetAddress?.hostAddress }
            try {
                onClientDisconnected?.invoke(clientIp)
            } catch (e: Throwable) {
                RemoteLog.e(TAG, "Error in onClientDisconnected callback", e)
            }
        }
    }

    fun sendToClient(socket: Socket, message: RemoteMessage) {
        val writer = clientWriters[socket] ?: return
        val json = message.toJson() + "\n"
        scope.launch(Dispatchers.IO) {
            try {
                synchronized(socket) {
                    writer.print(json)
                    writer.flush()
                }
            } catch (e: Exception) {
                RemoteLog.w(TAG, "Error writing to socket ${socket.inetAddress?.hostAddress}", e)
                removeClient(socket)
            }
        }
    }

    fun broadcast(message: RemoteMessage) {
        val json = message.toJson() + "\n"
        scope.launch(Dispatchers.IO) {
            for (client in activeClients) {
                val writer = clientWriters[client] ?: continue
                try {
                    synchronized(client) {
                        writer.print(json)
                        writer.flush()
                    }
                } catch (e: Exception) {
                    RemoteLog.w(TAG, "Error broadcasting to client ${client.inetAddress?.hostAddress}", e)
                    removeClient(client)
                }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val clientIp = socket.inetAddress?.hostAddress ?: "Unknown"
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isActive && !socket.isClosed) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) {
                        // Check if this is an HTTP GET request (e.g. from browser OAuth redirect)
                        if (line.startsWith("GET ") && (line.contains("/callback") || line.contains("/spotify-callback"))) {
                            handleHttpCallback(socket, line)
                            break
                        }
                        val msg = RemoteMessage.fromJson(line)
                        if (msg != null && msg.type != null) {
                            if (msg.type == RemoteMessageType.REQUEST_STATE || msg.type == RemoteMessageType.PING) {
                                if (msg.type == RemoteMessageType.PING) {
                                    sendToClient(socket, RemoteMessage(type = RemoteMessageType.PONG))
                                }
                                getInitialState?.invoke()?.let {
                                    sendToClient(socket, RemoteMessage(type = RemoteMessageType.STATE_SYNC, state = it))
                                }
                                getInitialConfig?.invoke()?.let {
                                    sendToClient(socket, RemoteMessage(type = RemoteMessageType.CONFIG_SYNC, config = it))
                                }
                            }
                            try {
                                onMessageReceived?.invoke(msg)
                            } catch (e: Throwable) {
                                RemoteLog.e(TAG, "Error handling message ${msg.type} from $clientIp", e)
                            }
                        } else {
                            RemoteLog.w(TAG, "Failed to parse RemoteMessage from $clientIp: ${line.take(100)}")
                        }
                    }
                }
            } catch (e: Exception) {
                RemoteLog.i(TAG, "Socket reader closed for $clientIp: ${e.message}")
            } finally {
                removeClient(socket)
            }
        }
    }

    private fun handleHttpCallback(socket: Socket, requestLine: String) {
        val queryStart = requestLine.indexOf('?')
        val queryEnd = requestLine.indexOf(" HTTP/", if (queryStart >= 0) queryStart else 0)
        val query = if (queryStart != -1 && queryEnd != -1) {
            requestLine.substring(queryStart + 1, queryEnd)
        } else if (queryStart != -1) {
            requestLine.substring(queryStart + 1).trim()
        } else ""

        val params = query.split('&').associate { param ->
            val parts = param.split('=', limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
        }

        val code = params["code"]
        val error = params["error"]

        val bodyContent = if (!code.isNullOrBlank()) {
            val decodedCode = java.net.URLDecoder.decode(code, "UTF-8")
            onSpotifyAuthCodeReceived?.invoke(decodedCode)
            """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Spotify 授权成功</title></head>
                <body style="margin:0;font-family:-apple-system,BlinkMacSystemFont,sans-serif;background:#121212;color:#ffffff;display:flex;align-items:center;justify-content:center;min-height:100vh;">
                    <div style="text-align:center;padding:32px 24px;background:#1e1e1e;border-radius:20px;box-shadow:0 12px 36px rgba(0,0,0,0.5);max-width:380px;width:90%;">
                        <div style="font-size:54px;margin-bottom:12px;">🎉</div>
                        <h2 style="color:#1ed760;margin:0 0 12px 0;">Spotify 授权成功！</h2>
                        <p style="color:#cccccc;font-size:15px;line-height:1.6;margin:0 0 20px 0;">已成功将凭证交付大屏，大屏已开启音乐同步与歌词展示。</p>
                        <p style="color:#888888;font-size:13px;margin:0;">您可以安全关闭此网页。</p>
                    </div>
                </body>
                </html>
            """.trimIndent()
        } else {
            """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>授权未完成</title></head>
                <body style="margin:0;font-family:-apple-system,BlinkMacSystemFont,sans-serif;background:#121212;color:#ffffff;display:flex;align-items:center;justify-content:center;min-height:100vh;">
                    <div style="text-align:center;padding:32px 24px;background:#1e1e1e;border-radius:20px;max-width:380px;width:90%;">
                        <div style="font-size:54px;margin-bottom:12px;">⚠️</div>
                        <h2 style="color:#ff453a;margin:0 0 12px 0;">授权未完成</h2>
                        <p style="color:#cccccc;font-size:15px;margin:0;">原因: ${error ?: "未收到有效的授权 Code"}</p>
                    </div>
                </body>
                </html>
            """.trimIndent()
        }

        val response = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: ${bodyContent.toByteArray(Charsets.UTF_8).size}\r\n" +
            "Connection: close\r\n\r\n" +
            bodyContent

        try {
            val out = socket.getOutputStream()
            out.write(response.toByteArray(Charsets.UTF_8))
            out.flush()
        } catch (_: Exception) {}
    }
}
