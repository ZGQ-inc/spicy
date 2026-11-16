package com.tx24.spicyplayer.ui.ambient

import com.tx24.spicyplayer.i18n.I18n
import com.tx24.spicyplayer.i18n.localizedCity
import com.tx24.spicyplayer.i18n.localizedWeather
import com.tx24.spicyplayer.i18n.t
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tx24.spicyplayer.lyrics.spicy.canvas.LyricsLayoutCalculator
import com.tx24.spicyplayer.network.data.weather.CurrentWeather
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Prominent, large-screen optimized ambient clock and weather header displayed directly
 * on the lyrics screen. Configurable for 12/24h, seconds, date, detailed weather metrics,
 * and scalable sizing for smart displays and TVs.
 */
@Composable
fun SmartDisplayHeader(
    weather: CurrentWeather?,
    useCelsius: Boolean = true,
    is12h: Boolean = false,
    showSeconds: Boolean = false,
    showDate: Boolean = true,
    showWeather: Boolean = true,
    showWeatherDetails: Boolean = true,
    clockScale: Float = 1.0f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var currentTimeText by remember { mutableStateOf("") }
    var currentSecondsText by remember { mutableStateOf("") }
    var currentAmPmText by remember { mutableStateOf("") }
    var currentDateText by remember { mutableStateOf("") }

    val isZh = I18n.isChinese()
    val timePattern = if (is12h) "hh:mm" else "HH:mm"
    val timeFormat = remember(is12h) { SimpleDateFormat(timePattern, Locale.getDefault()) }
    val amPmFormat = remember(isZh) {
        if (isZh) SimpleDateFormat("a", Locale.CHINESE)
        else SimpleDateFormat("a", Locale.ENGLISH)
    }
    val secondsFormat = remember { SimpleDateFormat(":ss", Locale.getDefault()) }
    val dateFormat = remember(I18n.appLanguage) {
        SimpleDateFormat(t("EEE, MMM d"), if (I18n.isChinese()) Locale.CHINESE else Locale.ENGLISH)
    }

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

    val baseClockSize = (64 * clockScale).sp
    val dateSize = (18 * clockScale).sp
    val tempSize = (42 * clockScale).sp
    val detailSize = (15 * clockScale).sp

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val timeTextStyle = remember(baseClockSize) {
        TextStyle(
            fontFamily = LyricsLayoutCalculator.spicyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = baseClockSize,
            color = Color.White,
            letterSpacing = 0.5.sp,
            lineHeight = baseClockSize,
            fontFeatureSettings = "tnum",
        )
    }

    val secondsTextStyle = remember(baseClockSize) {
        TextStyle(
            fontFamily = LyricsLayoutCalculator.spicyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (baseClockSize.value * 0.55f).sp,
            color = Color.White.copy(alpha = 0.75f),
            fontFeatureSettings = "tnum",
        )
    }

    val amPmTextStyle = remember(baseClockSize) {
        TextStyle(
            fontFamily = LyricsLayoutCalculator.spicyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (baseClockSize.value * 0.45f).sp,
            color = Color.White.copy(alpha = 0.85f),
            fontFeatureSettings = "tnum",
        )
    }

    val dateTextStyle = remember(dateSize) {
        TextStyle(
            fontSize = dateSize,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Normal,
        )
    }

    val maxTimeWidth = remember(timeTextStyle, is12h) {
        val sample1 = if (is12h) "12:88" else "88:88"
        val sample2 = if (is12h) "00:00" else "23:59"
        val m1 = textMeasurer.measure(sample1, timeTextStyle)
        val m2 = textMeasurer.measure(sample2, timeTextStyle)
        with(density) { maxOf(m1.size.width, m2.size.width).toDp() }
    }

    val maxSecondsWidth = remember(secondsTextStyle) {
        val m1 = textMeasurer.measure(":88", secondsTextStyle)
        val m2 = textMeasurer.measure(":00", secondsTextStyle)
        with(density) { maxOf(m1.size.width, m2.size.width).toDp() }
    }

    val maxAmPmWidth = remember(amPmTextStyle, isZh) {
        val sample = t(" PM")
        val m = textMeasurer.measure(sample, amPmTextStyle)
        with(density) { m.size.width.toDp() }
    }

    val maxDateWidth = remember(dateTextStyle, isZh) {
        val sample = t("Wednesday, Dec 31")
        val m = textMeasurer.measure(sample, dateTextStyle)
        with(density) { m.size.width.toDp() }
    }

    val totalTimeRowWidth = remember(maxTimeWidth, maxSecondsWidth, maxAmPmWidth, showSeconds, is12h) {
        maxTimeWidth + (if (showSeconds) maxSecondsWidth else 0.dp) + (if (is12h) maxAmPmWidth else 0.dp)
    }

    val leftColWidth = remember(totalTimeRowWidth, maxDateWidth, showDate) {
        if (showDate) maxOf(totalTimeRowWidth, maxDateWidth) else totalTimeRowWidth
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFocused) Color(0x551E93E5) else Color(0x38000000))
            .border(
                1.dp,
                if (isFocused) Color(30, 147, 229) else Color(0x28FFFFFF),
                RoundedCornerShape(16.dp),
            )
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left Column: Time, AM/PM, Seconds & Date (Width strictly fixed to prevent box resizing/jittering)
        Column(
            modifier = Modifier.width(leftColWidth),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = currentTimeText.ifBlank { "--:--" },
                    style = timeTextStyle,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.width(maxTimeWidth),
                )

                if (showSeconds) {
                    Text(
                        text = currentSecondsText,
                        style = secondsTextStyle,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .width(maxSecondsWidth)
                            .padding(bottom = 2.dp),
                    )
                }

                if (is12h && currentAmPmText.isNotBlank()) {
                    Text(
                        text = " $currentAmPmText",
                        style = amPmTextStyle,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .width(maxAmPmWidth)
                            .padding(bottom = 2.dp),
                    )
                }
            }

            if (showDate && currentDateText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentDateText,
                    style = dateTextStyle,
                    maxLines = 1,
                )
            }
        }

        // Right Column: Comprehensive Weather Information
        if (showWeather && weather != null) {
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                // Main Temp & Condition Description with inline weather icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Weather Icon inline before temperature
                    Icon(
                        imageVector = WeatherIcons.getIcon(weather.iconCode, weather.condition),
                        contentDescription = null,
                        tint = WeatherIcons.getTint(weather.iconCode, weather.condition, isNightMode = false),
                        modifier = Modifier.size((tempSize.value * 0.8f).dp),
                    )

                    val tempVal = if (useCelsius) "${weather.tempC.toInt()}°" else "${weather.tempF.toInt()}°"
                    Text(
                        text = tempVal,
                        style = TextStyle(
                            fontFamily = LyricsLayoutCalculator.spicyFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = tempSize,
                            color = Color.White,
                            lineHeight = tempSize,
                            fontFeatureSettings = "tnum",
                        ),
                    )
                    val cond = localizedWeather(weather.description, weather.condition)
                    if (cond.isNotBlank()) {
                        Text(
                            text = cond,
                            fontSize = detailSize,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 1.dp),
                        )
                    }
                }

                // Detailed metrics: feels like, min/max, humidity, city
                if (showWeatherDetails) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val city = localizedCity(weather.cityName)
                        if (city.isNotBlank()) {
                            Text(
                                text = city,
                                fontSize = detailSize,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                        val feels = if (useCelsius) "${weather.feelsLikeC.toInt()}°" else "${(weather.feelsLikeC * 9f / 5f + 32).toInt()}°"
                        Text(
                            text = t("Feels like %s", feels),
                            fontSize = detailSize,
                            color = Color.White.copy(alpha = 0.65f),
                        )
                        Text(
                            text = t("Humidity %d%%", weather.humidity),
                            fontSize = detailSize,
                            color = Color.White.copy(alpha = 0.65f),
                        )
                    }
                }
            }
        }
    }
}
