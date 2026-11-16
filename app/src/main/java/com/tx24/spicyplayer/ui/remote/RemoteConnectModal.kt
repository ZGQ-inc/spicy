package com.tx24.spicyplayer.ui.remote

import com.tx24.spicyplayer.i18n.t
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tx24.spicyplayer.playback.remote.DiscoveredDisplay
import com.tx24.spicyplayer.playback.remote.RemoteNsdHelper
import com.tx24.spicyplayer.playback.remote.RemoteSocketClient
import com.tx24.spicyplayer.ui.components.SpicyButton
import com.tx24.spicyplayer.ui.components.SpicyButtonStyle
import com.tx24.spicyplayer.ui.components.SpicyIconButton
import com.tx24.spicyplayer.ui.components.SpicyModal
import com.tx24.spicyplayer.ui.components.SpicyModalActions
import com.tx24.spicyplayer.ui.components.SpicyModalButton
import com.tx24.spicyplayer.ui.components.SpicyModalGap
import com.tx24.spicyplayer.ui.components.SpicyTextField
import com.tx24.spicyplayer.ui.components.outlinedCard
import com.tx24.spicyplayer.ui.settings.AppSettings
import com.tx24.spicyplayer.ui.theme.SpicyColors
import com.tx24.spicyplayer.ui.theme.SpicyRadii
import com.tx24.spicyplayer.ui.theme.SpicySpacing
import com.tx24.spicyplayer.ui.theme.SpicyType
import dev.chrisbanes.haze.HazeState

