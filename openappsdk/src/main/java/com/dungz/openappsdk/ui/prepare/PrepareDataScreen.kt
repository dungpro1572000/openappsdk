package com.dungz.openappsdk.ui.prepare

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.data.UserPreferences
import com.dungz.openappsdk.remotedata.RemoteDataObject
import com.dungz.openappsdk.ui.configUI.PrepareConfigUI
import com.dungz.our_ads.controller.InterAdsController
import com.dungz.our_ads.controller.NativeAdsController
import java.lang.ref.WeakReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PrepareDataScreen(
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val scope = rememberCoroutineScope()
    val prepConfig = OpenAppConfig.getPrepareDataConfig()
    val onbConfig = OpenAppConfig.getOnboardingConfig()

    var hasNavigated by remember { mutableStateOf(false) }

    fun navigateToMain() {
        if (!hasNavigated) {
            hasNavigated = true
            scope.launch {
                userPreferences.setIsOldUser(true)
                userPreferences.setOnboardingCompleted(true)
            }
            prepConfig.onNextToMainScreen?.invoke()
            onNavigateToMain()
        }
    }

    LaunchedEffect(Unit) {
        // Wait for the configured delay
        delay(prepConfig.delayTime.toLong())

        // Try show splash interstitial
        activity?.let { act ->
            if (RemoteDataObject.showAdSplInter || OpenAppConfig.getSplashConfig().idInter.isEmpty()) {
                navigateToMain()
            } else {
                if (InterAdsController.listAds[OpenAppConfig.getSplashConfig().idInter]!=null) {
                    InterAdsController.showAds(
                        activity = WeakReference(act),
                        adUnitId = OpenAppConfig.getSplashConfig().idInter,
                        onShowSuccess = { navigateToMain() },
                        onShowFailed = { navigateToMain() }
                    )
                } else {
                    navigateToMain()
                }
            }
        } ?: run {
            navigateToMain()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            if (prepConfig.content != null) {
                prepConfig.content.invoke()
            } else {
                PrepareDataContent()
            }
        }

        // Native ad area (prepare_native, preloaded on OnBoarding2)
        if (onbConfig.showPrepareAd) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                contentAlignment = Alignment.Center
            ) {
                activity?.let { act ->
                    NativeAdsController.MediumNativeContainerAdView(
                        activity = WeakReference(act),
                        adId = onbConfig.prepareNativeAdId,
                        nativeLayout = com.dungz.our_ads.R.layout.native_ad_medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PrepareDataContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (PrepareConfigUI.backgroundColor != Color.Transparent)
                    Modifier.background(PrepareConfigUI.backgroundColor)
                else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (PrepareConfigUI.progressCompose != null) {
            PrepareConfigUI.progressCompose!!()
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (PrepareConfigUI.titleCompose != null) {
            PrepareConfigUI.titleCompose!!()
        } else {
            Text(
                text = PrepareConfigUI.titleText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (PrepareConfigUI.subtitleCompose != null) {
            PrepareConfigUI.subtitleCompose!!()
        } else {
            Text(
                text = PrepareConfigUI.subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PrepareDataScreenPreview() {
    MaterialTheme {
        PrepareDataContent()
    }
}
