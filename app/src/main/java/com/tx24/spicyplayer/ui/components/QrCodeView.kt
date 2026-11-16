package com.tx24.spicyplayer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.tx24.spicyplayer.network.data.spotify.QrCodeGenerator

@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 400,
) {
    val bitmap = remember(content, sizePx) {
        val grid = QrCodeGenerator.generateBitMatrix(content, sizePx) ?: return@remember null
        val h = grid.size
        val w = grid[0].size
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val row = grid[y]
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (row[x]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        bmp
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = modifier,
        )
    }
}
