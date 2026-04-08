# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AdsOpen** is a multi-module Android SDK project providing a configurable onboarding flow (splash → language selection → onboarding → data preparation) with integrated AdMob ads. The SDK (`openappsdk`) is distributed as a library; the `app` module is a demo/sample app.

## Build Commands

```bash
# Build debug APK (dev flavor with test ad IDs)
./gradlew assembleAppDevDebug

# Build release APK
./gradlew assembleAppReleaseRelease

# Build the SDK AAR only
./gradlew buildOpenAppSdk

# Publish SDK to Maven Local (for local testing)
./gradlew publishOpenAppSdkLocal

# Publish SDK to JitPack (tags the submodule)
./gradlew publishOpenAppSdkToJitPack -PsdkVersion=1.0.1

# Clean
./gradlew clean
```

## Module Architecture

- **:app** — Demo app (`com.dungz.ads_open`). Contains `SplashActivity` (launcher), `AppApplication` (initialization), and `MainActivity` (post-onboarding destination). Uses `appDev`/`appRelease` product flavors to switch between test and production ad unit IDs via `BuildConfig`.
- **:openappsdk** — The SDK library (`com.dungz.openappsdk`). Git submodule pointing to `dungpro1572000/openappsdk`. Contains all onboarding screens, navigation, configuration, and data persistence.
- **:our_ads** — Ads wrapper library consumed via JitPack (`com.github.dungpro1572000:our_ads`). Manages native, interstitial, banner, and rewarded ads. Not a local module — referenced as a remote dependency.

## Key Architectural Patterns

**Navigation:** Custom Navigation3-style system (NOT `androidx.navigation`). Uses `@Serializable` sealed interface `OpenAppRoute` with `NavBackStack` (a `SnapshotStateList<OpenAppRoute>`) and `AnimatedContent` for transitions. Route flow: `Splash → Language1 → Language2 → OnBoarding1 → OnBoarding2 → [NativeFullScreen] → PrepareData`.

**Configuration:** `OpenAppConfig` is a builder-pattern singleton holding all SDK configuration (ad IDs, UI customization, screen-specific settings). Initialized in `AppApplication` with BuildConfig values.

**Data persistence:** `UserPreferences` and `RemoteDataPreferences` use AndroidX DataStore. `RemoteDataObject` wraps Firebase Remote Config with DataStore caching for ad visibility flags.

**Ad strategy:** Each screen has high/normal priority ad ID slots. Firebase Remote Config controls per-screen ad visibility with 12 boolean flags. Ads are preloaded on previous screens for smooth transitions.

## Tech Stack

- **UI:** Jetpack Compose with Material 3 (no XML layouts)
- **Language:** Kotlin with Kotlin DSL Gradle files
- **Serialization:** kotlinx.serialization (for type-safe navigation routes)
- **Min SDK:** 24 | **Target SDK:** 36 | **Java:** 11
- **Version catalog:** `gradle/libs.versions.toml`

## Submodule

The `openappsdk/` directory is a git submodule. After cloning, run:
```bash
git submodule update --init --recursive
```
