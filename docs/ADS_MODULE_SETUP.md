# 📱 Ads Module/Library Setup Guide

> **Version:** 3.0.0 | **Last Updated:** January 2025 | **Min SDK:** 21 | **Target SDK:** 34

---

## 📋 Checklist

### Setup Phase
- [ ] Tạo module mới `ads` trong project
- [ ] Thêm Google Mobile Ads dependency
- [ ] Cập nhật AndroidManifest.xml
- [ ] Tạo AdsInitializer.kt

### Ads Manager Phase
- [ ] Tạo AdState.kt
- [ ] Tạo InterstitialAdManager.kt (singleton, lưu nhiều ads trong Map)
- [ ] Tạo RewardedAdManager.kt (singleton, lưu nhiều ads trong Map)
- [ ] Tạo BannerAdManager.kt
- [ ] Tạo NativeAdManager.kt (có retry logic)

### Composables Phase
- [ ] Tạo BannerAdView.kt với Preview
- [ ] Tạo NativeAdView.kt với Preview
- [ ] Tạo Native Ad XML layouts

### Testing Phase
- [ ] Test load tuần tự (high fail → load normal)
- [ ] Test multiple ads instances
- [ ] Test show và auto-remove từ Map
- [ ] Test native ad retry logic

---

## 🔑 Key Features

- **Multiple Ads Support**: Lưu nhiều ads trong Map, phân biệt bằng key `"$adHigherId|$adNormalId"`
- **Sequential Loading**: Load high trước, nếu fail mới load normal
- **Auto Cleanup**: Sau khi show, tự động xóa ad khỏi Map
- **Native Ad Retry**: Hỗ trợ retry khi fail với `maxRetryCount` và auto-reload theo `reloadDuration`
- **Direct State Access**: Kiểm tra trạng thái trực tiếp qua `isReady()` và `getState()` thay vì observe StateFlow

---

## 1️⃣ Module Structure

```
ads/
├── src/main/
│   ├── kotlin/com/yourpackage/ads/
│   │   ├── AdsInitializer.kt
│   │   ├── state/AdState.kt
│   │   ├── manager/
│   │   │   ├── InterstitialAdManager.kt  // Singleton với Map
│   │   │   ├── RewardedAdManager.kt      // Singleton với Map
│   │   │   ├── BannerAdManager.kt
│   │   │   └── NativeAdManager.kt        // Có retry logic
│   │   └── ui/
│   │       ├── BannerAdView.kt
│   │       └── NativeAdView.kt
│   ├── res/layout/
│   │   ├── native_ad_small.xml
│   │   └── native_ad_medium.xml
│   └── AndroidManifest.xml
└── build.gradle.kts
```

---

## 2️⃣ build.gradle.kts

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yourpackage.ads"
    compileSdk = 34
    defaultConfig { minSdk = 21; consumerProguardFiles("consumer-rules.pro") }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## 3️⃣ AdsInitializer.kt

```kotlin
package com.yourpackage.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

object AdsInitializer {
    private var isInitialized = false

    fun initialize(
        context: Context,
        testDeviceIds: List<String> = emptyList(),
        onInitComplete: () -> Unit = {}
    ) {
        if (isInitialized) { onInitComplete(); return }
        if (testDeviceIds.isNotEmpty()) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            )
        }
        MobileAds.initialize(context) { isInitialized = true; onInitComplete() }
    }

    fun isInitialized(): Boolean = isInitialized
}
```

---

## 4️⃣ AdState.kt

```kotlin
package com.yourpackage.ads.state

/**
 * Trạng thái của một Ad
 */
sealed interface AdState {
    data object NotLoaded : AdState
    data object Loading : AdState
    data object Loaded : AdState
    data object Showing : AdState
    data class Failed(val message: String, val retryCount: Int = 0) : AdState
}

/**
 * Wrapper chứa ad và metadata
 */
data class AdHolder<T>(
    val ad: T?,
    val state: AdState = AdState.NotLoaded,
    val isHigherAd: Boolean = false, // true nếu đây là higher ad
    val loadedAt: Long = 0L
)

/**
 * Config cho việc retry load ad
 */
data class RetryConfig(
    val maxRetryCount: Int = 3,           // Số lần retry tối đa khi fail
    val reloadDuration: Long = 0L,        // Thời gian auto reload (ms), 0 = không auto reload
    val reloadTriggerCount: Int = 0       // Số lần trigger reload, 0 = không giới hạn
)

/**
 * Callback cho Rewarded Ads
 */
data class RewardItem(val type: String, val amount: Int)

/**
 * Tạo key unique cho mỗi cặp ad IDs
 */
fun createAdKey(adHigherId: String, adNormalId: String): String = "$adHigherId|$adNormalId"
```

---

## 5️⃣ InterstitialAdManager.kt

