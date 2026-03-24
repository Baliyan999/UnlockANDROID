package com.subnetik.unlock.presentation.screens.vocabulary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

private data class HskLevelInfo(
    val level: Int,
    val title: String,
    val subtitle: String,
    val wordCount: Int,
    val color: Color,
)

private val hskLevels = listOf(
    HskLevelInfo(1, "HSK 1", "Базовый – 150 слов", 150, BrandBlue),
    HskLevelInfo(2, "HSK 2", "Начальный – 300 слов", 300, BrandTeal),
    HskLevelInfo(3, "HSK 3", "Средний начальный – 600 слов", 600, BrandIndigo),
    HskLevelInfo(4, "HSK 4", "Средний – 1200 слов", 1200, BrandCoral),
    HskLevelInfo(5, "HSK 5", "Средний продвинутый – 3000 слов", 3000, BrandGold),
    HskLevelInfo(6, "HSK 6", "Продвинутый – 5000+ слов", 5000, BrandGreen),
)

@Composable
fun VocabularyScreen(
    onNavigateToLevel: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    // Load progress from local + server on appear
    LaunchedEffect(Unit) {
        viewModel.loadAllLevelProgress()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText) }
                Text("Словарь HSK", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.width(48.dp))
            }

            // Header
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(28.dp), tint = BrandBlue)
                }
                Spacer(Modifier.height(12.dp))
                Text("Лексика китайского языка", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Учи слова по уровням HSK 1-6. Карточки, поиск и\nотслеживание прогресса.",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                )
            }

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                items(hskLevels) { info ->
                    val knownCount = uiState.levelProgress[info.level] ?: 0
                    val progress = if (info.wordCount > 0) knownCount.toFloat() / info.wordCount else 0f

                    Surface(
                        onClick = { onNavigateToLevel(info.level) },
                        shape = RoundedCornerShape(18.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, strokeColor),
                    ) {
                        Column(
                            modifier = Modifier.height(100.dp).padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("HSK ${info.level}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = info.color)
                                Text("$knownCount/${formatCount(info.wordCount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = info.color)
                            }
                            Text(info.subtitle, style = MaterialTheme.typography.bodySmall, color = secondaryText, fontSize = 11.sp, maxLines = 2)
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = info.color,
                                trackColor = info.color.copy(alpha = 0.15f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Int): String {
    if (count >= 1000) {
        val formatted = java.text.NumberFormat.getNumberInstance(java.util.Locale("ru")).format(count)
        return formatted
    }
    return "$count"
}
