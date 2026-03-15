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
import com.dungz.our_ads.manager.InterstitialAdManager
import kotlinx.coroutines.delay
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

    // Function to show ad
    fun showAdAndNavigate() {
        if (hasNavigated || isAdShowing || activity == null) return

        val isReady = InterstitialAdManager.isReady(
            OpenAppConfig.getPrepareDataConfig().adIdHigh,
            OpenAppConfig.getPrepareDataConfig().adIdNormal
        )

        if (isReady) {
            isAdShowing = true
            InterstitialAdManager.showAd(
                activity = activity,
                adHigherId = OpenAppConfig.getPrepareDataConfig().adIdHigh,
                adNormalId = OpenAppConfig.getPrepareDataConfig().adIdNormal,
                showHigher = OpenAppConfig.getPrepareDataConfig().showAdHigh,
                showNormal = OpenAppConfig.getPrepareDataConfig().showAdNormal,
                onAdDismissed = {
                    // User closed ad -> navigate to main
                    navigateToMain()
                },
                onAdFailedToShow = {
                    // Failed to show -> navigate to main
                    navigateToMain()
                }
            )
        } else {
            // Ad not ready -> navigate to main
            navigateToMain()
        }
    }

    // Load ad and wait up to 5 seconds
    LaunchedEffect(Unit) {
        var adLoaded = false

        // Start loading ad
        InterstitialAdManager.loadAd(
                adHigherId = OpenAppConfig.getPrepareDataConfig().adIdHigh,
            adNormalId = OpenAppConfig.getPrepareDataConfig().adIdNormal,
            showHigher = OpenAppConfig.getPrepareDataConfig().showAdHigh,
            showNormal = OpenAppConfig.getPrepareDataConfig().showAdNormal,
            onLoaded = {
                adLoaded = true
            },
            onFailed = {
                // Will be handled by timeout
            }
        )

        // Wait up to 5 seconds for ad to load
        val startTime = System.currentTimeMillis()
        while (!adLoaded && System.currentTimeMillis() - startTime < 5000) {
            delay(100)
        }

        // After timeout or ad loaded -> show ad or navigate
        showAdAndNavigate()
    }

    if (content != null) content() else PrepareDataContent()
}

@Composable
private fun PrepareDataContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Loading animation
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 5.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Loading text
        Text(
            text = "Loading data...",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please wait while we prepare everything for you",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PrepareDataScreenPreview() {
    MaterialTheme {
        PrepareDataContent()
    }
}
