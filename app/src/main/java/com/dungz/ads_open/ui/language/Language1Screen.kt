package com.dungz.ads_open.ui.language

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.ads_open.model.LanguageList
import com.dungz.ads_open.ui.components.LanguageItem
import com.dungz.ads_open.ui.components.NativeAdPlaceholder

@Composable
fun Language1Screen(
    onLanguageSelected: (String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
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
                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Select Your Language",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose your preferred language",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language list
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(LanguageList.languages) { index, language ->
                        LanguageItem(
                            language = language,
                            isSelected = false,
                            onClick = { onLanguageSelected(language.code) },
                            showHandPointer = index == 1
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Language1ScreenPreview() {
    MaterialTheme {
        Language1Screen(
            onLanguageSelected = {}
        )
    }
}
