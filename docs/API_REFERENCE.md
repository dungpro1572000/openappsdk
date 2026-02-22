# API Reference

Complete API documentation for OpenApp SDK and Our Ads SDK.

---

## Table of Contents

1. [Our Ads SDK](#our-ads-sdk)
   - [AdsInitializer](#adsinitializer)
   - [NativeAdManager](#nativeadmanager)
   - [InterstitialAdManager](#interstitialadmanager)
   - [RewardedAdManager](#rewardedadmanager)
   - [BannerAdManager](#banneradmanager)
   - [AdState](#adstate)
   - [Composables](#composables)
2. [OpenApp SDK](#openapp-sdk)
   - [OpenAppConfig](#openappconfig)
   - [OpenAppNavigation](#openappnavigation)
   - [UserPreferences](#userpreferences)
   - [LanguageManager](#languagemanager)

---

# Our Ads SDK

## AdsInitializer

Singleton object for initializing Google Mobile Ads SDK.

### Methods

```kotlin
object AdsInitializer {
    /**
     * Initialize the Google Mobile Ads SDK
     *
     * @param context Application context
     * @param testDeviceIds List of test device IDs for testing
     * @param onInitComplete Callback when initialization is complete
     */
    fun initialize(
        context: Context,
        testDeviceIds: List<String> = emptyList(),
        onInitComplete: () -> Unit = {}
    )

    /**
     * Check if SDK is initialized
     *
     * @return true if initialized
     */
    fun isInitialized(): Boolean
}
```

### Example

```kotlin
AdsInitializer.initialize(
    context = applicationContext,
    testDeviceIds = listOf("YOUR_TEST_DEVICE_ID"),
    onInitComplete = {
        Log.d("Ads", "SDK initialized")
    }
)
```

---

## NativeAdManager

Singleton manager for Native Ads with retry logic and auto-reload support.

### Properties

```kotlin
object NativeAdManager {
    /**
     * Default configuration for retry behavior
     */
    var defaultRetryConfig: RetryConfig
}
```

### Methods

```kotlin
object NativeAdManager {
    /**
     * Initialize the manager
     *
     * @param context Application context
     */
    fun init(context: Context)

    /**
     * Load a Native Ad with sequential loading strategy
     *
     * @param adHigherId High priority ad unit ID
     * @param adNormalId Normal priority ad unit ID (fallback)
     * @param showHigher Enable loading high priority ad
     * @param showNormal Enable loading normal priority ad
     * @param retryConfig Custom retry configuration (optional)
     * @param onLoaded Callback with loaded NativeAd
     * @param onFailed Callback with error message
     */
    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        retryConfig: RetryConfig? = null,
        onLoaded: (NativeAd) -> Unit = {},
        onFailed: (String) -> Unit = {}
    )

    /**
     * Force reload an ad (destroys existing ad first)
     */
    fun forceReload(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        retryConfig: RetryConfig? = null,
        onLoaded: (NativeAd) -> Unit = {},
        onFailed: (String) -> Unit = {}
    )

    /**
     * Check if ad is ready to display
     *
     * @return true if ad is loaded and ready
     */
    fun isReady(adHigherId: String, adNormalId: String): Boolean

    /**
     * Get current ad state
     *
     * @return AdState enum value
     */
    fun getState(adHigherId: String, adNormalId: String): AdState

    /**
     * Get loaded NativeAd instance
     *
     * @return NativeAd or null if not loaded
     */
    fun getNativeAd(adHigherId: String, adNormalId: String): NativeAd?

    /**
     * Get StateFlow for reactive updates
     *
     * @return StateFlow<NativeAdHolder?>
     */
    fun getAdFlow(adHigherId: String, adNormalId: String): StateFlow<NativeAdHolder?>

    /**
     * Remove ad and cancel auto-reload
     */
    fun removeAd(adHigherId: String, adNormalId: String)

    /**
     * Clear all ads
     */
    fun clearAll()
}
```

### RetryConfig

```kotlin
data class RetryConfig(
    val maxRetryCount: Int = 3,        // Max retry attempts
    val reloadDuration: Long = 0L,     // Auto-reload interval (ms), 0 = disabled
    val reloadTriggerCount: Int = 0    // Max auto-reload count, 0 = unlimited
)
```

### Example

```kotlin
// Initialize
NativeAdManager.init(context)

// Configure default retry
NativeAdManager.defaultRetryConfig = RetryConfig(
    maxRetryCount = 3,
    reloadDuration = 60_000L,  // Reload every 60s
    reloadTriggerCount = 5     // Max 5 reloads
)

// Load ad
NativeAdManager.loadAd(
    adHigherId = "ca-app-pub-xxx/high",
    adNormalId = "ca-app-pub-xxx/normal",
    showHigher = true,
    showNormal = true,
    onLoaded = { nativeAd ->
        // Display ad
    },
    onFailed = { error ->
        // Handle error
    }
)

// Check state
if (NativeAdManager.isReady(highId, normalId)) {
    val ad = NativeAdManager.getNativeAd(highId, normalId)
}
```

---

## InterstitialAdManager

Singleton manager for Interstitial Ads.

### Methods

```kotlin
object InterstitialAdManager {
    /**
     * Initialize the manager
     */
    fun init(context: Context)

    /**
     * Load an Interstitial Ad
     *
     * @param adHigherId High priority ad unit ID
     * @param adNormalId Normal priority ad unit ID
     * @param showHigher Enable high priority ad
     * @param showNormal Enable normal priority ad
     * @param onLoaded Callback when loaded
     * @param onFailed Callback with error
     */
    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    )

    /**
     * Show loaded Interstitial Ad
     * Note: Ad is automatically removed after showing
     *
     * @param activity Activity for showing ad
     * @param onAdDismissed Callback when ad is closed
     * @param onAdFailedToShow Callback if show fails
     */
    fun showAd(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    )

    /**
     * Load and show immediately when ready
     */
    fun loadAndShow(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    )

    /**
     * Check if ad is ready
     */
    fun isReady(adHigherId: String, adNormalId: String): Boolean

    /**
     * Get current state
     */
    fun getState(adHigherId: String, adNormalId: String): AdState

    /**
     * Remove specific ad
     */
    fun removeAd(adHigherId: String, adNormalId: String)

    /**
     * Clear all ads
     */
    fun clearAll()
}
```

### Example

```kotlin
// Preload
InterstitialAdManager.loadAd(
    adHigherId = "ca-app-pub-xxx/high",
    adNormalId = "ca-app-pub-xxx/normal",
    showHigher = true,
    showNormal = true,
    onLoaded = { /* Ready to show */ }
)

// Show when ready
if (InterstitialAdManager.isReady(highId, normalId)) {
    InterstitialAdManager.showAd(
        activity = this,
        adHigherId = highId,
        adNormalId = normalId,
        showHigher = true,
        showNormal = true,
        onAdDismissed = {
            navigateToNextScreen()
        }
    )
}
```

---

## RewardedAdManager

Singleton manager for Rewarded Ads.

### Methods

```kotlin
object RewardedAdManager {
    fun init(context: Context)

    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    )

    fun showAd(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    )

    fun loadAndShow(
        activity: Activity,
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailedToShow: (String) -> Unit = {}
    )

    fun isReady(adHigherId: String, adNormalId: String): Boolean
    fun getState(adHigherId: String, adNormalId: String): AdState
    fun removeAd(adHigherId: String, adNormalId: String)
    fun clearAll()
}

data class RewardItem(
    val type: String,
    val amount: Int
)
```

### Example

```kotlin
RewardedAdManager.showAd(
    activity = this,
    adHigherId = highId,
    adNormalId = normalId,
    showHigher = true,
    showNormal = true,
    onUserEarnedReward = { reward ->
        grantCoins(reward.amount)
    },
    onAdDismissed = {
        updateUI()
    }
)
```

---

## BannerAdManager

Singleton manager for Banner Ads.

### Methods

```kotlin
object BannerAdManager {
    fun init(context: Context)

    fun loadAd(
        adHigherId: String,
        adNormalId: String,
        showHigher: Boolean,
        showNormal: Boolean,
        adSize: AdSize = AdSize.BANNER,
        onLoaded: (AdView) -> Unit = {},
        onFailed: (String) -> Unit = {}
    )

    fun isReady(adHigherId: String, adNormalId: String): Boolean
    fun getState(adHigherId: String, adNormalId: String): AdState
    fun getAdView(adHigherId: String, adNormalId: String): AdView?
    fun removeAd(adHigherId: String, adNormalId: String)
    fun clearAll()
}
```

---

## AdState

Sealed interface representing ad lifecycle states.

```kotlin
sealed interface AdState {
    data object NotLoaded : AdState
    data object Loading : AdState
    data object Loaded : AdState
    data object Showing : AdState
    data class Failed(val message: String, val retryCount: Int = 0) : AdState
}
```

### Example

```kotlin
when (val state = NativeAdManager.getState(highId, normalId)) {
    is AdState.NotLoaded -> showPlaceholder()
    is AdState.Loading -> showLoading()
    is AdState.Loaded -> showAd()
    is AdState.Showing -> { /* Ad is visible */ }
    is AdState.Failed -> showError(state.message)
}
```

---

## Composables

### NativeAdView

```kotlin
@Composable
fun NativeAdView(
    adHigherId: String,
    adNormalId: String,
    showHigher: Boolean,
    showNormal: Boolean,
    modifier: Modifier = Modifier,
    size: NativeAdSize = NativeAdSize.MEDIUM,  // SMALL (80dp) or MEDIUM (280dp)
    retryConfig: RetryConfig? = null,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (String) -> Unit = {}
)
```

### BannerAdView

```kotlin
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
)
```

---

# OpenApp SDK

## OpenAppConfig

Singleton configuration object using builder pattern.

### Methods

```kotlin
object OpenAppConfig {
    /**
     * Initialize configuration with builder
     *
     * @param builder Configuration lambda
     */
    fun init(builder: Builder.() -> Unit)

    /**
     * Get configuration instance
     */
    fun get(): OpenAppConfig

    // Configuration getters
    val splashConfig: SplashConfig
    val language1Config: LanguageConfig
    val language2Config: LanguageConfig
    val onboarding1Config: OnboardingConfig
    val onboarding2Config: OnboardingConfig
    val prepareDataConfig: PrepareDataConfig
    val themeConfig: ThemeConfig
    val adPlaceholderConfig: AdPlaceholderConfig
}
```

### Config Classes

```kotlin
data class SplashConfig(
    val delayTime: Int = 5000  // milliseconds
)

data class LanguageConfig(
    val adIdHigh: String = "",
    val adIdNormal: String = "",
    val showAdHigh: Boolean = true,
    val showAdNormal: Boolean = true
)

data class OnboardingConfig(
    val adIdHigh: String = "",
    val adIdNormal: String = "",
    val showAdHigh: Boolean = true,
    val showAdNormal: Boolean = true
)

data class PrepareDataConfig(
    val delayTime: Int = 5000,
    val adIdHigh: String = "",
    val adIdNormal: String = "",
    val showAdHigh: Boolean = true,
    val showAdNormal: Boolean = true
)
```

### Example

```kotlin
OpenAppConfig.init {
    splashConfig {
        delayTime(5000)
    }

    language1Config {
        adIdHigh("ca-app-pub-xxx/high")
        adIdNormal("ca-app-pub-xxx/normal")
        showAdHigh(true)
        showAdNormal(true)
    }

    language2Config {
        adIdHigh("ca-app-pub-xxx/high")
        adIdNormal("ca-app-pub-xxx/normal")
        showAdHigh(true)
        showAdNormal(true)
    }

    onboarding1Config {
        adIdHigh("ca-app-pub-xxx/high")
        adIdNormal("ca-app-pub-xxx/normal")
        showAdHigh(true)
        showAdNormal(true)
    }

    onboarding2Config {
        adIdHigh("ca-app-pub-xxx/high")
        adIdNormal("ca-app-pub-xxx/normal")
        showAdHigh(true)
        showAdNormal(true)
    }

    prepareDataConfig {
        delayTime(5000)
        adIdHigh("ca-app-pub-xxx/interstitial_high")
        adIdNormal("ca-app-pub-xxx/interstitial_normal")
        showAdHigh(true)
        showAdNormal(true)
    }
}
```

---

## OpenAppNavigation

Composable for the full onboarding navigation flow.

```kotlin
@Composable
fun OpenAppNavigation(
    onNavigateToMain: () -> Unit
)
```

### Navigation Routes

```kotlin
sealed interface OpenAppRoute : Serializable {
    data object Splash : OpenAppRoute
    data object Language1 : OpenAppRoute
    data class Language2(val selectedCode: String) : OpenAppRoute
    data object OnBoarding1 : OpenAppRoute
    data object OnBoarding2 : OpenAppRoute
    data object PrepareData : OpenAppRoute
}
```

### Example

```kotlin
setContent {
    OpenAppNavigation(
        onNavigateToMain = {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    )
}
```

---

## UserPreferences

DataStore-based user preferences singleton.

### Methods

```kotlin
class UserPreferences {
    companion object {
        fun getInstance(context: Context): UserPreferences
    }

    // Old User flag (skip onboarding)
    val isOldUserFlow: Flow<Boolean>
    suspend fun isOldUser(): Boolean
    suspend fun setIsOldUser(value: Boolean)

    // Selected Language
    val selectedLanguageFlow: Flow<String?>
    suspend fun getSelectedLanguage(): String?
    suspend fun setSelectedLanguage(languageCode: String)

    // Onboarding Completed
    val onboardingCompletedFlow: Flow<Boolean>
    suspend fun setOnboardingCompleted(value: Boolean)
}
```

### Example

```kotlin
val prefs = UserPreferences.getInstance(context)

// Check if user completed onboarding
if (prefs.isOldUser()) {
    navigateToMain()
} else {
    navigateToOnboarding()
}

// Save language preference
prefs.setSelectedLanguage("vi")

// Mark onboarding complete
prefs.setIsOldUser(true)
prefs.setOnboardingCompleted(true)
```

---

## LanguageManager

Utility object for locale management.

### Methods

```kotlin
object LanguageManager {
    /**
     * Apply language to context (returns new context)
     *
     * @param context Current context
     * @param languageCode Language code (e.g., "en", "vi", "pt_BR")
     * @return New context with applied locale
     */
    fun applyLanguage(context: Context, languageCode: String): Context

    /**
     * Apply language to activity and update configuration
     *
     * @param activity Activity to update
     * @param languageCode Language code
     */
    fun applyLanguageAndRecreate(activity: Activity, languageCode: String)

    /**
     * Get current locale from context
     *
     * @return Current Locale
     */
    fun getCurrentLocale(context: Context): Locale
}
```

### Supported Languages

| Code | Language |
|------|----------|
| `en` | English |
| `pt_BR` | Portuguese (Brazil) |
| `es_VE` | Spanish (Venezuela) |
| `ru` | Russian |
| `zh` | Chinese |
| `vi` | Vietnamese |
| `th` | Thai |
| `id` | Indonesian |
| `de` | German |
| `pt` | Portuguese |
| `ko` | Korean |
| `ja` | Japanese |

### Example

```kotlin
// Apply language to activity
LanguageManager.applyLanguageAndRecreate(activity, "vi")

// Get current locale
val locale = LanguageManager.getCurrentLocale(context)
```

---

## Language Model

```kotlin
data class Language(
    val code: String,           // e.g., "en", "pt_BR"
    val name: String,           // e.g., "English", "Brazil"
    val locale: Locale,         // Java Locale object
    @DrawableRes val flagRes: Int  // Flag drawable resource
)

object LanguageList {
    val languages: List<Language>

    fun getByCode(code: String): Language?
}
```

---

## Key Constants

### Ad Key Generation

All ad managers use a consistent key pattern:

```kotlin
fun createAdKey(adHigherId: String, adNormalId: String): String = "$adHigherId|$adNormalId"
```

### Thread Safety

All ad managers use `ConcurrentHashMap` for thread-safe storage.

---

## Error Handling

### Common Error Messages

| Error | Cause |
|-------|-------|
| `"Both ads disabled"` | showHigher and showNormal both false |
| `"No ad ready to show"` | Ad not loaded before show attempt |
| `"Higher ad not allowed"` | showHigher=false but higher ad loaded |
| `"Normal ad not allowed"` | showNormal=false but normal ad loaded |
| `"Both ads failed to load"` | High and normal both failed |
| `"All retries exhausted"` | Native ad retry limit reached |

---

## Best Practices

1. **Initialize early**: Call `init()` in `Application.onCreate()`
2. **Preload ads**: Load ads before they're needed
3. **Handle failures**: Always provide `onFailed` callbacks
4. **Cleanup**: Call `removeAd()` when done with specific ads
5. **Test with test IDs**: Never use production IDs during development
