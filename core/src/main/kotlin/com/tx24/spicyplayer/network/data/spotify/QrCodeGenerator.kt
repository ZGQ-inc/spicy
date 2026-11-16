package com.tx24.spicyplayer.network.data.spotify

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Platform-independent QR code generator using ZXing.
 */
object QrCodeGenerator {
    /**
     * Generates a 2D boolean grid representing the QR Code.
     * True means black module, false means white module.
     */
    fun generateBitMatrix(text: String, size: Int = 300): Array<BooleanArray>? {
        if (text.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
            )
            val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            val w = matrix.width
            val h = matrix.height
            Array(h) { y ->
                BooleanArray(w) { x -> matrix.get(x, y) }
            }
        } catch (_: Exception) {
            null
        }
    }
}
