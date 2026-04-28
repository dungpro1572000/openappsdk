package com.dungz.openappsdk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object OpenAppConfig {

    private lateinit var splashConfig: SplashConfig
    private lateinit var languageConfig: LanguageConfig
    private lateinit var onboardingConfig: OnboardingConfig
    private lateinit var prepareDataConfig: PrepareDataConfig
    private lateinit var adPlaceholderConfig: AdPlaceholderConfig

    var navigateToMainScreen: (() -> Unit)? = null
        private set

    /**
     * Set to `true` to disable swipe gestures on onboarding screens at runtime.
     * Can be changed at any time — takes effect immediately.
     */
    var disableSwipe = mutableStateOf(false)

    private var isInitialized = false

    fun init(builderAction: Builder.() -> Unit) {
        val builder = Builder()
        builder.builderAction()
        builder.build()
        isInitialized = true
    }

    fun getSplashConfig(): SplashConfig {
        checkInitialized()
        return splashConfig
    }

    fun getLanguageConfig(): LanguageConfig {
        checkInitialized()
        return languageConfig
    }

    fun getOnboardingConfig(): OnboardingConfig {
        checkInitialized()
        return onboardingConfig
    }

    fun getPrepareDataConfig(): PrepareDataConfig {
        checkInitialized()
        return prepareDataConfig
    }

    fun getAdPlaceholderConfig(): AdPlaceholderConfig {
        checkInitialized()
        return adPlaceholderConfig
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("OpenAppConfig has not been initialized. Call OpenAppConfig.init { } first.")
        }
    }

    class Builder {
        private var splashConfigBuilder: SplashConfig.Builder = SplashConfig.builder()
        private var languageConfigBuilder: LanguageConfig.Builder = LanguageConfig.builder()
        private var onboardingConfigBuilder: OnboardingConfig.Builder = OnboardingConfig.builder()
        private var prepareDataConfigBuilder: PrepareDataConfig.Builder = PrepareDataConfig.builder()
        private var adPlaceholderConfigBuilder: AdPlaceholderConfig.Builder = AdPlaceholderConfig.builder()
        private var navigateToMain: (() -> Unit)? = null

        fun splashConfig(action: SplashConfig.Builder.() -> Unit) = apply {
            splashConfigBuilder.action()
        }

        fun languageConfig(action: LanguageConfig.Builder.() -> Unit) = apply {
            languageConfigBuilder.action()
        }

        fun onboardingConfig(action: OnboardingConfig.Builder.() -> Unit) = apply {
            onboardingConfigBuilder.action()
        }

        fun prepareDataConfig(action: PrepareDataConfig.Builder.() -> Unit) = apply {
            prepareDataConfigBuilder.action()
        }

        fun adPlaceholderConfig(action: AdPlaceholderConfig.Builder.() -> Unit) = apply {
            adPlaceholderConfigBuilder.action()
        }

        fun navigateToMainScreen(action: () -> Unit) = apply {
            navigateToMain = action
        }

        internal fun build() {
            splashConfig = splashConfigBuilder.build()
            languageConfig = languageConfigBuilder.build()
            onboardingConfig = onboardingConfigBuilder.build()
            prepareDataConfig = prepareDataConfigBuilder.build()
            adPlaceholderConfig = adPlaceholderConfigBuilder.build()
            navigateToMainScreen = navigateToMain
        }
    }

    // =========================================================
    // SplashConfig — banner + interstitial on splash
    // =========================================================

    class SplashConfig private constructor(
        val idBanner: String,
        val idInter: String,
        val totalDelay: Int,
        val content: (@Composable () -> Unit)?
    ) {
        class Builder {
            private var idBanner: String = ""
            private var idInter: String = ""
            private var totalDelay: Int = 30000
            private var content: (@Composable () -> Unit)? = null

            fun idBanner(id: String) = apply { this.idBanner = id }
            fun idInter(id: String) = apply { this.idInter = id }
            fun totalDelay(ms: Int) = apply { this.totalDelay = ms }
            fun content(content: @Composable () -> Unit) = apply { this.content = content }

            fun build() = SplashConfig(idBanner, idInter, totalDelay, content)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    // =========================================================
    // LanguageConfig — single language screen
    // =========================================================

    class LanguageConfig private constructor(
        val backgroundColor: Color,
        val textColor: Color,
        val onLanguageSelected: ((String) -> Unit)?
    ) {
        class Builder {
            private var backgroundColor: Color = Color.Transparent
            private var textColor: Color = Color.Unspecified
            private var onLanguageSelected: ((String) -> Unit)? = null

            fun backgroundColor(color: Color) = apply { this.backgroundColor = color }
            fun textColor(color: Color) = apply { this.textColor = color }
            fun onLanguageSelected(callback: (String) -> Unit) = apply { this.onLanguageSelected = callback }

            fun build() = LanguageConfig(backgroundColor, textColor, onLanguageSelected)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    // =========================================================
    // OnboardingConfig — 3 onboarding screens + native ad IDs
    // =========================================================

    class OnboardingConfig private constructor(
        val onboardingContent1: (@Composable () -> Unit)?,
        val onboardingContent2: (@Composable () -> Unit)?,
        val onboardingContent3: (@Composable () -> Unit)?,
        val onb1NativeAdId: String,
        val onb2NativeAdId: String,
        val prepareNativeAdId: String,
        val showOnb1Ad: Boolean,
        val showOnb2Ad: Boolean,
        val showPrepareAd: Boolean
    ) {
        class Builder {
            private var onboardingContent1: (@Composable () -> Unit)? = null
            private var onboardingContent2: (@Composable () -> Unit)? = null
            private var onboardingContent3: (@Composable () -> Unit)? = null
            private var onb1NativeAdId: String = ""
            private var onb2NativeAdId: String = ""
            private var prepareNativeAdId: String = ""
            private var showOnb1Ad: Boolean = true
            private var showOnb2Ad: Boolean = true
            private var showPrepareAd: Boolean = true

            fun onboardingContent1(content: @Composable () -> Unit) = apply { this.onboardingContent1 = content }
            fun onboardingContent2(content: @Composable () -> Unit) = apply { this.onboardingContent2 = content }
            fun onboardingContent3(content: @Composable () -> Unit) = apply { this.onboardingContent3 = content }
            fun onb1NativeAdId(id: String) = apply { this.onb1NativeAdId = id }
            fun onb2NativeAdId(id: String) = apply { this.onb2NativeAdId = id }
            fun prepareNativeAdId(id: String) = apply { this.prepareNativeAdId = id }
            fun showOnb1Ad(show: Boolean) = apply { this.showOnb1Ad = show }
            fun showOnb2Ad(show: Boolean) = apply { this.showOnb2Ad = show }
            fun showPrepareAd(show: Boolean) = apply { this.showPrepareAd = show }

            fun build() = OnboardingConfig(
                onboardingContent1, onboardingContent2, onboardingContent3,
                onb1NativeAdId, onb2NativeAdId, prepareNativeAdId,
                showOnb1Ad, showOnb2Ad, showPrepareAd
            )
        }

        companion object {
            fun builder() = Builder()
        }
    }

    // =========================================================
    // PrepareDataConfig
    // =========================================================

    class PrepareDataConfig private constructor(
        val delayTime: Int,
        val content: (@Composable () -> Unit)?,
        val onNextToMainScreen: (() -> Unit)?
    ) {
        class Builder {
            private var delayTime: Int = 5000
            private var content: (@Composable () -> Unit)? = null
            private var onNextToMainScreen: (() -> Unit)? = null

            fun delayTime(ms: Int) = apply { this.delayTime = ms }
            fun content(content: @Composable () -> Unit) = apply { this.content = content }
            fun onNextToMainScreen(action: () -> Unit) = apply { this.onNextToMainScreen = action }

            fun build() = PrepareDataConfig(delayTime, content, onNextToMainScreen)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    // =========================================================
    // AdPlaceholderConfig — UI placeholder while ads are loading
    // =========================================================

    class AdPlaceholderConfig private constructor(
        val backgroundColor: Color,
        val shimmerBaseColor: Color,
        val shimmerHighlightColor: Color,
        val loadingIndicatorColor: Color,
        val cornerRadius: Dp,
        val useShimmer: Boolean,
        val showLoadingIndicator: Boolean,
        val nativeMediumPlaceholder: (@Composable () -> Unit)?,
        val nativeSmallPlaceholder: (@Composable () -> Unit)?,
        val bannerPlaceholder: (@Composable () -> Unit)?
    ) {
        class Builder {
            private var backgroundColor: Color = Color(0xFFF5F5F5)
            private var shimmerBaseColor: Color = Color(0xFFE0E0E0)
            private var shimmerHighlightColor: Color = Color(0xFFF5F5F5)
            private var loadingIndicatorColor: Color = Color(0xFF6750A4)
            private var cornerRadius: Dp = 12.dp
            private var useShimmer: Boolean = true
            private var showLoadingIndicator: Boolean = false
            private var nativeMediumPlaceholder: (@Composable () -> Unit)? = null
            private var nativeSmallPlaceholder: (@Composable () -> Unit)? = null
            private var bannerPlaceholder: (@Composable () -> Unit)? = null

            fun backgroundColor(color: Color) = apply { this.backgroundColor = color }
            fun shimmerBaseColor(color: Color) = apply { this.shimmerBaseColor = color }
            fun shimmerHighlightColor(color: Color) = apply { this.shimmerHighlightColor = color }
            fun loadingIndicatorColor(color: Color) = apply { this.loadingIndicatorColor = color }
            fun cornerRadius(radius: Dp) = apply { this.cornerRadius = radius }
            fun useShimmer(use: Boolean) = apply { this.useShimmer = use }
            fun showLoadingIndicator(show: Boolean) = apply { this.showLoadingIndicator = show }
            fun nativeMediumPlaceholder(content: @Composable () -> Unit) = apply { this.nativeMediumPlaceholder = content }
            fun nativeSmallPlaceholder(content: @Composable () -> Unit) = apply { this.nativeSmallPlaceholder = content }
            fun bannerPlaceholder(content: @Composable () -> Unit) = apply { this.bannerPlaceholder = content }

            fun build() = AdPlaceholderConfig(
                backgroundColor, shimmerBaseColor, shimmerHighlightColor,
                loadingIndicatorColor, cornerRadius, useShimmer, showLoadingIndicator,
                nativeMediumPlaceholder, nativeSmallPlaceholder, bannerPlaceholder
            )
        }

        companion object {
            fun builder() = Builder()
        }
    }
}
