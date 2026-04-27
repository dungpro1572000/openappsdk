plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    flavorDimensions += "version"

    // Test Ad Unit IDs
    val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    val TEST_INTER_ID = "ca-app-pub-3940256099942544/1033173712"
    val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"

    productFlavors {
        create("appDev") {
            dimension = "version"
            buildConfigField("Boolean", "IS_DEV", "true")
            // Splash Ads
            buildConfigField("String", "ADS_SPL_BANNER", "\"$TEST_BANNER_ID\"")
            buildConfigField("String", "ADS_SPL_INTER", "\"$TEST_INTER_ID\"")
            // Native Ads - Onboarding screens
            buildConfigField("String", "ADS_ONB_001", "\"$TEST_NATIVE_ID\"")
            buildConfigField("String", "ADS_ONB_002", "\"$TEST_NATIVE_ID\"")
            // Native Ads - PrepareData screen
            buildConfigField("String", "ADS_PREPARE_NATIVE", "\"$TEST_NATIVE_ID\"")
        }
        create("appRelease") {
            dimension = "version"
            buildConfigField("Boolean", "IS_DEV", "false")
            // Splash Ads (TODO: Replace with real ad unit IDs)
            buildConfigField("String", "ADS_SPL_BANNER", "\"ca-app-pub-xxx/splbanner\"")
            buildConfigField("String", "ADS_SPL_INTER", "\"ca-app-pub-xxx/splinter\"")
            // Native Ads - Onboarding screens (TODO: Replace with real ad unit IDs)
            buildConfigField("String", "ADS_ONB_001", "\"ca-app-pub-xxx/onb001\"")
            buildConfigField("String", "ADS_ONB_002", "\"ca-app-pub-xxx/onb002\"")
            // Native Ads - PrepareData screen (TODO: Replace with real ad unit IDs)
            buildConfigField("String", "ADS_PREPARE_NATIVE", "\"ca-app-pub-xxx/preparenative\"")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
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

    implementation(platform(libs.firebase.bom))
    implementation(project(":openappsdk"))
    implementation(libs.our.ads)
}
