# 📱 Open App Module Setup Guide

> **Version:** 1.0.0 | **Last Updated:** January 2025 | **Min SDK:** 24 | **Target SDK:** 34

---

## 📋 Checklist

### Setup Phase
- [ ] Tạo module mới `open-app` trong project
- [ ] Cài đặt Firebase (Remote Config, Crashlytics)
- [ ] Cài đặt DataStore, Room Database
- [ ] Cài đặt Coroutines, Compose

### Screens Phase
- [ ] Tạo SplashScreen
- [ ] Tạo Language1Screen (chọn ngôn ngữ lần đầu)
- [ ] Tạo Language2Screen (xác nhận/đổi ngôn ngữ)
- [ ] Tạo OnBoarding1Screen
- [ ] Tạo OnBoarding2Screen
- [ ] Tạo PrepareDataScreen

### Data Phase
- [ ] Tạo UserPreferences (DataStore)
- [ ] Tạo LanguageManager
- [ ] Tạo Navigation

### Testing Phase
- [ ] Test flow: New User
- [ ] Test flow: Old User (skip to MainActivity)
- [ ] Test language change
- [ ] Test Native Ads placeholder

---

Require Technology: 
Sử dụng Navigation3, Compose, Firebase (Remote Config, Crashlytics), DataStore, Room Database, Coroutines.

## 1️⃣ Module Structure

```
open-app/
├── src/main/
│   ├── kotlin/com/yourpackage/openapp/
│   │   ├── SplashActivity.kt          # Activity chính chứa toàn bộ flow
│   │   ├── navigation/
│   │   │   └── OpenAppNavigation.kt
│   │   ├── data/
│   │   │   ├── UserPreferences.kt
│   │   │   └── LanguageManager.kt
│   │   ├── model/
│   │   │   └── Language.kt
│   │   └── ui/
│   │       ├── splash/
│   │       │   └── SplashScreen.kt
│   │       ├── language/
│   │       │   ├── Language1Screen.kt
│   │       │   └── Language2Screen.kt
│   │       ├── onboarding/
│   │       │   ├── OnBoarding1Screen.kt
│   │       │   └── OnBoarding2Screen.kt
│   │       ├── prepare/
│   │       │   └── PrepareDataScreen.kt
│   │       └── components/
│   │           ├── LanguageItem.kt
│   │           ├── HandPointerAnimation.kt
│   │           └── NativeAdPlaceholder.kt
│   ├── res/
│   │   ├── drawable/
│   │   │   └── (flag icons)
│   │   └── values/
│   │       └── strings.xml
│   └── AndroidManifest.xml
└── build.gradle.kts
```

---

## 2️⃣ Project-level build.gradle.kts

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}
```

---

## 3️⃣ Module build.gradle.kts

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.yourpackage.openapp"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-config-ktx")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")

    // Preview
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## 4️⃣ Firebase Setup

### 4.1 Thêm google-services.json
Tải `google-services.json` từ Firebase Console và đặt vào thư mục `app/`

### 4.2 FirebaseInitializer.kt

```kotlin
package com.yourpackage.openapp.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

object FirebaseInitializer {

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        FirebaseApp.initializeApp(context)

        // Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
        }

        // Remote Config
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set defaults
        remoteConfig.setDefaultsAsync(mapOf(
            "show_ads_high" to true,
            "show_ads_normal" to true,
            // TODO: Add more default values
        ))

        isInitialized = true
    }

    suspend fun fetchRemoteConfig(): Boolean {
        return try {
            FirebaseRemoteConfig.getInstance().fetchAndActivate().await()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }

    fun getBoolean(key: String): Boolean = FirebaseRemoteConfig.getInstance().getBoolean(key)
    fun getString(key: String): String = FirebaseRemoteConfig.getInstance().getString(key)
    fun getLong(key: String): Long = FirebaseRemoteConfig.getInstance().getLong(key)
}
```

---

## 5️⃣ Language.kt (Model)

```kotlin
package com.yourpackage.openapp.model

import androidx.annotation.DrawableRes
import com.yourpackage.openapp.R
import java.util.Locale

data class Language(
    val code: String,
    val name: String,
    val locale: Locale,
    @DrawableRes val flagRes: Int
)

