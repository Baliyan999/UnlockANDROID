package com.subnetik.unlock.presentation.screens.vocabulary

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FlashcardScreen(
    level: Int,
    onBack: () -> Unit,
    viewModel: FlashcardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val color = hskLevelColor(level)
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f)

    LaunchedEffect(level) { viewModel.loadCards(level) }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText) }
                Text("Карточки HSK $level", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.width(48.dp))
            }

            if (uiState.isComplete) {
                // ─── Result ─────────────────────────────
                Column(
                    modifier = Modifier.fillMaxSize().padding(Brand.Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("✅", fontSize = 48.sp)
                    Spacer(Modifier.height(Brand.Spacing.lg))
                    Text("Готово!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = primaryText)
                    Spacer(Modifier.height(Brand.Spacing.sm))
                    Text("Знаю: ${uiState.knownCount} из ${uiState.totalCount}", style = MaterialTheme.typography.titleMedium, color = secondaryText)
                    Spacer(Modifier.height(Brand.Spacing.xxl))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    ) {
                        Text("Назад к словарю", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (uiState.currentWord != null) {
                val word = uiState.currentWord!!
                var isFlipped by remember(word.id) { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(400),
                    label = "flip",
                )

                // Swipe state
                val scope = rememberCoroutineScope()
                val offsetX = remember(word.id) { Animatable(0f) }
                val swipeThreshold = 150f

                // Swipe direction indicator color
                val swipeProgress = (offsetX.value / swipeThreshold).coerceIn(-1f, 1f)
                val swipeBorderColor = when {
                    swipeProgress > 0.3f -> BrandGreen.copy(alpha = swipeProgress)
                    swipeProgress < -0.3f -> BrandCoral.copy(alpha = -swipeProgress)
                    else -> color.copy(alpha = 0.15f)
                }

                // ─── Progress header ────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${uiState.currentIndex + 1} / ${uiState.totalCount}", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandGreen)
                            Text("${uiState.knownCount}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGreen)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandCoral)
                            Text("${uiState.reviewCount}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandCoral)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f),
                )

                Spacer(Modifier.weight(1f))

                // ─── Swipeable Flashcard ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Brand.Spacing.lg)
                        .height(320.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Swipe label overlays
                    if (abs(swipeProgress) > 0.3f) {
                        Text(
                            text = if (swipeProgress > 0) "Знаю ✓" else "Повторить ↻",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (swipeProgress > 0) BrandGreen else BrandCoral,
                            modifier = Modifier
                                .align(if (swipeProgress > 0) Alignment.TopEnd else Alignment.TopStart)
                                .padding(Brand.Spacing.lg)
                                .graphicsLayer { alpha = abs(swipeProgress) },
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                            .graphicsLayer {
                                rotationY = rotation
                                rotationZ = offsetX.value / 30f // Slight tilt
                                cameraDistance = 12f * density
                            }
                            .pointerInput(word.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            if (offsetX.value > swipeThreshold) {
                                                // Swipe right → Known
                                                offsetX.animateTo(1000f, tween(200))
                                                viewModel.markKnown()
                                            } else if (offsetX.value < -swipeThreshold) {
                                                // Swipe left → Review
                                                offsetX.animateTo(-1000f, tween(200))
                                                viewModel.markReview()
                                            } else {
                                                // Snap back
                                                offsetX.animateTo(0f, tween(300))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        scope.launch { offsetX.animateTo(0f, tween(300)) }
                                    },
                                ) { _, dragAmount ->
                                    scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                                }
                            }
                            .clickable { isFlipped = !isFlipped },
                        shape = RoundedCornerShape(24.dp),
                        color = cardColor,
                        border = BorderStroke(2.dp, swipeBorderColor),
                        shadowElevation = 8.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Watermark
                            Text(
                                word.character,
                                fontSize = 120.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                                modifier = Modifier.align(Alignment.TopEnd).padding(end = 10.dp),
                            )

                            if (rotation <= 90f) {
                                // Front
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(word.character, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Spacer(Modifier.height(Brand.Spacing.md))
                                    Text("Нажми, чтобы перевернуть", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                }
                            } else {
                                // Back (mirrored)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                                ) {
                                    Text(word.pinyin, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
                                    Spacer(Modifier.height(Brand.Spacing.sm))
                                    Text(word.character, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                    Spacer(Modifier.height(Brand.Spacing.md))
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), color = secondaryText.copy(alpha = 0.2f))
                                    Spacer(Modifier.height(Brand.Spacing.md))
                                    Text(word.translation, fontSize = 20.sp, textAlign = TextAlign.Center, color = primaryText.copy(alpha = 0.9f))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.lg))

                // Swipe hints
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.xl),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("← Повторить", style = MaterialTheme.typography.bodySmall, color = BrandCoral.copy(alpha = 0.6f))
                    Text("Знаю →", style = MaterialTheme.typography.bodySmall, color = BrandGreen.copy(alpha = 0.6f))
                }

                Spacer(Modifier.weight(1f))

                // ─── Action buttons ─────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Brand.Spacing.lg).navigationBarsPadding().padding(bottom = Brand.Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                ) {
                    Button(
                        onClick = { viewModel.markReview() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Повторить", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.markKnown() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Знаю", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = color) }
            }
        }
    }
}
