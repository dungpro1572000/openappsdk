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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.R
import com.dungz.openappsdk.ui.components.NativeAdMediumPlaceholder
import com.dungz.our_ads.controller.NativeAdsController
import com.dungz.our_ads.state.NativeAdState
import com.dungz.openappsdk.ui.configUI.OnboardingConfigUI
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.dungz.our_ads.MediumNativeContainerAdView

@Composable
fun OnBoarding2Screen(
    content: (@Composable () -> Unit)? = null,
    onStart: () -> Unit
) {
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
                    if (OnboardingConfigUI.image2Compose != null) {
                        OnboardingConfigUI.image2Compose!!()
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.onboarding_2),
                            contentDescription = "Onboarding 2",
                            modifier = Modifier.size(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    if (OnboardingConfigUI.title2Compose != null) {
                        OnboardingConfigUI.title2Compose!!()
                    } else {
                        Text(
                            text = OnboardingConfigUI.title2Text,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    if (OnboardingConfigUI.subtitle2Compose != null) {
                        OnboardingConfigUI.subtitle2Compose!!()
                    } else {
                        Text(
                            text = OnboardingConfigUI.subtitle2Text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Start button
                    if (OnboardingConfigUI.startButtonCompose != null) {
                        OnboardingConfigUI.startButtonCompose!!(onStart)
                    } else {
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = OnboardingConfigUI.startButtonText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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
            val nativeState = NativeAdsController.listAds["native_onb_002"]
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
private fun OnBoarding2ScreenPreview() {
    MaterialTheme {
        OnBoarding2Screen(onStart = {})
    }
}
