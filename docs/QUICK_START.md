# Quick Start Guide

Get the OpenApp SDK and Our Ads SDK running in under 5 minutes.

---

## 1. Add Modules

Copy `our_ads` and `openappsdk` folders to your project, then update:

```kotlin
// settings.gradle.kts
include(":app")
include(":our_ads")
include(":openappsdk")
```

---

## 2. Add Dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":our_ads"))
    implementation(project(":openappsdk"))
}
```

---

## 3. Configure AndroidManifest

```xml
<!-- AndroidManifest.xml -->
<application>
    <!-- Add Google Ads App ID -->
    <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-3940256099942544~3347511713" />

    <!-- Set SplashActivity as launcher -->
    <activity
        android:name=".SplashActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

---

## 4. Initialize in Application

```kotlin
// AppApplication.kt
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Ads
        AdsInitializer.initialize(this)
        NativeAdManager.init(this)
        InterstitialAdManager.init(this)

        // Configure OpenApp
        OpenAppConfig.init {
            language1Config {
                adIdHigh("ca-app-pub-3940256099942544/2247696110")
                adIdNormal("ca-app-pub-3940256099942544/2247696110")
                showAdHigh(true)
                showAdNormal(true)
            }
            prepareDataConfig {
                adIdHigh("ca-app-pub-3940256099942544/1033173712")
                adIdNormal("ca-app-pub-3940256099942544/1033173712")
                showAdHigh(true)
                showAdNormal(true)
            }
        }
    }
}
```

---

## 5. Create SplashActivity

```kotlin
// SplashActivity.kt
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenAppNavigation(
                onNavigateToMain = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }
}
```

---

## 6. Run the App

```bash
./gradlew :app:assembleDevDebug
```

---

## Test Ad Unit IDs

| Ad Type | Test ID |
|---------|---------|
| Native | `ca-app-pub-3940256099942544/2247696110` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |
| Banner | `ca-app-pub-3940256099942544/6300978111` |
| Rewarded | `ca-app-pub-3940256099942544/5224354917` |

---

## User Flow

```
App Launch
    │
    ▼
SplashScreen (5s) ─── isOldUser=true ───► MainActivity
    │
    │ isOldUser=false
    ▼
Language1Screen (select language)
    │
    ▼
Language2Screen (confirm + save)
    │
    ▼
OnBoarding1Screen
    │
    ▼
OnBoarding2Screen
    │
    ▼
PrepareDataScreen (5s + interstitial)
    │
    ▼
MainActivity (set isOldUser=true)
```

---

## Next Steps

- [Full Installation Guide](./Instruction.md)
- [Ads Module Setup](./ADS_MODULE_SETUP.md)
- [OpenApp Module Setup](./OPEN_APP_MODULE_SETUP.md)
- [API Reference](./API_REFERENCE.md)
