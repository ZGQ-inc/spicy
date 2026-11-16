package com.tx24.spicyplayer.ui.ambient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

object WeatherIcons {
    fun getIcon(iconCode: String?, condition: String?): ImageVector {
        val codePrefix = iconCode?.take(2).orEmpty()
        val isNight = iconCode?.endsWith("n") == true

        return when (codePrefix) {
            "01" -> if (isNight) Icons.Rounded.NightsStay else Icons.Rounded.WbSunny
            "02" -> if (isNight) Icons.Rounded.NightsStay else Icons.Rounded.WbCloudy
            "03", "04" -> Icons.Rounded.Cloud
            "09", "10" -> Icons.Rounded.WaterDrop
            "11" -> Icons.Rounded.Thunderstorm
            "13" -> Icons.Rounded.AcUnit
            "50" -> Icons.Rounded.Air
            else -> when (condition?.lowercase(Locale.ROOT)) {
                "clear" -> if (isNight) Icons.Rounded.NightsStay else Icons.Rounded.WbSunny
                "clouds" -> Icons.Rounded.Cloud
                "rain", "drizzle" -> Icons.Rounded.WaterDrop
                "thunderstorm" -> Icons.Rounded.Thunderstorm
                "snow" -> Icons.Rounded.AcUnit
                "mist", "fog", "haze" -> Icons.Rounded.Air
                else -> Icons.Rounded.WbSunny
            }
        }
    }

    fun getTint(iconCode: String?, condition: String?, isNightMode: Boolean = false): Color {
        if (isNightMode) return Color(0xFFE67E22)
        val codePrefix = iconCode?.take(2).orEmpty()
        val isNight = iconCode?.endsWith("n") == true

        return when (codePrefix) {
            "01" -> if (isNight) Color(0xFF90CAF9) else Color(0xFFFFB74D)
            "02" -> if (isNight) Color(0xFFB0BEC5) else Color(0xFFFFCC80)
            "03", "04" -> Color(0xFFCFD8DC)
            "09", "10" -> Color(0xFF4FC3F7)
            "11" -> Color(0xFFFFD54F)
            "13" -> Color(0xFFE1F5FE)
            "50" -> Color(0xFFB0BEC5)
            else -> when (condition?.lowercase(Locale.ROOT)) {
                "clear" -> if (isNight) Color(0xFF90CAF9) else Color(0xFFFFB74D)
                "clouds" -> Color(0xFFCFD8DC)
                "rain", "drizzle" -> Color(0xFF4FC3F7)
                "thunderstorm" -> Color(0xFFFFD54F)
                "snow" -> Color(0xFFE1F5FE)
                "mist", "fog", "haze" -> Color(0xFFB0BEC5)
                else -> Color(0xFFFFB74D)
            }
        }
    }
}
