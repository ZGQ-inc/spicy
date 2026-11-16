package com.tx24.spicyplayer.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Calculates a smooth, rotating orbital pixel-shift offset for active burn-in protection.
 * The displacement radius directly scales with [scale] so larger elements travel further
 * to completely vacate static pixel footprints on OLED / AMOLED displays.
 */
@Composable
fun rememberPixelShiftOffset(
    enabled: Boolean,
    scale: Float = 1.0f,
    baseRadiusDp: Float = 10f,
    intervalMs: Long = 30_000L,
): Pair<Dp, Dp> {
    var shiftX by remember { mutableStateOf(0.dp) }
    var shiftY by remember { mutableStateOf(0.dp) }

    val safeScale = scale.coerceIn(0.4f, 4.0f)

    LaunchedEffect(enabled, safeScale, baseRadiusDp) {
        if (!enabled) {
            shiftX = 0.dp
            shiftY = 0.dp
            return@LaunchedEffect
        }
        var step = 0
        while (true) {
            delay(intervalMs)
            step = (step + 1) % 8
            val baseAngle = step * (Math.PI / 4.0)
            val jitter = Random.nextDouble(-1.2, 1.2) * safeScale
            val effectiveRadius = (baseRadiusDp * safeScale) + jitter
            shiftX = (cos(baseAngle) * effectiveRadius).toInt().dp
            shiftY = (sin(baseAngle) * effectiveRadius).toInt().dp
        }
    }

    val animatedShiftX by animateDpAsState(shiftX, tween(3000), label = "pixelShiftX")
    val animatedShiftY by animateDpAsState(shiftY, tween(3000), label = "pixelShiftY")

    return Pair(animatedShiftX, animatedShiftY)
}

/**
 * Modifier that applies burn-in protection pixel shift, dynamically linked to the element's [scale].
 */
@Composable
fun Modifier.pixelShift(
    enabled: Boolean,
    scale: Float = 1.0f,
    baseRadiusDp: Float = 10f,
): Modifier {
    val (shiftX, shiftY) = rememberPixelShiftOffset(enabled, scale, baseRadiusDp)
    return this.offset(shiftX, shiftY)
}
