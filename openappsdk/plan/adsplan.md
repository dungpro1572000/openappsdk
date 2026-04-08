# Ad Load/Show Flow — openappsdk

## Screen Flow

```
Splash → Language1 → Language2 → OnBoarding1 → OnBoarding2 → Prepare → Main
```

---

## Pre-load Chain (each screen pre-loads the NEXT screen's ad)

```
┌─────────────┐   pre-loads    ┌─────────────┐   pre-loads    ┌─────────────┐
│   Splash    │ ──────────────→│  Language1   │ ──────────────→│  Language2   │
│             │  Native ad     │             │  Native ad     │             │
│  (no ad)    │  for Lang1     │  shows its   │  for Lang2     │  shows its   │
│             │                │  native ad   │                │  native ad   │
└─────────────┘                └─────────────┘                └─────────────┘
                                                                    │
                                                          pre-loads │ Native ad
                                                                    │ for OB1
                                                                    ▼
┌─────────────┐                ┌─────────────┐                ┌─────────────┐
│   Prepare   │                │ OnBoarding2  │                │ OnBoarding1  │
│             │  loads own     │             │  ←─────────────│             │
│  shows      │  interstitial  │  shows its   │   pre-loads    │  shows its   │
│  interstitial│  on entry     │  native ad   │   Native ad    │  native ad   │
│  then → Main│                │             │   for OB2      │             │
└─────────────┘                └─────────────┘                └─────────────┘
```

---

## Per-Screen Detail

### 1. SplashScreen
- **Shows:** No ad
- **Pre-loads:** `NativeAdManager.loadAd()` for Language1's native ad
- **When:** `LaunchedEffect(Unit)` on screen entry
- **Then:** Waits `SplashConfig.delayTime` (default 3s), routes to Language1 (new user) or Main (returning user)

### 2. Language1Screen
- **Shows:** Native ad (Medium 280dp) at bottom — `NativeAdView` with high/normal floor
- **Pre-loads:** `NativeAdManager.loadAd()` for Language2's native ad
- **When:** `LaunchedEffect(Unit)` on screen entry
- **Behavior:** Ad area hides completely if ad fails (`isAdFailed` flag)

### 3. Language2Screen
- **Shows:** Native ad (Medium 280dp) at bottom — `NativeAdView` with retry (5s reload)
- **Pre-loads:** `NativeAdManager.loadAd()` for OnBoarding1's native ad
- **When:** `LaunchedEffect(Unit)` on screen entry
- **Behavior:** Ad area hides completely if ad fails

### 4. OnBoarding1Screen
- **Shows:** Native ad (Medium 280dp) — takes 40% of screen height
- **Pre-loads:** `NativeAdManager.loadAd()` for OnBoarding2's native ad
- **When:** `LaunchedEffect(Unit)` on screen entry
- **Behavior:** Ad area always visible (40% weight), shows placeholder on fail

### 5. OnBoarding2Screen
- **Shows:** Native ad (Medium 280dp) — takes 40% of screen height
- **Pre-loads:** Nothing
- **Behavior:** Ad area always visible (40% weight), shows placeholder on fail

### 6. PrepareDataScreen
- **Shows:** Interstitial (fullscreen) ad
- **Pre-loads:** Loads its OWN interstitial on entry (no pre-load from previous screen)
- **Flow:**
  1. `InterstitialAdManager.loadAd()` starts loading
  2. Polls every 100ms for up to 10s waiting for ad to load
  3. If loaded → show interstitial → on dismiss → navigate to Main
  4. If failed/timeout → navigate to Main directly
- **Behavior:** Shows loading spinner while waiting

---

## Ad Strategy: High/Normal Floor

Every ad slot has two IDs: `adIdHigh` (higher CPM) and `adIdNormal` (fallback).

```
Try High Floor ad
  ├── Success → Use it
  └── Fail → Try Normal Floor ad
                ├── Success → Use it
                └── Fail → (retry or give up)
```

Config flags `showAdHigh` / `showAdNormal` can disable either tier.

---

## Ad Config Source

All ad IDs and show flags come from `OpenAppConfig.init { }` in the app:

```kotlin
OpenAppConfig.init {
    language1Config { adIdHigh("ca-app-pub-xxx"); adIdNormal("ca-app-pub-yyy") }
    language2Config { adIdHigh("..."); adIdNormal("...") }
    onboarding1Config { adIdHigh("..."); adIdNormal("...") }
    onboarding2Config { adIdHigh("..."); adIdNormal("...") }
    prepareDataConfig { adIdHigh("..."); adIdNormal("...") }
}
```
