package com.dungz.ads_open

import android.app.Application
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.our_ads.AdsInitializer
import com.dungz.our_ads.manager.InterstitialAdManager
import com.dungz.our_ads.manager.NativeAdManager

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdsInitializer.initialize(this)
        NativeAdManager.init(this)
        InterstitialAdManager.init(this)
        OpenAppConfig.init {
            onboarding2Config {
                this.adIdNormal(
                    BuildConfig.ADS_ONB_002_NORMAL,
                ).adIdHigh(BuildConfig.ADS_ONB_002_HIGH).showAdNormal(false).showAdHigh(false).build()
            }
            onboarding1Config {
                this.adIdNormal(
                    BuildConfig.ADS_ONB_001_NORMAL,
                )
                    .adIdHigh(BuildConfig.ADS_ONB_001_HIGH).showAdNormal(false).showAdHigh(false)
                    .build()
            }

            language1Config {
                this.adIdHigh(BuildConfig.ADS_LANG_001_HIGH)
                    .adIdNormal(BuildConfig.ADS_LANG_001_NORMAL).showAdHigh(false).showAdNormal(false)
                    .build()
            }
            language2Config {
                this.adIdHigh(BuildConfig.ADS_LANG_002_HIGH)
                    .adIdNormal(BuildConfig.ADS_LANG_002_NORMAL).showAdHigh(false).showAdNormal(false)
                    .build()
            }
            prepareDataConfig {
                this.adIdHigh(BuildConfig.ADS_INTER_001_HIGH)
                    .adIdNormal(BuildConfig.ADS_INTER_001_NORMAL).showAdHigh(true)
                    .showAdNormal(true)
                    .build()
            }
        }
    }
}