package com.nightcallaudio.ui.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLanguageManager {
    const val SPANISH = "es"
    const val ENGLISH = "en"
    private const val PREFERENCES = "nightcall_language"
    private const val KEY_LANGUAGE = "language"

    fun selectedLanguage(context: Context): String = context
        .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        .getString(KEY_LANGUAGE, SPANISH)
        .takeIf { it == SPANISH || it == ENGLISH } ?: SPANISH

    fun localizedContext(context: Context): Context {
        val locale = Locale.forLanguageTag(selectedLanguage(context))
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    fun selectLanguage(activity: Activity, language: String) {
        require(language == SPANISH || language == ENGLISH)
        if (selectedLanguage(activity) == language) return
        activity.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, language).apply()
        activity.recreate()
    }
}
