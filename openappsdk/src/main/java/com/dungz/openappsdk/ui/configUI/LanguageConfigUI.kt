package com.dungz.openappsdk.ui.configUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * LanguageConfigUI — Customization config for Language1Screen and Language2Screen.
 *
 * Usage (call before OpenAppConfig.init):
 * ```
 * LanguageConfigUI.apply {
 *     backgroundColor = Color(0xFFF5F5F5)
 *     title1Text = "Choose Language"
 *     saveButtonText = "Continue"
 *     saveButtonCompose = { selectedCode, onSave ->
 *         MyCustomButton(label = "Apply ($selectedCode)", onClick = onSave)
 *     }
 * }
 * ```
 */
object LanguageConfigUI {

    // =========================================================
    // Colors
    // =========================================================

    /** Background color for both language screens. [Color.Transparent] = use theme default. */
    var backgroundColor: Color = Color.Transparent

    /** AppBar background color. [Color.Transparent] = use theme default. */
    var appBarBackgroundColor: Color = Color.Transparent

    // =========================================================
    // Simple text overrides
    // =========================================================

    /** Title shown in Language1Screen AppBar. */
    var title1Text: String = "Select Language"

    /** Title shown in Language2Screen AppBar. */
    var title2Text: String = "Confirm Language"

    /** Hint text shown below the AppBar on both screens. */
    var descriptionText: String = "You can change language anytime"

    /** Text label on the default save button in Language2Screen. */
    var saveButtonText: String = "Save"

    // =========================================================
    // Composable overrides  (null = fall back to text values above)
    // =========================================================

    /** Override the entire Language1 AppBar title composable. */
    var title1Compose: (@Composable () -> Unit)? = null

    /** Override the entire Language2 AppBar title composable. */
    var title2Compose: (@Composable () -> Unit)? = null

    /**
     * Override the description composable shown on both screens.
     * Receives no parameters — use a closure if you need the selected code.
     */
    var descriptionCompose: (@Composable () -> Unit)? = null

    /**
     * Override the save button in Language2Screen.
     *
     * @param selectedCode The currently selected language code (e.g. "en", "vi").
     * @param onSave       Invoke this to commit the selection (saves prefs + calls onSaveButton).
     *
     * Example:
     * ```
     * saveButtonCompose = { selectedCode, onSave ->
     *     TextButton(onClick = onSave) { Text("Apply $selectedCode") }
     * }
     * ```
     */
    var saveButtonCompose: (@Composable (selectedCode: String, onSave: () -> Unit) -> Unit)? = null
}
