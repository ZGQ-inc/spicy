package com.tx24.spicyplayer.ui.ambient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tx24.spicyplayer.lyrics.spicy.canvas.LyricsLayoutCalculator
import com.tx24.spicyplayer.network.data.weather.CurrentWeather
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sleek, non-intrusive ambient clock & weather badge displayed directly on the lyrics screen.
 * Perfect for all-day smart displays (smart speakers, Android TV) while music is playing.
 */
@Composable
fun LyricsScreenClockWidget(
    weather: CurrentWeather?,
    useCelsius: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var currentTimeText by remember { mutableStateOf("") }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeText = timeFormat.format(Date())
            delay(1000L)
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x38000000))
            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(20.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = currentTimeText.ifBlank { "--:--" },
            fontFamily = LyricsLayoutCalculator.spicyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.95f),
            letterSpacing = 0.5.sp,
        )

        if (weather != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = weather.emoji,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.width(3.dp))
            val temp = if (useCelsius) "${weather.tempC.toInt()}°" else "${weather.tempF.toInt()}°"
            Text(
                text = temp,
                fontFamily = LyricsLayoutCalculator.spicyFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}
