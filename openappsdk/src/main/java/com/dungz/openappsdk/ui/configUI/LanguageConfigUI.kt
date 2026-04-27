package com.dungz.openappsdk.ui.configUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * LanguageConfigUI — Customization config for the single LanguageScreen.
 *
 * Usage (call before OpenAppConfig.init):
 * ```
 * LanguageConfigUI.apply {
 *     backgroundColor = Color(0xFFF5F5F5)
 *     titleText = "Choose Language"
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

    /** Background color for the language screen. [Color.Transparent] = use theme default. */
    var backgroundColor: Color = Color.Transparent

    /** AppBar background color. [Color.Transparent] = use theme default. */
    var appBarBackgroundColor: Color = Color.Transparent

    // =========================================================
    // Simple text overrides
    // =========================================================

    /** Title shown in LanguageScreen AppBar. */
    var titleText: String = "Select Language"

    /** Hint text shown below the AppBar. */
    var descriptionText: String = "You can change language anytime"

    /** Text label on the default save button. */
    var saveButtonText: String = "Save"

    // =========================================================
    // Composable overrides  (null = fall back to text values above)
    // =========================================================

    /** Override the entire AppBar title composable. */
    var titleCompose: (@Composable () -> Unit)? = null

    /** Override the description composable. */
    var descriptionCompose: (@Composable () -> Unit)? = null

    /**
     * Override the save button.
     *
     * @param selectedCode The currently selected language code (e.g. "en", "vi").
     * @param onSave       Invoke this to commit the selection.
     */
    var saveButtonCompose: (@Composable (selectedCode: String, onSave: () -> Unit) -> Unit)? = null
}
