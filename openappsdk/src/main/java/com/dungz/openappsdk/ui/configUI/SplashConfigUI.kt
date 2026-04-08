package com.dungz.openappsdk.ui.configUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * SplashConfigUI — Customization config for SplashScreen.
 *
 * Usage (call before OpenAppConfig.init):
 * ```
 * SplashConfigUI.apply {
 *     backgroundColor = Color(0xFFF5F5F5)
 *     subtitleText = "Loading, please wait..."
 *     logoCompose = { MyCustomLogo() }
 * }
 * ```
 */
object SplashConfigUI {

    // =========================================================
    // Colors
    // =========================================================

    /** Background color for the splash screen. [Color.Transparent] = use theme default. */
    var backgroundColor: Color = Color.Transparent

    // =========================================================
    // Simple text overrides
    // =========================================================

    /** Subtitle text shown below the progress indicator. */
    var subtitleText: String = "Processing, can contain ads"

    // =========================================================
    // Composable overrides  (null = fall back to defaults)
    // =========================================================

    /** Override the logo/app icon composable. */
    var logoCompose: (@Composable () -> Unit)? = null

    /** Override the progress indicator composable. */
    var progressCompose: (@Composable () -> Unit)? = null

    /** Override the subtitle composable. */
    var subtitleCompose: (@Composable () -> Unit)? = null
}
