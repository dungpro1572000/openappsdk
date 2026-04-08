# Hướng dẫn tích hợp Our Ads Library

## Mục lục

1. [Cài đặt ban đầu](#1-cài-đặt-ban-đầu)
2. [Khởi tạo SDK](#2-khởi-tạo-sdk)
3. [Remote Config](#3-remote-config)
4. [Banner Ad](#4-banner-ad)
5. [Native Ad - XML Layout](#5-native-ad---xml-layout)
6. [Native Ad - Compose thuần](#6-native-ad---compose-thuần)
7. [Native Ad Full Screen](#7-native-ad-full-screen)
8. [Interstitial Ad](#8-interstitial-ad)
9. [Rewarded Ad](#9-rewarded-ad)
10. [Preload Ads với Controller](#10-preload-ads-với-controller)

---

## 1. Cài đặt ban đầu

### 1.1 Thêm dependency

`settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

`app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.github.dungpro1572000:our_ads:1.1.0")
}
```

### 1.2 Yêu cầu SDK

```kotlin
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}
```

### 1.3 AndroidManifest.xml

Thêm AdMob App ID:
```xml
<manifest>
    <application>
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-xxxxxxxx~xxxxxxxx" />
    </application>
</manifest>
```

### 1.4 Firebase

- Thêm `google-services.json` vào thư mục `app/`
- Tạo key `enable_all_ads` trên Firebase Console > Remote Config, set `true` để bật ads

---

## 2. Khởi tạo SDK

Gọi trong `Application.onCreate()` hoặc `MainActivity.onCreate()`:

```kotlin
// Bước 1: GDPR Consent (bắt buộc cho EU)
val consentManager = GoogleMobileAdsConsentManager.getInstance(this)
consentManager.gatherConsent(this, { error ->
    if (error != null) Log.e("Ads", "Consent error: ${error.message}")

    if (consentManager.canRequestAds) {
        // Bước 2: Khởi tạo AdMob
        AdsInitializer.initialize(
            context = this,
            testDeviceIds = listOf("YOUR_TEST_DEVICE_ID"), // bỏ trống khi release
            onInitComplete = { Log.d("Ads", "AdMob initialized") }
        )
    }
})

// Bước 3: Sync Remote Config
lifecycleScope.launch {
    RemoteConfigData.syncData()
}
```

**Lưu ý:**
- `AdsInitializer.initialize()` tự động gọi `RemoteConfigData.init(context)` bên trong
- `AdsInitializer.isInitialized()` để kiểm tra trạng thái init

---

## 3. Remote Config

Library dùng key `enable_all_ads` để bật/tắt ads toàn cục:
- `true` = ads hoạt động
- `false` = tắt ads (default)

Thêm key tùy chỉnh bằng cách sửa `RemoteConfigData.localSyncRemoteConfigListKey`:
```kotlin
// Trong RemoteConfigData.kt
private val localSyncRemoteConfigListKey = listOf(
    enable_all_ads,
    DataKey("my_custom_key", "default_value"),  // String
    DataKey("show_banner", true),                // Boolean
    DataKey("max_retry", 3),                     // Int
)
```

Đọc giá trị:
```kotlin
val value = RemoteConfigData.get("enable_all_ads")  // suspend function
```

---

## 4. Banner Ad

Dùng `SmartBannerAd` composable — tự adaptive, quản lý lifecycle:

```kotlin
@Composable
fun MyScreen() {
    SmartBannerAd(adUnitId = "ca-app-pub-xxx/xxx")
}
```

**Hoạt động:**
- Tự check `enable_all_ads` từ Remote Config, nếu `false` thì không hiển thị
- Tự resume/pause/destroy theo lifecycle
- Dùng `AdSize.getLandscapeAnchoredAdaptiveBannerAdSize` (width 360dp)

---

## 5. Native Ad - XML Layout

### 5.1 Tạo XML layout

Root phải là `com.google.android.gms.ads.nativead.NativeAdView`. Các view ID **bắt buộc**:

| View ID | View Type | Mô tả |
|---|---|---|
| `@+id/ad_headline` | `TextView` | Tiêu đề ad |
| `@+id/ad_call_to_action` | `Button` | Nút CTA (Install, Download...) |
| `@+id/ad_media` | `MediaView` | Hình/video quảng cáo |
| `@+id/ad_body` | `TextView` | Mô tả ad |
| `@+id/ad_app_icon` | `ImageView` | Icon app |
| `@+id/ad_advertiser` | `TextView` | Tên nhà quảng cáo |
| `@+id/ad_stars` | `RatingBar` | Rating sao |

Library đã có sẵn 3 layout mẫu:
- `R.layout.native_ad_medium` — Layout medium có media view (180dp)
- `R.layout.native_ad_small` — Layout compact dạng horizontal (không media)
- `R.layout.native_ad_fullscreen_content` — Layout fullscreen (media fill space)

### 5.2 Ví dụ layout tùy chỉnh

Tạo file `res/layout/my_native_ad.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.gms.ads.nativead.NativeAdView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="12dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <ImageView
            android:id="@+id/ad_app_icon"
            android:layout_width="40dp"
            android:layout_height="40dp" />

        <TextView
            android:id="@+id/ad_headline"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/ad_advertiser"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <com.google.android.gms.ads.nativead.MediaView
            android:id="@+id/ad_media"
            android:layout_width="match_parent"
            android:layout_height="180dp" />

        <TextView
            android:id="@+id/ad_body"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />

        <RatingBar
            android:id="@+id/ad_stars"
            style="?android:attr/ratingBarStyleSmall"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:isIndicator="true"
            android:numStars="5" />

        <Button
            android:id="@+id/ad_call_to_action"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </LinearLayout>
</com.google.android.gms.ads.nativead.NativeAdView>
```

### 5.3 Hiển thị trong Compose

```kotlin
@Composable
fun MyScreen(nativeAdState: NativeAdState) {
    // Medium native ad với shimmer loading mặc định
    MediumNativeContainerAdView(
        nativeAdState = nativeAdState,
        nativeLayout = R.layout.my_native_ad
    )

    // Hoặc với custom shimmer
    MediumNativeContainerAdView(
        nativeAdState = nativeAdState,
        nativeLayout = R.layout.my_native_ad,
        shimmerAds = { MyCustomShimmer() }
    )
}
```

### 5.4 Load Native Ad

```kotlin
var nativeAdState by remember { mutableStateOf<NativeAdState>(NativeAdState.NotLoaded) }

// Load
scope.launch {
    nativeAdState = NativeAdState.Loading
    AppAdMob.loadSingleNativeAds(
        context = WeakReference(activity),
        id = "ca-app-pub-xxx/xxx",
        onAdClick = { /* ad clicked */ },
        onAdImpression = { /* ad impression */ },
        onLoadSuccess = { nativeAd ->
            nativeAdState = NativeAdState.Loaded(nativeAd)
        },
        onAdFailedToLoad = { error ->
            nativeAdState = NativeAdState.Failed(error)
        }
    )
}
```

---

## 6. Native Ad - Compose thuần

Dùng Compose wrapper để tự thiết kế layout không cần XML:

```kotlin
@Composable
fun MyNativeAd(nativeAd: NativeAd) {
    NativeAdView(nativeAd = nativeAd) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badge "Ad"
            NativeAdAttribution()

            Row {
                // Icon app
                NativeAdIconView(modifier = Modifier.size(40.dp)) {
                    nativeAd.icon?.drawable?.toBitmap()?.let {
                        Image(bitmap = it.asImageBitmap(), contentDescription = null)
                    }
                }

                Column {
                    NativeAdHeadlineView {
                        Text(text = nativeAd.headline ?: "")
                    }
                    NativeAdAdvertiserView {
                        Text(text = nativeAd.advertiser ?: "")
                    }
                }
            }

            // Media (hình/video) — truyền nativeAd vào
            NativeAdMediaView(nativeAd, modifier = Modifier.fillMaxWidth().height(200.dp))

            NativeAdBodyView {
                Text(text = nativeAd.body ?: "")
            }

            // Nút CTA — dùng NativeAdButton thay vì Button thường
            NativeAdCallToActionView {
                NativeAdButton(text = nativeAd.callToAction ?: "Install")
            }
        }
    }
}
```

**Compose wrapper có sẵn:**

| Composable | Mô tả |
|---|---|
| `NativeAdView` | Container chính, wrap toàn bộ ad |
| `NativeAdHeadlineView` | Tiêu đề |
| `NativeAdBodyView` | Mô tả |
| `NativeAdMediaView` | Media (hình/video), cần truyền `nativeAd` |
| `NativeAdIconView` | Icon app |
| `NativeAdCallToActionView` | Container cho nút CTA |
| `NativeAdAdvertiserView` | Tên nhà quảng cáo |
| `NativeAdStarRatingView` | Rating sao |
| `NativeAdPriceView` | Giá |
| `NativeAdStoreView` | Tên store |
| `NativeAdChoicesView` | AdChoices icon |
| `NativeAdAttribution` | Badge "Ad" |
| `NativeAdButton` | Nút CTA (không override click handler của ad) |

**Quan trọng:** Dùng `NativeAdButton` thay vì `Button` thường trong `NativeAdCallToActionView`, vì `Button` sẽ override click handler của native ad.

---

## 7. Native Ad Full Screen

### 7.1 Compose thuần

```kotlin
var showFullScreen by remember { mutableStateOf(false) }

if (showFullScreen && nativeAdState is NativeAdState.Loaded) {
    NativeFullScreenAdView(
        nativeAd = (nativeAdState as NativeAdState.Loaded).nativeAd,
        onCloseClick = { showFullScreen = false }
    )
}
```

### 7.2 XML layout

```kotlin
var showFullScreen by remember { mutableStateOf(false) }

if (showFullScreen) {
    FullScreenNativeContainerAdView(
        nativeAdState = nativeAdState,
        nativeLayout = R.layout.native_ad_fullscreen_content, // hoặc layout tùy chỉnh
        onCloseClick = { showFullScreen = false }
    )
}
```

**Lưu ý cho XML fullscreen:**
- Layout fullscreen wrapper (`native_ad_fullscreen.xml`) có `FrameLayout` root chứa `btn_close` (ImageButton) và `native_ad_view` (NativeAdView)
- Layout content (`native_ad_fullscreen_content.xml`) chỉ chứa NativeAdView, không có close button
- `FullScreenNativeContainerAdView` dùng `Dialog` với `usePlatformDefaultWidth = false`

---

## 8. Interstitial Ad

### 8.1 Load trực tiếp qua AppAdMob

```kotlin
var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

// Load
scope.launch {
    AppAdMob.loadInterstitialAds(
        context = WeakReference(activity),
        id = "ca-app-pub-xxx/xxx",
        onLoadSuccess = { ad -> interstitialAd = ad },
        onAdFailedToLoad = { error -> Log.e("Ads", error.message) }
    )
}

// Show (khi đã load xong)
interstitialAd?.let { ad ->
    AppAdMob.showInterstitialAd(
        activity = activity,
        interstitialAd = ad,
        onAdDismissed = { interstitialAd = null },
        onAdFailedToShow = { error -> Log.e("Ads", error.message) }
    )
}
```

### 8.2 Dùng Controller (preload)

```kotlin
// Preload sớm (VD: khi mở app)
lifecycleScope.launch {
    InterAdsController.preloadAds(
        activity = WeakReference(this@MainActivity),
        adUnitId = "ca-app-pub-xxx/xxx",
        preloadKey = "inter_main"
    )
}

// Kiểm tra và show
val state = InterAdsController.listAds["inter_main"]
if (state is InterAdState.Loaded) {
    AppAdMob.showInterstitialAd(
        activity = this,
        interstitialAd = state.interstitialAd,
        onAdDismissed = { /* reset state nếu cần */ },
        onAdFailedToShow = { error -> /* xử lý */ }
    )
}
```

---

## 9. Rewarded Ad

### 9.1 Load trực tiếp

```kotlin
var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

// Load
scope.launch {
    AppAdMob.loadRewardAds(
        context = WeakReference(activity),
        id = "ca-app-pub-xxx/xxx",
        onLoadSuccess = { ad -> rewardedAd = ad },
        onAdFailedToLoad = { error -> Log.e("Ads", error.message) }
    )
}

// Show
rewardedAd?.let { ad ->
    AppAdMob.showRewardAds(
        activity = activity,
        rewardedAd = ad,
        onUserEarnedReward = { reward ->
            Log.d("Ads", "Reward: ${reward.type}, amount: ${reward.amount}")
        },
        onAdDismissed = { rewardedAd = null },
        onAdFailedToShow = { error -> Log.e("Ads", error.message) }
    )
}
```

### 9.2 Dùng Controller (preload)

```kotlin
// Preload
lifecycleScope.launch {
    RewardAdsController.preloadAds(
        activity = WeakReference(this@MainActivity),
        adUnitId = "ca-app-pub-xxx/xxx",
        preloadKey = "reward_main"
    )
}

// Kiểm tra và show
val state = RewardAdsController.listAds["reward_main"]
if (state is RewardAdState.Loaded) {
    AppAdMob.showRewardAds(
        activity = this,
        rewardedAd = state.rewardedAd,
        onUserEarnedReward = { reward -> /* xử lý reward */ },
        onAdDismissed = { /* reset */ },
        onAdFailedToShow = { error -> /* xử lý */ }
    )
}
```

---

## 10. Preload Ads với Controller

Mỗi loại ad có controller singleton để preload và cache theo key:

| Controller | Ad Type | State Class | Loaded Property |
|---|---|---|---|
| `NativeAdsController` | Native | `NativeAdState` | `.nativeAd` |
| `InterAdsController` | Interstitial | `InterAdState` | `.interstitialAd` |
| `RewardAdsController` | Rewarded | `RewardAdState` | `.rewardedAd` |

### Pattern chung

```kotlin
// 1. Preload (gọi sớm — khi mở app, trước khi cần hiển thị)
XxxController.preloadAds(
    activity = WeakReference(activity),
    adUnitId = "ca-app-pub-xxx/xxx",
    preloadKey = "unique_key"
)

// 2. Kiểm tra state
val state = XxxController.listAds["unique_key"]

// 3. Xử lý theo state
when (state) {
    is XxxAdState.Loading -> { /* show shimmer / loading */ }
    is XxxAdState.Loaded -> { /* sẵn sàng hiển thị */ }
    is XxxAdState.Failed -> { /* thất bại: state.e */ }
    else -> { /* chưa load (NotLoaded hoặc null) */ }
}
```

### State flow

```
null → NotLoaded → Loading → Loaded ✓
                          → Failed ✗
```

### Test Ad IDs (Google)

| Loại | ID |
|---|---|
| Banner | `ca-app-pub-3940256099942544/6300978111` |
| Interstitial | `ca-app-pub-3940256099942544/1033173712` |
| Rewarded | `ca-app-pub-3940256099942544/5224354917` |
| Native | `ca-app-pub-3940256099942544/2247696110` |
