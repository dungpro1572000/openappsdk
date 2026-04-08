package com.dungz.openappsdk.ui.language

import android.app.Activity
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.data.LanguageManager
import com.dungz.openappsdk.data.UserPreferences
import com.dungz.openappsdk.model.Language
import com.dungz.openappsdk.model.LanguageList
import com.dungz.openappsdk.ui.components.LanguageItem
import java.lang.ref.WeakReference
import com.dungz.openappsdk.ui.components.NativeAdMediumPlaceholder
import com.dungz.our_ads.controller.NativeAdsController
import com.dungz.our_ads.state.NativeAdState
import com.dungz.our_ads.ui.MediumNativeContainerAdView
import com.dungz.openappsdk.ui.configUI.LanguageConfigUI
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language2Screen(
    initialSelectedCode: String,
    languageItem: (@Composable (language: Language, isSelected: Boolean) -> Unit)? = null,
    onSaveAndContinue: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences.Companion.getInstance(context) }
    val scope = rememberCoroutineScope()
    val config = OpenAppConfig.getLanguage2Config()

    var selectedCode by remember { mutableStateOf(initialSelectedCode) }

    val shouldShowAd = config.showAd

    // Preload native ad for OnBoarding1 screen
    LaunchedEffect(Unit) {
        val onb1Config = OpenAppConfig.getOnboarding1Config()
        if (onb1Config.showAd && onb1Config.adId.isNotEmpty()) {
            activity?.let { act ->
                NativeAdsController.preloadAds(
                    activity = WeakReference(act),
                    adUnitId = onb1Config.adId,
                    preloadKey = "native_onb_001"
                )
            }
        }
    }

    val onSave: () -> Unit = {
        scope.launch {
            userPreferences.setSelectedLanguage(selectedCode)
            activity?.let {
                LanguageManager.applyLanguageAndRecreate(it, selectedCode)
            }
            onSaveAndContinue()
        }
    }

    Scaffold(
        containerColor = if (LanguageConfigUI.backgroundColor != Color.Transparent)
            LanguageConfigUI.backgroundColor
        else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (LanguageConfigUI.title2Compose != null) {
                        LanguageConfigUI.title2Compose!!()
                    } else {
                        Text(
                            text = LanguageConfigUI.title2Text,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = if (LanguageConfigUI.appBarBackgroundColor != Color.Transparent)
                    TopAppBarDefaults.topAppBarColors(containerColor = LanguageConfigUI.appBarBackgroundColor)
                else TopAppBarDefaults.topAppBarColors(),
                actions = {
                    if (LanguageConfigUI.saveButtonCompose != null) {
                        LanguageConfigUI.saveButtonCompose!!(selectedCode, onSave)
                    } else {
                        Button(
                            onClick = onSave,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(LanguageConfigUI.saveButtonText)
                        }
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

                if (LanguageConfigUI.descriptionCompose != null) {
                    LanguageConfigUI.descriptionCompose!!()
                } else {
                    Text(
                        text = LanguageConfigUI.descriptionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language list
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LanguageList.languages) { language ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCode = language.code }
                        ) {
                            if (languageItem != null) {
                                languageItem(language, language.code == selectedCode)
                            } else {
                                LanguageItem(
                                    language = language,
                                    isSelected = language.code == selectedCode,
                                    onClick = { selectedCode = language.code },
                                    showHandPointer = false
                                )
                            }
                        }
                    }
                }
            }

            // Native Ad area
            if (shouldShowAd) {
                val nativeState = NativeAdsController.listAds["native_lang_002"]
                if (nativeState !is NativeAdState.Failed) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        MediumNativeContainerAdView(
                            nativeAdState = nativeState ?: NativeAdState.Loading,
                            nativeLayout = R.layout.native_ad_medium,
                            shimmerAds = { NativeAdMediumPlaceholder() }
                        )
                    }
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
