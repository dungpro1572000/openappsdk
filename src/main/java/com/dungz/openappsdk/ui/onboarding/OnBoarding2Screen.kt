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
import com.dungz.our_ads.ui.NativeAdSize
import com.dungz.our_ads.ui.NativeAdView

@Composable
fun OnBoarding2Screen(
    content: (@Composable () -> Unit)? = null,
    onStart: () -> Unit
) {
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
                        painter = painterResource(id = R.drawable.onboarding_2),
                        contentDescription = "Onboarding 2",
                        modifier = Modifier.size(200.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Ready to Begin?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You're all set! Tap Start to begin your journey with us.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

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
                            text = "Start",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
            // Show ads_onb_002 (loaded from Onboarding1)
            NativeAdView(
                adHigherId = OpenAppConfig.getOnboarding2Config().nativeAdIdHigh,
                adNormalId = OpenAppConfig.getOnboarding2Config().nativeAdIdNormal,
                showHigher = OpenAppConfig.getOnboarding2Config().showNativeAdHigh,
                showNormal = OpenAppConfig.getOnboarding2Config().showNativeAdNormal,
                size = NativeAdSize.MEDIUM,
                loadingPlaceholder = { NativeAdMediumPlaceholder() },
                modifier = Modifier.fillMaxWidth()
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
