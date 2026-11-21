package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuranPreferencesRepository
import com.example.data.QuranRepository
import com.example.model.FavoriteAyah
import com.example.model.ReadingPosition
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Quran app title`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("القرآن الكريم", appName)
    }

    @Test
    fun `offline quran dataset has all 114 surahs and 6236 ayahs`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranRepository(context)

        val integrity = repo.getIntegrityInfo()
        assertTrue(integrity.isVerified)
        assertEquals(114, integrity.surahCount)
        assertEquals(6236, integrity.totalAyahCount)

        val fatihah = repo.getSurah(1)!!
        assertEquals(7, fatihah.versesCount)
        assertTrue(fatihah.isMakki)

        val nas = repo.getSurah(114)!!
        assertEquals(6, nas.versesCount)
    }

    @Test
    fun `offline search finds normalized Arabic words while preserving Uthmani text`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = QuranRepository(context)

        val results = repo.searchVerses("الرحمن")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.surahNumber == 1 && it.verseNumber == 1 })
        assertTrue(results.any { it.surahNumber == 55 && it.verseNumber == 1 })
    }

    @Test
    fun `preferences repository persists reading position, favorites, and settings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = QuranPreferencesRepository(context)

        prefs.saveReadingPosition(
            ReadingPosition(
                surahNumber = 18,
                verseNumber = 10,
                surahName = "سُورَةُ الكَهْفِ",
                updatedAtMillis = 1000L
            )
        )
        assertEquals(18, prefs.lastReadingPosition.value?.surahNumber)
        assertEquals(10, prefs.lastReadingPosition.value?.verseNumber)

        val fav = FavoriteAyah(
            surahNumber = 2,
            verseNumber = 255,
            surahName = "سُورَةُ البَقَرَةِ",
            verseText = "ٱللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
            savedAtMillis = 2000L
        )
        prefs.toggleFavorite(fav)
        assertEquals(1, prefs.favorites.value.size)

        prefs.removeFavorite(2, 255)
        assertTrue(prefs.favorites.value.isEmpty())

        prefs.setDarkMode(true)
        assertTrue(prefs.isDarkMode.value)
        prefs.setDarkMode(false)
        assertFalse(prefs.isDarkMode.value)

        prefs.setFontSize(28f)
        assertEquals(28f, prefs.fontSizeSp.value)
    }
}
