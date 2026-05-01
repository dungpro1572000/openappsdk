# Hướng Dẫn Tích Hợp OpenApp SDK

Hướng dẫn chi tiết để tích hợp module `openappsdk` vào dự án Android của bạn. SDK cung cấp flow onboarding hoàn chỉnh (Splash → Chọn ngôn ngữ → Onboarding → Chuẩn bị dữ liệu) với quảng cáo AdMob tích hợp sẵn.

---

## Mục Lục

1. [Yêu cầu](#1-yêu-cầu)
2. [Thêm dependency](#2-thêm-dependency)
3. [Cấu hình Gradle](#3-cấu-hình-gradle)
4. [Cấu hình AndroidManifest](#4-cấu-hình-androidmanifest)
5. [Thiết lập Firebase](#5-thiết-lập-firebase)
6. [Khởi tạo SDK](#6-khởi-tạo-sdk)
7. [Tạo SplashActivity](#7-tạo-splashactivity)
8. [Tạo MainActivity](#8-tạo-mainactivity)
9. [Cấu hình quảng cáo](#9-cấu-hình-quảng-cáo)
10. [Tuỳ chỉnh giao diện](#10-tuỳ-chỉnh-giao-diện)
11. [Firebase Remote Config](#11-firebase-remote-config)
12. [API tham khảo nhanh](#12-api-tham-khảo-nhanh)
13. [Xử lý sự cố](#13-xử-lý-sự-cố)

---

## 1. Yêu cầu

| Yêu cầu | Phiên bản |
|----------|-----------|
| Min SDK | 24 |
| Target SDK | 35+ |
| Kotlin | 2.0+ |
| Java | 11 |
| Jetpack Compose | BOM 2024.09.00+ |
| Firebase | Đã cấu hình Remote Config |

---

## 2. Thêm Dependency

### Cách 1: JitPack (khuyến nghị cho dự án bên ngoài)

**settings.gradle.kts:**

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**app/build.gradle.kts:**

```kotlin
dependencies {
    // OpenApp SDK
    implementation("com.github.dungpro1572000:openappsdk:1.1.2")

    // Our Ads (bắt buộc - SDK phụ thuộc vào thư viện này)
    implementation("com.github.dungpro1572000:our_ads:1.2.5")
}
```

### Cách 2: Module trực tiếp (cho phát triển local)

1. Copy thư mục `openappsdk/` vào root dự án của bạn
2. Thêm vào **settings.gradle.kts:**

```kotlin
include(":openappsdk")
```

3. Thêm dependency trong **app/build.gradle.kts:**

```kotlin
dependencies {
    implementation(project(":openappsdk"))
    implementation("com.github.dungpro1572000:our_ads:1.2.5")
}
```

---

## 3. Cấu hình Gradle

### Plugins cần thiết

**app/build.gradle.kts:**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization) // Bắt buộc cho navigation routes
    alias(libs.plugins.google.services)       // Bắt buộc cho Firebase
}
```

### Product Flavors (khuyến nghị)

Tách biệt ad ID test và production bằng product flavors:

```kotlin
android {
    flavorDimensions += "version"
    productFlavors {
        create("appDev") {
            dimension = "version"
            // Ad ID test của Google
            buildConfigField("String", "ADS_BANNER", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADS_INTER", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADS_NATIVE_ONB1", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_NATIVE_ONB2", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADS_NATIVE_PREPARE", "\"ca-app-pub-3940256099942544/2247696110\"")
        }
        create("appRelease") {
            dimension = "version"
            // Thay bằng ad ID thật của bạn
            buildConfigField("String", "ADS_BANNER", "\"YOUR_BANNER_AD_ID\"")
            buildConfigField("String", "ADS_INTER", "\"YOUR_INTERSTITIAL_AD_ID\"")
            buildConfigField("String", "ADS_NATIVE_ONB1", "\"YOUR_NATIVE_AD_ID_1\"")
            buildConfigField("String", "ADS_NATIVE_ONB2", "\"YOUR_NATIVE_AD_ID_2\"")
            buildConfigField("String", "ADS_NATIVE_PREPARE", "\"YOUR_NATIVE_AD_ID_3\"")
        }
    }
}
```

### Dependencies đầy đủ

```kotlin
dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)

    // SDK
    implementation("com.github.dungpro1572000:openappsdk:1.1.2")
    implementation("com.github.dungpro1572000:our_ads:1.2.5")
}
```

---

## 4. Cấu hình AndroidManifest

**AndroidManifest.xml:**

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Quyền Internet (bắt buộc cho quảng cáo) -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApp">

        <!-- Google AdMob App ID (bắt buộc) -->
        <!-- Dùng test ID khi phát triển, thay bằng ID thật khi release -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />

        <!-- SplashActivity là màn hình khởi động -->
        <activity
            android:name=".SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.MyApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- MainActivity - màn hình chính sau onboarding -->
        <activity
            android:name=".MainActivity"
            android:exported="false" />

    </application>
</manifest>
```

> **Lưu ý:** Thay `ca-app-pub-3940256099942544~3347511713` bằng AdMob App ID thật của bạn khi release.

---

## 5. Thiết lập Firebase

### 5.1 Thêm Firebase vào dự án

1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Thêm app Android với package name của bạn
4. Tải file `google-services.json` và đặt vào thư mục `app/`
5. Bật **Remote Config** trong Firebase Console

### 5.2 Tạo Remote Config parameters

Tạo các parameter sau trong Firebase Remote Config:

| Parameter | Type | Default | Mô tả |
|-----------|------|---------|-------|
| `show_ad_spl_banner` | Boolean | `true` | Hiện banner ad ở Splash |
| `show_ad_spl_inter` | Boolean | `true` | Hiện interstitial ở Splash |
| `show_ad_onb1` | Boolean | `true` | Hiện native ad ở Onboarding 1 |
| `show_ad_onb2` | Boolean | `true` | Hiện native ad ở Onboarding 2 |
| `show_ad_prepare_native` | Boolean | `true` | Hiện native ad ở PrepareData |
| `ad_spl_banner_id` | String | `""` | Override banner ad ID |
| `ad_spl_inter_id` | String | `""` | Override interstitial ad ID |
| `ad_onb1_id` | String | `""` | Override native ad ID onboarding 1 |
| `ad_onb2_id` | String | `""` | Override native ad ID onboarding 2 |
| `ad_prepare_native_id` | String | `""` | Override native ad ID prepare |

> Remote Config cho phép bật/tắt quảng cáo từ xa mà không cần update app.

---

## 6. Khởi tạo SDK

Tạo class `Application` và khởi tạo theo **đúng thứ tự**:

```kotlin
import android.app.Application
import com.dungz.openappsdk.OpenAppConfig
import com.dungz.openappsdk.remotedata.RemoteDataObject
import com.dungz.our_ads.AdsInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Bước 1: Khởi tạo Google Mobile Ads (BẮT BUỘC gọi đầu tiên)
        AdsInitializer.initialize(this)

        // Bước 2: Khởi tạo Remote Data (load cache + fetch Firebase Remote Config)
        RemoteDataObject.init(this)

        // Bước 3: Cấu hình OpenApp SDK
        OpenAppConfig.init {

            // Cấu hình màn Splash
            splashConfig {
                idBanner(BuildConfig.ADS_BANNER)
                idInter(BuildConfig.ADS_INTER)
                totalDelay(30000) // Thời gian chờ tối đa (ms)
            }

            // Cấu hình màn chọn ngôn ngữ
            languageConfig {
                // Tuỳ chỉnh nếu cần (xem phần Tuỳ chỉnh giao diện)
            }

            // Cấu hình màn Onboarding
            onboardingConfig {
                onb1NativeAdId(BuildConfig.ADS_NATIVE_ONB1)
                onb2NativeAdId(BuildConfig.ADS_NATIVE_ONB2)
                prepareNativeAdId(BuildConfig.ADS_NATIVE_PREPARE)
                showOnb1Ad(RemoteDataObject.showAdOnb1)
                showOnb2Ad(RemoteDataObject.showAdOnb2)
                showPrepareAd(RemoteDataObject.showAdPrepareNative)
            }

            // Cấu hình màn chuẩn bị dữ liệu
            prepareDataConfig {
                delayTime(5000) // Thời gian chờ trước khi vào Main (ms)
            }
        }
    }
}
```

> **Quan trọng:** Thứ tự khởi tạo phải là: `AdsInitializer` → `RemoteDataObject` → `OpenAppConfig`. Nếu sai thứ tự sẽ gây crash hoặc quảng cáo không load được.

---

## 7. Tạo SplashActivity

Đây là Activity khởi động, chứa toàn bộ flow onboarding:

```kotlin
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.dungz.openappsdk.navigation.OpenAppNavigation
import com.example.myapp.ui.theme.MyAppTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                Surface {
                    OpenAppNavigation(
                        onNavigateToMain = {
                            // Chuyển sang MainActivity khi hoàn thành onboarding
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}
```

`OpenAppNavigation` xử lý toàn bộ flow:
- **Người dùng mới:** Splash → Language → Onboarding → PrepareData → MainActivity
- **Người dùng cũ:** Splash → PrepareData → MainActivity (bỏ qua onboarding)

---

## 8. Tạo MainActivity

Màn hình chính của app, hiển thị sau khi hoàn thành onboarding:

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                Scaffold { paddingValues ->
                    // UI chính của app bạn ở đây
                    Text("Chào mừng đến app!", modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}
```

---

## 9. Cấu hình quảng cáo

### Luồng preload quảng cáo

SDK tự động preload quảng cáo cho màn hình tiếp theo:

```
Splash (chờ 5s)
  └─ Preload: quảng cáo cho Language
      │
Language
  ├─ Hiện: quảng cáo đã preload
  └─ Preload: quảng cáo cho Onboarding 1
      │
Onboarding 1
  ├─ Hiện: quảng cáo đã preload
  └─ Preload: quảng cáo cho Onboarding 2
      │
Onboarding 2
  ├─ Hiện: quảng cáo đã preload
  │
PrepareData (chờ 5s)
  └─ Load & hiện interstitial (timeout 5s, bỏ qua nếu chưa sẵn sàng)
      │
MainActivity ← App chính của bạn
```

### Ad ID test (dùng khi phát triển)

```
Banner:       ca-app-pub-3940256099942544/9214589741
Interstitial: ca-app-pub-3940256099942544/1033173712
Native:       ca-app-pub-3940256099942544/2247696110
App ID:       ca-app-pub-3940256099942544~3347511713
```

> **Cảnh báo:** Chỉ dùng ID test khi phát triển. Dùng ID test trên production sẽ không có doanh thu. Dùng ID thật trên debug build có thể bị Google cấm tài khoản.

---

## 10. Tuỳ Chỉnh Giao Diện

### 10.1 Tuỳ chỉnh màn Splash

```kotlin
splashConfig {
    idBanner("your_banner_id")
    idInter("your_interstitial_id")
    totalDelay(30000)
    content {
        // Composable tuỳ chỉnh cho Splash
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(R.drawable.logo), contentDescription = null)
            Text("My App", style = MaterialTheme.typography.headlineLarge)
        }
    }
}
```

### 10.2 Tuỳ chỉnh màn chọn ngôn ngữ

```kotlin
languageConfig {
    backgroundColor(Color.White)
    textColor(Color.Black)
    onLanguageSelected { languageCode ->
        // Callback khi người dùng chọn ngôn ngữ
        Log.d("Language", "Đã chọn: $languageCode")
    }
}
```

### 10.3 Tuỳ chỉnh Onboarding

```kotlin
onboardingConfig {
    // Nội dung tuỳ chỉnh cho từng trang
    onboardingContent1 {
        Column {
            Image(painter = painterResource(R.drawable.onb1), contentDescription = null)
            Text("Chào mừng đến với app!")
        }
    }
    onboardingContent2 {
        Column {
            Image(painter = painterResource(R.drawable.onb2), contentDescription = null)
            Text("Tính năng tuyệt vời")
        }
    }
    onboardingContent3 {
        Column {
            Image(painter = painterResource(R.drawable.onb3), contentDescription = null)
            Text("Bắt đầu ngay!")
        }
    }

    // Ad IDs
    onb1NativeAdId("your_native_ad_id")
    onb2NativeAdId("your_native_ad_id")
    prepareNativeAdId("your_native_ad_id")

    // Bật/tắt quảng cáo (thường lấy từ Remote Config)
    showOnb1Ad(true)
    showOnb2Ad(true)
    showPrepareAd(true)
}
```

### 10.4 Tuỳ chỉnh PrepareData

```kotlin
prepareDataConfig {
    delayTime(5000)
    content {
        // UI tuỳ chỉnh cho màn chuẩn bị
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text("Đang chuẩn bị dữ liệu...")
        }
    }
    onNextToMainScreen {
        // Callback trước khi chuyển sang MainActivity
        Log.d("Prepare", "Sẵn sàng chuyển màn hình")
    }
}
```

### 10.5 Tuỳ chỉnh Ad Placeholder

```kotlin
adPlaceholderConfig {
    backgroundColor(Color(0xFFF5F5F5))
    shimmerBaseColor(Color(0xFFE0E0E0))
    shimmerHighlightColor(Color(0xFFF5F5F5))
    loadingIndicatorColor(Color(0xFF6750A4))
    cornerRadius(12.dp)
    useShimmer(true)            // Hiệu ứng shimmer khi loading
    showLoadingIndicator(false) // Hiện vòng tròn loading

    // Hoặc dùng placeholder hoàn toàn tuỳ chỉnh
    nativeMediumPlaceholder {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Text("Đang tải quảng cáo...")
        }
    }
}
```

### 10.6 Điều khiển runtime

```kotlin
// Tắt/bật swipe trên màn onboarding (có hiệu lực ngay lập tức)
OpenAppConfig.disableSwipe.value = true   // Tắt swipe
OpenAppConfig.disableSwipe.value = false  // Bật swipe
```

---

## 11. Firebase Remote Config

### Cách hoạt động

1. Khi app khởi động, `RemoteDataObject.init(context)` được gọi
2. **Đồng bộ:** Load giá trị cache từ DataStore (dùng `runBlocking`)
3. **Bất đồng bộ:** Fetch giá trị mới từ Firebase Remote Config
4. Khi fetch thành công → lưu vào DataStore cho lần mở app tiếp theo

```
App khởi động
  │
  ├─ [Đồng bộ] Load cache từ DataStore
  │   → Có giá trị cũ → Dùng ngay (không chờ mạng)
  │
  └─ [Bất đồng bộ] Fetch từ Firebase
      ├─ Thành công → Cập nhật bộ nhớ + Lưu DataStore
      └─ Thất bại → Tiếp tục dùng cache
```

### Fetch interval

| Build | Interval |
|-------|----------|
| Debug | 0 giây (fetch mỗi lần mở) |
| Release | 12 giờ |

### Truy cập giá trị Remote Config

```kotlin
// Sau khi gọi RemoteDataObject.init(context), truy cập trực tiếp:
RemoteDataObject.showAdOnb1        // Boolean - hiện ad onboarding 1?
RemoteDataObject.showAdOnb2        // Boolean - hiện ad onboarding 2?
RemoteDataObject.showAdPrepareNative // Boolean - hiện ad prepare?
RemoteDataObject.adOnb1Id          // String - override ad ID
RemoteDataObject.adSplBannerId     // String - override banner ID
// ... và các field khác
```

---

## 12. API Tham Khảo Nhanh

### OpenAppConfig

```kotlin
// Khởi tạo
OpenAppConfig.init { /* builder */ }

// Lấy config
OpenAppConfig.getSplashConfig()
OpenAppConfig.getLanguageConfig()
OpenAppConfig.getOnboardingConfig()
OpenAppConfig.getPrepareDataConfig()
OpenAppConfig.getAdPlaceholderConfig()

// Runtime control
OpenAppConfig.disableSwipe.value = true/false
```

### OpenAppNavigation

```kotlin
@Composable
fun OpenAppNavigation(
    onNavigateToMain: () -> Unit  // Callback khi hoàn thành flow
)
```

### UserPreferences

```kotlin
val prefs = UserPreferences.getInstance(context)

// Kiểm tra người dùng cũ/mới
prefs.isOldUser()                          // suspend fun → Boolean
prefs.setIsOldUser(true)                   // suspend fun

// Ngôn ngữ đã chọn
prefs.getSelectedLanguage()                // suspend fun → String?
prefs.setSelectedLanguage("vi")            // suspend fun

// Trạng thái onboarding
prefs.onboardingCompletedFlow              // Flow<Boolean>
prefs.setOnboardingCompleted(true)         // suspend fun
```

### LanguageManager

```kotlin
// Áp dụng ngôn ngữ
LanguageManager.applyLanguage(context, "vi")              // → Context mới
LanguageManager.applyLanguageAndRecreate(activity, "vi")  // Áp dụng + recreate

// Lấy locale hiện tại
LanguageManager.getCurrentLocale(context)                  // → Locale
```

### Ngôn ngữ hỗ trợ

`en`, `pt_BR`, `es_VE`, `ru`, `zh`, `vi`, `th`, `id`, `de`, `pt`, `ko`, `ja`

---

## 13. Xử Lý Sự Cố

### App crash khi khởi động

**Nguyên nhân:** Sai thứ tự khởi tạo.

**Giải pháp:** Đảm bảo thứ tự: `AdsInitializer.initialize()` → `RemoteDataObject.init()` → `OpenAppConfig.init {}`

---

### Quảng cáo không hiển thị

**Kiểm tra:**
1. Đã thêm `INTERNET` permission trong AndroidManifest chưa?
2. Ad ID có đúng không? (dùng test ID khi debug)
3. AdMob App ID trong `<meta-data>` có đúng không?
4. Firebase Remote Config có tắt quảng cáo không? (kiểm tra `show_ad_*` flags)
5. Có kết nối mạng không?

---

### Navigation không hoạt động

**Nguyên nhân:** Thiếu plugin `kotlin.serialization`.

**Giải pháp:** Thêm vào `build.gradle.kts`:
```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}
```

---

### DataStore conflict

**Nguyên nhân:** Nếu app của bạn cũng dùng DataStore với tên `"user_preferences"` hoặc `"remote_data_preferences"`, sẽ xảy ra xung đột.

**Giải pháp:** Đảm bảo DataStore trong app bạn dùng tên khác.

---

### Build lỗi với JitPack

**Kiểm tra:**
1. Đã thêm `maven { url = uri("https://jitpack.io") }` vào repositories chưa?
2. Version SDK có đúng không? (hiện tại: `1.1.2`)
3. JitPack cần OpenJDK 17 để build

---

## Checklist Tích Hợp

- [ ] Thêm JitPack repository vào `settings.gradle.kts`
- [ ] Thêm dependency `openappsdk` và `our_ads`
- [ ] Thêm plugin `kotlin.serialization` và `google.services`
- [ ] Cấu hình Firebase và tải `google-services.json`
- [ ] Thêm AdMob App ID vào AndroidManifest
- [ ] Tạo class Application với đúng thứ tự khởi tạo
- [ ] Tạo SplashActivity với `OpenAppNavigation`
- [ ] Tạo MainActivity
- [ ] Cấu hình product flavors (test/release ad IDs)
- [ ] Tạo Remote Config parameters trên Firebase Console
- [ ] Test flow: người dùng mới (full onboarding)
- [ ] Test flow: người dùng cũ (bỏ qua onboarding)
- [ ] Thay test ad IDs bằng production IDs trước khi release
