package com.dungz.openappsdk.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.dungz.our_ads.manager.NativeAdManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLanguage1: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences.Companion.getInstance(context) }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Initialize Ads SDK and managers

            // Start loading ads_lang_001 for Language1 screen
            NativeAdManager.loadAd(
                adHigherId = OpenAppConfig.getLanguage1Config().adIdHigh,
                adNormalId = OpenAppConfig.getLanguage1Config().adIdNormal,
                showHigher = OpenAppConfig.getLanguage1Config().showAdHigh,
                showNormal = OpenAppConfig.getLanguage1Config().showAdNormal
            )


        // Wait 5 seconds as per strategy
        delay(5000)

        // Check if old user
        val isOldUser = userPreferences.isOldUser()

        isLoading = false

        if (isOldUser) {
            // Old user -> go to MainActivity directly
            onNavigateToMain()
        } else {
            // New user -> go to Language selection
            onNavigateToLanguage1()
        }
    }

    SplashContent()
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
            // App Icon
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress indicator
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Processing text
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
