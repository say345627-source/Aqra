package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.QuranPreferencesRepository
import com.example.data.QuranRepository
import com.example.model.FavoriteAyah
import com.example.model.QuranIntegrityInfo
import com.example.model.QuranSearchResult
import com.example.model.ReadingPosition
import com.example.model.SurahJson
import com.example.model.SurahSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface QuranUiState {
    data object Loading : QuranUiState
    data class Ready(
        val surahs: List<SurahJson>,
        val summaries: List<SurahSummary>,
        val integrity: QuranIntegrityInfo
    ) : QuranUiState
    data class Error(val message: String) : QuranUiState
}

class QuranViewModel(
    application: Application,
    private val quranRepository: QuranRepository,
    private val preferencesRepository: QuranPreferencesRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<QuranUiState>(QuranUiState.Loading)
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    val lastReadingPosition: StateFlow<ReadingPosition?> =
        preferencesRepository.lastReadingPosition
    val favorites: StateFlow<List<FavoriteAyah>> = preferencesRepository.favorites
    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
    val fontSizeSp: StateFlow<Float> = preferencesRepository.fontSizeSp

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<QuranSearchResult>>(emptyList())
    val searchResults: StateFlow<List<QuranSearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadQuranData()
    }

    fun loadQuranData() {
        viewModelScope.launch {
            _uiState.value = QuranUiState.Loading
            runCatching {
                val surahs = quranRepository.getAllSurahs()
                val summaries = quranRepository.getSurahSummaries()
                val integrity = quranRepository.getIntegrityInfo()
                Triple(surahs, summaries, integrity)
            }.onSuccess { (surahs, summaries, integrity) ->
                if (integrity.isVerified) {
                    _uiState.value = QuranUiState.Ready(
                        surahs = surahs,
                        summaries = summaries,
                        integrity = integrity
                    )
                } else {
                    _uiState.value = QuranUiState.Error(
                        "تعذر التحقق من اكتمال المصحف (${integrity.surahCount} سورة / ${integrity.totalAyahCount} آية)"
                    )
                }
            }.onFailure { err ->
                _uiState.value = QuranUiState.Error(
                    err.localizedMessage ?: "حدث خطأ غير متوقع أثناء تحميل المصحف"
                )
            }
        }
    }

    fun saveReadingPosition(surahNumber: Int, verseNumber: Int, surahName: String) {
        preferencesRepository.saveReadingPosition(
            ReadingPosition(
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                surahName = surahName,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    fun toggleFavorite(surahNumber: Int, verseNumber: Int, surahName: String, verseText: String) {
        preferencesRepository.toggleFavorite(
            FavoriteAyah(
                surahNumber = surahNumber,
                verseNumber = verseNumber,
                surahName = surahName,
                verseText = verseText,
                savedAtMillis = System.currentTimeMillis()
            )
        )
    }

    fun removeFavorite(surahNumber: Int, verseNumber: Int) {
        preferencesRepository.removeFavorite(surahNumber, verseNumber)
    }

    fun setDarkMode(enabled: Boolean) {
        preferencesRepository.setDarkMode(enabled)
    }

    fun setFontSize(sizeSp: Float) {
        preferencesRepository.setFontSize(sizeSp)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _isSearching.value = false
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            delay(160)
            _searchResults.value = quranRepository.searchVerses(trimmed)
            _isSearching.value = false
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val quranRepo = QuranRepository(application)
            val prefsRepo = QuranPreferencesRepository(application)
            return QuranViewModel(application, quranRepo, prefsRepo) as T
        }
    }
}
