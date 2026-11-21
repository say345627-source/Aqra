package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AyahJson(
    val numberInSurah: Int,
    val text: String,
    val juz: Int,
    val page: Int,
    val sajda: Boolean
)

@JsonClass(generateAdapter = true)
data class SurahJson(
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String,
    val ayahs: List<AyahJson>
) {
    val isMakki: Boolean
        get() = revelationType.equals("Meccan", ignoreCase = true)

    val versesCount: Int
        get() = ayahs.size
}

data class SurahSummary(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val isMakki: Boolean,
    val versesCount: Int
)

@JsonClass(generateAdapter = true)
data class FavoriteAyah(
    val surahNumber: Int,
    val verseNumber: Int,
    val surahName: String,
    val verseText: String,
    val savedAtMillis: Long
) {
    val key: String
        get() = "$surahNumber:$verseNumber"
}

@JsonClass(generateAdapter = true)
data class ReadingPosition(
    val surahNumber: Int,
    val verseNumber: Int,
    val surahName: String,
    val updatedAtMillis: Long
)

data class QuranSearchResult(
    val surahNumber: Int,
    val verseNumber: Int,
    val surahName: String,
    val verseText: String,
    val juz: Int
)

data class QuranIntegrityInfo(
    val surahCount: Int,
    val totalAyahCount: Int,
    val isVerified: Boolean
)
