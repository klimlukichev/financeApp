package ru.rsreu.klimlukichev.financeapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.android.ext.android.inject
import ru.rsreu.klimlukichev.financeapp.domain.repository.ThemeRepository
import ru.rsreu.klimlukichev.financeapp.ui.home.HomeScreen
import ru.rsreu.klimlukichev.financeapp.ui.locale.DEFAULT_LANGUAGE_TAG
import ru.rsreu.klimlukichev.financeapp.ui.locale.readStoredLanguageTag
import ru.rsreu.klimlukichev.financeapp.ui.locale.wrapWithStoredAppLocale
import ru.rsreu.klimlukichev.financeapp.ui.theme.FinanceAppTheme

class MainActivity : ComponentActivity() {

    private val themeRepository: ThemeRepository by inject()

    private var appliedLanguageTag: String? = null

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        // The app can still be used if notifications are disabled.
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrapWithStoredAppLocale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appliedLanguageTag = readStoredLanguageTag()
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        setContent {
            val isDarkThemeEnabled = themeRepository.observeDarkThemeEnabled()
                .collectAsStateWithLifecycle(initialValue = false)
                .value
            val languageTag = themeRepository.observeLanguageTag()
                .collectAsStateWithLifecycle(initialValue = appliedLanguageTag ?: DEFAULT_LANGUAGE_TAG)
                .value

            LaunchedEffect(languageTag) {
                val previousTag = appliedLanguageTag
                appliedLanguageTag = languageTag
                if (previousTag != null && previousTag != languageTag) {
                    recreate()
                }
            }

            FinanceAppTheme(darkTheme = isDarkThemeEnabled) {
                HomeScreen()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