```kotlin
package com.yourpackage.ads.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yourpackage.ads.state.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton Manager cho Interstitial Ads
 * - Hỗ trợ nhiều ads, lưu trong Map với key = "$adHigherId|$adNormalId"
 * - Load tuần tự: high trước, fail thì load normal
 * - Show xong tự động xóa khỏi Map
 * - Kiểm tra trạng thái trực tiếp qua adsMap[key]?.state
 */
object InterstitialAdManager {

    private lateinit var appContext: Context

    // Map lưu trữ ads: key -> AdHolder
    private val adsMap = ConcurrentHashMap<String, AdHolder<InterstitialAd>>()

    /**
     * Khởi tạo với Application Context
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Kiểm tra ad đã ready chưa
     */
    fun isReady(adHigherId: String, adNormalId: String): Boolean {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state == AdState.Loaded
    }

    /**
     * Lấy trạng thái của ad
     */
    fun getState(adHigherId: String, adNormalId: String): AdState {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state ?: AdState.NotLoaded
    }

    /**
     * Load Interstitial Ad - Tuần tự: High trước, fail thì Normal
     *
     * @param adHigherId Ad Unit ID với priority cao
     * @param adNormalId Ad Unit ID bình thường
     * @param showHigher Có cho phép load/show higher ad không
     * @param showNormal Có cho phép load/show normal ad không
     * @param onLoaded Callback khi load thành công
     * @param onFailed Callback khi cả 2 đều fail
     */
    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        if (!showHigher && !showNormal) {
            onFailed("Both ads disabled")
            return
        }

        val key = createAdKey(adHigherId, adNormalId)

        // Đã ready thì không load lại
        if (adsMap[key]?.state == AdState.Loaded) {
            onLoaded()
            return
        }

        // Đang loading thì không load lại
        if (adsMap[key]?.state == AdState.Loading) {
            return
        }

        adsMap[key] = AdHolder(ad = null, state = AdState.Loading)

        // Load tuần tự: Higher trước
        if (showHigher) {
            loadSingleAd(adHigherId, isHigher = true) { success ->
                if (success) {
                    adsMap[key] = AdHolder(
                        ad = adsMap[key]?.ad,
                        state = AdState.Loaded,
                        isHigherAd = true,
                        loadedAt = System.currentTimeMillis()
                    )
                    onLoaded()
                } else if (showNormal) {
                    // Higher fail, load Normal
                    loadSingleAd(adNormalId, isHigher = false) { normalSuccess ->
                        if (normalSuccess) {
                            adsMap[key] = AdHolder(
                                ad = adsMap[key]?.ad,
                                state = AdState.Loaded,
                                isHigherAd = false,
                                loadedAt = System.currentTimeMillis()
                            )
                            onLoaded()
                        } else {
                            adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Both ads failed"))
                            onFailed("Both ads failed to load")
                        }
                    }
                } else {
                    adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Higher ad failed, normal disabled"))
                    onFailed("Higher ad failed, normal disabled")
                }
            }
        } else if (showNormal) {
            // Chỉ load Normal
            loadSingleAd(adNormalId, isHigher = false) { success ->
                if (success) {
                    adsMap[key] = AdHolder(
                        ad = adsMap[key]?.ad,
                        state = AdState.Loaded,
                        isHigherAd = false,
                        loadedAt = System.currentTimeMillis()
                    )
                    onLoaded()
                } else {
                    adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Normal ad failed"))
                    onFailed("Normal ad failed to load")
                }
            }
        }
    }

    private fun loadSingleAd(
        adUnitId: String,
        isHigher: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        val key = adsMap.keys.find { 
            if (isHigher) it.startsWith(adUnitId) else it.endsWith(adUnitId) 
        } ?: adUnitId

        InterstitialAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // Tìm key chính xác và update
                    val correctKey = adsMap.keys.find { k ->
                        k.contains(adUnitId)
                    } ?: createAdKey(adUnitId, adUnitId)
                    
                    adsMap[correctKey] = AdHolder(
                        ad = ad,
                        state = AdState.Loaded,
                        isHigherAd = isHigher,
                        loadedAt = System.currentTimeMillis()
                    )
                    onResult(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onResult(false)
                }
            }
        )
    }

    /**
     * Show Interstitial Ad
     */
    fun showAd(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        val key = createAdKey(adHigherId, adNormalId)
        val holder = adsMap[key]

        if (holder?.state != AdState.Loaded || holder.ad == null) {
            onAdFailedToShow("No ad ready to show")
            return
        }

        // Kiểm tra permission
        if (holder.isHigherAd && !showHigher) {
            onAdFailedToShow("Higher ad not allowed")
            return
        }
        if (!holder.isHigherAd && !showNormal) {
            onAdFailedToShow("Normal ad not allowed")
            return
        }

        val ad = holder.ad
        adsMap[key] = holder.copy(state = AdState.Showing)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Xóa ad khỏi Map sau khi show
                adsMap.remove(key)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                adsMap.remove(key)
                onAdFailedToShow(error.message)
            }

            override fun onAdShowedFullScreenContent() {
                // State đã được set = Showing ở trên
            }
        }

        ad.show(activity)
    }

    /**
     * Load và Show ngay khi ready
     */
    fun loadAndShow(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        if (isReady(adHigherId, adNormalId)) {
            showAd(activity, adHigherId, adNormalId, showHigher, showNormal, onAdDismissed, onAdFailedToShow)
        } else {
            loadAd(
                adHigherId, adNormalId, showHigher, showNormal,
                onLoaded = { showAd(activity, adHigherId, adNormalId, showHigher, showNormal, onAdDismissed, onAdFailedToShow) },
                onFailed = onAdFailedToShow
            )
        }
    }

    /**
     * Xóa ad cụ thể
     */
    fun removeAd(adHigherId: String, adNormalId: String) {
        val key = createAdKey(adHigherId, adNormalId)
        adsMap.remove(key)
    }

    /**
     * Xóa tất cả ads
     */
    fun clearAll() {
        adsMap.clear()
    }
}
```

---

## 6️⃣ RewardedAdManager.kt

```kotlin
package com.yourpackage.ads.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.yourpackage.ads.state.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton Manager cho Rewarded Ads
 * - Hỗ trợ nhiều ads, lưu trong Map với key = "$adHigherId|$adNormalId"
 * - Load tuần tự: high trước, fail thì load normal
 * - Show xong tự động xóa khỏi Map
 * - Kiểm tra trạng thái trực tiếp qua adsMap[key]?.state
 */
object RewardedAdManager {

    private lateinit var appContext: Context

    private val adsMap = ConcurrentHashMap<String, AdHolder<RewardedAd>>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isReady(adHigherId: String, adNormalId: String): Boolean {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state == AdState.Loaded
    }

    fun getState(adHigherId: String, adNormalId: String): AdState {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state ?: AdState.NotLoaded
    }

    /**
     * Load Rewarded Ad - Tuần tự: High trước, fail thì Normal
     */
    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        if (!showHigher && !showNormal) {
            onFailed("Both ads disabled")
            return
        }

        val key = createAdKey(adHigherId, adNormalId)

        if (adsMap[key]?.state == AdState.Loaded) {
            onLoaded()
            return
        }

        if (adsMap[key]?.state == AdState.Loading) {
            return
        }

        adsMap[key] = AdHolder(ad = null, state = AdState.Loading)

        if (showHigher) {
            loadSingleAd(key, adHigherId, isHigher = true) { success ->
                if (success) {
                    onLoaded()
                } else if (showNormal) {
                    loadSingleAd(key, adNormalId, isHigher = false) { normalSuccess ->
                        if (normalSuccess) {
                            onLoaded()
                        } else {
                            adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Both ads failed"))
                            onFailed("Both ads failed to load")
                        }
                    }
                } else {
                    adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Higher ad failed"))
                    onFailed("Higher ad failed, normal disabled")
                }
            }
        } else if (showNormal) {
            loadSingleAd(key, adNormalId, isHigher = false) { success ->
                if (success) {
                    onLoaded()
                } else {
                    adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Normal ad failed"))
                    onFailed("Normal ad failed to load")
                }
            }
        }
    }

    private fun loadSingleAd(
        key: String,
        adUnitId: String,
        isHigher: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        RewardedAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    adsMap[key] = AdHolder(
                        ad = ad,
                        state = AdState.Loaded,
                        isHigherAd = isHigher,
                        loadedAt = System.currentTimeMillis()
                    )
                    onResult(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onResult(false)
                }
            }
        )
    }

    /**
     * Show Rewarded Ad
     */
    fun showAd(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        val key = createAdKey(adHigherId, adNormalId)
        val holder = adsMap[key]

        if (holder?.state != AdState.Loaded || holder.ad == null) {
            onAdFailedToShow("No ad ready to show")
            return
        }

        if (holder.isHigherAd && !showHigher) {
            onAdFailedToShow("Higher ad not allowed")
            return
        }
        if (!holder.isHigherAd && !showNormal) {
            onAdFailedToShow("Normal ad not allowed")
            return
        }

        val ad = holder.ad
        adsMap[key] = holder.copy(state = AdState.Showing)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                adsMap.remove(key)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                adsMap.remove(key)
                onAdFailedToShow(error.message)
            }

            override fun onAdShowedFullScreenContent() {
                // State đã được set = Showing ở trên
            }
        }

        ad.show(activity) { reward ->
            onUserEarnedReward(RewardItem(reward.type, reward.amount))
        }
    }

    fun loadAndShow(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    ) {
        if (isReady(adHigherId, adNormalId)) {
            showAd(activity, adHigherId, adNormalId, showHigher, showNormal, onUserEarnedReward, onAdDismissed, onAdFailedToShow)
        } else {
            loadAd(
                adHigherId, adNormalId, showHigher, showNormal,
                onLoaded = { showAd(activity, adHigherId, adNormalId, showHigher, showNormal, onUserEarnedReward, onAdDismissed, onAdFailedToShow) },
                onFailed = onAdFailedToShow
            )
        }
    }

    fun removeAd(adHigherId: String, adNormalId: String) {
        val key = createAdKey(adHigherId, adNormalId)
        adsMap.remove(key)
    }

    fun clearAll() {
        adsMap.clear()
    }
}
```

