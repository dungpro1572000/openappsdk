# Ad Loading Strategy

This document describes the ad loading strategy for the onboarding flow.

---

## 1. Splash Stage (Initialization)

**Duration:** 5 seconds (Custom SplashScreen)

**Load Action:**
1. Start loading `ads_lang_001_high`
2. If `ads_lang_001_high` fails (No fill/Network error), immediately load `ads_lang_001_normal`

**Policy:** SplashScreen must have a clear app logo, no fake system UI elements.

---

## 2. Language Selection Stage

### Language Selection Screen 1

**Show:** Display Native Ad `ads_lang_001` (loaded from Splash screen)

**Policy:** Native ads must have clear "Ad" or "Advertisement" label, not placed near navigation buttons to avoid accidental clicks.

**Next Load Action:** After user interaction, start sequential loading for next screen:
`ads_lang_002_high` -> (if fail) -> `ads_lang_002_normal`

### Language Selection Screen 2

**Show:** Display Native Ad `ads_lang_002` (loaded from previous screen)

**Next Load Action:** Start sequential loading for Onboarding:
`ads_onb_001_high` -> (if fail) -> `ads_onb_001_normal`

---

## 3. Onboarding Stage

### Onboarding Screen 1

**Show:** Display Native Ad `ads_onb_001`

**Next Load Action:** Start sequential loading:
`ads_onb_002_high` -> (if fail) -> `ads_onb_002_normal`

### Onboarding Screen 2

**Show:** Display Native Ad `ads_onb_002`

**Transition Action:** When Onboarding 2 ends, navigate to Prepare Data screen.

---

## 4. Prepare Data Stage (Interstitial Gate)

**Duration:** 5 seconds (Timeout)

**Load Action:**
1. Start loading Interstitial: `ads_inter_001_high`
2. If fail, immediately load `ads_inter_001_normal`

**Display Logic (Policy Compliance):**

**Case 1:** Ad loads within 5s -> Show Interstitial immediately. After user closes ad, navigate to Home screen.

**Case 2:** After 5s, ad hasn't loaded -> Cancel load request and navigate directly to Home (Prevents sudden intrusive ads while user is already in Home - UX violation).

---

## Flow Diagram

```
Splash (5s)
    │
    ├── Preload: ads_lang_001_high -> ads_lang_001_normal
    │
    ▼
Language1Screen
    │
    ├── Show: ads_lang_001
    ├── Preload: ads_lang_002_high -> ads_lang_002_normal
    │
    ▼
Language2Screen
    │
    ├── Show: ads_lang_002
    ├── Preload: ads_onb_001_high -> ads_onb_001_normal
    │
    ▼
OnBoarding1Screen
    │
    ├── Show: ads_onb_001
    ├── Preload: ads_onb_002_high -> ads_onb_002_normal
    │
    ▼
OnBoarding2Screen
    │
    ├── Show: ads_onb_002
    │
    ▼
PrepareDataScreen (5s timeout)
    │
    ├── Load: ads_inter_001_high -> ads_inter_001_normal
    │
    ├── If loaded in 5s: Show Interstitial -> Home
    └── If timeout: Skip ad -> Home
```

---

## Ad Types Summary

| Screen | Ad Type | High Priority ID | Normal Priority ID |
|--------|---------|------------------|-------------------|
| Language 1 | Native | ads_lang_001_high | ads_lang_001_normal |
| Language 2 | Native | ads_lang_002_high | ads_lang_002_normal |
| Onboarding 1 | Native | ads_onb_001_high | ads_onb_001_normal |
| Onboarding 2 | Native | ads_onb_002_high | ads_onb_002_normal |
| Prepare Data | Interstitial | ads_inter_001_high | ads_inter_001_normal |