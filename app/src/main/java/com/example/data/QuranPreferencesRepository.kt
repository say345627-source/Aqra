package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.FavoriteAyah
import com.example.model.ReadingPosition
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuranPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val readingPositionAdapter = moshi.adapter(ReadingPosition::class.java)
    private val favoritesListType =
        Types.newParameterizedType(List::class.java, FavoriteAyah::class.java)
    private val favoritesAdapter = moshi.adapter<List<FavoriteAyah>>(favoritesListType)

    private val _lastReadingPosition = MutableStateFlow(loadReadingPosition())
    val lastReadingPosition: StateFlow<ReadingPosition?> = _lastReadingPosition.asStateFlow()

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<List<FavoriteAyah>> = _favorites.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(
        prefs.getFloat(KEY_FONT_SIZE, DEFAULT_FONT_SIZE).coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
    )
    val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

    fun saveReadingPosition(position: ReadingPosition) {
        val json = readingPositionAdapter.toJson(position)
        prefs.edit().putString(KEY_LAST_POSITION, json).apply()
        _lastReadingPosition.value = position
    }

    fun toggleFavorite(ayah: FavoriteAyah) {
        val current = _favorites.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.key == ayah.key }
        if (existingIdx >= 0) {
            current.removeAt(existingIdx)
        } else {
            current.add(0, ayah)
        }
        persistFavorites(current)
    }

    fun removeFavorite(surahNumber: Int, verseNumber: Int) {
        val current = _favorites.value.filterNot {
            it.surahNumber == surahNumber && it.verseNumber == verseNumber
        }
        persistFavorites(current)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setFontSize(sizeSp: Float) {
        val clamped = sizeSp.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        prefs.edit().putFloat(KEY_FONT_SIZE, clamped).apply()
        _fontSizeSp.value = clamped
    }

    private fun persistFavorites(list: List<FavoriteAyah>) {
        val sorted = list.sortedByDescending { it.savedAtMillis }
        val json = favoritesAdapter.toJson(sorted)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
        _favorites.value = sorted
    }

    private fun loadReadingPosition(): ReadingPosition? {
        val raw = prefs.getString(KEY_LAST_POSITION, null) ?: return null
        return runCatching { readingPositionAdapter.fromJson(raw) }.getOrNull()
    }

    private fun loadFavorites(): List<FavoriteAyah> {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return runCatching { favoritesAdapter.fromJson(raw).orEmpty() }
            .getOrDefault(emptyList())
            .sortedByDescending { it.savedAtMillis }
    }

    companion object {
        private const val PREFS_NAME = "quran_offline_prefs"
        private const val KEY_LAST_POSITION = "last_reading_position"
        private const val KEY_FAVORITES = "favorite_ayahs"
        private const val KEY_DARK_MODE = "is_dark_mode"
        private const val KEY_FONT_SIZE = "quran_font_size"

        const val MIN_FONT_SIZE = 18f
        const val MAX_FONT_SIZE = 38f
        const val DEFAULT_FONT_SIZE = 25f
    }
}