---

## 7️⃣ BannerAdManager.kt

```kotlin
package com.yourpackage.ads.manager

import android.content.Context
import com.google.android.gms.ads.*
import com.yourpackage.ads.state.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton Manager cho Banner Ads
 * - Load tuần tự: high trước, fail thì load normal
 * - Kiểm tra trạng thái trực tiếp qua adsMap[key]?.state
 */
object BannerAdManager {

    private lateinit var appContext: Context

    private val adsMap = ConcurrentHashMap<String, AdHolder<AdView>>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isReady(adHigherId: String, adNormalId: String): Boolean {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state == AdState.Loaded
    }

    fun getState(adHigherId: String, adNormalId: String): AdState {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state ?: AdState.NotLoaded
    }

    /**
     * Load Banner Ad - Tuần tự: High trước, fail thì Normal
     */
    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        adSize: AdSize = AdSize.BANNER,
        onLoaded: (AdView) -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        if (!showHigher && !showNormal) {
            onFailed("Both ads disabled")
            return
        }

        val key = createAdKey(adHigherId, adNormalId)

        if (adsMap[key]?.state == AdState.Loaded && adsMap[key]?.ad != null) {
            onLoaded(adsMap[key]!!.ad!!)
            return
        }

        if (adsMap[key]?.state == AdState.Loading) {
            return
        }

        adsMap[key] = AdHolder(ad = null, state = AdState.Loading)

        if (showHigher) {
            loadSingleBanner(key, adHigherId, adSize, isHigher = true) { adView ->
                if (adView != null) {
                    onLoaded(adView)
                } else if (showNormal) {
                    loadSingleBanner(key, adNormalId, adSize, isHigher = false) { normalAdView ->
                        if (normalAdView != null) {
                            onLoaded(normalAdView)
                        } else {
                            adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Both ads failed"))
                            onFailed("Both ads failed to load")
                        }
                    }
                } else {
                    adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Higher ad failed"))
                    onFailed("Higher ad failed, normal disabled")
                }
            }
        } else if (showNormal) {
            loadSingleBanner(key, adNormalId, adSize, isHigher = false) { adView ->
                if (adView != null) {
                    onLoaded(adView)
                } else {
                    adsMap[key] = AdHolder(ad = null, state = AdState.Failed("Normal ad failed"))
                    onFailed("Normal ad failed to load")
                }
            }
        }
    }

    private fun loadSingleBanner(
        key: String,
        adUnitId: String,
        adSize: AdSize,
        isHigher: Boolean,
        onResult: (AdView?) -> Unit
    ) {
        val adView = AdView(appContext).apply {
            this.adUnitId = adUnitId
            setAdSize(adSize)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    adsMap[key] = AdHolder(
                        ad = this@apply,
                        state = AdState.Loaded,
                        isHigherAd = isHigher,
                        loadedAt = System.currentTimeMillis()
                    )
                    onResult(this@apply)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onResult(null)
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    fun getAdView(adHigherId: String, adNormalId: String): AdView? {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.ad
    }

    fun removeAd(adHigherId: String, adNormalId: String) {
        val key = createAdKey(adHigherId, adNormalId)
        adsMap[key]?.ad?.destroy()
        adsMap.remove(key)
    }

    fun clearAll() {
        adsMap.values.forEach { it.ad?.destroy() }
        adsMap.clear()
    }
}
```

---

## 8️⃣ NativeAdManager.kt (với Retry Logic)

