package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.QuranRepository
import com.example.model.AyahJson
import com.example.model.SurahJson
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldMedium
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.GoldWarm

@Composable
fun SurahNumberBadge(
    number: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier.size(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val side = size.minDimension * 0.68f
            val topLeft = Offset((size.width - side) / 2f, (size.height - side) / 2f)
            val rectSize = androidx.compose.ui.geometry.Size(side, side)
            drawRect(
                color = primaryColor.copy(alpha = 0.12f),
                topLeft = topLeft,
                size = rectSize
            )
            drawRect(
                color = accentColor,
                topLeft = topLeft,
                size = rectSize,
                style = Stroke(width = 1.6.dp.toPx())
            )
            rotate(degrees = 45f) {
                drawRect(
                    color = primaryColor.copy(alpha = 0.10f),
                    topLeft = topLeft,
                    size = rectSize
                )
                drawRect(
                    color = accentColor,
                    topLeft = topLeft,
                    size = rectSize,
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
        }
        Text(
            text = QuranRepository.toArabicIndicDigits(number),
            style = MaterialTheme.typography.labelLarge.copy(
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun SurahHeaderBanner(
    surah: SurahJson,
    modifier: Modifier = Modifier
) {
    val revelationLabel = if (surah.isMakki) {
        stringResource(R.string.makki_label)
    } else {
        stringResource(R.string.madani_label)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(EmeraldDeep, EmeraldMedium)
                )
            )
            .border(
                width = 1.5.dp,
                color = GoldWarm.copy(alpha = 0.75f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.name,
                style = TextStyle(
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = 30.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderPill(text = "رقم ${QuranRepository.toArabicIndicDigits(surah.number)}")
                HeaderPill(text = "${QuranRepository.toArabicIndicDigits(surah.versesCount)} آية")
                HeaderPill(text = revelationLabel)
            }
        }
    }
}

@Composable
private fun HeaderPill(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = GoldSoft,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun AyahCard(
    ayah: AyahJson,
    isFavorite: Boolean,
    isHighlighted: Boolean,
    fontSizeSp: Float,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isHighlighted) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isHighlighted) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isHighlighted) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "آية ${QuranRepository.toArabicIndicDigits(ayah.numberInSurah)} • الجزء ${QuranRepository.toArabicIndicDigits(ayah.juz)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (ayah.sajda) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = stringResource(R.string.sajdah_label),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("favorite_toggle_${ayah.numberInSurah}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Rounded.Bookmark
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = stringResource(R.string.toggle_favorite_desc),
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val marker = QuranRepository.formatAyahMarker(ayah.numberInSurah)
            val ayahAnnotated = buildAnnotatedString {
                append(ayah.text)
                append(" ")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = (fontSizeSp * 0.88f).sp
                    )
                ) {
                    append(marker)
                }
            }

            Text(
                text = ayahAnnotated,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.95f).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
            )
        }
    }
}
