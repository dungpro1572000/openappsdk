package com.dungz.openappsdk.ui.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.R
import com.dungz.openappsdk.data.UserPreferences
import com.dungz.openappsdk.findActivity
import com.dungz.openappsdk.remotedata.RemoteDataObject
import com.dungz.our_ads.controller.InterAdsController
import com.dungz.our_ads.controller.NativeAdsController
import com.dungz.our_ads.ui.SmartBannerAd
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLanguage: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val config = remember { OpenAppConfig.getSplashConfig() }


    fun navigateAway(isOldUser: Boolean) {
        if (isOldUser) {
            onNavigateToMain()
        } else {
            onNavigateToLanguage()
        }
    }


    LaunchedEffect(Unit) {
        val isOldUser = userPreferences.isOldUser()
        var timeoutJob: Job? = null
        var adJob: Job? = null
        if (activity == null) {
            navigateAway(isOldUser)
            return@LaunchedEffect
        }

        // For new users, preload onb1_native
        if (!isOldUser) {
            val onbConfig = OpenAppConfig.getOnboardingConfig()
            if (onbConfig.showOnb1Ad && onbConfig.onb1NativeAdId.isNotEmpty()) {
                NativeAdsController.preloadAds(
                    activity = WeakReference(activity),
                    adUnitId = onbConfig.onb1NativeAdId
                )
            }
        }

         adJob = launch {

            val hasBanner = config.idBanner.isNotEmpty() && RemoteDataObject.showAdSplBanner
            val hasInter = config.idInter.isNotEmpty() && RemoteDataObject.showAdSplInter

            if (!hasBanner && !hasInter) {
                // No ads to load, just wait a bit and navigate
                delay(2000)
                navigateAway(isOldUser)
                return@launch
            }

            // Load interstitial
            if (hasInter) {
                InterAdsController.preloadAds(
                    activity = WeakReference(activity),
                    adUnitId = config.idInter,
                    onLoadSuccess = {
                        InterAdsController.showAds(
                            WeakReference(activity),
                            isShow = RemoteDataObject.showAdSplInter,
                            adUnitId = config.idInter,
                            onShowSuccess = {
                                Log.d("DungNT222","check onSHow Success")
                                timeoutJob?.cancel()
                                navigateAway(isOldUser)
                            },
                            onShowFailed = {
                                timeoutJob?.cancel()
                                navigateAway(isOldUser)
                            },
                        )
                    },
                    onLoadFailed = {}
                )
            }
        }
        timeoutJob = launch {
            if (config.totalDelay > 0) {
                delay(config.totalDelay.toLong())
            } else {
                delay(30000) // Default max wait time
            }
            navigateAway(isOldUser)
        }

        joinAll(adJob, timeoutJob)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main splash content
        if (config.content != null) {
            config.content.invoke()
        } else {
            SplashContent()
        }

        // Banner at bottom
        if (RemoteDataObject.showAdSplBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                SmartBannerAd(adUnitId = config.idBanner)
            }
        }
    }
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Processing, can contain ads",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        SplashContent()
    }
}
