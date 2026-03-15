package com.dungz.openappsdk.ui.configUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * PrepareConfigUI — Customization config for PrepareDataScreen.
 *
 * Usage (call before OpenAppConfig.init):
 * ```
 * PrepareConfigUI.apply {
 *     backgroundColor = Color(0xFFF5F5F5)
 *     titleText = "Getting ready..."
 *     subtitleText = "Setting up your experience"
 * }
 * ```
 */
object PrepareConfigUI {

    // =========================================================
    // Colors
    // =========================================================

    /** Background color for the prepare screen. [Color.Transparent] = use theme default. */
    var backgroundColor: Color = Color.Transparent

    // =========================================================
    // Simple text overrides
    // =========================================================

    /** Title text shown on the prepare screen. */
    var titleText: String = "Loading data..."

    /** Subtitle text shown below the title. */
    var subtitleText: String = "Please wait while we prepare everything for you"

    // =========================================================
    // Composable overrides  (null = fall back to text values above)
    // =========================================================

    /** Override the progress indicator composable. */
    var progressCompose: (@Composable () -> Unit)? = null

    /** Override the title composable. */
    var titleCompose: (@Composable () -> Unit)? = null

    /** Override the subtitle composable. */
    var subtitleCompose: (@Composable () -> Unit)? = null
}
