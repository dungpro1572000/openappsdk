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
        val title: String? = null,
        val subTitle: String? = null,
        @field:IntegerRes val img: Int? = null,
        val adId: String,
        val showAd: Boolean
    ) {
        class Builder {
            private var title: String = ""
            private var subTitle: String = ""
            private var img: Int = 0
            private var adId: String = ""
            private var showAd: Boolean = true

            fun adId(adId: String) = apply { this.adId = adId }
            fun showAd(showAd: Boolean) = apply { this.showAd = showAd }
            fun title(title: String) = apply { this.title = title }
            fun subTitle(subTitle: String) = apply { this.subTitle = subTitle }
            fun img(@RawRes img: Int) = apply { this.img = img }

            fun build() = Onboarding1Config(title, subTitle, img, adId, showAd)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    class Onboarding2Config private constructor(
        val title: String? = null,
        val subTitle: String? = null,
        @field:IntegerRes val img: Int? = null,
        val adId: String,
        val showAd: Boolean
    ) {
        class Builder {
            private var title: String = ""
            private var subTitle: String = ""
            private var img: Int = 0
            private var adId: String = ""
            private var showAd: Boolean = true

            fun adId(adId: String) = apply { this.adId = adId }
            fun showAd(showAd: Boolean) = apply { this.showAd = showAd }
            fun title(title: String) = apply { this.title = title }
            fun subTitle(subTitle: String) = apply { this.subTitle = subTitle }
            fun img(@RawRes img: Int) = apply { this.img = img }

            fun build() = Onboarding2Config(title, subTitle, img, adId, showAd)
        }

        companion object {
            fun builder() = Builder()
        }
    }


    abstract class BaseLanguageConfig(
        val adId: String,
        val showAd: Boolean
    )

    class Language1Config private constructor(
        adId: String,
        showAd: Boolean
    ) : BaseLanguageConfig(adId, showAd) {

        class Builder {
            private var adId: String = ""
            private var showAd: Boolean = true

            fun adId(adId: String) = apply { this.adId = adId }
            fun showAd(showAd: Boolean) = apply { this.showAd = showAd }

            fun build() = Language1Config(adId, showAd)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    class Language2Config private constructor(
        adId: String,
        showAd: Boolean
    ) : BaseLanguageConfig(adId, showAd) {

        class Builder {
            private var adId: String = ""
            private var showAd: Boolean = true

            fun adId(adId: String) = apply { this.adId = adId }
            fun showAd(showAd: Boolean) = apply { this.showAd = showAd }

            fun build() = Language2Config(adId, showAd)
        }

        companion object {
            fun builder() = Builder()
        }
    }

    class PrepareDataConfig private constructor(
        val delayTime: Int,
        val adId: String,
        val showAd: Boolean
    ) {
        class Builder {
            private var delayTime: Int = 5000
            private var adId: String = ""
            private var showAd: Boolean = true

            fun delayTime(delayTime: Int) = apply { this.delayTime = delayTime }
            fun adId(adId: String) = apply { this.adId = adId }
            fun showAd(showAd: Boolean) = apply { this.showAd = showAd }

            fun build() = PrepareDataConfig(delayTime, adId, showAd)
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
