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
    private const val KEY_AD_LANG1_ID = "ad_lang1_id"
    private const val KEY_AD_LANG2_ID = "ad_lang2_id"
    private const val KEY_AD_ONB1_ID = "ad_onb1_id"
    private const val KEY_AD_ONB2_ID = "ad_onb2_id"
    private const val KEY_AD_INTER_ID = "ad_inter_id"
    private const val KEY_AD_NATIVE_FULL_ID = "ad_native_full_id"

    // Remote Config keys - Show flags
    private const val KEY_SHOW_AD_LANG1 = "show_ad_lang1"
    private const val KEY_SHOW_AD_LANG2 = "show_ad_lang2"
    private const val KEY_SHOW_AD_ONB1 = "show_ad_onb1"
    private const val KEY_SHOW_AD_ONB2 = "show_ad_onb2"
    private const val KEY_SHOW_AD_INTER = "show_ad_inter"
    private const val KEY_SHOW_AD_NATIVE_FULL = "show_ad_native_full"

    // In-memory cached values - Ad IDs
    var adLang1Id: String = ""; private set
    var adLang2Id: String = ""; private set
    var adOnb1Id: String = ""; private set
    var adOnb2Id: String = ""; private set
    var adInterId: String = ""; private set
    var adNativeFullId: String = ""; private set

    // In-memory cached values - Show flags
    var showAdLang1: Boolean = true; private set
    var showAdLang2: Boolean = true; private set
    var showAdOnb1: Boolean = true; private set
    var showAdOnb2: Boolean = true; private set
    var showAdInter: Boolean = true; private set
    var showAdNativeFull: Boolean = true; private set

    fun init(context: Context) {
        val preferences = RemoteDataPreferences.getInstance(context)

        // Load cached values from DataStore (blocking, so they're available immediately)
        runBlocking {
            val cached = preferences.loadAll()
            adLang1Id = cached.adLang1Id
            adLang2Id = cached.adLang2Id
            adOnb1Id = cached.adOnb1Id
            adOnb2Id = cached.adOnb2Id
            adInterId = cached.adInterId
            adNativeFullId = cached.adNativeFullId
            showAdLang1 = cached.showAdLang1
            showAdLang2 = cached.showAdLang2
            showAdOnb1 = cached.showAdOnb1
            showAdOnb2 = cached.showAdOnb2
            showAdInter = cached.showAdInter
            showAdNativeFull = cached.showAdNativeFull
        }
        Log.d(TAG, "Loaded cached values: lang1=$adLang1Id, lang2=$adLang2Id")

        // Set up Firebase Remote Config
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val defaults = mapOf<String, Any>(
            KEY_AD_LANG1_ID to "",
            KEY_AD_LANG2_ID to "",
            KEY_AD_ONB1_ID to "",
            KEY_AD_ONB2_ID to "",
            KEY_AD_INTER_ID to "",
            KEY_AD_NATIVE_FULL_ID to "",
            KEY_SHOW_AD_LANG1 to true,
            KEY_SHOW_AD_LANG2 to true,
            KEY_SHOW_AD_ONB1 to true,
            KEY_SHOW_AD_ONB2 to true,
            KEY_SHOW_AD_INTER to true,
            KEY_SHOW_AD_NATIVE_FULL to true
        )

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (isDebugBuild(context)) 0L else 43200L // 12 hours
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)

        // Fetch and activate (async) — values apply on next launch via DataStore cache
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Remote Config fetch and activate succeeded")
                val flags = RemoteDataPreferences.RemoteAdFlags(
                    adLang1Id = remoteConfig.getString(KEY_AD_LANG1_ID),
                    adLang2Id = remoteConfig.getString(KEY_AD_LANG2_ID),
                    adOnb1Id = remoteConfig.getString(KEY_AD_ONB1_ID),
                    adOnb2Id = remoteConfig.getString(KEY_AD_ONB2_ID),
                    adInterId = remoteConfig.getString(KEY_AD_INTER_ID),
                    adNativeFullId = remoteConfig.getString(KEY_AD_NATIVE_FULL_ID),
                    showAdLang1 = remoteConfig.getBoolean(KEY_SHOW_AD_LANG1),
                    showAdLang2 = remoteConfig.getBoolean(KEY_SHOW_AD_LANG2),
                    showAdOnb1 = remoteConfig.getBoolean(KEY_SHOW_AD_ONB1),
                    showAdOnb2 = remoteConfig.getBoolean(KEY_SHOW_AD_ONB2),
                    showAdInter = remoteConfig.getBoolean(KEY_SHOW_AD_INTER),
                    showAdNativeFull = remoteConfig.getBoolean(KEY_SHOW_AD_NATIVE_FULL)
                )
                // Save to DataStore for next launch
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
