package com.dungz.openappsdk.ui.prepare

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.dungz.our_ads.AppAdMob
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.lang.ref.WeakReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import com.dungz.openappsdk.ui.configUI.PrepareConfigUI
import androidx.compose.foundation.background
import kotlinx.coroutines.launch

@Composable
fun PrepareDataScreen(
    content: (@Composable () -> Unit)? = null,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences.Companion.getInstance(context) }
    val scope = rememberCoroutineScope()

    var hasNavigated by remember { mutableStateOf(false) }
    var isAdShowing by remember { mutableStateOf(false) }

    // Function to navigate to main (only once)
    fun navigateToMain() {
        if (!hasNavigated) {
            hasNavigated = true
            scope.launch {
                userPreferences.setIsOldUser(true)
                userPreferences.setOnboardingCompleted(true)
            }
            onNavigateToMain()
        }
    }

    // Load and show interstitial ad
    LaunchedEffect(Unit) {
        val prepConfig = OpenAppConfig.getPrepareDataConfig()
        val adId = if (prepConfig.showAd && prepConfig.adId.isNotEmpty()) prepConfig.adId else null

        var loadedAd: InterstitialAd? = null

        if (adId != null && activity != null) {
            val adDeferred = CompletableDeferred<InterstitialAd?>()
            AppAdMob.loadInterstitialAds(
                context = WeakReference(activity),
                id = adId,
                onLoadSuccess = { ad -> adDeferred.complete(ad) },
                onAdFailedToLoad = { adDeferred.complete(null) }
            )
            loadedAd = withTimeoutOrNull(5000) { adDeferred.await() }
        }

        if (loadedAd != null && activity != null && !hasNavigated) {
            isAdShowing = true
            AppAdMob.showInterstitialAd(
                activity = activity,
                interstitialAd = loadedAd,
                onAdDismissed = { navigateToMain() },
                onAdFailedToShow = { navigateToMain() }
            )
        } else {
            navigateToMain()
        }
    }

    if (content != null) content() else PrepareDataContent()
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
        // Loading animation
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

        // Title
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

        // Subtitle
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