```kotlin
package com.yourpackage.ads.manager

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.yourpackage.ads.state.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Data class chứa Native Ad và metadata
 */
data class NativeAdHolder(
    val ad: NativeAd?,
    val state: AdState = AdState.NotLoaded,
    val isHigherAd: Boolean = false,
    val loadedAt: Long = 0L,
    val retryCount: Int = 0,
    val reloadCount: Int = 0  // Số lần đã reload theo duration
)

/**
 * Singleton Manager cho Native Ads
 * - Load tuần tự: high trước, fail thì load normal
 * - Hỗ trợ retry khi fail
 * - Hỗ trợ auto-reload theo thời gian
 * - Kiểm tra trạng thái trực tiếp qua adsMap[key]?.state
 */
object NativeAdManager {

    private lateinit var appContext: Context
    private val handler = Handler(Looper.getMainLooper())

    private val adsMap = ConcurrentHashMap<String, NativeAdHolder>()
    private val retryConfigMap = ConcurrentHashMap<String, RetryConfig>()
    private val reloadRunnables = ConcurrentHashMap<String, Runnable>()

    // Default config
    var defaultRetryConfig = RetryConfig(
        maxRetryCount = 3,
        reloadDuration = 0L,
        reloadTriggerCount = 0
    )

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isReady(adHigherId: String, adNormalId: String): Boolean {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state == AdState.Loaded
    }

    fun getState(adHigherId: String, adNormalId: String): AdState {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.state ?: AdState.NotLoaded
    }

    /**
     * Load Native Ad với Retry Config
     *
     * @param adHigherId Ad Unit ID với priority cao
     * @param adNormalId Ad Unit ID bình thường
     * @param showHigher Có cho phép load/show higher ad không
     * @param showNormal Có cho phép load/show normal ad không
     * @param retryConfig Config cho việc retry (null = dùng default)
     * @param onLoaded Callback khi load thành công
     * @param onFailed Callback khi fail (sau khi đã retry hết)
     */
    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        retryConfig: RetryConfig? = null,
        onLoaded: (NativeAd) -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        if (!showHigher && !showNormal) {
            onFailed("Both ads disabled")
            return
        }

        val key = createAdKey(adHigherId, adNormalId)
        val config = retryConfig ?: defaultRetryConfig
        retryConfigMap[key] = config

        // Đã ready thì không load lại
        if (adsMap[key]?.state == AdState.Loaded && adsMap[key]?.ad != null) {
            onLoaded(adsMap[key]!!.ad!!)
            return
        }

        // Đang loading thì không load lại
        if (adsMap[key]?.state == AdState.Loading) {
            return
        }

        adsMap[key] = NativeAdHolder(ad = null, state = AdState.Loading)

        loadWithRetry(
            key = key,
            adHigherId = adHigherId,
            adNormalId = adNormalId,
            showHigher = showHigher,
            showNormal = showNormal,
            currentRetry = 0,
            config = config,
            onLoaded = onLoaded,
            onFailed = onFailed
        )
    }

    private fun loadWithRetry(
        key: String,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        currentRetry: Int,
        config: RetryConfig,
        onLoaded: (NativeAd) -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (showHigher) {
            loadSingleNativeAd(key, adHigherId, isHigher = true) { nativeAd ->
                if (nativeAd != null) {
                    setupAutoReload(key, adHigherId, adNormalId, showHigher, showNormal, config)
                    onLoaded(nativeAd)
                } else if (showNormal) {
                    // Higher fail, try Normal
                    loadSingleNativeAd(key, adNormalId, isHigher = false) { normalAd ->
                        if (normalAd != null) {
                            setupAutoReload(key, adHigherId, adNormalId, showHigher, showNormal, config)
                            onLoaded(normalAd)
                        } else {
                            // Cả 2 đều fail, retry nếu còn quota
                            handleRetry(key, adHigherId, adNormalId, showHigher, showNormal, currentRetry, config, onLoaded, onFailed)
                        }
                    }
                } else {
                    // Higher fail, Normal disabled, retry
                    handleRetry(key, adHigherId, adNormalId, showHigher, showNormal, currentRetry, config, onLoaded, onFailed)
                }
            }
        } else if (showNormal) {
            loadSingleNativeAd(key, adNormalId, isHigher = false) { nativeAd ->
                if (nativeAd != null) {
                    setupAutoReload(key, adHigherId, adNormalId, showHigher, showNormal, config)
                    onLoaded(nativeAd)
                } else {
                    handleRetry(key, adHigherId, adNormalId, showHigher, showNormal, currentRetry, config, onLoaded, onFailed)
                }
            }
        }
    }

    private fun handleRetry(
        key: String,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        currentRetry: Int,
        config: RetryConfig,
        onLoaded: (NativeAd) -> Unit,
        onFailed: (String) -> Unit
    ) {
        if (currentRetry < config.maxRetryCount) {
            // Delay trước khi retry (exponential backoff)
            val delayMs = (1000L * (currentRetry + 1)).coerceAtMost(5000L)
            handler.postDelayed({
                adsMap[key] = (adsMap[key] ?: NativeAdHolder(null)).copy(
                    retryCount = currentRetry + 1
                )
                loadWithRetry(
                    key, adHigherId, adNormalId, showHigher, showNormal,
                    currentRetry + 1, config, onLoaded, onFailed
                )
            }, delayMs)
        } else {
            adsMap[key] = NativeAdHolder(ad = null, state = AdState.Failed("All retries exhausted", currentRetry))
            onFailed("Failed after $currentRetry retries")
        }
    }

    private fun setupAutoReload(
        key: String,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        config: RetryConfig
    ) {
        // Cancel existing reload
        reloadRunnables[key]?.let { handler.removeCallbacks(it) }

        if (config.reloadDuration > 0) {
            val currentReloadCount = adsMap[key]?.reloadCount ?: 0

            // Check trigger count limit
            if (config.reloadTriggerCount > 0 && currentReloadCount >= config.reloadTriggerCount) {
                return
            }

            val reloadRunnable = Runnable {
                // Destroy old ad
                adsMap[key]?.ad?.destroy()
                adsMap[key] = (adsMap[key] ?: NativeAdHolder(null)).copy(
                    ad = null,
                    state = AdState.NotLoaded,
                    reloadCount = currentReloadCount + 1
                )

                // Reload
                loadAd(adHigherId, adNormalId, showHigher, showNormal, config)
            }

            reloadRunnables[key] = reloadRunnable
            handler.postDelayed(reloadRunnable, config.reloadDuration)
        }
    }

    private fun loadSingleNativeAd(
        key: String,
        adUnitId: String,
        isHigher: Boolean,
        onResult: (NativeAd?) -> Unit
    ) {
        AdLoader.Builder(appContext, adUnitId)
            .forNativeAd { nativeAd ->
                // Destroy old ad if exists
                adsMap[key]?.ad?.destroy()

                adsMap[key] = NativeAdHolder(
                    ad = nativeAd,
                    state = AdState.Loaded,
                    isHigherAd = isHigher,
                    loadedAt = System.currentTimeMillis(),
                    retryCount = adsMap[key]?.retryCount ?: 0,
                    reloadCount = adsMap[key]?.reloadCount ?: 0
                )
                onResult(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    onResult(null)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    /**
     * Lấy Native Ad đã load
     */
    fun getNativeAd(adHigherId: String, adNormalId: String): NativeAd? {
        val key = createAdKey(adHigherId, adNormalId)
        return adsMap[key]?.ad
    }

    /**
     * Xóa ad và cancel auto-reload
     */
    fun removeAd(adHigherId: String, adNormalId: String) {
        val key = createAdKey(adHigherId, adNormalId)

        // Cancel reload
        reloadRunnables[key]?.let { handler.removeCallbacks(it) }
        reloadRunnables.remove(key)

        // Destroy and remove
        adsMap[key]?.ad?.destroy()
        adsMap.remove(key)
        retryConfigMap.remove(key)
    }

    /**
     * Xóa tất cả
     */
    fun clearAll() {
        // Cancel all reloads
        reloadRunnables.values.forEach { handler.removeCallbacks(it) }
        reloadRunnables.clear()

        // Destroy all ads
        adsMap.values.forEach { it.ad?.destroy() }
        adsMap.clear()
        retryConfigMap.clear()
    }

    /**
     * Force reload ad ngay lập tức
     */
    fun forceReload(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        retryConfig: RetryConfig? = null,
        onLoaded: (NativeAd) -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        removeAd(adHigherId, adNormalId)
        loadAd(adHigherId, adNormalId, showHigher, showNormal, retryConfig, onLoaded, onFailed)
    }
}
```

---

## 9️⃣ BannerAdView.kt (Composable)

