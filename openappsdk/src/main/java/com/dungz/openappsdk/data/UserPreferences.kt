package com.dungz.openappsdk.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val IS_OLD_USER = booleanPreferencesKey("is_old_user")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Is Old User
    val isOldUserFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_OLD_USER] ?: false
    }

    suspend fun isOldUser(): Boolean = isOldUserFlow.first()

    suspend fun setIsOldUser(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_OLD_USER] = value
        }
    }

    // Selected Language
    val selectedLanguageFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_LANGUAGE]
    }

    suspend fun getSelectedLanguage(): String? = selectedLanguageFlow.first()

    suspend fun setSelectedLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_LANGUAGE] = languageCode
        }
    }

    // Onboarding Completed
    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = value
        }
    }
}
