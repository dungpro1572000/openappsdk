package com.dungz.ads_open.ui.language

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.dungz.ads_open.data.LanguageManager
import com.dungz.ads_open.data.UserPreferences
import com.dungz.ads_open.model.LanguageList
import com.dungz.ads_open.ui.components.LanguageItem
import com.dungz.ads_open.ui.components.NativeAdPlaceholder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language2Screen(
    initialSelectedCode: String,
    onSaveAndContinue: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val scope = rememberCoroutineScope()

    var selectedCode by remember { mutableStateOf(initialSelectedCode) }

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenHeight = maxHeight
            val bottomHeight = screenHeight * 0.4f

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top content (60% height)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
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

                // Bottom area (40% height) for Native Ad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bottomHeight)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NativeAdPlaceholder(
                        modifier = Modifier.fillMaxWidth(),
                        height = bottomHeight - 32.dp
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
