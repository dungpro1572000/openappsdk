package com.dungz.ads_open

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
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
                onboardingContent1 {
                    androidx.compose.foundation.layout.Column(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "Custom Onboarding Page 1",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                        )
                        androidx.compose.foundation.layout.Spacer(
                            modifier = androidx.compose.ui.Modifier.height(24.dp)
                        )
                        androidx.compose.material3.Button(
                            onClick = { OpenAppConfig.getOnboardingConfig().onNext() }
                        ) {
                            androidx.compose.material3.Text("Next")
                        }
                    }
                }
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