```kotlin
package com.yourpackage.ads.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.yourpackage.ads.manager.BannerAdManager
import com.yourpackage.ads.state.AdState

@Composable
fun BannerAdView(
    adHigherId: String,
    adNormalId: String,
    showHigher: Boolean,
    showNormal: Boolean,
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    if (!showHigher && !showNormal) return

    var currentAdView by remember { mutableStateOf<AdView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasFailed by remember { mutableStateOf(false) }

    LaunchedEffect(adHigherId, adNormalId, showHigher, showNormal) {
        if (BannerAdManager.isReady(adHigherId, adNormalId)) {
            currentAdView = BannerAdManager.getAdView(adHigherId, adNormalId)
            if (currentAdView != null) onAdLoaded()
        } else {
            isLoading = true
            BannerAdManager.loadAd(
                adHigherId, adNormalId, showHigher, showNormal, adSize,
                onLoaded = { adView ->
                    currentAdView = adView
                    isLoading = false
                    onAdLoaded()
                },
                onFailed = { error ->
                    isLoading = false
                    hasFailed = true
                    onAdFailed(error)
                }
            )
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(adSize.height.dp), contentAlignment = Alignment.Center) {
        when {
            isLoading -> CircularProgressIndicator()
            currentAdView != null -> AndroidView(modifier = Modifier.fillMaxWidth(), factory = { currentAdView!! })
            else -> {}
        }
    }
}

// PREVIEWS
@Preview(showBackground = true, name = "Banner - Loading")
@Composable
private fun BannerLoadingPreview() {
    Box(Modifier.fillMaxWidth().height(50.dp).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true, name = "Banner - Loaded")
@Composable
private fun BannerLoadedPreview() {
    Box(Modifier.fillMaxWidth().height(50.dp).background(Color(0xFFE8E8E8)), contentAlignment = Alignment.Center) {
        Text("Banner Ad (320x50)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}
```

---

## 🔟 NativeAdView.kt (Composable)

```kotlin
package com.yourpackage.ads.ui

import android.view.LayoutInflater
import android.widget.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.yourpackage.ads.R
import com.yourpackage.ads.manager.NativeAdManager
import com.yourpackage.ads.state.RetryConfig

enum class NativeAdSize { SMALL, MEDIUM }

@Composable
fun NativeAdView(
    adHigherId: String,
    adNormalId: String,
    showHigher: Boolean,
    showNormal: Boolean,
    modifier: Modifier = Modifier,
    size: NativeAdSize = NativeAdSize.MEDIUM,
    retryConfig: RetryConfig? = null,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
) {
    if (!showHigher && !showNormal) return

    var currentNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasFailed by remember { mutableStateOf(false) }

    LaunchedEffect(adHigherId, adNormalId, showHigher, showNormal) {
        if (NativeAdManager.isReady(adHigherId, adNormalId)) {
            currentNativeAd = NativeAdManager.getNativeAd(adHigherId, adNormalId)
            if (currentNativeAd != null) onAdLoaded()
        } else {
            isLoading = true
            NativeAdManager.loadAd(
                adHigherId, adNormalId, showHigher, showNormal, retryConfig,
                onLoaded = { ad ->
                    currentNativeAd = ad
                    isLoading = false
                    onAdLoaded()
                },
                onFailed = { error ->
                    isLoading = false
                    hasFailed = true
                    onAdFailed(error)
                }
            )
        }
    }

    Card(modifier = modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp)) {
        val height = if (size == NativeAdSize.SMALL) 80.dp else 280.dp
        when {
            isLoading -> Box(Modifier.fillMaxWidth().height(height).background(Color.White), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            currentNativeAd != null -> AndroidView(Modifier.fillMaxWidth()) { ctx ->
                val layout = if (size == NativeAdSize.SMALL) R.layout.native_ad_small else R.layout.native_ad_medium
                (LayoutInflater.from(ctx).inflate(layout, null) as NativeAdView).also { populateNativeAdView(currentNativeAd!!, it) }
            }
            hasFailed -> Box(Modifier.fillMaxWidth().height(height).background(Color.White), contentAlignment = Alignment.Center) {
                Text("Ad not available", color = Color.Gray)
            }
            else -> Box(Modifier.fillMaxWidth().height(height).background(Color.White))
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    (adView.headlineView as? TextView)?.text = nativeAd.headline
    adView.bodyView = adView.findViewById(R.id.ad_body)
    nativeAd.body?.let { (adView.bodyView as? TextView)?.apply { text = it; visibility = android.view.View.VISIBLE } }
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    nativeAd.callToAction?.let { (adView.callToActionView as? Button)?.apply { text = it; visibility = android.view.View.VISIBLE } }
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    nativeAd.icon?.let { (adView.iconView as? ImageView)?.setImageDrawable(it.drawable) }
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    nativeAd.starRating?.let { (adView.starRatingView as? RatingBar)?.rating = it.toFloat() }
    adView.mediaView = adView.findViewById(R.id.ad_media)
    adView.mediaView?.mediaContent = nativeAd.mediaContent
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    nativeAd.advertiser?.let { (adView.advertiserView as? TextView)?.text = it }
    adView.setNativeAd(nativeAd)
}

// PREVIEWS
@Preview(showBackground = true, name = "Native Small - Loading")
@Composable
private fun NativeSmallLoadingPreview() {
    Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp)) {
        Box(Modifier.fillMaxWidth().height(80.dp).background(Color.White), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}

@Preview(showBackground = true, name = "Native Small - Loaded")
@Composable
private fun NativeSmallLoadedPreview() {
    Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text("App Headline", style = MaterialTheme.typography.titleSmall); Text("Advertiser", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary).padding(12.dp, 6.dp)) { Text("Install", color = Color.White, fontSize = 12.sp) }
        }
    }
}

@Preview(showBackground = true, name = "Native Medium - Loaded")
@Composable
private fun NativeMediumLoadedPreview() {
    Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("App Headline", style = MaterialTheme.typography.titleSmall); Text("Advertiser", color = Color.Gray) }
                Box(Modifier.background(Color(0xFFFFA500)).padding(4.dp, 2.dp)) { Text("Ad", fontSize = 10.sp, color = Color.White) }
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) { Text("Media", color = Color.Gray) }
            Spacer(Modifier.height(8.dp))
            Text("Body text of the native ad.")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("★★★★☆", color = Color(0xFFFFB800))
                Spacer(Modifier.weight(1f))
                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary).padding(16.dp, 8.dp)) { Text("Install", color = Color.White) }
            }
        }
    }
}

@Preview(showBackground = true, name = "Native - Failed")
@Composable
private fun NativeFailedPreview() {
    Card(Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(12.dp)) {
        Box(Modifier.fillMaxWidth().height(100.dp).background(Color.White), contentAlignment = Alignment.Center) {
            Text("Ad not available", color = Color.Gray)
        }
    }
}
```

---

## 1️⃣1️⃣ XML Layouts

