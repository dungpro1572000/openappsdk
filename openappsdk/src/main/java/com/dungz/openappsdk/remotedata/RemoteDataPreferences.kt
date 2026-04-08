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
        val KEY_AD_LANG1_ID = stringPreferencesKey("ad_lang1_id")
        val KEY_AD_LANG2_ID = stringPreferencesKey("ad_lang2_id")
        val KEY_AD_ONB1_ID = stringPreferencesKey("ad_onb1_id")
        val KEY_AD_ONB2_ID = stringPreferencesKey("ad_onb2_id")
        val KEY_AD_INTER_ID = stringPreferencesKey("ad_inter_id")
        val KEY_AD_NATIVE_FULL_ID = stringPreferencesKey("ad_native_full_id")

        // Show ad flags (Boolean)
        val KEY_SHOW_AD_LANG1 = booleanPreferencesKey("show_ad_lang1")
        val KEY_SHOW_AD_LANG2 = booleanPreferencesKey("show_ad_lang2")
        val KEY_SHOW_AD_ONB1 = booleanPreferencesKey("show_ad_onb1")
        val KEY_SHOW_AD_ONB2 = booleanPreferencesKey("show_ad_onb2")
        val KEY_SHOW_AD_INTER = booleanPreferencesKey("show_ad_inter")
        val KEY_SHOW_AD_NATIVE_FULL = booleanPreferencesKey("show_ad_native_full")

        @Volatile
        private var INSTANCE: RemoteDataPreferences? = null

        fun getInstance(context: Context): RemoteDataPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RemoteDataPreferences(context.applicationContext.remoteDataStore).also { INSTANCE = it }
            }
        }
    }

    data class RemoteAdFlags(
        val adLang1Id: String = "",
        val adLang2Id: String = "",
        val adOnb1Id: String = "",
        val adOnb2Id: String = "",
        val adInterId: String = "",
        val adNativeFullId: String = "",
        val showAdLang1: Boolean = true,
        val showAdLang2: Boolean = true,
        val showAdOnb1: Boolean = true,
        val showAdOnb2: Boolean = true,
        val showAdInter: Boolean = true,
        val showAdNativeFull: Boolean = true
    )

    suspend fun loadAll(): RemoteAdFlags {
        val prefs = dataStore.data.first()
        return RemoteAdFlags(
            adLang1Id = prefs[KEY_AD_LANG1_ID] ?: "",
            adLang2Id = prefs[KEY_AD_LANG2_ID] ?: "",
            adOnb1Id = prefs[KEY_AD_ONB1_ID] ?: "",
            adOnb2Id = prefs[KEY_AD_ONB2_ID] ?: "",
            adInterId = prefs[KEY_AD_INTER_ID] ?: "",
            adNativeFullId = prefs[KEY_AD_NATIVE_FULL_ID] ?: "",
            showAdLang1 = prefs[KEY_SHOW_AD_LANG1] ?: true,
            showAdLang2 = prefs[KEY_SHOW_AD_LANG2] ?: true,
            showAdOnb1 = prefs[KEY_SHOW_AD_ONB1] ?: true,
            showAdOnb2 = prefs[KEY_SHOW_AD_ONB2] ?: true,
            showAdInter = prefs[KEY_SHOW_AD_INTER] ?: true,
            showAdNativeFull = prefs[KEY_SHOW_AD_NATIVE_FULL] ?: true
        )
    }

    suspend fun saveAll(flags: RemoteAdFlags) {
        dataStore.edit { prefs ->
            prefs[KEY_AD_LANG1_ID] = flags.adLang1Id
            prefs[KEY_AD_LANG2_ID] = flags.adLang2Id
            prefs[KEY_AD_ONB1_ID] = flags.adOnb1Id
            prefs[KEY_AD_ONB2_ID] = flags.adOnb2Id
            prefs[KEY_AD_INTER_ID] = flags.adInterId
            prefs[KEY_AD_NATIVE_FULL_ID] = flags.adNativeFullId
            prefs[KEY_SHOW_AD_LANG1] = flags.showAdLang1
            prefs[KEY_SHOW_AD_LANG2] = flags.showAdLang2
            prefs[KEY_SHOW_AD_ONB1] = flags.showAdOnb1
            prefs[KEY_SHOW_AD_ONB2] = flags.showAdOnb2
            prefs[KEY_SHOW_AD_INTER] = flags.showAdInter
            prefs[KEY_SHOW_AD_NATIVE_FULL] = flags.showAdNativeFull
        }
    }
}
