package com.subnetik.unlock.presentation.screens.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun VocabularyLevelScreen(
    level: Int,
    onNavigateToFlashcards: () -> Unit,
    onBack: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var expandedWordId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(level) { viewModel.loadWords(level) }
    LaunchedEffect(searchQuery) { viewModel.search(level, searchQuery) }

    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val color = hskLevelColor(level)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText) }
                Text("HSK $level", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.width(48.dp))
            }

            // Search bar
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.sm),
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = secondaryText)
                    Spacer(Modifier.width(10.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = primaryText),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("Поиск слова...", style = MaterialTheme.typography.bodyMedium, color = secondaryText)
                            }
                            inner()
                        },
                    )
                }
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
            ) {
                VocabStatChip(modifier = Modifier.weight(1f), value = "${uiState.totalWords}", label = "Всего", valueColor = BrandBlue, cardColor = cardColor, strokeColor = strokeColor, primaryText = primaryText, secondaryText = secondaryText)
                VocabStatChip(modifier = Modifier.weight(1f), value = "${uiState.knownCount}", label = "Выучено", valueColor = BrandGreen, cardColor = cardColor, strokeColor = strokeColor, primaryText = primaryText, secondaryText = secondaryText)
                VocabStatChip(modifier = Modifier.weight(1f), value = "${uiState.reviewCount}", label = "Повторить", valueColor = BrandCoral, cardColor = cardColor, strokeColor = strokeColor, primaryText = primaryText, secondaryText = secondaryText)
            }

            Spacer(Modifier.height(Brand.Spacing.md))

            // Flashcard mode button
            Button(
                onClick = onNavigateToFlashcards,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Режим карточек", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(Brand.Spacing.sm))

            // Word list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
            ) {
                items(uiState.filteredWords, key = { it.id }) { word ->
                    val isExpanded = expandedWordId == word.id
                    val isKnown = word.id in uiState.knownWordIds
                    val isReview = word.id in uiState.reviewWordIds
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { expandedWordId = if (isExpanded) null else word.id },
                        shape = RoundedCornerShape(16.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, if (isExpanded) color.copy(alpha = 0.3f) else strokeColor),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    word.character,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText,
                                    modifier = Modifier.width(60.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(word.pinyin, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
                                    Text(word.translation, style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                }
                                if (isKnown) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Выучено",
                                        modifier = Modifier.size(24.dp),
                                        tint = BrandGreen,
                                    )
                                } else if (isReview) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Повторить",
                                        modifier = Modifier.size(24.dp),
                                        tint = BrandCoral,
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                                ) {
                                    Button(
                                        onClick = { viewModel.markKnown(word.id, level) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Знаю", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.markReview(word.id, level) },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Повторить", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabStatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = secondaryText)
        }
    }
}
