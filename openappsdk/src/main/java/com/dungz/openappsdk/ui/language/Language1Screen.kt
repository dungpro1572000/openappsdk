package com.dungz.openappsdk.ui.language

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.dungz.openappsdk.model.Language
import com.dungz.openappsdk.model.LanguageList
import com.dungz.openappsdk.ui.components.LanguageItem
import android.app.Activity
import java.lang.ref.WeakReference
import com.dungz.openappsdk.ui.components.NativeAdMediumPlaceholder
import com.dungz.openappsdk.ui.configUI.LanguageConfigUI
import com.dungz.our_ads.controller.NativeAdsController
import com.dungz.our_ads.state.NativeAdState
import com.dungz.our_ads.ui.MediumNativeContainerAdView
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language1Screen(
    languageItem: (@Composable (language: Language, isSelected: Boolean) -> Unit)? = null,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val config = OpenAppConfig.getLanguage1Config()
    val shouldShowAd = config.showAd

    // Preload native ad for Language2 screen
    LaunchedEffect(Unit) {
        val lang2Config = OpenAppConfig.getLanguage2Config()
        if (lang2Config.showAd && lang2Config.adId.isNotEmpty()) {
            activity?.let { act ->
                NativeAdsController.preloadAds(
                    activity = WeakReference(act),
                    adUnitId = lang2Config.adId,
                    preloadKey = "native_lang_002"
                )
            }
        }
    }

    Scaffold(
        containerColor = if (LanguageConfigUI.backgroundColor != Color.Transparent)
            LanguageConfigUI.backgroundColor
        else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (LanguageConfigUI.title1Compose != null) {
                        LanguageConfigUI.title1Compose!!()
                    } else {
                        Text(
                            text = LanguageConfigUI.title1Text,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = if (LanguageConfigUI.appBarBackgroundColor != Color.Transparent)
                    TopAppBarDefaults.topAppBarColors(containerColor = LanguageConfigUI.appBarBackgroundColor)
                else TopAppBarDefaults.topAppBarColors(),
                actions = {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(LanguageConfigUI.saveButtonText)
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
                    itemsIndexed(LanguageList.languages) { index, language ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onLanguageSelected(language.code) }
                        ) {
                            if (languageItem != null) {
                                languageItem(language, false)
                            } else {
                                LanguageItem(
                                    language = language,
                                    isSelected = false,
                                    onClick = { onLanguageSelected(language.code) },
                                    showHandPointer = index == 1
                                )
                            }
                        }
                    }
                }
            }

            // Native Ad area
            if (shouldShowAd) {
                val nativeState = NativeAdsController.listAds["native_lang_001"]
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
private fun Language1ScreenPreview() {
    MaterialTheme {
        Language1Screen(
            onLanguageSelected = {}
        )
    }
}
