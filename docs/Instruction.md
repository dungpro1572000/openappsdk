# OpenApp SDK & Our Ads SDK - Installation Guide

> **Version:** 1.0.0 | **Last Updated:** February 2025 | **Min SDK:** 24 | **Target SDK:** 36

This guide will help you integrate the **OpenApp SDK** (onboarding flow) and **Our Ads SDK** (Google Mobile Ads wrapper) into your Android project.

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Installation](#installation)
   - [Step 1: Add Module Dependencies](#step-1-add-module-dependencies)
   - [Step 2: Configure Gradle](#step-2-configure-gradle)
   - [Step 3: Configure AndroidManifest](#step-3-configure-androidmanifest)
4. [Quick Start](#quick-start)
5. [SDK Architecture](#sdk-architecture)
6. [Detailed Documentation](#detailed-documentation)
7. [Troubleshooting](#troubleshooting)

---

## Overview

| SDK | Description |
|-----|-------------|
| **our_ads** | Complete wrapper around Google Mobile Ads SDK with support for Native, Interstitial, Rewarded, and Banner ads |
| **openappsdk** | Complete onboarding flow with Splash, Language Selection, Onboarding screens, and ad integration |

### Features

**Our Ads SDK:**
- Multiple ads instance support with Map storage
- Sequential loading strategy (High priority -> Normal fallback)
- Auto cleanup after ad display
- Native ad retry logic with configurable attempts
- Jetpack Compose UI components

**OpenApp SDK:**
- Complete onboarding flow (Splash -> Language -> Onboarding -> Prepare Data)
- DataStore-based user preferences
- 12 language support with flag icons
- Custom navigation system
- Ad integration ready
- Theming support (Light/Dark/Dynamic)

---

## Prerequisites

Before installing the SDKs, ensure you have:

- Android Studio Arctic Fox or later
- Kotlin 1.9.0+
- Gradle 8.0+
- Min SDK 24 (Android 7.0)
- Target SDK 36
- Google Play Services Ads dependency

---

## Installation

### Step 1: Add Module Dependencies

#### Option A: Copy Modules Directly

1. Copy the `our_ads` and `openappsdk` folders to your project root directory.

2. Add the modules to your `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "YourAppName"
include(":app")
include(":our_ads")
include(":openappsdk")
```

#### Option B: Using Git Submodules (Recommended for version control)

```bash
# Add as git submodules
git submodule add <repository-url> our_ads
git submodule add <repository-url> openappsdk

# Update settings.gradle.kts as shown above
```

---

### Step 2: Configure Gradle

#### 2.1 Project-level build.gradle.kts

```kotlin
// build.gradle.kts (Project level)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

#### 2.2 Version Catalog (gradle/libs.versions.toml)

Add required dependencies to your version catalog:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
playServicesAds = "23.6.0"
datastorePreferences = "1.1.1"
kotlinxSerializationJson = "1.7.3"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerializationJson" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

#### 2.3 App-level build.gradle.kts

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yourpackage.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yourpackage.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    // Product flavors for different ad configurations
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"

            // Test Ad Unit IDs
            buildConfigField("String", "ADS_LANG_001_HIGH", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_LANG_001_NORMAL", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_LANG_002_HIGH", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_LANG_002_NORMAL", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_ONB_001_HIGH", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_ONB_001_NORMAL", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_ONB_002_HIGH", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_ONB_002_NORMAL", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_INTER_001_HIGH", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADS_INTER_001_NORMAL", "\"ca-app-pub-3940256099942544/1033173712\"")
        }

        create("release") {
            dimension = "environment"

            // Production Ad Unit IDs - Replace with your actual IDs
            buildConfigField("String", "ADS_LANG_001_HIGH", "\"YOUR_PRODUCTION_AD_ID\"")
            buildConfigField("String", "ADS_LANG_001_NORMAL", "\"YOUR_PRODUCTION_AD_ID\"")
            // ... add all production ad IDs
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // SDK Modules
    implementation(project(":our_ads"))
    implementation(project(":openappsdk"))

    // Core dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
}
```

---

### Step 3: Configure AndroidManifest

#### 3.1 App AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Required for ads -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".AppApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.YourApp">

        <!-- Google Mobile Ads App ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX" />

        <!-- SplashActivity - Launcher Activity -->
        <activity
            android:name=".SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.YourApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- MainActivity - Main app screen -->
        <activity
            android:name=".MainActivity"
            android:exported="false"
            android:theme="@style/Theme.YourApp" />

    </application>
</manifest>
```

---

## Quick Start

### 1. Create Application Class

```kotlin
// AppApplication.kt
package com.yourpackage.app

import android.app.Application
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.our_ads.AdsInitializer
import com.dungz.our_ads.manager.InterstitialAdManager
import com.dungz.our_ads.manager.NativeAdManager

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Google Mobile Ads SDK
        AdsInitializer.initialize(
            context = this,
            testDeviceIds = listOf("YOUR_TEST_DEVICE_ID"), // Remove in production
            onInitComplete = {
                // SDK ready
            }
        )

        // 2. Initialize Ad Managers
        NativeAdManager.init(this)
        InterstitialAdManager.init(this)

        // 3. Configure OpenApp SDK
        OpenAppConfig.init {
            // Splash configuration
            splashConfig {
                delayTime(5000) // 5 seconds
            }

            // Language 1 Screen - Native Ad
            language1Config {
                adIdHigh(BuildConfig.ADS_LANG_001_HIGH)
                adIdNormal(BuildConfig.ADS_LANG_001_NORMAL)
                showAdHigh(true)
                showAdNormal(true)
            }

            // Language 2 Screen - Native Ad
            language2Config {
                adIdHigh(BuildConfig.ADS_LANG_002_HIGH)
                adIdNormal(BuildConfig.ADS_LANG_002_NORMAL)
                showAdHigh(true)
                showAdNormal(true)
            }

            // Onboarding 1 Screen - Native Ad
            onboarding1Config {
                adIdHigh(BuildConfig.ADS_ONB_001_HIGH)
                adIdNormal(BuildConfig.ADS_ONB_001_NORMAL)
                showAdHigh(true)
                showAdNormal(true)
            }

            // Onboarding 2 Screen - Native Ad
            onboarding2Config {
                adIdHigh(BuildConfig.ADS_ONB_002_HIGH)
                adIdNormal(BuildConfig.ADS_ONB_002_NORMAL)
                showAdHigh(true)
                showAdNormal(true)
            }

            // Prepare Data Screen - Interstitial Ad
            prepareDataConfig {
                delayTime(5000) // 5 second timeout
                adIdHigh(BuildConfig.ADS_INTER_001_HIGH)
                adIdNormal(BuildConfig.ADS_INTER_001_NORMAL)
                showAdHigh(true)
                showAdNormal(true)
            }
        }
    }
}
```

### 2. Create SplashActivity

```kotlin
// SplashActivity.kt
package com.yourpackage.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dungz.openappsdk.navigation.OpenAppNavigation
import com.yourpackage.app.ui.theme.YourAppTheme

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YourAppTheme {
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

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
```

### 3. Create MainActivity

```kotlin
// MainActivity.kt
package com.yourpackage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yourpackage.app.ui.theme.YourAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YourAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Welcome to Your App!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}
```

---

## SDK Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         YOUR APP                                  │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    SplashActivity                            │ │
│  │  (Entry point - contains full onboarding flow)               │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    openappsdk module                         │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │ OpenAppNavigation                                       │ │ │
│  │  │   ├── SplashScreen (preload ads, check user)            │ │ │
│  │  │   ├── Language1Screen (select language + native ad)     │ │ │
│  │  │   ├── Language2Screen (confirm language + native ad)    │ │ │
│  │  │   ├── OnBoarding1Screen (intro + native ad)             │ │ │
│  │  │   ├── OnBoarding2Screen (intro + native ad)             │ │ │
│  │  │   └── PrepareDataScreen (loading + interstitial)        │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │ OpenAppConfig (configuration)                           │ │ │
│  │  │ UserPreferences (DataStore)                             │ │ │
│  │  │ LanguageManager (locale handling)                       │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                     our_ads module                           │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │ Ad Managers (Singletons)                                │ │ │
│  │  │   ├── NativeAdManager (with retry logic)                │ │ │
│  │  │   ├── InterstitialAdManager                             │ │ │
│  │  │   ├── RewardedAdManager                                 │ │ │
│  │  │   └── BannerAdManager                                   │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  │  ┌─────────────────────────────────────────────────────────┐ │ │
│  │  │ Composables                                             │ │ │
│  │  │   ├── NativeAdView                                      │ │ │
│  │  │   └── BannerAdView                                      │ │ │
│  │  └─────────────────────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    MainActivity                              │ │
│  │  (Your main app content)                                     │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

---

## Detailed Documentation

For detailed information about each SDK:

| Document | Description |
|----------|-------------|
| [ADS_MODULE_SETUP.md](./ADS_MODULE_SETUP.md) | Complete guide for Our Ads SDK - all ad types, managers, composables |
| [OPEN_APP_MODULE_SETUP.md](./OPEN_APP_MODULE_SETUP.md) | Complete guide for OpenApp SDK - screens, navigation, configuration |
| [QUICK_START.md](./QUICK_START.md) | Quick start guide with minimal setup |
| [API_REFERENCE.md](./API_REFERENCE.md) | API reference for all public classes and methods |
| [AdsStragy.md](./AdsStragy.md) | Ad loading strategy documentation |

---

## Troubleshooting

### Common Issues

#### 1. Ads not showing

```kotlin
// Ensure you've initialized the SDK in Application.onCreate()
AdsInitializer.initialize(context = this)
NativeAdManager.init(this)
InterstitialAdManager.init(this)
```

#### 2. Module not found error

Make sure modules are added to `settings.gradle.kts`:
```kotlin
include(":our_ads")
include(":openappsdk")
```

#### 3. Missing Google Mobile Ads App ID

Add to AndroidManifest.xml:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXX~XXXXXXXXXX" />
```

#### 4. Test ads not appearing

Use test device IDs:
```kotlin
AdsInitializer.initialize(
    context = this,
    testDeviceIds = listOf("YOUR_DEVICE_ID")
)
```

Get your device ID from Logcat when loading ads.

#### 5. DataStore conflicts

If you have multiple DataStore instances, ensure unique names:
```kotlin
// In UserPreferences.kt
private val Context.dataStore by preferencesDataStore(name = "openapp_user_preferences")
```

---

## License

This SDK is proprietary software. All rights reserved.

---

## Support

For support and questions:
- Open an issue on the repository
- Contact: your-email@example.com
