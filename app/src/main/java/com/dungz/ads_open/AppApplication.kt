package com.dungz.ads_open

import android.app.Application
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.remotedata.RemoteDataObject
import com.dungz.our_ads.AdsInitializer
import com.dungz.our_ads.manager.InterstitialAdManager
import com.dungz.our_ads.manager.NativeAdManager

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdsInitializer.initialize(this)
        NativeAdManager.init(this)
        InterstitialAdManager.init(this)
        RemoteDataObject.init(this)
        OpenAppConfig.init {
            splashConfig {
                delayTime(3000)
            }
            language1Config {
                this.adId(BuildConfig.ADS_LANG_001)
                    .showAd(RemoteDataObject.showAdLang1)
                    .build()
            }
            language2Config {
                this.adId(BuildConfig.ADS_LANG_002)
                    .showAd(RemoteDataObject.showAdLang2)
                    .build()
            }
            onboarding1Config {
                this.adId(BuildConfig.ADS_ONB_001)
                    .showAd(RemoteDataObject.showAdOnb1)
                    .build()
            }
            onboarding2Config {
                this.adId(BuildConfig.ADS_ONB_002)
                    .showAd(RemoteDataObject.showAdOnb2)
                    .build()
            }
            prepareDataConfig {
                this.adId(BuildConfig.ADS_INTER_001)
                    .showAd(RemoteDataObject.showAdInter)
                    .build()
            }
        }
    }
}
