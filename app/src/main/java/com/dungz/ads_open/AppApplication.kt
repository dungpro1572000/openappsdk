package com.dungz.ads_open

import android.app.Application
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.remotedata.RemoteDataObject
import com.dungz.our_ads.AdsInitializer

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AdsInitializer.initialize(this)
        RemoteDataObject.init(this)

        OpenAppConfig.init {
            splashConfig {
                idBanner(BuildConfig.ADS_SPL_BANNER)
                idInter(BuildConfig.ADS_SPL_INTER)
                totalDelay(30000)
            }
            languageConfig {
                // No special config needed for language screen
            }
            onboardingConfig {
                onb1NativeAdId(BuildConfig.ADS_ONB_001)
                onb2NativeAdId(BuildConfig.ADS_ONB_002)
                prepareNativeAdId(BuildConfig.ADS_PREPARE_NATIVE)
                showOnb1Ad(RemoteDataObject.showAdOnb1)
                showOnb2Ad(RemoteDataObject.showAdOnb2)
                showPrepareAd(RemoteDataObject.showAdPrepareNative)
                layoutNative(R.layout.app_native_ads)
            }
            prepareDataConfig {
                delayTime(5000)
            }
        }
    }
}
