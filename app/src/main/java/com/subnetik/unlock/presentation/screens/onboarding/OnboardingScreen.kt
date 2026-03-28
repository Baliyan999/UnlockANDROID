package com.subnetik.unlock.presentation.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.theme.*
import kotlinx.coroutines.launch

// ─── Page data model ────────────────────────────────────────

private data class OnboardingPage(
    val character: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val tintColor: Color,
    val gradientColors: List<Color>,
)

private val pages = listOf(
    OnboardingPage(
        character = "\u5F00",       // 开
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Добро пожаловать в UNLOCK",
        subtitle = "Учебный центр китайского языка в Ташкенте. Начни свой путь к владению китайским!",
        tintColor = BrandBlue,
        gradientColors = GradientBlue,
    ),
    OnboardingPage(
        character = "\u5B66",       // 学
        icon = Icons.Default.Groups,
        title = "Учись с лучшими",
        subtitle = "Опытные преподаватели, группы по уровням HSK 1\u20136, индивидуальный подход к каждому ученику.",
        tintColor = BrandTeal,
        gradientColors = listOf(BrandTeal, Color(0xFF3BC4CA)),
    ),
    OnboardingPage(
        character = "\u8D62",       // 赢
        icon = Icons.Default.Star,
        title = "Зарабатывай токены",
        subtitle = "Получай Unlock Tokens за активность, приглашай друзей и обменивай токены на призы.",
        tintColor = BrandGold,
        gradientColors = GradientGold,
    ),
)

// ─── Screen ─────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage
    val isLastPage = currentPage >= pages.size - 1

    // Animate background tint based on current page
    val bgTopColor by animateColorAsState(
        targetValue = when (currentPage) {
            0 -> Color(0xFF0D1120)
            1 -> Color(0xFF0D1120)
            else -> Color(0xFF0D1120)
        },
        animationSpec = tween(400),
        label = "bgTop",
    )

    // Glow color that changes per page
    val glowColor by animateColorAsState(
        targetValue = pages[currentPage].tintColor.copy(alpha = 0.18f),
        animationSpec = tween(400),
        label = "glow",
    )

    Box(modifier = Modifier.fillMaxSize().clipToBounds()) {

        // ── Dark gradient background (AdminBackground-style) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0D1120), Color(0xFF1A1E33))
                    )
                ),
        )
        // Glow blobs (fully inside screen bounds)
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-40).dp, y = 20.dp)
                .background(glowColor, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = (-40).dp)
                .background(
                    Color(0xFF8552DB).copy(alpha = 0.15f),
                    CircleShape,
                ),
        )

        // ── Content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Brand.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Skip button (pages 1-2 only) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Brand.Spacing.lg),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onComplete) {
                        Text(
                            "Пропустить",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else {
                    // Keep the same height so layout doesn't jump
                    Spacer(Modifier.height(48.dp))
                }
            }

            // ── Pager ──
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                val data = pages[page]

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    // Large faded Chinese character in background
                    Text(
                        text = data.character,
                        fontSize = 260.sp,
                        fontWeight = FontWeight.Black,
                        color = data.tintColor.copy(alpha = 0.07f),
                    )

                    // Foreground content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        // Colored circle with icon
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(data.gradientColors)
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = data.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp),
                            )
                        }

                        Spacer(Modifier.height(Brand.Spacing.xxl))

                        // Title
                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )

                        Spacer(Modifier.height(Brand.Spacing.md))

                        // Subtitle
                        Text(
                            text = data.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                        )
                    }
                }
            }

            // ── Page indicator dots ──
            PageIndicatorDots(
                pageCount = pages.size,
                currentPage = currentPage,
                activeColor = pages[currentPage].tintColor,
                modifier = Modifier.padding(vertical = Brand.Spacing.lg),
            )

            Spacer(Modifier.height(Brand.Spacing.sm))

            // ── Action button ──
            val btnGradient = pages[currentPage].gradientColors
            val btnText = if (isLastPage) "Начать" else "Далее"

            Button(
                onClick = {
                    if (!isLastPage) {
                        scope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = Brand.Shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                ),
                contentPadding = PaddingValues(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            brush = Brush.horizontalGradient(btnGradient),
                            shape = Brand.Shapes.medium,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = btnText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xxxl))
        }
    }
}

// ─── Custom page indicator with colored active capsule ──────

@Composable
private fun PageIndicatorDots(
    pageCount: Int,
    currentPage: Int,
    activeColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isActive) 28.dp else 8.dp,
                animationSpec = tween(300),
                label = "dot_width",
            )
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.3f),
                animationSpec = tween(300),
                label = "dot_color",
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
