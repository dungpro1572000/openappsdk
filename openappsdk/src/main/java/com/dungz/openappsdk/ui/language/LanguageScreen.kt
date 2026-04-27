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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.dungz.openappsdk.ui.configUI.LanguageConfigUI
import com.dungz.our_ads.controller.InterAdsController
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    languageItem: (@Composable (language: Language, isSelected: Boolean) -> Unit)? = null,
    onNavigateToOnBoarding1: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val scope = rememberCoroutineScope()
    val config = OpenAppConfig.getLanguageConfig()

    var selectedCode by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val onSave: () -> Unit = {
        if (selectedCode.isNotEmpty() && !isSaving) {
            isSaving = true
            scope.launch {
                // 1. Save language preference
                userPreferences.setSelectedLanguage(selectedCode)

                // 2. Apply language
                activity?.let {
                    LanguageManager.applyLanguageAndRecreate(it, selectedCode)
                }

                // 3. Invoke config callback
                config.onLanguageSelected?.invoke(selectedCode)

                // 4. Try show splash interstitial
                activity?.let { act ->
                    if (InterAdsController.listAds[OpenAppConfig.getSplashConfig().idInter] != null) {
                        InterAdsController.showAds(
                            activity = WeakReference(act),
                            adUnitId = OpenAppConfig.getSplashConfig().idInter,
                            onShowSuccess = { onNavigateToOnBoarding1() },
                            onShowFailed = { onNavigateToOnBoarding1() }
                        )
                    } else {
                        onNavigateToOnBoarding1()
                    }
                } ?: run {
                    onNavigateToOnBoarding1()
                }
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
                    if (LanguageConfigUI.titleCompose != null) {
                        LanguageConfigUI.titleCompose!!()
                    } else {
                        Text(
                            text = LanguageConfigUI.titleText,
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
                            enabled = selectedCode.isNotEmpty() && !isSaving,
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
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LanguageScreenPreview() {
    MaterialTheme {
        LanguageScreen(
            onNavigateToOnBoarding1 = {}
        )
    }
}
