package com.tx24.spicyplayer.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery0Bar
import androidx.compose.material.icons.rounded.Battery1Bar
import androidx.compose.material.icons.rounded.Battery2Bar
import androidx.compose.material.icons.rounded.Battery3Bar
import androidx.compose.material.icons.rounded.Battery4Bar
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.Battery6Bar
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tx24.spicyplayer.ui.theme.SpicyColors
import com.tx24.spicyplayer.ui.theme.SpicyType

data class BatteryState(
    val percent: Int,
    val isCharging: Boolean,
)

@Composable
fun rememberBatteryState(): BatteryState {
    val context = LocalContext.current
    var state by remember { mutableStateOf(getBatteryState(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                state = getBatteryState(context)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }
    return state
}

private fun getBatteryState(context: Context): BatteryState {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        ?: return BatteryState(100, false)
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val percent = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 100
    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    return BatteryState(percent.coerceIn(0, 100), isCharging)
}

/**
 * Clean ambient battery indicator pill displaying real-time battery percentage
 * and dynamic charging / discharging icons, perfectly matching Spicy Player UI aesthetics.
 */
@Composable
fun BatteryIndicator(
    modifier: Modifier = Modifier,
    scale: Float = 1.0f,
) {
    val state = rememberBatteryState()
    val clampedScale = scale.coerceIn(0.5f, 2.5f)
    val pillShape = RoundedCornerShape((20 * clampedScale).dp)

    val (icon, tint) = when {
        state.isCharging -> Icons.Rounded.BatteryChargingFull to SpicyColors.StatusSuccess
        state.percent <= 15 -> Icons.Rounded.BatteryAlert to SpicyColors.StatusDanger
        state.percent <= 25 -> Icons.Rounded.Battery1Bar to SpicyColors.TextPrimary
        state.percent <= 45 -> Icons.Rounded.Battery3Bar to SpicyColors.TextPrimary
        state.percent <= 70 -> Icons.Rounded.Battery4Bar to SpicyColors.TextPrimary
        state.percent <= 85 -> Icons.Rounded.Battery5Bar to SpicyColors.TextPrimary
        state.percent <= 95 -> Icons.Rounded.Battery6Bar to SpicyColors.TextPrimary
        else -> Icons.Rounded.BatteryFull to SpicyColors.TextPrimary
    }

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(SpicyColors.TintBg)
            .border(1.dp, SpicyColors.Hairline, pillShape)
            .padding(horizontal = (10 * clampedScale).dp, vertical = (7 * clampedScale).dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((5 * clampedScale).dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size((16 * clampedScale).dp),
            )
            Text(
                text = "${state.percent}%",
                style = SpicyType.Caption.copy(
                    fontSize = (SpicyType.Caption.fontSize.value * clampedScale).sp,
                    color = tint,
                    fontWeight = FontWeight.Medium,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}
