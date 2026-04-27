package com.dungz.openappsdk.ui.onboarding

import android.app.Activity
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.R
import com.dungz.our_ads.controller.InterAdsController
import com.dungz.our_ads.controller.NativeAdsController
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

private val PAGE_TITLES = listOf("Welcome", "Get Started", "You're All Set")
private val PAGE_SUBTITLES = listOf(
    "Discover amazing features",
    "Everything is ready for you",
    "Let's begin your journey"
)
private const val NEXT_BUTTON_TEXT = "Next"
private const val START_BUTTON_TEXT = "Start"

@Composable
fun OnboardingScreen(
    onNavigateToPrepareData: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val config = OpenAppConfig.getOnboardingConfig()
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    var isNavigating by remember { mutableStateOf(false) }

    // Preload ads based on current page
    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> {
                if (config.showOnb2Ad && config.onb2NativeAdId.isNotEmpty()) {
                    activity?.let { act ->
                        NativeAdsController.preloadAds(
                            activity = WeakReference(act),
                            adUnitId = config.onb2NativeAdId
                        )
                    }
                }
            }
            1 -> {
                if (config.showPrepareAd && config.prepareNativeAdId.isNotEmpty()) {
                    activity?.let { act ->
                        NativeAdsController.preloadAds(
                            activity = WeakReference(act),
                            adUnitId = config.prepareNativeAdId
                        )
                    }
                }
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = !isNavigating
    ) { page ->
        Column(modifier = Modifier.fillMaxSize()) {
            when (page) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (config.showOnb1Ad) 0.6f else 1f)
                    ) {
                        OnboardingPageContent(
                            config = config,
                            page = 0,
                            onAction = {
                                scope.launch { pagerState.animateScrollToPage(1) }
                            }
                        )
                    }
                    if (config.showOnb1Ad) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.4f),
                            contentAlignment = Alignment.Center
                        ) {
                            activity?.let { act ->
                                NativeAdsController.MediumNativeContainerAdView(
                                    activity = WeakReference(act),
                                    adId = config.onb1NativeAdId,
                                    nativeLayout = com.dungz.our_ads.R.layout.native_ad_medium
                                )
                            }
                        }
                    }
                }

                1 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OnboardingPageContent(
                            config = config,
                            page = 1,
                            onAction = {
                                scope.launch { pagerState.animateScrollToPage(2) }
                            }
                        )
                    }
                }

                2 -> {
                    val onStart: () -> Unit = {
                        if (!isNavigating) {
                            isNavigating = true
                            activity?.let { act ->
                                scope.launch {
                                    if (InterAdsController.listAds[OpenAppConfig.getSplashConfig().idInter] != null) {
                                        InterAdsController.showAds(
                                            activity = WeakReference(act),
                                            adUnitId = OpenAppConfig.getSplashConfig().idInter,
                                            onShowSuccess = { onNavigateToPrepareData() },
                                            onShowFailed = { onNavigateToPrepareData() }
                                        )
                                    } else {
                                        onNavigateToPrepareData()
                                    }
                                }
                            } ?: run {
                                onNavigateToPrepareData()
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (config.showOnb2Ad) 0.6f else 1f)
                    ) {
                        OnboardingPageContent(
                            config = config,
                            page = 2,
                            onAction = onStart
                        )
                    }
                    if (config.showOnb2Ad) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.4f),
                            contentAlignment = Alignment.Center
                        ) {
                            activity?.let { act ->
                                NativeAdsController.MediumNativeContainerAdView(
                                    activity = WeakReference(act),
                                    adId = config.onb2NativeAdId,
                                    nativeLayout = com.dungz.our_ads.R.layout.native_ad_medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    config: OpenAppConfig.OnboardingConfig,
    page: Int,
    onAction: () -> Unit
) {
    val contentOverride = when (page) {
        0 -> config.onboardingContent1
        1 -> config.onboardingContent2
        2 -> config.onboardingContent3
        else -> null
    }
    if (contentOverride != null) {
        contentOverride()
        return
    }

    val imageRes = when (page) {
        0 -> R.drawable.onboarding_1
        1 -> R.drawable.onboarding_2
        2 -> R.drawable.onboarding_3
        else -> R.drawable.onboarding_1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Onboarding ${page + 1}",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = PAGE_TITLES[page],
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = PAGE_SUBTITLES[page],
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = if (page == 2) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(
                text = if (page == 2) START_BUTTON_TEXT else NEXT_BUTTON_TEXT,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (page == 2) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen(onNavigateToPrepareData = {})
    }
}
