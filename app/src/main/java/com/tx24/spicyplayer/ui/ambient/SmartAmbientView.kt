package com.tx24.spicyplayer.ui.ambient

import com.tx24.spicyplayer.i18n.I18n
import com.tx24.spicyplayer.i18n.localizedCity
import com.tx24.spicyplayer.i18n.localizedWeather
import com.tx24.spicyplayer.i18n.t
import com.tx24.spicyplayer.ui.components.rememberPixelShiftOffset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tx24.spicyplayer.network.data.weather.CurrentWeather
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * All-day smart ambient display for smart speakers and TVs.
 * Displays high-aesthetic large clock, date, OpenWeatherMap real-time weather details,
 * with pixel-shifting screen protection and night mode support.
 */
@Composable
fun SmartAmbientView(
    weather: CurrentWeather?,
    isNightMode: Boolean,
    pixelShiftEnabled: Boolean,
    useCelsius: Boolean,
    is12h: Boolean = false,
    showSeconds: Boolean = true,
    showDate: Boolean = true,
    showWeather: Boolean = true,
    showWeatherDetails: Boolean = true,
    clockScale: Float = 1.0f,
    nowPlayingTitle: String? = null,
    nowPlayingArtist: String? = null,
    isPlaying: Boolean = false,
    showBatteryIndicator: Boolean = true,
    batteryScale: Float = 1.0f,
    onWakeRequested: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onToggleNightMode: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var currentTimeText by remember { mutableStateOf("") }
    var currentDateText by remember { mutableStateOf("") }
    var currentSecondsText by remember { mutableStateOf("") }
    var currentAmPmText by remember { mutableStateOf("") }
    var showQuickBar by remember { mutableStateOf(false) }

    val isZh = I18n.isChinese()
    val timePattern = if (is12h) "hh:mm" else "HH:mm"
    val timeFormat = remember(is12h) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val amPmFormat = remember(isZh) {
        if (isZh) SimpleDateFormat("a", Locale.CHINESE)
        else SimpleDateFormat("a", Locale.ENGLISH)
    }
    val secondsFormat = remember { SimpleDateFormat(":ss", Locale.getDefault()) }
    val dateFormat = remember(I18n.appLanguage) {
        SimpleDateFormat(t("EEEE, MMMM d, yyyy"), if (I18n.isChinese()) Locale.CHINESE else Locale.ENGLISH)
    }

    // Pixel shift offsets (burn-in prevention linked directly to element scale)
    val (clockShiftX, clockShiftY) = rememberPixelShiftOffset(
        enabled = pixelShiftEnabled,
        scale = clockScale,
        baseRadiusDp = 16f,
    )
    val (batteryShiftX, batteryShiftY) = rememberPixelShiftOffset(
        enabled = pixelShiftEnabled,
        scale = batteryScale,
        baseRadiusDp = 10f,
    )

    // Time ticker
    LaunchedEffect(is12h, showSeconds) {
        while (true) {
            val now = Date()
            currentTimeText = timeFormat.format(now)
            if (is12h) currentAmPmText = amPmFormat.format(now)
            if (showSeconds) currentSecondsText = secondsFormat.format(now)
            currentDateText = dateFormat.format(now)
            delay(1000L)
        }
    }

    // Auto-hide quick toolbar after 5 seconds
    LaunchedEffect(showQuickBar) {
        if (showQuickBar) {
            delay(5000L)
            showQuickBar = false
        }
    }

    val textColor by animateColorAsState(
        if (isNightMode) Color(0xFFE67E22).copy(alpha = 0.75f) else Color.White,
        tween(1000),
        label = "textColor",
    )
    val subTextColor by animateColorAsState(
        if (isNightMode) Color(0xFFD35400).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.75f),
        tween(1000),
        label = "subTextColor",
    )
    val cardBackground by animateColorAsState(
        if (isNightMode) Color(0x33000000) else Color(0x22FFFFFF),
        tween(1000),
        label = "cardBg",
    )

    val mainClockFontSize = (144 * clockScale).sp
    val secondsFontSize = (44 * clockScale).sp
    val dateFontSize = (28 * clockScale).sp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDragX = 0f
                    var totalDragY = 0f
                    var isDrag = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        if (!change.pressed) {
                            if (isDrag) {
                                val distance = kotlin.math.hypot(totalDragX, totalDragY)
                                if (distance > 45.dp.toPx()) {
                                    onWakeRequested()
                                }
                            } else {
                                showQuickBar = !showQuickBar
                            }
                            break
                        }
                        val dragAmount = change.position - change.previousPosition
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                        if (kotlin.math.hypot(totalDragX, totalDragY) > 15f) {
                            isDrag = true
                            change.consume()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .offset(clockShiftX, clockShiftY)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Main Large Clock
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = currentTimeText.ifBlank { "12:00" },
                    fontSize = mainClockFontSize,
                    fontWeight = FontWeight.ExtraLight,
                    fontFamily = FontFamily.SansSerif,
                    color = textColor,
                    letterSpacing = 2.sp,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                )
                if (showSeconds && !isNightMode) {
                    Text(
                        text = currentSecondsText,
                        fontSize = secondsFontSize,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        color = subTextColor,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                        modifier = Modifier.padding(bottom = (20 * clockScale).dp, start = 4.dp),
                    )
                }
                if (is12h && currentAmPmText.isNotBlank()) {
                    Text(
                        text = " $currentAmPmText",
                        fontSize = (secondsFontSize.value * 0.8f).sp,
                        fontWeight = FontWeight.Normal,
                        color = subTextColor,
                        modifier = Modifier.padding(bottom = (20 * clockScale).dp, start = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date & Weekday
            if (showDate) {
                Text(
                    text = currentDateText.ifBlank { "" },
                    fontSize = dateFontSize,
                    fontWeight = FontWeight.Normal,
                    color = subTextColor,
                )
            }

            // Rich Weather Card
            if (showWeather && weather != null && !isNightMode) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .background(cardBackground, RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Primary weather row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = WeatherIcons.getIcon(weather.iconCode, weather.condition),
                            contentDescription = null,
                            tint = WeatherIcons.getTint(weather.iconCode, weather.condition, isNightMode),
                            modifier = Modifier.size((38 * clockScale.coerceIn(0.8f, 2.2f)).dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        val tempVal = if (useCelsius) "${weather.tempC.toInt()}°C" else "${weather.tempF.toInt()}°F"
                        Text(
                            text = tempVal,
                            fontSize = (32 * clockScale).sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        )
                        val cond = localizedWeather(weather.description, weather.condition)
                        if (cond.isNotBlank()) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cond,
                                fontSize = (22 * clockScale).sp,
                                color = subTextColor,
                            )
                        }
                    }

                    // Detailed weather metrics
                    if (showWeatherDetails) {
                        val detailFontSize = (15 * clockScale).sp
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            val city = localizedCity(weather.cityName)
                            if (city.isNotBlank()) {
                                Text(
                                    text = city,
                                    fontSize = detailFontSize,
                                    color = subTextColor,
                                )
                            }
                            val minMax = if (useCelsius) "${weather.tempMinC.toInt()}° / ${weather.tempMaxC.toInt()}°" else "${weather.tempMinF.toInt()}° / ${weather.tempMaxF.toInt()}°"
                            Text(
                                text = minMax,
                                fontSize = detailFontSize,
                                color = subTextColor,
                            )
                            val feels = if (useCelsius) "${weather.feelsLikeC.toInt()}°" else "${(weather.feelsLikeC * 9f / 5f + 32).toInt()}°"
                            Text(
                                text = t("Feels like %s", feels),
                                fontSize = detailFontSize,
                                color = subTextColor.copy(alpha = 0.8f),
                            )
                            Text(
                                text = t("Humidity %d%%", weather.humidity),
                                fontSize = detailFontSize,
                                color = subTextColor.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }

            // Now Playing / Recently Played Hint
            if (!nowPlayingTitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onWakeRequested,
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.PlayArrow else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = subTextColor,
                        modifier = Modifier.size(20.dp).padding(end = 6.dp),
                    )
                    Text(
                        text = buildAnnotatedString {
                            val isNothingPlaying = nowPlayingTitle.equals("Nothing playing", ignoreCase = true)
                            if (isNothingPlaying) {
                                withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Medium)) {
                                    append(t("Nothing playing"))
                                }
                            } else {
                                withStyle(SpanStyle(color = subTextColor, fontWeight = FontWeight.Normal)) {
                                    append(if (isPlaying) t("Now Playing: ") else t("Recently Played: "))
                                }
                                withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.Medium)) {
                                    append(nowPlayingTitle)
                                    if (!nowPlayingArtist.isNullOrBlank()) {
                                        append(" · $nowPlayingArtist")
                                    }
                                }
                            }
                        },
                        fontSize = (13 * clockScale.coerceIn(0.85f, 1.3f)).sp,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )
                }
            }
        }

        // Quick Bottom Action Bar
        if (showBatteryIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 28.dp, end = 28.dp)
                    .offset(batteryShiftX, batteryShiftY),
            ) {
                com.tx24.spicyplayer.ui.components.BatteryIndicator(scale = batteryScale)
            }
        }

        AnimatedVisibility(
            visible = showQuickBar,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xCC222222))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuickBarItem(
                    text = t("Back to Playback"),
                    onClick = onWakeRequested,
                )
                QuickBarItem(
                    text = if (isNightMode) t("Day Mode") else t("Night Glow"),
                    onClick = onToggleNightMode,
                )
                QuickBarItem(
                    text = t("System Settings"),
                    onClick = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun QuickBarItem(
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (isFocused) Color(30, 147, 229) else Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color(0x441E93E5) else Color.Transparent)
            .border(
                1.dp,
                if (isFocused) Color(30, 147, 229) else Color.Transparent,
                RoundedCornerShape(12.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
