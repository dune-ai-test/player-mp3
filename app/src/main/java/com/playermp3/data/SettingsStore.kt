package com.playermp3.data

import android.content.Context
import com.playermp3.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val folders: Set<String> = emptySet(),
    val playbackSpeed: Float = 1.0f,
    val equalizer: Boolean = false,
    val gapless: Boolean = false,
)

class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("cadence_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun read(): AppSettings =
        AppSettings(
            themeMode = ThemeMode.from(prefs.getString(KEY_THEME, "dark") ?: "dark"),
            folders = prefs.getStringSet(KEY_FOLDERS, emptySet()).orEmpty(),
            playbackSpeed = prefs.getFloat(KEY_SPEED, 1.0f),
            equalizer = prefs.getBoolean(KEY_EQ, false),
            gapless = prefs.getBoolean(KEY_GAPLESS, false),
        )

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.storage).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun addFolder(uri: String) {
        val folders = (_settings.value.folders + uri).toSet()
        prefs.edit().putStringSet(KEY_FOLDERS, folders).apply()
        _settings.value = _settings.value.copy(folders = folders)
    }

    fun clearFolders() {
        prefs.edit().remove(KEY_FOLDERS).apply()
        _settings.value = _settings.value.copy(folders = emptySet())
    }

    fun cyclePlaybackSpeed(): Float {
        val current = _settings.value.playbackSpeed
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val next = speeds[(speeds.indexOf(current).coerceAtLeast(0) + 1) % speeds.size]
        prefs.edit().putFloat(KEY_SPEED, next).apply()
        _settings.value = _settings.value.copy(playbackSpeed = next)
        return next
    }

    fun setEqualizer(on: Boolean) {
        prefs.edit().putBoolean(KEY_EQ, on).apply()
        _settings.value = _settings.value.copy(equalizer = on)
    }

    fun setGapless(on: Boolean) {
        prefs.edit().putBoolean(KEY_GAPLESS, on).apply()
        _settings.value = _settings.value.copy(gapless = on)
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_FOLDERS = "folders"
        const val KEY_SPEED = "speed"
        const val KEY_EQ = "equalizer"
        const val KEY_GAPLESS = "gapless"
    }
}