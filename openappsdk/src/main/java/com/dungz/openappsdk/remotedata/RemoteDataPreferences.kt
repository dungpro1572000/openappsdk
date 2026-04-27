package com.dungz.openappsdk.remotedata

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.remoteDataStore: DataStore<Preferences> by preferencesDataStore(name = "remote_data_preferences")

class RemoteDataPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        // Ad ID keys (String)
        val KEY_AD_SPL_BANNER_ID = stringPreferencesKey("ad_spl_banner_id")
        val KEY_AD_SPL_INTER_ID = stringPreferencesKey("ad_spl_inter_id")
        val KEY_AD_ONB1_ID = stringPreferencesKey("ad_onb1_id")
        val KEY_AD_ONB2_ID = stringPreferencesKey("ad_onb2_id")
        val KEY_AD_PREPARE_NATIVE_ID = stringPreferencesKey("ad_prepare_native_id")

        // Show ad flags (Boolean)
        val KEY_SHOW_AD_SPL_BANNER = booleanPreferencesKey("show_ad_spl_banner")
        val KEY_SHOW_AD_SPL_INTER = booleanPreferencesKey("show_ad_spl_inter")
        val KEY_SHOW_AD_ONB1 = booleanPreferencesKey("show_ad_onb1")
        val KEY_SHOW_AD_ONB2 = booleanPreferencesKey("show_ad_onb2")
        val KEY_SHOW_AD_PREPARE_NATIVE = booleanPreferencesKey("show_ad_prepare_native")

        @Volatile
        private var INSTANCE: RemoteDataPreferences? = null

        fun getInstance(context: Context): RemoteDataPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RemoteDataPreferences(context.applicationContext.remoteDataStore).also { INSTANCE = it }
            }
        }
    }

    data class RemoteAdFlags(
        val adSplBannerId: String = "",
        val adSplInterId: String = "",
        val adOnb1Id: String = "",
        val adOnb2Id: String = "",
        val adPrepareNativeId: String = "",
        val showAdSplBanner: Boolean = true,
        val showAdSplInter: Boolean = true,
        val showAdOnb1: Boolean = true,
        val showAdOnb2: Boolean = true,
        val showAdPrepareNative: Boolean = true
    )

    suspend fun loadAll(): RemoteAdFlags {
        val prefs = dataStore.data.first()
        return RemoteAdFlags(
            adSplBannerId = prefs[KEY_AD_SPL_BANNER_ID] ?: "",
            adSplInterId = prefs[KEY_AD_SPL_INTER_ID] ?: "",
            adOnb1Id = prefs[KEY_AD_ONB1_ID] ?: "",
            adOnb2Id = prefs[KEY_AD_ONB2_ID] ?: "",
            adPrepareNativeId = prefs[KEY_AD_PREPARE_NATIVE_ID] ?: "",
            showAdSplBanner = prefs[KEY_SHOW_AD_SPL_BANNER] ?: true,
            showAdSplInter = prefs[KEY_SHOW_AD_SPL_INTER] ?: true,
            showAdOnb1 = prefs[KEY_SHOW_AD_ONB1] ?: true,
            showAdOnb2 = prefs[KEY_SHOW_AD_ONB2] ?: true,
            showAdPrepareNative = prefs[KEY_SHOW_AD_PREPARE_NATIVE] ?: true
        )
    }

    suspend fun saveAll(flags: RemoteAdFlags) {
        dataStore.edit { prefs ->
            prefs[KEY_AD_SPL_BANNER_ID] = flags.adSplBannerId
            prefs[KEY_AD_SPL_INTER_ID] = flags.adSplInterId
            prefs[KEY_AD_ONB1_ID] = flags.adOnb1Id
            prefs[KEY_AD_ONB2_ID] = flags.adOnb2Id
            prefs[KEY_AD_PREPARE_NATIVE_ID] = flags.adPrepareNativeId
            prefs[KEY_SHOW_AD_SPL_BANNER] = flags.showAdSplBanner
            prefs[KEY_SHOW_AD_SPL_INTER] = flags.showAdSplInter
            prefs[KEY_SHOW_AD_ONB1] = flags.showAdOnb1
            prefs[KEY_SHOW_AD_ONB2] = flags.showAdOnb2
            prefs[KEY_SHOW_AD_PREPARE_NATIVE] = flags.showAdPrepareNative
        }
    }
}
