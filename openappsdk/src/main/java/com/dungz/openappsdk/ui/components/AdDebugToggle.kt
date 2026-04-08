package com.dungz.openappsdk.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dungz.our_ads.utils.AdLogger

/**
 * Modifier extension that adds a hidden double-tap gesture to enable ad debug mode.
 *
 * Once enabled, it stays ON for the entire app session (never turns off).
 * - [AdLogger.isEnabled] = true (Logcat output)
 * - [AdLogger.showToasts] = true (Toast on every ad event)
 * - Shows Toast with current status summary of all tracked ads
 *
 * Usage: `Modifier.size(120.dp).adDebugToggle()`
 */
@Composable
fun Modifier.adDebugToggle(): Modifier {
    val context = LocalContext.current
    var tapCount by remember { mutableIntStateOf(0) }

    return this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        if (AdLogger.showToasts) return@clickable

        tapCount++
        if (tapCount >= 2) {
            tapCount = 0
            AdLogger.isEnabled = true
            AdLogger.showToasts = true
            AdLogger.onLogListener = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            val summary = AdLogger.getStatusSummary()
            Toast.makeText(context, "Ad Debug ON\n$summary", Toast.LENGTH_LONG).show()
        }
    }
}
