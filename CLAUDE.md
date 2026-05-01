# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AdsOpen** is a multi-module Android SDK project providing a configurable onboarding flow (splash → language selection → onboarding → data preparation) with integrated AdMob ads. The SDK (`openappsdk`) is distributed as a library via JitPack; the `app` module is a demo/sample app.

## Build Commands

```bash
# Build debug APK (dev flavor with test ad IDs)
./gradlew assembleAppDevDebug

# Build release APK (production ad IDs — requires replacing TODOs in app/build.gradle.kts)
./gradlew assembleAppReleaseRelease

# Build the SDK AAR only
./gradlew buildOpenAppSdk

# Publish SDK to Maven Local (for local testing)
./gradlew publishOpenAppSdkLocal

# Publish SDK to JitPack (tags the submodule)
./gradlew publishOpenAppSdkToJitPack -PsdkVersion=1.0.1

# Clean all modules
./gradlew clean
```

No automated tests exist yet. The project includes test runner config but no test files.

## Module Architecture

- **:app** — Demo app (`com.dungz.ads_open`). Uses `appDev`/`appRelease` product flavors to switch between test and production ad unit IDs via `BuildConfig`. Key files: `SplashActivity` (launcher with `OpenAppNavigation`), `AppApplication` (initialization hub), `MainActivity` (post-onboarding destination).
- **:openappsdk** — The SDK library (`com.dungz.openappsdk`). Contains all onboarding screens, navigation, configuration, ad integration, and data persistence. Published to JitPack as `com.github.dungpro1572000:openappsdk`.
- **:our_ads** — Ads wrapper library. Referenced as a remote JitPack dependency (`com.github.dungpro1572000:our_ads:1.2.5` in openappsdk, version catalog entry in app). The `our_ads/` directory is included in `settings.gradle.kts` but does not exist locally — build will warn about this.

## Key Source Files

| File | Purpose |
|---|---|
| `openappsdk/.../navigation/OpenAppNavigation.kt` (620 lines) | Custom navigation system: `OpenAppRoute` sealed interface, `NavBackStack`, `AnimatedContent` transitions |
| `openappsdk/.../OpenAppConfig.kt` (340 lines) | Builder-pattern singleton for all SDK configuration (ad IDs, UI customization, per-screen settings) |
| `openappsdk/.../remotedata/RemoteDataObject.kt` | Firebase Remote Config + DataStore caching singleton (12 remote flags controlling ad visibility) |
| `openappsdk/.../data/UserPreferences.kt` | DataStore for user state: `isOldUser`, `selectedLanguage`, `onboardingCompleted` |
| `openappsdk/.../data/RemoteDataPreferences.kt` | DataStore for cached Remote Config values (12 fields: 6 ad IDs + 6 show flags) |
| `app/.../AppApplication.kt` | Initialization order: AdsInitializer → NativeAdManager → InterstitialAdManager → RemoteDataObject → OpenAppConfig |

## Navigation System

Custom implementation (NOT `androidx.navigation`). Uses `@Serializable` sealed interface `OpenAppRoute` with a `NavBackStack` (`SnapshotStateList<OpenAppRoute>`) and `AnimatedContent` for transitions.

**Route flow:** `Splash → Language1 → Language2 → OnBoarding1 → OnBoarding2 → [NativeFullScreen] → PrepareData`

**Transition rules:**
- Splash, NativeFullScreen, PrepareData: fadeIn/fadeOut
- Language1 → Language2: instant (no animation)
- All other forward: slideInHorizontally + fadeIn
- Backward: slideOutHorizontally + fadeOut

## Ad Preloading Strategy

Each screen preloads ads for the **next** screen in its `LaunchedEffect`:
- Splash → preloads Language1 ads
- Language1 → preloads Language2 ads
- Language2 → preloads OnBoarding1 ads
- OnBoarding1 → preloads OnBoarding2 ads
- PrepareData → loads interstitial with 5-second timeout (shows if ready, skips otherwise)

Each screen has high/normal priority ad ID slots. Firebase Remote Config controls per-screen visibility with 12 boolean flags (`show_ad_lang1`, `show_ad_lang2`, `show_ad_onb1`, `show_ad_onb2`, `show_ad_inter`, `show_ad_native_full`).

## Data Persistence

Two separate DataStore instances (not SharedPreferences):
- `"user_preferences"` — `UserPreferences` singleton: onboarding state, language selection, old-user flag
- `"remote_data_preferences"` — `RemoteDataPreferences` singleton: cached Firebase Remote Config values for offline use

`RemoteDataObject.init()` loads cached DataStore values synchronously (`runBlocking`), then fetches Remote Config async in background. On success, saves updated values to DataStore for next launch.

## UI Customization

All UI customization is done through `OpenAppConfig` builder — each screen config (`SplashConfig`, `LanguageConfig`, `PrepareDataConfig`) includes both ad/behavior settings and UI overrides (colors, text, composable overrides). `OpenAppConfig.AdPlaceholderConfig` controls ad loading state appearance (shimmer colors, corner radius, custom placeholder composables).

## Tech Stack

- **UI:** Jetpack Compose with Material 3 (no XML layouts, except `native_ad_medium.xml` for AdMob native ad rendering)
- **Language:** Kotlin with Kotlin DSL Gradle files
- **Serialization:** kotlinx.serialization (for type-safe navigation routes)
- **Min SDK:** 24 | **Target SDK:** 36 (app) / 35 (SDK) | **Java:** 11
- **Version catalog:** `gradle/libs.versions.toml`
- **JitPack builds require:** OpenJDK 17 (per `openappsdk/jitpack.yml`)

## Submodule

The `openappsdk/` directory is a git submodule. After cloning, run:
```bash
git submodule update --init --recursive
```

## Documentation

Detailed docs in `docs/`:
- `QUICK_START.md` — 5-minute setup guide with test ad IDs
- `API_REFERENCE.md` — Complete API documentation for both our_ads and openappsdk
- `AdsStragy.md` — Ad loading flow diagram and policy compliance notes
- `OPEN_APP_MODULE_SETUP.md` — Comprehensive SDK module setup guide
- `ADS_MODULE_SETUP.md` — Ads module architecture and integration guide
