package com.subnetik.unlock.presentation.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subnetik.unlock.presentation.components.AnimatedPageIndicator
import com.subnetik.unlock.presentation.components.UnlockButton
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.GradientIndigo
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val character: String,
    val title: String,
    val description: String,
)

private val pages = listOf(
    OnboardingPage("开", "Добро пожаловать\nв UNLOCK!", "Платформа для изучения\nкитайского языка"),
    OnboardingPage("学", "Лучшие\nпреподаватели", "Индивидуальный подход\nи проверенная методология"),
    OnboardingPage("赢", "Зарабатывай\nтокены", "Получай награды за активность\nи обменивай на призы"),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GradientIndigo))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = Brand.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Brand.Spacing.lg),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onComplete) {
                    Text(
                        "Пропустить",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Decorative circle behind character
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Text(
                            text = pages[page].character,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.xxl))

                    Text(
                        text = pages[page].title,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(Brand.Spacing.md))
                    Text(
                        text = pages[page].description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            // Page indicators
            AnimatedPageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(vertical = Brand.Spacing.lg),
            )

            Spacer(Modifier.height(Brand.Spacing.sm))

            // Button
            UnlockButton(
                text = if (pagerState.currentPage < pages.size - 1) "Далее" else "Начать",
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete()
                    }
                },
                gradientColors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.7f)),
            )

            Spacer(Modifier.height(Brand.Spacing.xxxl))
        }
    }
}
