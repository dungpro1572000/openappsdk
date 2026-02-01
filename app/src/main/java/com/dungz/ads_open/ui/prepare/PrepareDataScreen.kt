package com.dungz.ads_open.ui.prepare

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.ads_open.data.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PrepareDataScreen(
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Wait 5 seconds
        delay(5000)

        // Set isOldUser = true before navigating
        scope.launch {
            userPreferences.setIsOldUser(true)
            userPreferences.setOnboardingCompleted(true)
        }

        onNavigateToMain()
    }

    PrepareDataContent()
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
