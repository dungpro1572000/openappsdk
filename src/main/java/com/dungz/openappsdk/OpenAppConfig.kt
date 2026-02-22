package com.dungz.openappsdk

import androidx.annotation.IntegerRes
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object OpenAppConfig {

    private lateinit var splashConfig: SplashConfig
    private lateinit var language1Config: Language1Config
    private lateinit var language2Config: Language2Config
    private lateinit var onboarding1Config: Onboarding1Config
    private lateinit var onboarding2Config: Onboarding2Config
    private lateinit var prepareDataConfig: PrepareDataConfig
    private lateinit var themeConfig: ThemeConfig
    private lateinit var adPlaceholderConfig: AdPlaceholderConfig

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

    fun getLanguage1Config(): Language1Config {
        checkInitialized()
        return language1Config
    }

    fun getLanguage2Config(): Language2Config {
        checkInitialized()
        return language2Config
    }

    fun getPrepareDataConfig(): PrepareDataConfig {
        checkInitialized()
        return prepareDataConfig
    }

    fun getOnboarding1Config(): Onboarding1Config {
        checkInitialized()
        return onboarding1Config
    }

    fun getOnboarding2Config(): Onboarding2Config {
        checkInitialized()
        return onboarding2Config
    }

    fun getThemeConfig(): ThemeConfig {
        checkInitialized()
        return themeConfig
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
        private var language1ConfigBuilder: Language1Config.Builder = Language1Config.builder()
        private var language2ConfigBuilder: Language2Config.Builder = Language2Config.builder()
        private var prepareDataConfigBuilder: PrepareDataConfig.Builder = PrepareDataConfig.builder()
        private var onboarding1ConfigBuilder: Onboarding1Config.Builder = Onboarding1Config.builder()
        private var onboarding2ConfigBuilder: Onboarding2Config.Builder = Onboarding2Config.builder()
        private var themeConfigBuilder: ThemeConfig.Builder = ThemeConfig.builder()
        private var adPlaceholderConfigBuilder: AdPlaceholderConfig.Builder = AdPlaceholderConfig.builder()

        fun splashConfig(action: SplashConfig.Builder.() -> Unit) = apply {
            splashConfigBuilder.action()
        }

        fun language1Config(action: Language1Config.Builder.() -> Unit) = apply {
            language1ConfigBuilder.action()
        }

        fun language2Config(action: Language2Config.Builder.() -> Unit) = apply {
            language2ConfigBuilder.action()
        }

        fun prepareDataConfig(action: PrepareDataConfig.Builder.() -> Unit) = apply {
            prepareDataConfigBuilder.action()
        }

        fun onboarding1Config(action: Onboarding1Config.Builder.() -> Unit) = apply {
            onboarding1ConfigBuilder.action()
        }

        fun onboarding2Config(action: Onboarding2Config.Builder.() -> Unit) = apply {
            onboarding2ConfigBuilder.action()
        }

        fun themeConfig(action: ThemeConfig.Builder.() -> Unit) = apply {
            themeConfigBuilder.action()
        }

        fun adPlaceholderConfig(action: AdPlaceholderConfig.Builder.() -> Unit) = apply {
            adPlaceholderConfigBuilder.action()
        }

        internal fun build() {
            splashConfig = splashConfigBuilder.build()
            language1Config = language1ConfigBuilder.build()
            language2Config = language2ConfigBuilder.build()
            onboarding1Config = onboarding1ConfigBuilder.build()
            onboarding2Config = onboarding2ConfigBuilder.build()
            prepareDataConfig = prepareDataConfigBuilder.build()
            themeConfig = themeConfigBuilder.build()
            adPlaceholderConfig = adPlaceholderConfigBuilder.build()
        }
    }

    class SplashConfig private constructor(
        val delayTime: Int
    ) {
        class Builder {
            private var delayTime: Int = 3000

            fun delayTime(delayTime: Int) = apply { this.delayTime = delayTime }

            fun build() = SplashConfig(delayTime)
        }

        companion object {
            fun builder() = Builder()
        }
    }
    class Onboarding1Config private constructor(
        val title:String?=null,
        val subTitle:String?=null,
        @field:IntegerRes val img:Int?=null,
        val adIdHigh: String,
        val adIdNormal: String,
        val showAdHigh: Boolean,
        val showAdNormal: Boolean
    ) {
        class Builder {
            private var title: String = ""
            private var subTitle: String = ""
            private var img: Int = 0
            private var adIdHigh: String = ""
            private var adIdNormal: String = ""
            private var showAdHigh: Boolean = true
            private var showAdNormal: Boolean = true

            fun adIdHigh(adIdHigh: String) = apply { this.adIdHigh = adIdHigh }
            fun adIdNormal(adIdNormal: String) = apply { this.adIdNormal = adIdNormal }
            fun showAdHigh(showAdHigh: Boolean) = apply { this.showAdHigh = showAdHigh }
            fun showAdNormal(showAdNormal: Boolean) = apply { this.showAdNormal = showAdNormal }
            fun title(title: String) = apply { this.title = title }
            fun subTitle(subTitle: String) = apply { this.subTitle = subTitle }
            fun img(@RawRes img: Int) = apply { this.img = img }

            fun build() = Onboarding1Config( title, subTitle, img,adIdHigh, adIdNormal, showAdHigh, showAdNormal)
        }

        companion object {
            fun builder() = Builder()
        }
    }
    class Onboarding2Config private constructor(
        val title:String?=null,
        val subTitle:String?=null,
        @field:IntegerRes val img:Int?=null,
        val nativeAdIdHigh: String,
        val nativeAdIdNormal: String,
        val showNativeAdHigh: Boolean,
        val showNativeAdNormal: Boolean
    ) {
        class Builder {
            private var title: String = ""
            private var subTitle: String = ""
            private var img: Int = 0
            private var adIdHigh: String = ""
            private var adIdNormal: String = ""
            private var showAdHigh: Boolean = true
            private var showAdNormal: Boolean = true

            fun adIdHigh(adIdHigh: String) = apply { this.adIdHigh = adIdHigh }
            fun adIdNormal(adIdNormal: String) = apply { this.adIdNormal = adIdNormal }
            fun showAdHigh(showAdHigh: Boolean) = apply { this.showAdHigh = showAdHigh }
            fun showAdNormal(showAdNormal: Boolean) = apply { this.showAdNormal = showAdNormal }
            fun title(title: String) = apply { this.title = title }
            fun subTitle(subTitle: String) = apply { this.subTitle = subTitle }
            fun img(@RawRes img: Int) = apply { this.img = img }


            fun build() = Onboarding2Config( title, subTitle, img,adIdHigh, adIdNormal, showAdHigh, showAdNormal)
        }

        companion object {
            fun builder() = Builder()
        }
    }


    abstract class BaseLanguageConfig(
        val adIdHigh: String,
        val adIdNormal: String,
        val showAdHigh: Boolean,
        val showAdNormal: Boolean
    )

    class Language1Config private constructor(
        adIdHigh: String,
        adIdNormal: String,
        showAdHigh: Boolean,
        showAdNormal: Boolean
    ) : BaseLanguageConfig(adIdHigh, adIdNormal, showAdHigh, showAdNormal) {

        class Builder {
            private var adIdHigh: String = ""
            private var adIdNormal: String = ""
            private var showAdHigh: Boolean = true
            private var showAdNormal: Boolean = true

            fun adIdHigh(adIdHigh: String) = apply { this.adIdHigh = adIdHigh }
            fun adIdNormal(adIdNormal: String) = apply { this.adIdNormal = adIdNormal }
            fun showAdHigh(showAdHigh: Boolean) = apply { this.showAdHigh = showAdHigh }
            fun showAdNormal(showAdNormal: Boolean) = apply { this.showAdNormal = showAdNormal }

            fun build() = Language1Config(adIdHigh, adIdNormal, showAdHigh, showAdNormal)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    class Language2Config private constructor(
        adIdHigh: String,
        adIdNormal: String,
        showAdHigh: Boolean,
        showAdNormal: Boolean
    ) : BaseLanguageConfig(adIdHigh, adIdNormal, showAdHigh, showAdNormal) {

        class Builder {
            private var adIdHigh: String = ""
            private var adIdNormal: String = ""
            private var showAdHigh: Boolean = true
            private var showAdNormal: Boolean = true

            fun adIdHigh(adIdHigh: String) = apply { this.adIdHigh = adIdHigh }
            fun adIdNormal(adIdNormal: String) = apply { this.adIdNormal = adIdNormal }
            fun showAdHigh(showAdHigh: Boolean) = apply { this.showAdHigh = showAdHigh }
            fun showAdNormal(showAdNormal: Boolean) = apply { this.showAdNormal = showAdNormal }

            fun build() = Language2Config(adIdHigh, adIdNormal, showAdHigh, showAdNormal)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    class PrepareDataConfig private constructor(
        val delayTime: Int,
        val adIdHigh: String,
        val adIdNormal: String,
        val showAdHigh: Boolean,
        val showAdNormal: Boolean
    ) {
        class Builder {
            private var delayTime: Int = 5000
            private var adIdHigh: String = ""
            private var adIdNormal: String = ""
            private var showAdHigh: Boolean = true
            private var showAdNormal: Boolean = true

            fun delayTime(delayTime: Int) = apply { this.delayTime = delayTime }
            fun adIdHigh(adIdHigh: String) = apply { this.adIdHigh = adIdHigh }
            fun adIdNormal(adIdNormal: String) = apply { this.adIdNormal = adIdNormal }
            fun showAdHigh(showAdHigh: Boolean) = apply { this.showAdHigh = showAdHigh }
            fun showAdNormal(showAdNormal: Boolean) = apply { this.showAdNormal = showAdNormal }

            fun build() = PrepareDataConfig(delayTime, adIdHigh, adIdNormal, showAdHigh, showAdNormal)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    /**
     * ThemeConfig - Cấu hình màu sắc cho theme
     *
     * Cho phép tùy chỉnh màu sắc của Material3 ColorScheme cho cả Light và Dark theme
     */
    class ThemeConfig private constructor(
        // Light theme colors
        val lightPrimary: Color,
        val lightOnPrimary: Color,
        val lightPrimaryContainer: Color,
        val lightOnPrimaryContainer: Color,
        val lightSecondary: Color,
        val lightOnSecondary: Color,
        val lightSecondaryContainer: Color,
        val lightOnSecondaryContainer: Color,
        val lightTertiary: Color,
        val lightOnTertiary: Color,
        val lightTertiaryContainer: Color,
        val lightOnTertiaryContainer: Color,
        val lightBackground: Color,
        val lightOnBackground: Color,
        val lightSurface: Color,
        val lightOnSurface: Color,
        val lightSurfaceVariant: Color,
        val lightOnSurfaceVariant: Color,
        val lightError: Color,
        val lightOnError: Color,
        // Dark theme colors
        val darkPrimary: Color,
        val darkOnPrimary: Color,
        val darkPrimaryContainer: Color,
        val darkOnPrimaryContainer: Color,
        val darkSecondary: Color,
        val darkOnSecondary: Color,
        val darkSecondaryContainer: Color,
        val darkOnSecondaryContainer: Color,
        val darkTertiary: Color,
        val darkOnTertiary: Color,
        val darkTertiaryContainer: Color,
        val darkOnTertiaryContainer: Color,
        val darkBackground: Color,
        val darkOnBackground: Color,
        val darkSurface: Color,
        val darkOnSurface: Color,
        val darkSurfaceVariant: Color,
        val darkOnSurfaceVariant: Color,
        val darkError: Color,
        val darkOnError: Color,
        // Options
        val useDynamicColor: Boolean
    ) {
        class Builder {
            // Default Light colors (Material3 defaults)
            private var lightPrimary: Color = Color(0xFF6750A4)
            private var lightOnPrimary: Color = Color.White
            private var lightPrimaryContainer: Color = Color(0xFFEADDFF)
            private var lightOnPrimaryContainer: Color = Color(0xFF21005D)
            private var lightSecondary: Color = Color(0xFF625B71)
            private var lightOnSecondary: Color = Color.White
            private var lightSecondaryContainer: Color = Color(0xFFE8DEF8)
            private var lightOnSecondaryContainer: Color = Color(0xFF1D192B)
            private var lightTertiary: Color = Color(0xFF7D5260)
            private var lightOnTertiary: Color = Color.White
            private var lightTertiaryContainer: Color = Color(0xFFFFD8E4)
            private var lightOnTertiaryContainer: Color = Color(0xFF31111D)
            private var lightBackground: Color = Color(0xFFFFFBFE)
            private var lightOnBackground: Color = Color(0xFF1C1B1F)
            private var lightSurface: Color = Color(0xFFFFFBFE)
            private var lightOnSurface: Color = Color(0xFF1C1B1F)
            private var lightSurfaceVariant: Color = Color(0xFFE7E0EC)
            private var lightOnSurfaceVariant: Color = Color(0xFF49454F)
            private var lightError: Color = Color(0xFFB3261E)
            private var lightOnError: Color = Color.White

            // Default Dark colors (Material3 defaults)
            private var darkPrimary: Color = Color(0xFFD0BCFF)
            private var darkOnPrimary: Color = Color(0xFF381E72)
            private var darkPrimaryContainer: Color = Color(0xFF4F378B)
            private var darkOnPrimaryContainer: Color = Color(0xFFEADDFF)
            private var darkSecondary: Color = Color(0xFFCCC2DC)
            private var darkOnSecondary: Color = Color(0xFF332D41)
            private var darkSecondaryContainer: Color = Color(0xFF4A4458)
            private var darkOnSecondaryContainer: Color = Color(0xFFE8DEF8)
            private var darkTertiary: Color = Color(0xFFEFB8C8)
            private var darkOnTertiary: Color = Color(0xFF492532)
            private var darkTertiaryContainer: Color = Color(0xFF633B48)
            private var darkOnTertiaryContainer: Color = Color(0xFFFFD8E4)
            private var darkBackground: Color = Color(0xFF1C1B1F)
            private var darkOnBackground: Color = Color(0xFFE6E1E5)
            private var darkSurface: Color = Color(0xFF1C1B1F)
            private var darkOnSurface: Color = Color(0xFFE6E1E5)
            private var darkSurfaceVariant: Color = Color(0xFF49454F)
            private var darkOnSurfaceVariant: Color = Color(0xFFCAC4D0)
            private var darkError: Color = Color(0xFFF2B8B5)
            private var darkOnError: Color = Color(0xFF601410)

            private var useDynamicColor: Boolean = false

            // Light theme setters
            fun lightPrimary(color: Color) = apply { this.lightPrimary = color }
            fun lightOnPrimary(color: Color) = apply { this.lightOnPrimary = color }
            fun lightPrimaryContainer(color: Color) = apply { this.lightPrimaryContainer = color }
            fun lightOnPrimaryContainer(color: Color) = apply { this.lightOnPrimaryContainer = color }
            fun lightSecondary(color: Color) = apply { this.lightSecondary = color }
            fun lightOnSecondary(color: Color) = apply { this.lightOnSecondary = color }
            fun lightSecondaryContainer(color: Color) = apply { this.lightSecondaryContainer = color }
            fun lightOnSecondaryContainer(color: Color) = apply { this.lightOnSecondaryContainer = color }
            fun lightTertiary(color: Color) = apply { this.lightTertiary = color }
            fun lightOnTertiary(color: Color) = apply { this.lightOnTertiary = color }
            fun lightTertiaryContainer(color: Color) = apply { this.lightTertiaryContainer = color }
            fun lightOnTertiaryContainer(color: Color) = apply { this.lightOnTertiaryContainer = color }
            fun lightBackground(color: Color) = apply { this.lightBackground = color }
            fun lightOnBackground(color: Color) = apply { this.lightOnBackground = color }
            fun lightSurface(color: Color) = apply { this.lightSurface = color }
            fun lightOnSurface(color: Color) = apply { this.lightOnSurface = color }
            fun lightSurfaceVariant(color: Color) = apply { this.lightSurfaceVariant = color }
            fun lightOnSurfaceVariant(color: Color) = apply { this.lightOnSurfaceVariant = color }
            fun lightError(color: Color) = apply { this.lightError = color }
            fun lightOnError(color: Color) = apply { this.lightOnError = color }

            // Dark theme setters
            fun darkPrimary(color: Color) = apply { this.darkPrimary = color }
            fun darkOnPrimary(color: Color) = apply { this.darkOnPrimary = color }
            fun darkPrimaryContainer(color: Color) = apply { this.darkPrimaryContainer = color }
            fun darkOnPrimaryContainer(color: Color) = apply { this.darkOnPrimaryContainer = color }
            fun darkSecondary(color: Color) = apply { this.darkSecondary = color }
            fun darkOnSecondary(color: Color) = apply { this.darkOnSecondary = color }
            fun darkSecondaryContainer(color: Color) = apply { this.darkSecondaryContainer = color }
            fun darkOnSecondaryContainer(color: Color) = apply { this.darkOnSecondaryContainer = color }
            fun darkTertiary(color: Color) = apply { this.darkTertiary = color }
            fun darkOnTertiary(color: Color) = apply { this.darkOnTertiary = color }
            fun darkTertiaryContainer(color: Color) = apply { this.darkTertiaryContainer = color }
            fun darkOnTertiaryContainer(color: Color) = apply { this.darkOnTertiaryContainer = color }
            fun darkBackground(color: Color) = apply { this.darkBackground = color }
            fun darkOnBackground(color: Color) = apply { this.darkOnBackground = color }
            fun darkSurface(color: Color) = apply { this.darkSurface = color }
            fun darkOnSurface(color: Color) = apply { this.darkOnSurface = color }
            fun darkSurfaceVariant(color: Color) = apply { this.darkSurfaceVariant = color }
            fun darkOnSurfaceVariant(color: Color) = apply { this.darkOnSurfaceVariant = color }
            fun darkError(color: Color) = apply { this.darkError = color }
            fun darkOnError(color: Color) = apply { this.darkOnError = color }

            // Options
            fun useDynamicColor(use: Boolean) = apply { this.useDynamicColor = use }

            fun build() = ThemeConfig(
                lightPrimary, lightOnPrimary, lightPrimaryContainer, lightOnPrimaryContainer,
                lightSecondary, lightOnSecondary, lightSecondaryContainer, lightOnSecondaryContainer,
                lightTertiary, lightOnTertiary, lightTertiaryContainer, lightOnTertiaryContainer,
                lightBackground, lightOnBackground, lightSurface, lightOnSurface,
                lightSurfaceVariant, lightOnSurfaceVariant, lightError, lightOnError,
                darkPrimary, darkOnPrimary, darkPrimaryContainer, darkOnPrimaryContainer,
                darkSecondary, darkOnSecondary, darkSecondaryContainer, darkOnSecondaryContainer,
                darkTertiary, darkOnTertiary, darkTertiaryContainer, darkOnTertiaryContainer,
                darkBackground, darkOnBackground, darkSurface, darkOnSurface,
                darkSurfaceVariant, darkOnSurfaceVariant, darkError, darkOnError,
                useDynamicColor
            )
        }

        companion object {
            fun builder() = Builder()
        }
    }

    /**
     * AdPlaceholderConfig - Cấu hình UI placeholder khi ads đang loading
     *
     * Cho phép tùy chỉnh:
     * - Màu nền placeholder
     * - Màu loading indicator
     * - Hiệu ứng shimmer
     * - Bo góc
     * - Custom placeholder composable
     */
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

            /**
             * Custom placeholder cho Native Ad Medium (280dp height)
             */
            fun nativeMediumPlaceholder(content: @Composable () -> Unit) = apply {
                this.nativeMediumPlaceholder = content
            }

            /**
             * Custom placeholder cho Native Ad Small (80dp height)
             */
            fun nativeSmallPlaceholder(content: @Composable () -> Unit) = apply {
                this.nativeSmallPlaceholder = content
            }

            /**
             * Custom placeholder cho Banner Ad
             */
            fun bannerPlaceholder(content: @Composable () -> Unit) = apply {
                this.bannerPlaceholder = content
            }

            fun build() = AdPlaceholderConfig(
                backgroundColor,
                shimmerBaseColor,
                shimmerHighlightColor,
                loadingIndicatorColor,
                cornerRadius,
                useShimmer,
                showLoadingIndicator,
                nativeMediumPlaceholder,
                nativeSmallPlaceholder,
                bannerPlaceholder
            )
        }

        companion object {
            fun builder() = Builder()
        }
    }
}
