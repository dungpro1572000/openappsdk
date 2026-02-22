plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dungz.ads_open"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.dungz.ads_open"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    flavorDimensions += "version"

    // Test Ad Unit IDs
    val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    val TEST_INTER_ID = "ca-app-pub-3940256099942544/1033173712"

    productFlavors {
        create("appDev") {
            dimension = "version"
            buildConfigField("Boolean", "IS_DEV", "true")
            // Native Ads - Language screens
            buildConfigField("String", "ADS_LANG_001_HIGH", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_LANG_001_NORMAL", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_LANG_002_HIGH", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_LANG_002_NORMAL", "\"$TEST_NATIVE_ID\"")
            // Native Ads - Onboarding screens
            buildConfigField("String", "ADS_ONB_001_HIGH", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_ONB_001_NORMAL", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_ONB_002_HIGH", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_ONB_002_NORMAL", "\"$TEST_NATIVE_ID\"")
            // Interstitial Ads - PrepareData screen
            buildConfigField("String", "ADS_INTER_001_HIGH", "\"$TEST_INTER_ID\"")
            buildConfigField("String", "ADS_INTER_001_NORMAL", "\"$TEST_INTER_ID\"")
        }
        create("appRelease") {
            dimension = "version"
            buildConfigField("Boolean", "IS_DEV", "false")
            // Native Ads - Language screens (TODO: Replace with real ad unit IDs)
            buildConfigField("String", "ADS_LANG_001_HIGH", "\"ca-app-pub-xxx/lang001high\"")
            buildConfigField("String", "ADS_LANG_001_NORMAL", "\"ca-app-pub-xxx/lang001normal\"")
            buildConfigField("String", "ADS_LANG_002_HIGH", "\"ca-app-pub-xxx/lang002high\"")
            buildConfigField("String", "ADS_LANG_002_NORMAL", "\"ca-app-pub-xxx/lang002normal\"")
            // Native Ads - Onboarding screens (TODO: Replace with real ad unit IDs)
            buildConfigField("String", "ADS_ONB_001_HIGH", "\"ca-app-pub-xxx/onb001high\"")
            buildConfigField("String", "ADS_ONB_001_NORMAL", "\"ca-app-pub-xxx/onb001normal\"")
            buildConfigField("String", "ADS_ONB_002_HIGH", "\"ca-app-pub-xxx/onb002high\"")
            buildConfigField("String", "ADS_ONB_002_NORMAL", "\"ca-app-pub-xxx/onb002normal\"")
            // Interstitial Ads - PrepareData screen (TODO: Replace with real ad unit IDs)
            buildConfigField("String", "ADS_INTER_001_HIGH", "\"ca-app-pub-xxx/inter001high\"")
            buildConfigField("String", "ADS_INTER_001_NORMAL", "\"ca-app-pub-xxx/inter001normal\"")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    // Navigation3 style - using custom implementation with AnimatedContent + Serialization
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)


    // Ads module
    implementation(project(":our_ads"))
    implementation(project(":openappsdk"))
}