package com.tx24.spicyplayer.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tx24.spicyplayer.LyricsActivity

/**
 * Autostarts the Spicy Display app upon system boot for unattended kiosk/display operation.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            try {
                val launchIntent = Intent(context, LyricsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(launchIntent)
            } catch (_: Exception) {}
        }
    }
}
