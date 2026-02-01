package com.dungz.ads_open

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dungz.ads_open.navigation.OpenAppNavigation
import com.dungz.ads_open.ui.theme.AdsOpenTheme

/**
 * SplashActivity - Activity launcher của app
 *
 * Chứa toàn bộ flow khởi động:
 * - SplashScreen (2s loading + check user)
 * - Language1Screen (chọn ngôn ngữ lần đầu)
 * - Language2Screen (xác nhận ngôn ngữ)
 * - OnBoarding1Screen
 * - OnBoarding2Screen
 * - PrepareDataScreen (loading 5s + show interstitial)
 *
 * Sau khi hoàn thành → Navigate đến MainActivity
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdsOpenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OpenAppNavigation(
                        onNavigateToMain = {
                            navigateToMainActivity()
                        }
                    )
                }
            }
        }
    }

    /**
     * Navigate đến MainActivity sau khi hoàn thành flow
     * - Old user: SplashScreen → MainActivity (skip language/onboarding)
     * - New user: SplashScreen → Language → Onboarding → PrepareData → MainActivity
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
