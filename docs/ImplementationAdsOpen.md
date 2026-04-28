# Hướng dẫn tích hợp OpenApp SDK (AdsOpen)

> Hướng dẫn chi tiết cách tích hợp OpenApp SDK vào dự án Android mới, bao gồm onboarding flow với quảng cáo AdMob.

---

## Mục lục

1. [Yêu cầu](#1-yêu-cầu)
2. [Thêm dependency](#2-thêm-dependency)
3. [Cấu hình AndroidManifest](#3-cấu-hình-androidmanifest)
4. [Tạo BuildConfig cho Ad IDs](#4-tạo-buildconfig-cho-ad-ids)
5. [Khởi tạo trong Application](#5-khởi-tạo-trong-application)
6. [Tạo SplashActivity](#6-tạo-splashactivity)
7. [Tạo MainActivity](#7-tạo-mainactivity)
8. [Cấu hình OpenAppConfig chi tiết](#8-cấu-hình-openappconfig-chi-tiết)
9. [Điều khiển Swipe trong Onboarding](#9-điều-khiển-swipe-trong-onboarding)
10. [Firebase Remote Config](#10-firebase-remote-config)
11. [User Flow](#11-user-flow)
12. [Ad Loading Strategy](#12-ad-loading-strategy)
13. [Test Ad IDs](#13-test-ad-ids)
14. [Troubleshooting](#14-troubleshooting)

---

## 1. Yêu cầu

| Yêu cầu | Giá trị |
|----------|---------|
| Min SDK | 24 |
| Target SDK | 35+ |
| Java | 11 |
| Kotlin | 2.0+ |
| Jetpack Compose | Required |
| Firebase | Remote Config |

---

## 2. Thêm dependency

### 2.1 settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

include(":app")
include(":openappsdk")  // hoặc dùng JitPack dependency
```

### 2.2 app/build.gradle.kts

**Cách 1: Local module**
```kotlin
dependencies {
    implementation(project(":openappsdk"))
    implementation("com.github.dungpro1572000:our_ads:1.2.5")
}
```

**Cách 2: JitPack (remote)**
```kotlin
dependencies {
    implementation("com.github.dungpro1572000:openappsdk:1.0.0")
    implementation("com.github.dungpro1572000:our_ads:1.2.5")
}
```

### 2.3 Plugins cần thiết

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services) // Firebase
}
```

### 2.4 Build features

```kotlin
android {
    buildFeatures {
        compose = true
        buildConfig = true  // Cần cho ad unit IDs
    }
}
```

---

## 3. Cấu hình AndroidManifest

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".AppApplication"
        ... >

        <!-- AdMob App ID (bắt buộc) -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />
        <!-- ↑ Test ID. Thay bằng ID thật khi release -->

        <!-- SplashActivity là launcher -->
        <activity
            android:name=".SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.App.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".MainActivity" />
    </application>
</manifest>
```

---

## 4. Tạo BuildConfig cho Ad IDs

Sử dụng product flavors để phân tách test/production ad IDs:

```kotlin
// app/build.gradle.kts
android {
    flavorDimensions += "version"

    // Test Ad IDs
    val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    val TEST_INTER_ID = "ca-app-pub-3940256099942544/1033173712"
    val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"

    productFlavors {
        create("appDev") {
            dimension = "version"
            buildConfigField("String", "ADS_SPL_BANNER", "\"$TEST_BANNER_ID\"")
            buildConfigField("String", "ADS_SPL_INTER", "\"$TEST_INTER_ID\"")
            buildConfigField("String", "ADS_ONB_001", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_ONB_002", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_PREPARE_NATIVE", "\"$TEST_NATIVE_ID\"")
        }
        create("appRelease") {
            dimension = "version"
            // TODO: Thay bằng ad unit IDs thật
            buildConfigField("String", "ADS_SPL_BANNER", "\"ca-app-pub-xxx/splbanner\"")
            buildConfigField("String", "ADS_SPL_INTER", "\"ca-app-pub-xxx/splinter\"")
            buildConfigField("String", "ADS_ONB_001", "\"ca-app-pub-xxx/onb001\"")
            buildConfigField("String", "ADS_ONB_002", "\"ca-app-pub-xxx/onb002\"")
            buildConfigField("String", "ADS_PREPARE_NATIVE", "\"ca-app-pub-xxx/preparenative\"")
        }
    }
}
```

---

## 5. Khởi tạo trong Application

Tạo `AppApplication.kt` — thứ tự khởi tạo rất quan trọng:

```kotlin
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Khởi tạo Ads SDK
        AdsInitializer.initialize(this)

        // 2. Khởi tạo Remote Config (load cached → fetch async)
        RemoteDataObject.init(this)

        // 3. Cấu hình OpenApp SDK
        OpenAppConfig.init {

            // Splash: banner + interstitial + timeout
            splashConfig {
                idBanner(BuildConfig.ADS_SPL_BANNER)
                idInter(BuildConfig.ADS_SPL_INTER)
                totalDelay(30000) // 30s timeout
            }

            // Language screen
            languageConfig {
                // backgroundColor(Color.White)
                // textColor(Color.Black)
                // onLanguageSelected { langCode -> Log.d("App", "Selected: $langCode") }
            }

            // Onboarding: 3 pages với native ads
            onboardingConfig {
                onb1NativeAdId(BuildConfig.ADS_ONB_001)
                onb2NativeAdId(BuildConfig.ADS_ONB_002)
                prepareNativeAdId(BuildConfig.ADS_PREPARE_NATIVE)
                showOnb1Ad(RemoteDataObject.showAdOnb1)
                showOnb2Ad(RemoteDataObject.showAdOnb2)
                showPrepareAd(RemoteDataObject.showAdPrepareNative)

                // Custom content (null = dùng default SDK)
                // onboardingContent1 { MyCustomPage1() }
                // onboardingContent2 { MyCustomPage2() }
                // onboardingContent3 { MyCustomPage3() }
            }

            // Prepare data screen
            prepareDataConfig {
                delayTime(5000) // 5s delay
                // content { MyCustomPrepareScreen() }
            }

            // Tùy chỉnh placeholder khi ad đang load
            adPlaceholderConfig {
                useShimmer(true)
                // shimmerBaseColor(Color(0xFFE0E0E0))
                // shimmerHighlightColor(Color(0xFFF5F5F5))
                // cornerRadius(12.dp)
            }
        }
    }
}
```

---

## 6. Tạo SplashActivity

`SplashActivity` là launcher — hiển thị toàn bộ onboarding flow:

```kotlin
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

`OpenAppNavigation` quản lý toàn bộ flow: Splash → Language → Onboarding (3 pages) → PrepareData → Main.

---

## 7. Tạo MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YourAppTheme {
                // Nội dung chính của app
                MainScreen()
            }
        }
    }
}
```

---

## 8. Cấu hình OpenAppConfig chi tiết

### SplashConfig

| Thuộc tính | Kiểu | Default | Mô tả |
|------------|------|---------|--------|
| `idBanner` | String | `""` | Banner ad ID cho splash screen |
| `idInter` | String | `""` | Interstitial ad ID |
| `totalDelay` | Int | `30000` | Timeout (ms) chờ load ads |
| `content` | `@Composable (() -> Unit)?` | `null` | Custom splash UI (null = dùng default) |

### LanguageConfig

| Thuộc tính | Kiểu | Default | Mô tả |
|------------|------|---------|--------|
| `backgroundColor` | Color | `Transparent` | Màu nền |
| `textColor` | Color | `Unspecified` | Màu chữ |
| `onLanguageSelected` | `((String) -> Unit)?` | `null` | Callback khi chọn ngôn ngữ |

### OnboardingConfig

| Thuộc tính | Kiểu | Default | Mô tả |
|------------|------|---------|--------|
| `onboardingContent1` | `@Composable (() -> Unit)?` | `null` | Custom UI page 1 |
| `onboardingContent2` | `@Composable (() -> Unit)?` | `null` | Custom UI page 2 |
| `onboardingContent3` | `@Composable (() -> Unit)?` | `null` | Custom UI page 3 |
| `onb1NativeAdId` | String | `""` | Native ad ID cho onboarding page 1 |
| `onb2NativeAdId` | String | `""` | Native ad ID cho onboarding page 3 |
| `prepareNativeAdId` | String | `""` | Native ad ID cho prepare data screen |
| `showOnb1Ad` | Boolean | `true` | Hiện/ẩn ad onboarding 1 |
| `showOnb2Ad` | Boolean | `true` | Hiện/ẩn ad onboarding 3 |
| `showPrepareAd` | Boolean | `true` | Hiện/ẩn ad prepare data |

### PrepareDataConfig

| Thuộc tính | Kiểu | Default | Mô tả |
|------------|------|---------|--------|
| `delayTime` | Int | `5000` | Delay (ms) trước khi chuyển sang main |
| `content` | `@Composable (() -> Unit)?` | `null` | Custom prepare UI |
| `onNextToMainScreen` | `(() -> Unit)?` | `null` | Callback khi chuyển sang main |

### AdPlaceholderConfig

| Thuộc tính | Kiểu | Default | Mô tả |
|------------|------|---------|--------|
| `backgroundColor` | Color | `#F5F5F5` | Màu nền placeholder |
| `shimmerBaseColor` | Color | `#E0E0E0` | Màu shimmer base |
| `shimmerHighlightColor` | Color | `#F5F5F5` | Màu shimmer highlight |
| `cornerRadius` | Dp | `12.dp` | Bo góc placeholder |
| `useShimmer` | Boolean | `true` | Bật/tắt shimmer effect |
| `showLoadingIndicator` | Boolean | `false` | Hiện loading indicator |

---

## 9. Điều khiển Swipe trong Onboarding

SDK cung cấp biến `OpenAppConfig.disableSwipe` để kiểm soát swipe gesture trong onboarding screens lúc runtime.

```kotlin
// Tắt swipe (user chỉ navigate bằng nút Next/Start)
OpenAppConfig.disableSwipe.value = true

// Bật lại swipe
OpenAppConfig.disableSwipe.value = false
```

**Đặc điểm:**
- Dùng `MutableState<Boolean>` — thay đổi có hiệu lực ngay lập tức (Compose recompose tự động)
- Default: `false` (cho phép swipe)
- Có thể set bất kỳ lúc nào, không cần config trong `init {}`
- SDK tự động set `disableSwipe = true` mỗi khi chuyển page trong onboarding (để đợi ads load)

**Ví dụ: Tắt swipe ngay từ đầu**
```kotlin
// Trong Application.onCreate(), sau OpenAppConfig.init {}
OpenAppConfig.disableSwipe.value = true
```

---

## 10. Firebase Remote Config

SDK sử dụng Firebase Remote Config để điều khiển hiển thị ads từ xa.

### 10.1 Setup Firebase

1. Thêm `google-services.json` vào thư mục `app/`
2. Thêm Firebase dependencies (đã có trong SDK)

### 10.2 Remote Config Keys

Tạo các key sau trên Firebase Console > Remote Config:

| Key | Type | Default | Mô tả |
|-----|------|---------|--------|
| `show_ad_spl_banner` | Boolean | `true` | Hiện banner splash |
| `show_ad_spl_inter` | Boolean | `true` | Hiện interstitial splash |
| `show_ad_onb1` | Boolean | `true` | Hiện native ad onboarding 1 |
| `show_ad_onb2` | Boolean | `true` | Hiện native ad onboarding 2 |
| `show_ad_prepare_native` | Boolean | `true` | Hiện native ad prepare |
| `ad_spl_banner_id` | String | `""` | Override banner ad ID |
| `ad_spl_inter_id` | String | `""` | Override interstitial ad ID |
| `ad_onb1_id` | String | `""` | Override onboarding 1 ad ID |
| `ad_onb2_id` | String | `""` | Override onboarding 2 ad ID |
| `ad_prepare_native_id` | String | `""` | Override prepare native ad ID |

### 10.3 Cách hoạt động

1. `RemoteDataObject.init(context)` — load cached values từ DataStore (blocking)
2. Fetch Remote Config async ở background
3. Khi fetch thành công → lưu vào DataStore cho lần launch sau
4. Lần launch đầu tiên sẽ dùng default values

---

## 11. User Flow

### New User (lần đầu mở app)

```
App Launch
    │
    ▼
SplashScreen (chờ ads load, max 30s timeout)
    │  ├── Load banner + interstitial
    │  ├── Preload native ads cho onboarding
    │
    ▼
LanguageScreen (chọn ngôn ngữ)
    │  ├── Show interstitial nếu chưa show ở splash
    │
    ▼
OnboardingScreen (3 pages — HorizontalPager)
    │  ├── Page 1: Welcome + native ad (onb1)
    │  ├── Page 2: Get Started
    │  ├── Page 3: You're All Set + native ad (onb2)
    │  ├── Show interstitial trước khi sang prepare
    │
    ▼
PrepareDataScreen (5s delay)
    │  ├── Show native ad (prepare)
    │  ├── Check/show interstitial nếu chưa show
    │
    ▼
MainActivity ✓ (set isOldUser = true)
```

### Old User (đã hoàn thành onboarding)

```
App Launch
    │
    ▼
SplashScreen (chờ ads, max 30s timeout)
    │
    ▼
MainActivity ✓ (skip toàn bộ onboarding)
```

---

## 12. Ad Loading Strategy

SDK sử dụng chiến lược **preload trước** — mỗi màn hình load ads cho màn tiếp theo:

| Màn hiện tại | Preload cho | Loại ads |
|-------------|-------------|----------|
| Splash | Language | Banner + Interstitial + Native (onb1) |
| Onboarding Page 1 | Onboarding Page 3 | Native (onb2) |
| Onboarding Page 2 | PrepareData | Native (prepare) |
| PrepareData | — | Interstitial (5s timeout) |

### Interstitial Logic

Interstitial (`spl_inter`) được load từ Splash và được check/show tại nhiều điểm:
1. **Language Screen** — sau khi chọn ngôn ngữ, check nếu chưa show thì show
2. **Onboarding Page 3** — trước khi sang PrepareData, check nếu chưa show thì show
3. **PrepareData** — lần cuối check, nếu có thì show, không thì bỏ qua

---

## 13. Test Ad IDs

Sử dụng các test ID của Google khi phát triển:

| Loại | Test Ad Unit ID |
|------|-----------------|
| Banner | `ca-app-pub-3940256099942544/9214589741` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |
| Native | `ca-app-pub-3940256099942544/2247696110` |
| Rewarded | `ca-app-pub-3940256099942544/5224354917` |
| App ID (test) | `ca-app-pub-3940256099942544~3347511713` |

> **Quan trọng:** Luôn dùng test IDs khi develop. Sử dụng ads thật trong quá trình phát triển có thể dẫn đến tài khoản AdMob bị suspend.

---

## 14. Troubleshooting

### Module `our_ads` không tìm thấy

`our_ads` là remote dependency. Đảm bảo JitPack repository đã được thêm:
```kotlin
// settings.gradle.kts
maven { url = uri("https://jitpack.io") }
```

### Ads không hiển thị

1. Kiểm tra `AdsInitializer.initialize(this)` đã gọi trước `OpenAppConfig.init {}`
2. Kiểm tra internet permission trong AndroidManifest
3. Kiểm tra AdMob App ID trong `<meta-data>`
4. Đảm bảo dùng test ad IDs khi develop
5. Kiểm tra Firebase Remote Config — `show_ad_*` keys phải là `true`

### Splash timeout quá lâu

Giảm `totalDelay` trong `splashConfig`:
```kotlin
splashConfig {
    totalDelay(15000) // 15s thay vì 30s
}
```

### Custom UI không hiển thị

Đảm bảo composable function không null:
```kotlin
onboardingConfig {
    onboardingContent1 {
        // Phải có content ở đây
        MyCustomPage()
    }
}
```

### Build lỗi với JitPack

Kiểm tra `openappsdk/jitpack.yml` yêu cầu OpenJDK 17:
```yaml
jdk:
  - openjdk17
```

---

## Tài liệu liên quan

- [Quick Start Guide](./QUICK_START.md) — Setup nhanh 5 phút
- [API Reference](./API_REFERENCE.md) — API chi tiết cho cả our_ads và openappsdk
- [Ad Loading Strategy](./AdsStragy.md) — Flow diagram chi tiết ad loading
- [Ads Module Setup](./ADS_MODULE_SETUP.md) — Kiến trúc ads module
- [Open App Module Setup](./OPEN_APP_MODULE_SETUP.md) — Chi tiết module setup
- [Our Ads Library](./instruction.md) — Hướng dẫn tích hợp our_ads library
