package com.dungz.openappsdk.ui.configUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * OnboardingConfigUI — Customization config for OnBoarding1Screen and OnBoarding2Screen.
 *
 * Usage (call before OpenAppConfig.init):
 * ```
 * OnboardingConfigUI.apply {
 *     backgroundColor = Color(0xFFF5F5F5)
 *     nextButtonText = "Continue"
 *     startButtonText = "Get Started"
 *     startButtonCompose = { onStart ->
 *         MyCustomButton(label = "Let's Go", onClick = onStart)
 *     }
 * }
 * ```
 */
object OnboardingConfigUI {

    // =========================================================
    // Colors
    // =========================================================

    /** Background color for both onboarding screens. [Color.Transparent] = use theme default. */
    var backgroundColor: Color = Color.Transparent

    // =========================================================
    // Simple text overrides
    // =========================================================

    /** Title text shown in OnBoarding1Screen. */
    var title1Text: String = "Welcome"

    /** Title text shown in OnBoarding2Screen. */
    var title2Text: String = "Get Started"

    /** Subtitle text shown in OnBoarding1Screen. */
    var subtitle1Text: String = "Discover amazing features"

    /** Subtitle text shown in OnBoarding2Screen. */
    var subtitle2Text: String = "Everything is ready for you"

    /** Text label on the "Next" button in OnBoarding1Screen. */
    var nextButtonText: String = "Next"

    /** Text label on the "Start" button in OnBoarding2Screen. */
    var startButtonText: String = "Start"

    // =========================================================
    // Composable overrides  (null = fall back to defaults)
    // =========================================================

    /** Override the title composable in OnBoarding1Screen. */
    var title1Compose: (@Composable () -> Unit)? = null

    /** Override the title composable in OnBoarding2Screen. */
    var title2Compose: (@Composable () -> Unit)? = null

    /** Override the subtitle composable in OnBoarding1Screen. */
    var subtitle1Compose: (@Composable () -> Unit)? = null

    /** Override the subtitle composable in OnBoarding2Screen. */
    var subtitle2Compose: (@Composable () -> Unit)? = null

    /** Override the image composable in OnBoarding1Screen. */
    var image1Compose: (@Composable () -> Unit)? = null

    /** Override the image composable in OnBoarding2Screen. */
    var image2Compose: (@Composable () -> Unit)? = null

    /**
     * Override the "Next" button in OnBoarding1Screen.
     *
     * @param onNext Invoke this to navigate to the next screen.
     */
    var nextButtonCompose: (@Composable (onNext: () -> Unit) -> Unit)? = null

    /**
     * Override the "Start" button in OnBoarding2Screen.
     *
     * @param onStart Invoke this to start the app.
     */
    var startButtonCompose: (@Composable (onStart: () -> Unit) -> Unit)? = null
}