### native_ad_small.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.gms.ads.nativead.NativeAdView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content" android:background="@android:color/white" android:padding="12dp">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="horizontal" android:gravity="center_vertical">
        <ImageView android:id="@+id/ad_app_icon" android:layout_width="48dp" android:layout_height="48dp" />
        <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="12dp" android:orientation="vertical">
            <TextView android:id="@+id/ad_headline" android:layout_width="match_parent" android:layout_height="wrap_content" android:maxLines="1" android:textStyle="bold" />
            <TextView android:id="@+id/ad_advertiser" android:layout_width="match_parent" android:layout_height="wrap_content" android:maxLines="1" android:textColor="@android:color/darker_gray" />
            <TextView android:id="@+id/ad_body" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
            <RatingBar android:id="@+id/ad_stars" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
        </LinearLayout>
        <Button android:id="@+id/ad_call_to_action" android:layout_width="wrap_content" android:layout_height="36dp" />
        <com.google.android.gms.ads.nativead.MediaView android:id="@+id/ad_media" android:layout_width="0dp" android:layout_height="0dp" android:visibility="gone" />
    </LinearLayout>
</com.google.android.gms.ads.nativead.NativeAdView>
```

### native_ad_medium.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.gms.ads.nativead.NativeAdView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent" android:layout_height="wrap_content" android:background="@android:color/white" android:padding="12dp">
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:orientation="vertical">
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:gravity="center_vertical">
            <ImageView android:id="@+id/ad_app_icon" android:layout_width="40dp" android:layout_height="40dp" />
            <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1" android:layout_marginStart="12dp" android:orientation="vertical">
                <TextView android:id="@+id/ad_headline" android:layout_width="match_parent" android:layout_height="wrap_content" android:maxLines="2" android:textStyle="bold" />
                <TextView android:id="@+id/ad_advertiser" android:layout_width="match_parent" android:layout_height="wrap_content" android:textColor="@android:color/darker_gray" />
            </LinearLayout>
            <TextView android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="Ad" android:textColor="@android:color/white" android:background="#FFA500" android:padding="4dp" />
        </LinearLayout>
        <com.google.android.gms.ads.nativead.MediaView android:id="@+id/ad_media" android:layout_width="match_parent" android:layout_height="180dp" android:layout_marginTop="8dp" />
        <TextView android:id="@+id/ad_body" android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginTop="8dp" android:maxLines="2" />
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content" android:layout_marginTop="8dp" android:gravity="center_vertical">
            <RatingBar android:id="@+id/ad_stars" style="?android:attr/ratingBarStyleSmall" android:layout_width="wrap_content" android:layout_height="wrap_content" android:isIndicator="true" />
            <View android:layout_width="0dp" android:layout_height="0dp" android:layout_weight="1" />
            <Button android:id="@+id/ad_call_to_action" android:layout_width="wrap_content" android:layout_height="40dp" />
        </LinearLayout>
    </LinearLayout>
</com.google.android.gms.ads.nativead.NativeAdView>
```

---

## 📌 API Summary

### InterstitialAdManager / RewardedAdManager (Singleton)

```kotlin
// Initialize (trong Application)
InterstitialAdManager.init(context)
RewardedAdManager.init(context)

// Load - tuần tự: high fail → normal
InterstitialAdManager.loadAd(
    adHigherId = "ca-app-pub-xxx/high",
    adNormalId = "ca-app-pub-xxx/normal",
    showHigher = true,
    showNormal = true,
    onLoaded = { },
    onFailed = { error -> }
)

// Show (auto-remove sau khi show)
InterstitialAdManager.showAd(
    activity, adHigherId, adNormalId, showHigher, showNormal,
    onAdDismissed = { },
    onAdFailedToShow = { }
)

// Check & Utils
InterstitialAdManager.isReady(adHigherId, adNormalId)
InterstitialAdManager.getState(adHigherId, adNormalId)
InterstitialAdManager.removeAd(adHigherId, adNormalId)
InterstitialAdManager.clearAll()
```

### NativeAdManager (với Retry)

```kotlin
// Initialize
NativeAdManager.init(context)

// Set default config
NativeAdManager.defaultRetryConfig = RetryConfig(
    maxRetryCount = 3,           // Retry tối đa 3 lần khi fail
    reloadDuration = 60_000L,    // Auto reload sau 60s
    reloadTriggerCount = 5       // Tối đa 5 lần auto reload
)

// Load với custom config
NativeAdManager.loadAd(
    adHigherId, adNormalId, showHigher, showNormal,
    retryConfig = RetryConfig(maxRetryCount = 5),
    onLoaded = { nativeAd -> },
    onFailed = { error -> }
)

// Force reload
NativeAdManager.forceReload(adHigherId, adNormalId, showHigher, showNormal)
```

---

## 📚 Logic Flow

```
loadAd() called
     │
     ▼
┌─────────────────────┐
│ Both flags false?   │──Yes──▶ onFailed("Both disabled")
└─────────────────────┘
     │ No
     ▼
┌─────────────────────┐
│ Already loaded?     │──Yes──▶ onLoaded() (return cached)
└─────────────────────┘
     │ No
     ▼
┌─────────────────────┐
│ showHigher = true?  │──Yes──▶ Load Higher Ad
└─────────────────────┘              │
     │ No                      ┌─────┴─────┐
     │                         ▼           ▼
     │                     Success      Failed
     │                         │           │
     │                    onLoaded    ┌────┴────┐
     │                                │showNormal│
     │                                └────┬────┘
     │                              Yes    │    No
     │                               ▼     │     ▼
     │                        Load Normal  │  onFailed
     │                              │      │
     │                        ┌─────┴─────┐│
     │                        ▼           ▼│
     │                    Success      Failed
     │                        │           │
     │                   onLoaded    [RETRY?]
     │                                    │
     ▼                                    ▼
Load Normal only              retry < maxRetryCount?
     │                              Yes → Retry after delay
┌────┴────┐                         No  → onFailed
▼         ▼
Success  Failed → [RETRY?]
```

---

## ⚠️ Important Notes

1. **Singleton Pattern**: `InterstitialAdManager`, `RewardedAdManager`, `BannerAdManager`, `NativeAdManager` đều là singleton, cần gọi `init(context)` trong Application

2. **Multiple Ads**: Mỗi cặp `(adHigherId, adNormalId)` được lưu riêng trong Map với key = `"$adHigherId|$adNormalId"`

3. **Sequential Loading**: Luôn load High trước, chỉ load Normal khi High fail

4. **Auto Cleanup**: Sau khi show Interstitial/Rewarded, ad tự động bị xóa khỏi Map

5. **Native Retry**:
   - `maxRetryCount`: Số lần retry tối đa khi fail (default = 3)
   - `reloadDuration`: Thời gian auto reload (ms), 0 = không auto reload
   - `reloadTriggerCount`: Số lần auto reload tối đa, 0 = không giới hạn

