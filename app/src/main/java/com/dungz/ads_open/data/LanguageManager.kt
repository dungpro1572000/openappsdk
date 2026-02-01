package com.dungz.ads_open.data

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LanguageManager {

    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = parseLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLanguageAndRecreate(activity: Activity, languageCode: String) {
        val locale = parseLocale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        @Suppress("DEPRECATION")
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    private fun parseLocale(languageCode: String): Locale {
        return if (languageCode.contains("_")) {
            val parts = languageCode.split("_")
            Locale(parts[0], parts[1])
        } else {
            Locale(languageCode)
        }
    }

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
}
