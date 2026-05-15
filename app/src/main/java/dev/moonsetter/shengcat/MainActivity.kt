package dev.moonsetter.shengcat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.moonsetter.shengcat.data.repository.SettingsRepository
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_DARK
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_LIGHT
import dev.moonsetter.shengcat.ui.theme.ShengCatTheme
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by settingsRepository.theme.collectAsState(initial = SettingsRepository.THEME_SYSTEM)
            val lang by settingsRepository.language.collectAsState(initial = SettingsRepository.LANG_EN)

            val locale = Locale.forLanguageTag(lang)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            createConfigurationContext(config)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)

            val darkTheme = when (theme) {
                THEME_DARK -> true
                THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            ShengCatTheme(darkTheme = darkTheme, dynamicColor = true) {
                AppNavigation()
            }
        }
    }
}