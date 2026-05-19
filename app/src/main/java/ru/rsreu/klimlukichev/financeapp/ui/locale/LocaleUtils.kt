package ru.rsreu.klimlukichev.financeapp.ui.locale

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import ru.rsreu.klimlukichev.financeapp.data.local.ThemeSettingsPreferences
import ru.rsreu.klimlukichev.financeapp.data.local.themeSettingsDataStore
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun Context.wrapWithStoredAppLocale(): Context {
    val languageTag = readStoredLanguageTag()
    val locale = Locale.forLanguageTag(languageTag)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

fun Context.readStoredLanguageTag(): String =
    runBlocking {
        themeSettingsDataStore.data.first()[ThemeSettingsPreferences.LANGUAGE_TAG]
            ?: ThemeSettingsPreferences.DEFAULT_LANGUAGE_TAG
    }

tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

const val DEFAULT_LANGUAGE_TAG = ThemeSettingsPreferences.DEFAULT_LANGUAGE_TAG
