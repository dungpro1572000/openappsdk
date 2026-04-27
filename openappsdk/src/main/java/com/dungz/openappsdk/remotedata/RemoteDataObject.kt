package com.dungz.openappsdk.remotedata

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object RemoteDataObject {

    private const val TAG = "RemoteDataObject"

    // Remote Config keys - Ad IDs
    private const val KEY_AD_SPL_BANNER_ID = "ad_spl_banner_id"
    private const val KEY_AD_SPL_INTER_ID = "ad_spl_inter_id"
    private const val KEY_AD_ONB1_ID = "ad_onb1_id"
    private const val KEY_AD_ONB2_ID = "ad_onb2_id"
    private const val KEY_AD_PREPARE_NATIVE_ID = "ad_prepare_native_id"

    // Remote Config keys - Show flags
    private const val KEY_SHOW_AD_SPL_BANNER = "show_ad_spl_banner"
    private const val KEY_SHOW_AD_SPL_INTER = "show_ad_spl_inter"
    private const val KEY_SHOW_AD_ONB1 = "show_ad_onb1"
    private const val KEY_SHOW_AD_ONB2 = "show_ad_onb2"
    private const val KEY_SHOW_AD_PREPARE_NATIVE = "show_ad_prepare_native"

    // In-memory cached values - Ad IDs
    var adSplBannerId: String = ""; private set
    var adSplInterId: String = ""; private set
    var adOnb1Id: String = ""; private set
    var adOnb2Id: String = ""; private set
    var adPrepareNativeId: String = ""; private set

    // In-memory cached values - Show flags
    var showAdSplBanner: Boolean = true; private set
    var showAdSplInter: Boolean = true; private set
    var showAdOnb1: Boolean = true; private set
    var showAdOnb2: Boolean = true; private set
    var showAdPrepareNative: Boolean = true; private set

    fun init(context: Context) {
        val preferences = RemoteDataPreferences.getInstance(context)

        // Load cached values from DataStore (blocking, so they're available immediately)
        runBlocking {
            val cached = preferences.loadAll()
            adSplBannerId = cached.adSplBannerId
            adSplInterId = cached.adSplInterId
            adOnb1Id = cached.adOnb1Id
            adOnb2Id = cached.adOnb2Id
            adPrepareNativeId = cached.adPrepareNativeId
            showAdSplBanner = cached.showAdSplBanner
            showAdSplInter = cached.showAdSplInter
            showAdOnb1 = cached.showAdOnb1
            showAdOnb2 = cached.showAdOnb2
            showAdPrepareNative = cached.showAdPrepareNative
        }
        Log.d(TAG, "Loaded cached values: splBanner=$adSplBannerId, splInter=$adSplInterId")

        // Set up Firebase Remote Config
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val defaults = mapOf<String, Any>(
            KEY_AD_SPL_BANNER_ID to "",
            KEY_AD_SPL_INTER_ID to "",
            KEY_AD_ONB1_ID to "",
            KEY_AD_ONB2_ID to "",
            KEY_AD_PREPARE_NATIVE_ID to "",
            KEY_SHOW_AD_SPL_BANNER to true,
            KEY_SHOW_AD_SPL_INTER to true,
            KEY_SHOW_AD_ONB1 to true,
            KEY_SHOW_AD_ONB2 to true,
            KEY_SHOW_AD_PREPARE_NATIVE to true
        )

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (isDebugBuild(context)) 0L else 43200L
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)

        // Fetch and activate (async)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Remote Config fetch and activate succeeded")
                val flags = RemoteDataPreferences.RemoteAdFlags(
                    adSplBannerId = remoteConfig.getString(KEY_AD_SPL_BANNER_ID),
                    adSplInterId = remoteConfig.getString(KEY_AD_SPL_INTER_ID),
                    adOnb1Id = remoteConfig.getString(KEY_AD_ONB1_ID),
                    adOnb2Id = remoteConfig.getString(KEY_AD_ONB2_ID),
                    adPrepareNativeId = remoteConfig.getString(KEY_AD_PREPARE_NATIVE_ID),
                    showAdSplBanner = remoteConfig.getBoolean(KEY_SHOW_AD_SPL_BANNER),
                    showAdSplInter = remoteConfig.getBoolean(KEY_SHOW_AD_SPL_INTER),
                    showAdOnb1 = remoteConfig.getBoolean(KEY_SHOW_AD_ONB1),
                    showAdOnb2 = remoteConfig.getBoolean(KEY_SHOW_AD_ONB2),
                    showAdPrepareNative = remoteConfig.getBoolean(KEY_SHOW_AD_PREPARE_NATIVE)
                )
                CoroutineScope(Dispatchers.IO).launch {
                    preferences.saveAll(flags)
                    Log.d(TAG, "Saved remote values to DataStore cache")
                }
            } else {
                Log.w(TAG, "Remote Config fetch failed", task.exception)
            }
        }
    }

    private fun isDebugBuild(context: Context): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
