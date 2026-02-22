package com.dungz.openappsdk.ui.language

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.data.LanguageManager
import com.dungz.openappsdk.data.UserPreferences
import com.dungz.openappsdk.model.LanguageList
import com.dungz.openappsdk.ui.components.LanguageItem
import com.dungz.openappsdk.ui.components.NativeAdMediumPlaceholder
import com.dungz.our_ads.manager.NativeAdManager
import com.dungz.our_ads.state.RetryConfig
import com.dungz.our_ads.ui.NativeAdSize
import com.dungz.our_ads.ui.NativeAdView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language2Screen(
    initialSelectedCode: String,
    onSaveAndContinue: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences.Companion.getInstance(context) }
    val scope = rememberCoroutineScope()
    val config = OpenAppConfig.getLanguage2Config()

    var selectedCode by remember { mutableStateOf(initialSelectedCode) }

    // Track if ad should be shown
    val shouldShowAd = config.showAdHigh || config.showAdNormal
    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }

    // Determine if ad area should be visible
    val showAdArea = shouldShowAd && !isAdFailed

    // Load ads_onb_001 when entering this screen (for Onboarding1)
    LaunchedEffect(Unit) {
        NativeAdManager.loadAd(
            adHigherId = OpenAppConfig.getOnboarding1Config().adIdHigh,
            adNormalId = OpenAppConfig.getOnboarding1Config().adIdNormal,
            showHigher = OpenAppConfig.getOnboarding1Config().showAdHigh,
            showNormal = OpenAppConfig.getOnboarding1Config().showAdNormal
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Confirm Language",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                // Save language preference
                                userPreferences.setSelectedLanguage(selectedCode)

                                // Apply language change
                                activity?.let {
                                    LanguageManager.applyLanguageAndRecreate(it, selectedCode)
                                }

                                // Continue to onboarding
                                onSaveAndContinue()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Language list - takes remaining space
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You can change language anytime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language list
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LanguageList.languages) { language ->
                        LanguageItem(
                            language = language,
                            isSelected = language.code == selectedCode,
                            onClick = { selectedCode = language.code },
                            showHandPointer = false
                        )
                    }
                }
            }

            // Native Ad area - only show if ads are enabled
            if (showAdArea) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    NativeAdView(
                        adHigherId = config.adIdHigh,
                        adNormalId = config.adIdNormal,
                        showHigher = config.showAdHigh,
                        showNormal = config.showAdNormal,
                        size = NativeAdSize.MEDIUM,
                        loadingPlaceholder = { NativeAdMediumPlaceholder() },
                        modifier = Modifier.fillMaxWidth(),
                        retryConfig = RetryConfig(reloadDuration = 5000),
                        onAdLoaded = { isAdLoaded = true },
                        onAdFailed = { isAdFailed = true }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Language2ScreenPreview() {
    MaterialTheme {
        Language2Screen(
            initialSelectedCode = "en",
            onSaveAndContinue = {}
        )
    }
}
