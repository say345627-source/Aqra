package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SurahJson
import com.example.ui.components.AyahCard
import com.example.ui.components.SurahHeaderBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(
    surahs: List<SurahJson>,
    initialSurahNumber: Int,
    initialVerseNumber: Int,
    favoriteKeys: Set<String>,
    fontSizeSp: Float,
    onBack: () -> Unit,
    onPositionChanged: (surahNumber: Int, verseNumber: Int, surahName: String) -> Unit,
    onToggleFavorite: (surahNumber: Int, verseNumber: Int, surahName: String, verseText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSurahNumber by remember(initialSurahNumber) {
        mutableIntStateOf(initialSurahNumber.coerceIn(1, 114))
    }
    var highlightedVerse by remember(initialSurahNumber, initialVerseNumber) {
        mutableIntStateOf(initialVerseNumber.coerceAtLeast(1))
    }
    var showJumpDialog by remember { mutableStateOf(false) }

    val currentSurah = remember(surahs, currentSurahNumber) {
        surahs.firstOrNull { it.number == currentSurahNumber }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentSurah?.number, highlightedVerse) {
        val surah = currentSurah ?: return@LaunchedEffect
        val validVerse = highlightedVerse.coerceIn(1, surah.versesCount)
        onPositionChanged(surah.number, validVerse, surah.name)
        // Item 0 is the SurahHeaderBanner; ayahs start at index 1
        listState.scrollToItem(index = validVerse)
    }

    if (currentSurah == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.loading_quran))
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentSurah.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showJumpDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("jump_to_ayah_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = stringResource(R.string.jump_to_ayah)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentSurahNumber > 1) {
                                currentSurahNumber -= 1
                                highlightedVerse = 1
                            }
                        },
                        enabled = currentSurahNumber > 1,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("prev_surah_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.previous_surah),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (currentSurahNumber < 114) {
                                currentSurahNumber += 1
                                highlightedVerse = 1
                            }
                        },
                        enabled = currentSurahNumber < 114,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("next_surah_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.next_surah),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item(key = "header_${currentSurah.number}") {
                    SurahHeaderBanner(surah = currentSurah)
                }

                items(
                    items = currentSurah.ayahs,
                    key = { ayah -> "${currentSurah.number}:${ayah.numberInSurah}" }
                ) { ayah ->
                    val key = "${currentSurah.number}:${ayah.numberInSurah}"
                    AyahCard(
                        ayah = ayah,
                        isFavorite = favoriteKeys.contains(key),
                        isHighlighted = highlightedVerse == ayah.numberInSurah,
                        fontSizeSp = fontSizeSp,
                        onSelect = {
                            highlightedVerse = ayah.numberInSurah
                            onPositionChanged(
                                currentSurah.number,
                                ayah.numberInSurah,
                                currentSurah.name
                            )
                        },
                        onToggleFavorite = {
                            onToggleFavorite(
                                currentSurah.number,
                                ayah.numberInSurah,
                                currentSurah.name,
                                ayah.text
                            )
                        }
                    )
                }
            }
        }
    }

    if (showJumpDialog) {
        JumpToAyahDialog(
            maxVerses = currentSurah.versesCount,
            onDismiss = { showJumpDialog = false },
            onJump = { verse ->
                showJumpDialog = false
                highlightedVerse = verse
                onPositionChanged(currentSurah.number, verse, currentSurah.name)
                coroutineScope.launch {
                    listState.animateScrollToItem(index = verse)
                }
            }
        )
    }
}

@Composable
private fun JumpToAyahDialog(
    maxVerses: Int,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit
) {
    var input by remember { mutableStateOf("") }
    val parsed = input.trim().toIntOrNull()
    val isValid = parsed != null && parsed in 1..maxVerses

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.jump_to_ayah)) },
        text = {
            Column {
                Text(
                    text = "اختر رقم الآية من ١ إلى $maxVerses",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            input = value
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("jump_ayah_input"),
                    placeholder = { Text(stringResource(R.string.jump_to_ayah_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (parsed != null) onJump(parsed) },
                enabled = isValid,
                modifier = Modifier.testTag("confirm_jump_button")
            ) {
                Text(stringResource(R.string.go))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