@Composable
fun RemoteConnectModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    client: RemoteSocketClient,
    settings: AppSettings,
    nsdHelper: RemoteNsdHelper,
    backdrop: HazeState?,
    onConnect: (String, Int) -> Unit = { host, port ->
        settings.remoteTargetHost = host
        settings.remoteTargetPort = port
        client.connect(host, port)
    },
) {
    val isConnected by client.isConnected.collectAsState()
    val connectionStatus by client.connectionStatus.collectAsState()
    val discoveredDisplays by nsdHelper.discoveredDisplays.collectAsState()
    var connectingIp by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isConnected) {
        if (isConnected && connectingIp != null) {
            connectingIp = null
            onDismissRequest()
        }
    }

    LaunchedEffect(connectionStatus) {
        if (connectionStatus.startsWith("连接失败")) {
            connectingIp = null
        }
    }

    DisposableEffect(visible) {
        if (visible) {
            nsdHelper.startDiscovery()
        }
        onDispose {
            nsdHelper.stopDiscovery()
        }
    }

    val onRefreshDiscovery = {
        nsdHelper.startDiscovery()
    }

    SpicyModal(
        visible = visible,
        onDismissRequest = onDismissRequest,
        backdrop = backdrop,
        title = t("Connect to Target Display"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(SpicySpacing.S3),
        ) {
            // 1. Connection Status Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .outlinedCard(tinted = true)
                    .padding(SpicySpacing.S3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) SpicyColors.StatusSuccess else SpicyColors.StatusDanger),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isConnected) t("Connected to Target Display") else t("Not connected to display"),
                            style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = if (isConnected) {
                                if (settings.remoteDeviceName.isNotBlank()) "${settings.remoteDeviceName} · ${settings.remoteTargetHost}"
                                else settings.remoteTargetHost
                            } else connectionStatus,
                            style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary),
                        )
                    }
                }

                if (isConnected) {
                    SpicyButton(
                        text = t("Disconnect"),
                        brand = SpicyColors.StatusDanger,
                        horizontalPadding = 12.dp,
                        onClick = { client.disconnect() },
                    )
                }
            }

            // 2. Discovered Devices
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpicySpacing.S1),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = SpicyColors.Accent,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = t("Auto-discover Displays on LAN"),
                        style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                SpicyIconButton(onClick = onRefreshDiscovery) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = t("Refresh"),
                        tint = SpicyColors.TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (discoveredDisplays.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .outlinedCard(tinted = false)
                        .padding(vertical = 20.dp, horizontal = SpicySpacing.S4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SpicyColors.Accent,
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = t("Searching for display devices on the local network…"),
                        style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary),
                    )
                    Text(
                        text = t("Please make sure both devices are on the same Wi-Fi network"),
                        style = SpicyType.Caption.copy(color = SpicyColors.TextTertiary, fontSize = 11.sp),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (device in discoveredDisplays) {
                        val ipStr = device.host.hostAddress ?: ""
                        val isCurrent = isConnected && settings.remoteTargetHost.startsWith(ipStr)
                        val shape = RoundedCornerShape(SpicyRadii.Md)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .then(
                                    if (isCurrent) Modifier.background(SpicyColors.TintBg).border(1.dp, SpicyColors.HairlineStrong, shape)
                                    else Modifier.outlinedCard(tinted = true),
                                )
                                .padding(horizontal = SpicySpacing.S3, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(SpicyRadii.Sm))
                                    .background(if (isCurrent) SpicyColors.Accent.copy(alpha = 0.2f) else SpicyColors.TintBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.Tv,
                                    contentDescription = null,
                                    tint = if (isCurrent) SpicyColors.Accent else SpicyColors.TextSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.name, style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold))
                                Text(
                                    "$ipStr:${device.port}",
                                    style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary, fontFamily = FontFamily.Monospace),
                                )
                            }

                            if (isCurrent) {
                                Text(
                                    text = t("Connected"),
                                    style = SpicyType.Caption.copy(color = SpicyColors.StatusSuccess, fontWeight = FontWeight.SemiBold),
                                )
                            } else if (connectingIp == ipStr) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = SpicyColors.Accent)
                                    Text(t("Connecting…"), style = SpicyType.Caption.copy(color = SpicyColors.Accent))
                                }
                            } else {
                                SpicyButton(
                                    text = t("Connect"),
                                    onClick = {
                                        connectingIp = ipStr
                                        onConnect(ipStr, device.port)
                                    },
                                    horizontalPadding = 14.dp,
                                )
                            }
                        }
                    }
                }
            }

            // 3. Manual IP Direct Connect
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpicySpacing.S1),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Rounded.Link,
                    contentDescription = null,
                    tint = SpicyColors.Accent,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = t("Manual IP Direct Connect"),
                    style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold),
                )
            }

            Text(
                text = t("If your router has AP isolation enabled, check the IP in [Settings -> Display Receiver Service] on the display device and enter it here:"),
                style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary),
            )

            var ipInput by remember(settings.remoteTargetHost) {
                mutableStateOf(settings.remoteTargetHost.substringBefore(":").ifBlank { "" })
            }
            var portInput by remember(settings.remoteTargetHost) {
                mutableStateOf(settings.remoteTargetHost.substringAfter(":", "8887"))
            }

            val doManualConnect = {
                val raw = ipInput.trim()
                if (raw.isNotBlank()) {
                    val host = if (raw.contains(":")) raw.substringBefore(":") else raw
                    val port = if (raw.contains(":")) raw.substringAfter(":").toIntOrNull() ?: 8887 else (portInput.toIntOrNull() ?: 8887)
                    connectingIp = host
                    onConnect(host, port)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2),
            ) {
                SpicyTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    placeholder = t("Display IP (e.g. 192.168.1.100)"),
                    modifier = Modifier.weight(1f),
                    leading = {
                        Icon(Icons.Rounded.Language, null, tint = SpicyColors.TextTertiary, modifier = Modifier.size(18.dp))
                    },
                    onSearch = doManualConnect,
                )
                SpicyTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    placeholder = "8887",
                    modifier = Modifier.width(76.dp),
                    onSearch = doManualConnect,
                )
                SpicyButton(
                    text = t("Connect"),
                    onClick = doManualConnect,
                    modifier = Modifier.height(40.dp),
                    enabled = ipInput.isNotBlank(),
                )
            }

            SpicyModalGap()
            SpicyModalActions {
                SpicyModalButton(
                    text = t("Close"),
                    style = SpicyButtonStyle.Quiet,
                    onClick = onDismissRequest,
                )
            }
        }
    }
}