object LanguageList {
    val languages = listOf(
        Language("en", "English", Locale.ENGLISH, R.drawable.flag_us),
        Language("pt_BR", "Brazil", Locale("pt", "BR"), R.drawable.flag_brazil),
        Language("es_VE", "Venezuela", Locale("es", "VE"), R.drawable.flag_venezuela),
        Language("ru", "Russia", Locale("ru"), R.drawable.flag_russia),
        Language("zh", "Chinese", Locale.CHINESE, R.drawable.flag_china),
        Language("vi", "Vietnamese", Locale("vi"), R.drawable.flag_vietnam),
        Language("th", "Thailand", Locale("th"), R.drawable.flag_thailand),
        Language("id", "Indonesia", Locale("id"), R.drawable.flag_indonesia),
        Language("de", "German", Locale.GERMAN, R.drawable.flag_germany),
        Language("pt", "Portugal", Locale("pt"), R.drawable.flag_portugal),
        Language("ko", "Korea", Locale.KOREAN, R.drawable.flag_korea),
        Language("ja", "Japanese", Locale.JAPANESE, R.drawable.flag_japan),
    )

    fun getByCode(code: String): Language? = languages.find { it.code == code }
}
```

---

## 6️⃣ UserPreferences.kt (DataStore)

```kotlin
package com.yourpackage.openapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val IS_OLD_USER = booleanPreferencesKey("is_old_user")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Is Old User
    val isOldUserFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_OLD_USER] ?: false
    }

    suspend fun isOldUser(): Boolean = isOldUserFlow.first()

    suspend fun setIsOldUser(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_OLD_USER] = value
        }
    }

    // Selected Language
    val selectedLanguageFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_LANGUAGE]
    }

    suspend fun getSelectedLanguage(): String? = selectedLanguageFlow.first()

    suspend fun setSelectedLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_LANGUAGE] = languageCode
        }
    }

    // Onboarding Completed
    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = value
        }
    }
}
```

---

## 7️⃣ LanguageManager.kt

```kotlin
package com.yourpackage.openapp.data

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LanguageManager {

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = parseLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLanguageAndRecreate(activity: Activity, languageCode: String) {
        val locale = parseLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)

        // Không recreate ngay, để flow navigation xử lý
    }

    private fun parseLocale(languageCode: String): Locale {
        return if (languageCode.contains("_")) {
            val parts = languageCode.split("_")
            Locale(parts[0], parts[1])
        } else {
            Locale(languageCode)
        }
    }

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
}
```

---

## 8️⃣ OpenAppNavigation.kt

```kotlin
package com.yourpackage.openapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yourpackage.openapp.ui.language.Language1Screen
import com.yourpackage.openapp.ui.language.Language2Screen
import com.yourpackage.openapp.ui.onboarding.OnBoarding1Screen
import com.yourpackage.openapp.ui.onboarding.OnBoarding2Screen
import com.yourpackage.openapp.ui.prepare.PrepareDataScreen
import com.yourpackage.openapp.ui.splash.SplashScreen

sealed class OpenAppRoute(val route: String) {
    data object Splash : OpenAppRoute("splash")
    data object Language1 : OpenAppRoute("language1")
    data object Language2 : OpenAppRoute("language2/{selectedCode}") {
        fun createRoute(selectedCode: String) = "language2/$selectedCode"
    }
    data object OnBoarding1 : OpenAppRoute("onboarding1")
    data object OnBoarding2 : OpenAppRoute("onboarding2")
    data object PrepareData : OpenAppRoute("prepare_data")
}

