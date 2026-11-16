package com.tx24.spicyplayer.ui.controls

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Official Spotify Encore 24x24 vector icons extracted directly from the Spotify Android client.
 */
object SpotifyIcons {
    /** Spotify Encore Shuffle (24x24) */
    val Shuffle: ImageVector = icon(
        24f, 24f,
        "M18.788,14.289a1,1 0,0 0,0 1.414L20.086,17h-1.518a5,5 0,0 1,-3.826 -1.78l-1.403,-1.668 -1.306,1.554 1.178,1.4A7,7 0,0 0,18.568 19h1.518l-1.298,1.298a1,1 0,1 0,1.414 1.414L23.914,18l-3.712,-3.713a1,1 0,0 0,-1.414 0zM7.396,6.49l2.023,2.404 -1.307,1.553 -2.246,-2.67a5,5 0,0 0,-3.826 -1.78H1v-2h1.04A7,7 0,0 1,7.396 6.49",
        "M18.788,3.702a1,1 0,0 1,1.414 -1.414L23.914,6l-3.712,3.712a1,1 0,1 1,-1.414 -1.414L20.086,7h-1.518a5,5 0,0 0,-3.826 1.78l-7.346,8.73a7,7 0,0 1,-5.356 2.494H1v-2h1.04a5,5 0,0 0,3.826 -1.781l7.345,-8.73A7,7 0,0 1,18.569 5h1.518l-1.298,-1.298z",
    )

    /** Spotify Encore Repeat All (24x24) */
    val Repeat: ImageVector = icon(
        24f, 24f,
        "M6,2a5,5 0,0 0,-5 5v8a5,5 0,0 0,5 5h1v-2H6a3,3 0,0 1,-3 -3V7a3,3 0,0 1,3 -3h12a3,3 0,0 1,3 3v8a3,3 0,0 1,-3 3h-4.798l1.298,-1.298a1,1 0,1 0,-1.414 -1.414L9.373,19l3.713,3.712a1,1 0,0 0,1.414 -1.414L13.202,20H18a5,5 0,0 0,5 -5V7a5,5 0,0 0,-5 -5z",
    )

    /** Spotify Encore Repeat One (24x24) */
    val RepeatOne: ImageVector = icon(
        24f, 24f,
        "M1,7a5,5 0,0 1,5 -5h1v2L6,4a3,3 0,0 0,-3 3v8a3,3 0,0 0,3 3h1v2L6,20a5,5 0,0 1,-5 -5zM18,4h-1L17,2h1a5,5 0,0 1,5 5v8a5,5 0,0 1,-5 5h-7.202v-2L18,18a3,3 0,0 0,3 -3L21,7a3,3 0,0 0,-3 -3",
        "M14.5,15.288a1,1 0,0 1,0 1.414L12.202,19l2.298,2.298a1,1 0,0 1,-1.414 1.415L9.373,19l3.713,-3.712a1,1 0,0 1,1.414 0M11.967,3.202l1.008,0.91H9.559v-1.4q1.316,-0.098 1.918,-0.49c0.602,-0.392 0.649,-0.649 0.742,-1.162h1.75V11h-2.002z",
        "m12.975,4.112 l-1.008,-0.91v0.91z",
    )

    /** Spotify Encore Play (24x24) with rounded corners */
    val Play: ImageVector = icon(
        24f, 24f,
        "m7.05,3.606 l13.49,7.788a0.7,0.7 0,0 1,0 1.212L7.05,20.394A0.7,0.7 0,0 1,6 19.788V4.212a0.7,0.7 0,0 1,1.05 -0.606",
    )

    /** Spotify Encore Pause (24x24) with rounded corners */
    val Pause: ImageVector = icon(
        24f, 24f,
        "M5.7,3a0.7,0.7 0,0 0,-0.7 0.7v16.6a0.7,0.7 0,0 0,0.7 0.7h2.6a0.7,0.7 0,0 0,0.7 -0.7L9,3.7a0.7,0.7 0,0 0,-0.7 -0.7zM15.7,3a0.7,0.7 0,0 0,-0.7 0.7v16.6a0.7,0.7 0,0 0,0.7 0.7h2.6a0.7,0.7 0,0 0,0.7 -0.7L19,3.7a0.7,0.7 0,0 0,-0.7 -0.7z",
    )

    /** Spotify Encore Skip Back / Previous (24x24) */
    val SkipBack: ImageVector = icon(
        24f, 24f,
        "M6.3,3a0.7,0.7 0,0 1,0.7 0.7v6.805l11.95,-6.899a0.7,0.7 0,0 1,1.05 0.606v15.576a0.7,0.7 0,0 1,-1.05 0.606L7,13.495V20.3a0.7,0.7 0,0 1,-0.7 0.7H4.7a0.7,0.7 0,0 1,-0.7 -0.7V3.7a0.7,0.7 0,0 1,0.7 -0.7z",
    )

    /** Spotify Encore Skip Forward / Next (24x24) */
    val SkipForward: ImageVector = icon(
        24f, 24f,
        "M17.7,3a0.7,0.7 0,0 0,-0.7 0.7v6.805L5.05,3.606A0.7,0.7 0,0 0,4 4.212v15.576a0.7,0.7 0,0 0,1.05 0.606L17,13.495V20.3a0.7,0.7 0,0 0,0.7 0.7h1.6a0.7,0.7 0,0 0,0.7 -0.7V3.7a0.7,0.7 0,0 0,-0.7 -0.7z",
    )

    private fun icon(width: Float, height: Float, vararg paths: String): ImageVector =
        ImageVector.Builder(
            defaultWidth = width.dp,
            defaultHeight = height.dp,
            viewportWidth = width,
            viewportHeight = height,
        ).apply {
            paths.forEach {
                addPath(PathParser().parsePathString(it).toNodes(), fill = SolidColor(Color.White))
            }
        }.build()
}
