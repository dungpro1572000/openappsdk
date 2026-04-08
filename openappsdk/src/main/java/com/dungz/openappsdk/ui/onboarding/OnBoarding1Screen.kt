package com.dungz.openappsdk.ui.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.R
import android.app.Activity
import java.lang.ref.WeakReference
import androidx.compose.ui.platform.LocalContext
import com.dungz.openappsdk.ui.components.NativeAdMediumPlaceholder
import com.dungz.our_ads.controller.NativeAdsController
import com.dungz.our_ads.state.NativeAdState
import com.dungz.openappsdk.ui.configUI.OnboardingConfigUI
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.dungz.our_ads.MediumNativeContainerAdView

@Composable
fun OnBoarding1Screen(
    content: (@Composable () -> Unit)? = null,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Preload native ad for OnBoarding2 screen
    LaunchedEffect(Unit) {
        val onb2Config = OpenAppConfig.getOnboarding2Config()
        if (onb2Config.showAd && onb2Config.adId.isNotEmpty()) {
            activity?.let { act ->
                NativeAdsController.preloadAds(
                    activity = WeakReference(act),
                    adUnitId = onb2Config.adId,
                    preloadKey = "native_onb_002"
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (OnboardingConfigUI.backgroundColor != Color.Transparent)
                    Modifier.background(OnboardingConfigUI.backgroundColor)
                else Modifier
            )
    ) {
        // Top content (60% height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            if (content != null) {
                content()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Image
                    if (OnboardingConfigUI.image1Compose != null) {
                        OnboardingConfigUI.image1Compose!!()
                    } else {
                        Image(
                            painter = painterResource(
                                id = OpenAppConfig.getOnboarding1Config().img ?: R.drawable.onboarding_1
                            ),
                            contentDescription = "Onboarding 1",
                            modifier = Modifier.size(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    if (OnboardingConfigUI.title1Compose != null) {
                        OnboardingConfigUI.title1Compose!!()
                    } else {
                        Text(
                            text = OnboardingConfigUI.title1Text,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    if (OnboardingConfigUI.subtitle1Compose != null) {
                        OnboardingConfigUI.subtitle1Compose!!()
                    } else {
                        Text(
                            text = OnboardingConfigUI.subtitle1Text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Next button
                    if (OnboardingConfigUI.nextButtonCompose != null) {
                        OnboardingConfigUI.nextButtonCompose!!(onNext)
                    } else {
                        Button(
                            onClick = onNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = OnboardingConfigUI.nextButtonText,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        // Bottom area (40% height) for Native Ad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            contentAlignment = Alignment.Center
        ) {
            val nativeState = NativeAdsController.listAds["native_onb_001"]
            MediumNativeContainerAdView(
                nativeAdState = nativeState ?: NativeAdState.Loading,
                nativeLayout = R.layout.native_ad_medium,
                shimmerAds = { NativeAdMediumPlaceholder() }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnBoarding1ScreenPreview() {
    MaterialTheme {
        OnBoarding1Screen(onNext = {})
    }
}