@Composable
fun OpenAppNavigation(
    navController: NavHostController = rememberNavController(),
    onNavigateToMain: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = OpenAppRoute.Splash.route
    ) {
        composable(OpenAppRoute.Splash.route) {
            SplashScreen(
                onNavigateToMain = onNavigateToMain,
                onNavigateToLanguage1 = {
                    navController.navigate(OpenAppRoute.Language1.route) {
                        popUpTo(OpenAppRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(OpenAppRoute.Language1.route) {
            Language1Screen(
                onLanguageSelected = { languageCode ->
                    navController.navigate(OpenAppRoute.Language2.createRoute(languageCode))
                }
            )
        }

        composable(OpenAppRoute.Language2.route) { backStackEntry ->
            val selectedCode = backStackEntry.arguments?.getString("selectedCode") ?: "en"
            Language2Screen(
                initialSelectedCode = selectedCode,
                onSaveAndContinue = {
                    navController.navigate(OpenAppRoute.OnBoarding1.route) {
                        popUpTo(OpenAppRoute.Language1.route) { inclusive = true }
                    }
                }
            )
        }

        composable(OpenAppRoute.OnBoarding1.route) {
            OnBoarding1Screen(
                onNext = {
                    navController.navigate(OpenAppRoute.OnBoarding2.route)
                }
            )
        }

        composable(OpenAppRoute.OnBoarding2.route) {
            OnBoarding2Screen(
                onStart = {
                    navController.navigate(OpenAppRoute.PrepareData.route) {
                        popUpTo(OpenAppRoute.OnBoarding1.route) { inclusive = true }
                    }
                }
            )
        }

        composable(OpenAppRoute.PrepareData.route) {
            PrepareDataScreen(
                onNavigateToMain = onNavigateToMain
            )
        }
    }
}
```

---

## 9️⃣ Components

### 9.1 NativeAdPlaceholder.kt

```kotlin
package com.yourpackage.openapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Placeholder cho Native Ads
 * TODO: Thay thế bằng NativeAdView thực từ ads module
 */
@Composable
fun NativeAdPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 280.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F0F0)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Native Ad Placeholder",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Preview
@Composable
private fun NativeAdPlaceholderPreview() {
    NativeAdPlaceholder()
}
```

### 9.2 HandPointerAnimation.kt

```kotlin
package com.yourpackage.openapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HandPointerAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hand_pointer")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_y"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Icon(
        imageVector = Icons.Default.TouchApp,
        contentDescription = "Tap here",
        modifier = modifier
            .size((32 * scale).dp)
            .offset(y = offsetY.dp),
        tint = Color(0xFFFF6B00)
    )
}

@Preview
@Composable
private fun HandPointerAnimationPreview() {
    HandPointerAnimation()
}
```

### 9.3 LanguageItem.kt

```kotlin
package com.yourpackage.openapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourpackage.openapp.R
import com.yourpackage.openapp.model.Language
import java.util.Locale

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showHandPointer: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag
        Image(
            painter = painterResource(id = language.flagRes),
            contentDescription = language.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Language name
        Text(
            text = language.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        // Hand pointer animation
        if (showHandPointer) {
            HandPointerAnimation()
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Checkmark
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageItemPreview() {
    LanguageItem(
        language = Language("en", "English", Locale.ENGLISH, R.drawable.flag_us),
        isSelected = false,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LanguageItemSelectedPreview() {
    LanguageItem(
        language = Language("en", "English", Locale.ENGLISH, R.drawable.flag_us),
        isSelected = true,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LanguageItemWithHandPointerPreview() {
    LanguageItem(
        language = Language("pt_BR", "Brazil", Locale("pt", "BR"), R.drawable.flag_brazil),
        isSelected = false,
        onClick = {},
        showHandPointer = true
    )
}
```

---

## 🔟 SplashScreen.kt

```kotlin
package com.yourpackage.openapp.ui.splash

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
import com.yourpackage.openapp.R
import com.yourpackage.openapp.data.UserPreferences
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToLanguage1: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences.getInstance(context) }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Simulate loading / fetch remote config
        delay(2000)

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
                // TODO: Replace with your app icon
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
```

---

## 1️⃣1️⃣ Language1Screen.kt

```kotlin
package com.yourpackage.openapp.ui.language

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
import com.yourpackage.openapp.model.LanguageList
import com.yourpackage.openapp.ui.components.LanguageItem
import com.yourpackage.openapp.ui.components.NativeAdPlaceholder

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
                            isSelected = false, // No selection in Language1
                            onClick = { onLanguageSelected(language.code) },
                            showHandPointer = index == 1 // Hand pointer on 2nd item (index 1)
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
```

---

## 1️⃣2️⃣ Language2Screen.kt

```kotlin
package com.yourpackage.openapp.ui.language

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
import com.yourpackage.openapp.data.LanguageManager
import com.yourpackage.openapp.data.UserPreferences
import com.yourpackage.openapp.model.LanguageList
import com.yourpackage.openapp.ui.components.LanguageItem
import com.yourpackage.openapp.ui.components.NativeAdPlaceholder
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
                                showHandPointer = false // No hand pointer in Language2
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
```

---

## 1️⃣3️⃣ OnBoarding1Screen.kt

```kotlin
package com.yourpackage.openapp.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourpackage.openapp.R
import com.yourpackage.openapp.ui.components.NativeAdPlaceholder

@Composable
fun OnBoarding1Screen(
    onNext: () -> Unit
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Image
                Image(
                    // TODO: Replace with your onboarding image
                    painter = painterResource(id = R.drawable.onboarding_1),
                    contentDescription = "Onboarding 1",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Welcome to Our App",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Discover amazing features that will help you in your daily life. Let's get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Next button
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.titleMedium
                    )
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
private fun OnBoarding1ScreenPreview() {
    MaterialTheme {
        OnBoarding1Screen(onNext = {})
    }
}
```

---

## 1️⃣4️⃣ OnBoarding2Screen.kt

```kotlin
package com.yourpackage.openapp.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yourpackage.openapp.R
import com.yourpackage.openapp.ui.components.NativeAdPlaceholder

@Composable
fun OnBoarding2Screen(
    onStart: () -> Unit
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Image
                Image(
                    // TODO: Replace with your onboarding image
                    painter = painterResource(id = R.drawable.onboarding_2),
                    contentDescription = "Onboarding 2",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Ready to Begin?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "You're all set! Tap Start to begin your journey with us.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Start button (different style from Next)
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
private fun OnBoarding2ScreenPreview() {
    MaterialTheme {
        OnBoarding2Screen(onStart = {})
    }
}
```

---

## 1️⃣5️⃣ PrepareDataScreen.kt

```kotlin
package com.yourpackage.openapp.ui.prepare

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
import com.yourpackage.openapp.data.UserPreferences
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

        // TODO: Start MainActivity here
        // Example:
        // val intent = Intent(context, MainActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // context.startActivity(intent)

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
```

---

## 1️⃣6️⃣ SplashActivity.kt

> **Note:** `SplashActivity` là Activity launcher chính của app, chứa toàn bộ flow: Splash → Language → Onboarding → PrepareData. Sau khi hoàn thành flow, sẽ navigate đến `MainActivity` (app chính).

```kotlin
package com.yourpackage.openapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yourpackage.openapp.data.FirebaseInitializer
import com.yourpackage.openapp.navigation.OpenAppNavigation
import com.yourpackage.app.MainActivity // Import MainActivity của app chính

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

        // Initialize Firebase
        FirebaseInitializer.initialize(this)

        setContent {
            MaterialTheme {
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
```

---

## 1️⃣7️⃣ Drawable Resources (Flag Icons)

Tạo các file flag icon trong `res/drawable/`:

```
res/drawable/
├── flag_us.xml (hoặc .png)
├── flag_brazil.xml
├── flag_venezuela.xml
├── flag_russia.xml
├── flag_china.xml
├── flag_vietnam.xml
├── flag_thailand.xml
├── flag_indonesia.xml
├── flag_germany.xml
├── flag_portugal.xml
├── flag_korea.xml
├── flag_japan.xml
├── ic_app_logo.xml
├── onboarding_1.xml
└── onboarding_2.xml
```

**Ví dụ flag_us.xml (placeholder):**

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#3C3B6E"/>
    <size android:width="48dp" android:height="48dp"/>
</shape>
```

> **Note:** Thay thế bằng actual flag icons từ nguồn như [flagicons.lipis.dev](https://flagicons.lipis.dev/) hoặc tự tạo.

---

## 📌 Flow Summary

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         SplashActivity                                   │
│  (Activity launcher - chứa toàn bộ flow dưới đây)                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  App Launch                                                              │
│      │                                                                   │
│      ▼                                                                   │
│  ┌─────────────────┐                                                     │
│  │  SplashScreen   │                                                     │
│  │  (2s loading)   │                                                     │
│  │  + Load Native  │                                                     │
│  │    Ads          │                                                     │
│  └─────────────────┘                                                     │
│      │                                                                   │
│      ├── isOldUser = true ─────────────────────────────────────────┐    │
│      │                                                              │    │
│      ▼                                                              │    │
│  ┌─────────────────┐                                                │    │
│  │ Language1Screen │                                                │    │
│  │ (Select lang)   │                                                │    │
│  │ Hand pointer    │                                                │    │
│  │ on item #2      │                                                │    │
│  │ Native Ad (40%) │                                                │    │
│  └─────────────────┘                                                │    │
│      │                                                              │    │
│      │ Click any language                                           │    │
│      ▼                                                              │    │
│  ┌─────────────────┐                                                │    │
│  │ Language2Screen │                                                │    │
│  │ (Confirm lang)  │                                                │    │
│  │ Check selected  │                                                │    │
│  │ [Save] button   │                                                │    │
│  │ Native Ad (40%) │                                                │    │
│  └─────────────────┘                                                │    │
│      │                                                              │    │
│      │ Click Save (apply locale)                                    │    │
│      ▼                                                              │    │
│  ┌─────────────────┐                                                │    │
│  │OnBoarding1Screen│                                                │    │
│  │ [Next] button   │                                                │    │
│  │ Native Ad (40%) │                                                │    │
│  └─────────────────┘                                                │    │
│      │                                                              │    │
│      ▼                                                              │    │
│  ┌─────────────────┐                                                │    │
│  │OnBoarding2Screen│                                                │    │
│  │ [Start] button  │                                                │    │
│  │ Native Ad (40%) │                                                │    │
│  └─────────────────┘                                                │    │
│      │                                                              │    │
│      ▼                                                              │    │
│  ┌─────────────────┐                                                │    │
│  │PrepareDataScreen│                                                │    │
│  │ Loading 5s      │                                                │    │
│  │ Show Interstitial                                                │    │
│  │ Set isOldUser   │                                                │    │
│  │   = true        │                                                │    │
│  └─────────────────┘                                                │    │
│      │                                                              │    │
│      └──────────────────────────────────────────────────────────────┘    │
│                                    │                                     │
└────────────────────────────────────┼─────────────────────────────────────┘
                                     │
                                     ▼
                          ┌─────────────────┐
                          │  MainActivity   │
                          │  (App chính)    │
                          └─────────────────┘
```

**Giải thích:**
- `SplashActivity` là Activity launcher, được khai báo trong AndroidManifest với `MAIN` + `LAUNCHER`
- Toàn bộ flow (Splash → Language → Onboarding → PrepareData) chạy trong `SplashActivity`
- Khi flow hoàn thành (hoặc old user), `SplashActivity` gọi `startActivity(MainActivity)` và `finish()`
- `MainActivity` là Activity chính của app, không cần khai báo launcher

---

## ⚠️ TODO Checklist

1. **SplashScreen**:
   - [ ] Replace `R.drawable.ic_app_logo` với app icon thực

2. **Language Screens**:
   - [ ] Replace placeholder flag icons với actual flags
   - [ ] Integrate Native Ads từ ads module

3. **OnBoarding Screens**:
   - [ ] Replace `R.drawable.onboarding_1`, `onboarding_2` với images thực
   - [ ] Integrate Native Ads từ ads module

4. **PrepareDataScreen**:
   - [ ] Integrate Interstitial Ads từ ads module

5. **SplashActivity**:
   - [ ] Import và implement navigation đến MainActivity

6. **AndroidManifest.xml** (app module):
   ```xml
   <!-- SplashActivity - Launcher Activity (chứa toàn bộ flow) -->
   <activity
       android:name=".SplashActivity"
       android:exported="true"
       android:theme="@style/Theme.YourApp.Splash">
       <intent-filter>
           <action android:name="android.intent.action.MAIN" />
           <category android:name="android.intent.category.LAUNCHER" />
       </intent-filter>
   </activity>

   <!-- MainActivity - App chính (KHÔNG phải launcher) -->
   <activity
       android:name=".MainActivity"
       android:exported="false"
       android:theme="@style/Theme.YourApp">
   </activity>
   ```

---

## 📚 Dependencies Summary

| Library | Purpose |
|---------|---------|
| Firebase BOM | Version management |
| firebase-crashlytics | Crash reporting |
| firebase-analytics | Analytics |
| firebase-config | Remote Config |
| compose-bom | Compose version management |
| navigation-compose | Screen navigation |
| datastore-preferences | User preferences storage |
| room | Local database |
| coroutines | Async operations |
| lottie-compose | Animations (optional) |