6. **Thread Safety**: Sử dụng `ConcurrentHashMap` để đảm bảo thread-safe

7. **State Checking**: Không sử dụng StateFlow để observe, thay vào đó kiểm tra trạng thái trực tiếp:
   ```kotlin
   // Kiểm tra ad đã sẵn sàng chưa
   if (InterstitialAdManager.isReady(adHigherId, adNormalId)) {
       // Ad ready to show
   }

   // Lấy trạng thái chi tiết
   val state = InterstitialAdManager.getState(adHigherId, adNormalId)
   when (state) {
       is AdState.Loaded -> // Ready
       is AdState.Loading -> // Đang load
       is AdState.Failed -> // Lỗi: state.message
       else -> // NotLoaded hoặc Showing
   }
   ```

---

## 🚀 Hướng Dẫn Sử Dụng Chi Tiết

### 1. Khởi tạo trong Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Khởi tạo Google Mobile Ads SDK
        AdsInitializer.initialize(
            context = this,
            testDeviceIds = listOf("YOUR_TEST_DEVICE_ID"), // Bỏ trong production
            onInitComplete = {
                // SDK đã sẵn sàng
            }
        )

        // 2. Khởi tạo các Ad Managers
        InterstitialAdManager.init(this)
        RewardedAdManager.init(this)
        BannerAdManager.init(this)
        NativeAdManager.init(this)

        // 3. (Optional) Cấu hình Native Ad retry
        NativeAdManager.defaultRetryConfig = RetryConfig(
            maxRetryCount = 3,
            reloadDuration = 60_000L,  // Auto reload sau 60s
            reloadTriggerCount = 5      // Tối đa 5 lần
        )
    }
}
```

---

### 2. Sử dụng Interstitial Ads

#### 2.1 Pre-load trước khi cần show

```kotlin
// Trong ViewModel hoặc Activity
class HomeViewModel : ViewModel() {

    companion object {
        const val INTER_HIGH = "ca-app-pub-xxx/inter_high"
        const val INTER_NORMAL = "ca-app-pub-xxx/inter_normal"
    }

    fun preloadInterstitial() {
        InterstitialAdManager.loadAd(
            adHigherId = INTER_HIGH,
            adNormalId = INTER_NORMAL,
            showHigher = true,
            showNormal = true,
            onLoaded = {
                Log.d("Ads", "Interstitial ready")
            },
            onFailed = { error ->
                Log.e("Ads", "Failed to load: $error")
            }
        )
    }
}
```

#### 2.2 Show khi user action

```kotlin
// Trong Activity
fun onButtonClick() {
    if (InterstitialAdManager.isReady(INTER_HIGH, INTER_NORMAL)) {
        InterstitialAdManager.showAd(
            activity = this,
            adHigherId = INTER_HIGH,
            adNormalId = INTER_NORMAL,
            showHigher = true,
            showNormal = true,
            onAdDismissed = {
                // User đóng ad, tiếp tục flow
                navigateToNextScreen()
            },
            onAdFailedToShow = { error ->
                // Không show được, vẫn tiếp tục
                navigateToNextScreen()
            }
        )
    } else {
        // Ad chưa ready, bỏ qua
        navigateToNextScreen()
    }
}
```

#### 2.3 Load và Show ngay (không preload)

```kotlin
fun showInterstitialNow() {
    InterstitialAdManager.loadAndShow(
        activity = this,
        adHigherId = INTER_HIGH,
        adNormalId = INTER_NORMAL,
        showHigher = true,
        showNormal = true,
        onAdDismissed = {
            navigateToNextScreen()
        },
        onAdFailedToShow = { error ->
            navigateToNextScreen()
        }
    )
}
```

---

### 3. Sử dụng Rewarded Ads

#### 3.1 Pre-load Rewarded Ad

```kotlin
companion object {
    const val REWARD_HIGH = "ca-app-pub-xxx/reward_high"
    const val REWARD_NORMAL = "ca-app-pub-xxx/reward_normal"
}

