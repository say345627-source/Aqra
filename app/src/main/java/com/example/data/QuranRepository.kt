package com.example.data

import android.content.Context
import com.example.model.QuranIntegrityInfo
import com.example.model.QuranSearchResult
import com.example.model.SurahJson
import com.example.model.SurahSummary
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class QuranRepository(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loadMutex = Mutex()
    private var cachedSurahs: List<SurahJson>? = null
    private var cachedNormalizedIndex: List<Pair<SurahJson, List<String>>>? = null

    suspend fun getAllSurahs(): List<SurahJson> = withContext(Dispatchers.IO) {
        loadMutex.withLock {
            cachedSurahs?.let { return@withContext it }

            val jsonText = context.assets.open(ASSET_QURAN_FILE)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val listType = Types.newParameterizedType(List::class.java, SurahJson::class.java)
            val adapter = moshi.adapter<List<SurahJson>>(listType)
            val parsed = adapter.fromJson(jsonText).orEmpty()

            // Remove only invisible UTF-8 BOM if present; keep all Arabic letters and diacritics intact
            val sanitized = parsed.map { surah ->
                surah.copy(
                    ayahs = surah.ayahs.map { ayah ->
                        if (ayah.text.startsWith('\uFEFF')) {
                            ayah.copy(text = ayah.text.trimStart('\uFEFF'))
                        } else {
                            ayah
                        }
                    }
                )
            }

            cachedSurahs = sanitized
            cachedNormalizedIndex = sanitized.map { surah ->
                surah to surah.ayahs.map { ayah -> normalizeArabicForSearch(ayah.text) }
            }
            sanitized
        }
    }

    suspend fun getSurahSummaries(): List<SurahSummary> {
        return getAllSurahs().map { surah ->
            SurahSummary(
                number = surah.number,
                arabicName = surah.name,
                englishName = surah.englishName,
                isMakki = surah.isMakki,
                versesCount = surah.versesCount
            )
        }
    }

    suspend fun getSurah(surahNumber: Int): SurahJson? {
        return getAllSurahs().firstOrNull { it.number == surahNumber }
    }

    suspend fun getIntegrityInfo(): QuranIntegrityInfo {
        val surahs = getAllSurahs()
        val totalAyahs = surahs.sumOf { it.ayahs.size }
        return QuranIntegrityInfo(
            surahCount = surahs.size,
            totalAyahCount = totalAyahs,
            isVerified = surahs.size == EXPECTED_SURAH_COUNT && totalAyahs == EXPECTED_AYAH_COUNT
        )
    }

    suspend fun searchVerses(query: String, limit: Int = 150): List<QuranSearchResult> =
        withContext(Dispatchers.Default) {
            getAllSurahs()
            val index = cachedNormalizedIndex ?: return@withContext emptyList()
            val normalizedQuery = normalizeArabicForSearch(query)
            if (normalizedQuery.length < 2) return@withContext emptyList()

            val results = ArrayList<QuranSearchResult>()
            for ((surah, normalizedAyahs) in index) {
                for (i in normalizedAyahs.indices) {
                    if (normalizedAyahs[i].contains(normalizedQuery)) {
                        val originalAyah = surah.ayahs[i]
                        results.add(
                            QuranSearchResult(
                                surahNumber = surah.number,
                                verseNumber = originalAyah.numberInSurah,
                                surahName = surah.name,
                                verseText = originalAyah.text,
                                juz = originalAyah.juz
                            )
                        )
                        if (results.size >= limit) return@withContext results
                    }
                }
            }
            results
        }

    companion object {
        private const val ASSET_QURAN_FILE = "quran_uthmani.json"
        private const val EXPECTED_SURAH_COUNT = 114
        private const val EXPECTED_AYAH_COUNT = 6236

        private val DIACRITICS_REGEX =
            Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")
        private val WHITESPACE_REGEX = Regex("\\s+")

        fun normalizeArabicForSearch(input: String): String {
            val noDiacritics = DIACRITICS_REGEX.replace(input, "")
            return buildString(noDiacritics.length) {
                for (ch in noDiacritics) {
                    when (ch) {
                        'أ', 'إ', 'آ', 'ٱ' -> append('ا')
                        'ى' -> append('ي')
                        'ة' -> append('ه')
                        'ؤ' -> append('و')
                        'ئ' -> append('ي')
                        else -> append(ch)
                    }
                }
            }.replace(WHITESPACE_REGEX, " ").trim()
        }

        fun toArabicIndicDigits(number: Int): String {
            val digits = number.toString()
            return buildString(digits.length) {
                for (ch in digits) {
                    if (ch in '0'..'9') {
                        append(('٠'.code + (ch - '0')).toChar())
                    } else {
                        append(ch)
                    }
                }
            }
        }

        fun formatAyahMarker(number: Int): String = "﴿${toArabicIndicDigits(number)}﴾"
    }
}
