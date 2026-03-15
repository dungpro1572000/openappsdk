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
import com.dungz.openappsdk.ui.components.NativeAdMediumPlaceholder
import com.dungz.our_ads.manager.NativeAdManager
import com.dungz.our_ads.ui.NativeAdSize
import com.dungz.our_ads.ui.NativeAdView

@Composable
fun OnBoarding1Screen(
    content: (@Composable () -> Unit)? = null,
    onNext: () -> Unit
) {
    // Load ads_onb_002 when entering this screen (for Onboarding2)
    LaunchedEffect(Unit) {
        NativeAdManager.loadAd(
            adHigherId = OpenAppConfig.getOnboarding2Config().nativeAdIdHigh,
            adNormalId = OpenAppConfig.getOnboarding2Config().nativeAdIdNormal,
            showHigher = OpenAppConfig.getOnboarding2Config().showNativeAdHigh,
            showNormal = OpenAppConfig.getOnboarding2Config().showNativeAdNormal
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
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
                    Image(
                        painter = painterResource(
                            id = OpenAppConfig.getOnboarding1Config().img ?: R.drawable.onboarding_1
                        ),
                        contentDescription = "Onboarding 1",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OpenAppConfig.getOnboarding1Config().title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OpenAppConfig.getOnboarding2Config().subTitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.titleMedium
                        )
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
            // Show ads_onb_001 (loaded from Language2)
            NativeAdView(
                adHigherId = OpenAppConfig.getOnboarding1Config().adIdHigh,
                adNormalId = OpenAppConfig.getOnboarding1Config().adIdNormal,
                showHigher = OpenAppConfig.getOnboarding1Config().showAdHigh,
                showNormal = OpenAppConfig.getOnboarding1Config().showAdNormal,
                size = NativeAdSize.MEDIUM,
                loadingPlaceholder = { NativeAdMediumPlaceholder() },
                modifier = Modifier.fillMaxWidth()
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