fun preloadRewardedAd() {
    RewardedAdManager.loadAd(
        adHigherId = REWARD_HIGH,
        adNormalId = REWARD_NORMAL,
        showHigher = true,
        showNormal = true,
        onLoaded = {
            // Enable nút xem ads
            btnWatchAd.isEnabled = true
        },
        onFailed = { error ->
            // Ẩn/disable nút xem ads
            btnWatchAd.isEnabled = false
        }
    )
}
```

#### 3.2 Show và nhận reward

```kotlin
fun onWatchAdClick() {
    if (RewardedAdManager.isReady(REWARD_HIGH, REWARD_NORMAL)) {
        RewardedAdManager.showAd(
            activity = this,
            adHigherId = REWARD_HIGH,
            adNormalId = REWARD_NORMAL,
            showHigher = true,
            showNormal = true,
            onUserEarnedReward = { reward ->
                // User xem xong và nhận thưởng
                grantReward(reward.type, reward.amount)
                Toast.makeText(this, "Bạn nhận được ${reward.amount} ${reward.type}!", Toast.LENGTH_SHORT).show()
            },
            onAdDismissed = {
                // User đóng ad (có thể đã nhận hoặc chưa nhận reward)
                preloadRewardedAd() // Preload lại cho lần sau
            },
            onAdFailedToShow = { error ->
                Toast.makeText(this, "Không thể hiển thị quảng cáo", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
```

---

### 4. Sử dụng Banner Ads

#### 4.1 Trong Jetpack Compose

```kotlin
@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Nội dung chính
        MainContent(modifier = Modifier.weight(1f))

        // Banner ở dưới cùng
        BannerAdView(
            adHigherId = "ca-app-pub-xxx/banner_high",
            adNormalId = "ca-app-pub-xxx/banner_normal",
            showHigher = true,
            showNormal = true,
            adSize = AdSize.BANNER, // hoặc AdSize.LARGE_BANNER, AdSize.MEDIUM_RECTANGLE
            onAdLoaded = {
                Log.d("Ads", "Banner loaded")
            },
            onAdFailed = { error ->
                Log.e("Ads", "Banner failed: $error")
            }
        )
    }
}
```

#### 4.2 Adaptive Banner (khuyến nghị)

```kotlin
@Composable
fun AdaptiveBannerAd(
    adHigherId: String,
    adNormalId: String,
    showHigher: Boolean,
    showNormal: Boolean
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Tính toán adaptive banner size
    val adWidth = configuration.screenWidthDp
    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        context,
        adWidth
    )

    BannerAdView(
        adHigherId = adHigherId,
        adNormalId = adNormalId,
        showHigher = showHigher,
        showNormal = showNormal,
        adSize = adSize
    )
}
```

---

### 5. Sử dụng Native Ads

#### 5.1 Native Ad nhỏ (trong list item)

```kotlin
@Composable
fun LanguageSelectionScreen() {
    LazyColumn {
        items(languages) { language ->
            LanguageItem(language)
        }

        // Native Ad sau mỗi 5 items
        item {
            NativeAdView(
                adHigherId = "ca-app-pub-xxx/native_high",
                adNormalId = "ca-app-pub-xxx/native_normal",
                showHigher = true,
                showNormal = true,
                size = NativeAdSize.SMALL, // Compact layout
                onAdLoaded = { },
                onAdFailed = { }
            )
        }
    }
}
```

#### 5.2 Native Ad lớn (full card)

```kotlin
@Composable
fun OnboardingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Onboarding content
        OnboardingContent(modifier = Modifier.weight(1f))

        // Native Ad lớn
        NativeAdView(
            adHigherId = "ca-app-pub-xxx/native_high",
            adNormalId = "ca-app-pub-xxx/native_normal",
            showHigher = true,
            showNormal = true,
            size = NativeAdSize.MEDIUM, // Full card với media
            retryConfig = RetryConfig(
                maxRetryCount = 3,
                reloadDuration = 0L // Không auto reload
            ),
            onAdLoaded = { },
            onAdFailed = { }
        )

        // Button
        Button(onClick = { navigateNext() }) {
            Text("Tiếp tục")
        }
    }
}
```

#### 5.3 Preload Native Ad cho màn tiếp theo

```kotlin
// Trong màn hiện tại, preload cho màn tiếp theo
LaunchedEffect(Unit) {
    // Load native ad cho màn tiếp theo
    NativeAdManager.loadAd(
        adHigherId = "ca-app-pub-xxx/next_screen_native_high",
        adNormalId = "ca-app-pub-xxx/next_screen_native_normal",
        showHigher = true,
        showNormal = true,
        retryConfig = RetryConfig(maxRetryCount = 2),
        onLoaded = { },
        onFailed = { }
    )
}
```

---

### 6. Pattern: Màn Prepare Data với Timeout

```kotlin
@Composable
fun PrepareDataScreen(
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    var isLoading by remember { mutableStateOf(true) }
    var adReady by remember { mutableStateOf(false) }

    // Load interstitial với timeout 5s
    LaunchedEffect(Unit) {
        // Bắt đầu load ad
        InterstitialAdManager.loadAd(
            adHigherId = "ca-app-pub-xxx/inter_high",
            adNormalId = "ca-app-pub-xxx/inter_normal",
            showHigher = true,
            showNormal = true,
            onLoaded = {
                adReady = true
            },
            onFailed = { }
        )

        // Timeout 5s
        delay(5000)
        isLoading = false
    }

    // Khi hết loading
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            if (adReady && InterstitialAdManager.isReady(
                "ca-app-pub-xxx/inter_high",
                "ca-app-pub-xxx/inter_normal"
            )) {
                // Ad sẵn sàng, show ngay
                InterstitialAdManager.showAd(
                    activity = activity,
                    adHigherId = "ca-app-pub-xxx/inter_high",
                    adNormalId = "ca-app-pub-xxx/inter_normal",
                    showHigher = true,
                    showNormal = true,
                    onAdDismissed = {
                        onNavigateToHome()
                    },
                    onAdFailedToShow = {
                        onNavigateToHome()
                    }
                )
            } else {
                // Timeout, bỏ qua ad
                InterstitialAdManager.removeAd(
                    "ca-app-pub-xxx/inter_high",
                    "ca-app-pub-xxx/inter_normal"
                )
                onNavigateToHome()
            }
        }
    }

    // UI Loading
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Đang chuẩn bị dữ liệu...")
        }
    }
}
```

---

### 7. Quản lý Ad IDs với Remote Config

```kotlin
object AdIds {
    // Default values (fallback)
    private var _interHighId = "ca-app-pub-xxx/inter_high"
    private var _interNormalId = "ca-app-pub-xxx/inter_normal"
    private var _nativeHighId = "ca-app-pub-xxx/native_high"
    private var _nativeNormalId = "ca-app-pub-xxx/native_normal"

    // Getters
    val interHighId get() = _interHighId
    val interNormalId get() = _interNormalId
    val nativeHighId get() = _nativeHighId
    val nativeNormalId get() = _nativeNormalId

    // Show flags (controlled by Remote Config)
    var showInterHigher = true
    var showInterNormal = true
    var showNativeHigher = true
    var showNativeNormal = true

    // Update from Remote Config
    fun updateFromRemoteConfig(config: FirebaseRemoteConfig) {
        _interHighId = config.getString("inter_high_id").ifEmpty { _interHighId }
        _interNormalId = config.getString("inter_normal_id").ifEmpty { _interNormalId }
        _nativeHighId = config.getString("native_high_id").ifEmpty { _nativeHighId }
        _nativeNormalId = config.getString("native_normal_id").ifEmpty { _nativeNormalId }

        showInterHigher = config.getBoolean("show_inter_higher")
        showInterNormal = config.getBoolean("show_inter_normal")
        showNativeHigher = config.getBoolean("show_native_higher")
        showNativeNormal = config.getBoolean("show_native_normal")
    }
}

// Sử dụng
InterstitialAdManager.loadAd(
    adHigherId = AdIds.interHighId,
    adNormalId = AdIds.interNormalId,
    showHigher = AdIds.showInterHigher,
    showNormal = AdIds.showInterNormal
)
```

---

### 8. Cleanup khi không cần thiết

```kotlin
// Trong Activity/Fragment onDestroy
override fun onDestroy() {
    super.onDestroy()

    // Xóa ads cụ thể
    BannerAdManager.removeAd(adHigherId, adNormalId)
    NativeAdManager.removeAd(adHigherId, adNormalId)

    // Hoặc xóa tất cả (khi thoát app)
    // BannerAdManager.clearAll()
    // NativeAdManager.clearAll()
    // InterstitialAdManager.clearAll()
    // RewardedAdManager.clearAll()
}
```

---

## 📋 Best Practices

| Loại Ad | Khi nào load | Khi nào show |
|---------|--------------|--------------|
| **Interstitial** | Preload trước 1-2 màn | Sau user action rõ ràng (click, complete task) |
| **Rewarded** | Khi user có thể cần reward | Khi user chủ động bấm xem |
| **Banner** | Khi màn hình hiển thị | Luôn visible ở bottom/top |
| **Native** | Preload từ màn trước | Khi scroll đến hoặc màn load xong |

### Do's ✅

- Preload ads trước khi cần show
- Luôn handle cả success và failure cases
- Cleanup ads khi không cần thiết
- Sử dụng timeout cho Interstitial ở màn chốt chặn
- Test với test ad IDs trước khi release

### Don'ts ❌

- Không show Interstitial đột ngột khi user đang tương tác
- Không đặt Native Ad sát các nút điều hướng
- Không show quá nhiều ads liên tiếp
- Không block UI khi đang load ads
