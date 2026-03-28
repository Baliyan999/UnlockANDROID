package com.subnetik.unlock.presentation.screens.guest

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.R
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GuestHomeViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val isDarkTheme = settingsDataStore.isDarkTheme
}

@Composable
fun GuestHomeScreen(
    onNavigateToTest: () -> Unit = {},
    onNavigateToLead: () -> Unit = {},
    onNavigateToTeachers: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToBlog: () -> Unit = {},
    onNavigateToCalculator: () -> Unit = {},
    viewModel: GuestHomeViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()
    val context = LocalContext.current
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ─── Top Bar (no bell icon for guest) ─────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                shape = Brand.Shapes.extraLarge,
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.unlock_logo),
                        contentDescription = "Unlock",
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                    Text(
                        text = "UNLOCK",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        letterSpacing = 2.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // ─── Hero Marketing Card ──────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Brand.Spacing.lg),
                shape = Brand.Shapes.extraLarge,
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(
                    modifier = Modifier.padding(Brand.Spacing.xl),
                ) {
                    Text(
                        text = "Курсы китайского языка UNLOCK \u2013 твой ключ от китайского с нуля.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        lineHeight = 28.sp,
                    )

                    Spacer(Modifier.height(Brand.Spacing.sm))

                    Text(
                        text = "Подготовка к HSK 1-6. Разговорная практика. Онлайн и офлайн, Ташкент.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                    )

                    Spacer(Modifier.height(Brand.Spacing.xl))

                    // "Пробный урок" gradient button (blue → purple like iOS)
                    Button(
                        onClick = onNavigateToLead,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF3A82F5), Color(0xFF7C5CFC)),
                                    ),
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Пробный урок",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp,
                            )
                        }
                    }

                    Spacer(Modifier.height(Brand.Spacing.md))

                    // "Написать в Telegram" outline button
                    OutlinedButton(
                        onClick = { openUrl("https://t.me/unlock_language") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f),
                        ),
                    ) {
                        Text(
                            "Написать в Telegram",
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.xl))

                    // Feature badges (teal like iOS)
                    val badges = listOf(
                        "Подготовка к HSK",
                        "Группы и индивидуально",
                        "Гибкая оплата",
                        "от 825 000 сум/мес",
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        badges.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                            ) {
                                row.forEach { badge ->
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = Brand.Shapes.full,
                                        color = BrandTeal.copy(alpha = 0.10f),
                                        border = BorderStroke(1.dp, BrandTeal.copy(alpha = 0.25f)),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 7.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = badge,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BrandTeal,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xl))

            // ─── Навигация section ────────────────────────────
            Text(
                "Навигация",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            val navItems = listOf(
                NavCardData(
                    icon = Icons.Default.School,
                    iconBg = BrandIndigo,
                    title = "Преподаватели",
                    description = "Познакомьтесь с нашей командой",
                    onClick = onNavigateToTeachers,
                ),
                NavCardData(
                    icon = Icons.Default.Star,
                    iconBg = BrandGold,
                    title = "Отзывы",
                    description = "Что говорят наши студенты",
                    onClick = onNavigateToReviews,
                ),
                NavCardData(
                    icon = Icons.Default.Description,
                    iconBg = BrandTeal,
                    title = "Блог",
                    description = "Статьи о китайском языке",
                    onClick = onNavigateToBlog,
                ),
                NavCardData(
                    icon = Icons.Default.CheckCircle,
                    iconBg = BrandGreen,
                    title = "Тест",
                    description = "Определите ваш уровень HSK",
                    onClick = onNavigateToTest,
                ),
                NavCardData(
                    icon = Icons.Default.Calculate,
                    iconBg = BrandCoral,
                    title = "Калькулятор",
                    description = "Рассчитайте стоимость обучения",
                    onClick = onNavigateToCalculator,
                ),
                NavCardData(
                    icon = Icons.Default.Edit,
                    iconBg = BrandBlue,
                    title = "Заявка",
                    description = "Оставьте заявку на обучение",
                    onClick = onNavigateToLead,
                ),
            )

            // 2-column grid
            Column(
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                navItems.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                    ) {
                        row.forEach { item ->
                            GuestNavCard(
                                data = item,
                                modifier = Modifier.weight(1f),
                                isDark = isDark,
                                cardColor = cardColor,
                                strokeColor = strokeColor,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                            )
                        }
                        // Pad last row if odd number
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xl))

            // ─── Форматы обучения ─────────────────────────────
            Text(
                "Форматы обучения",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            Column(
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                FormatCard(
                    icon = Icons.Default.Groups,
                    title = "Группа",
                    details = listOf(
                        "8-12 уроков в месяц",
                        "Длительность урока 80 минут",
                        "Цена: от 825 000 сум/мес",
                        "Что входит: программа HSK, разговорная практика, домашние задания",
                    ),
                    accentColor = BrandBlue,
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
                FormatCard(
                    icon = Icons.Default.Person,
                    title = "Индивидуально",
                    details = listOf(
                        "12 уроков в месяц",
                        "Длительность урока 60 минут",
                        "Цена: от 1 450 000 сум/мес",
                    ),
                    accentColor = Color(0xFFE8754A),
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
                FormatCard(
                    icon = Icons.Default.FlashOn,
                    title = "Интенсив",
                    details = listOf(
                        "Обсуждается строго с преподавателем",
                    ),
                    accentColor = BrandTeal,
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
            }

            Spacer(Modifier.height(Brand.Spacing.xl))

            // ─── Программа и уровни ───────────────────────────
            Text(
                "Программа и уровни",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.xs))

            Text(
                "HSK 1-6: цели, грамматика, лексика, результат.",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            val hskLevels = listOf(
                HskLevelInfo(1, "Базовые фразы, знакомство, числа, дата"),
                HskLevelInfo(2, "Бытовые ситуации, покупки, работа"),
                HskLevelInfo(3, "Путешествия, учёба, здоровье"),
                HskLevelInfo(4, "Профессиональное общение, медиа"),
                HskLevelInfo(5, "Литература, наука, деловой стиль"),
                HskLevelInfo(6, "Свободное владение, нюансы языка"),
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Brand.Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                hskLevels.forEach { level ->
                    HskLevelCard(
                        level = level,
                        isDark = isDark,
                        cardColor = cardColor,
                        strokeColor = strokeColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                    )
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xl))

            // ─── Контакты ─────────────────────────────────────
            Text(
                "Контакты",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.xs))

            Text(
                "Свяжитесь с нами любым удобным способом",
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            Column(
                modifier = Modifier.padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                ContactItem(
                    icon = Icons.Default.Phone,
                    iconBg = BrandGreen,
                    label = "Телефон",
                    value = "+998 77 268 68 86",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = { openUrl("tel:+998772686886") },
                )
                ContactItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    iconBg = BrandGreen,
                    label = "WhatsApp",
                    value = "+998 77 268 68 86",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = { openUrl("https://wa.me/998772686886") },
                )
                ContactItem(
                    icon = Icons.AutoMirrored.Filled.Send,
                    iconBg = BrandBlue,
                    label = "Telegram",
                    value = "@unlock_language",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = { openUrl("https://t.me/unlock_language") },
                )
                ContactItem(
                    icon = Icons.Default.LocationOn,
                    iconBg = BrandCoral,
                    label = "Адрес",
                    value = "ул. Якуба Коласа, 2/1, гостиница Central Palace, 6 этаж",
                    isDark = isDark,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = { openUrl("https://maps.google.com/?q=Yakuba+Kolasa+2/1+Tashkent") },
                )
            }

            Spacer(Modifier.height(Brand.Spacing.xxxl))
        }
    }
}

// ─── Data classes ────────────────────────────────────────

private data class NavCardData(
    val icon: ImageVector,
    val iconBg: Color,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

private data class HskLevelInfo(
    val level: Int,
    val description: String,
)

// ─── Composable components ──────────────────────────────

@Composable
private fun GuestNavCard(
    data: NavCardData,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    val tintedCardColor = if (isDark) data.iconBg.copy(alpha = 0.08f).compositeOver(Color(0xFF0D1120))
        else data.iconBg.copy(alpha = 0.06f).compositeOver(Color.White)
    val tintedStroke = if (isDark) data.iconBg.copy(alpha = 0.15f) else data.iconBg.copy(alpha = 0.10f)

    Surface(
        onClick = data.onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = tintedCardColor,
        border = BorderStroke(1.dp, tintedStroke),
    ) {
        Box {
            // Watermark icon bottom-right
            Icon(
                data.icon,
                contentDescription = null,
                tint = data.iconBg.copy(alpha = 0.10f),
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp),
            )

            Column(
                modifier = Modifier.padding(Brand.Spacing.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(data.iconBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            data.icon,
                            contentDescription = null,
                            tint = data.iconBg,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.height(Brand.Spacing.md))

                Text(
                    data.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    maxLines = 1,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    data.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    maxLines = 2,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun FormatCard(
    icon: ImageVector,
    title: String,
    details: List<String>,
    accentColor: Color,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(Brand.Spacing.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
            }

            Spacer(Modifier.height(Brand.Spacing.md))

            details.forEach { detail ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        "\u2022",
                        color = secondaryText,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                    )
                }
            }
        }
    }
}

@Composable
private fun HskLevelCard(
    level: HskLevelInfo,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    val levelColor = when (level.level) {
        1 -> BrandBlue
        2 -> BrandTeal
        3 -> BrandIndigo
        4 -> BrandGold
        5 -> BrandCoral
        6 -> Color(0xFF8B2252)
        else -> BrandBlue
    }
    Surface(
        modifier = Modifier.width(160.dp).height(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
            Text(
                "HSK ${level.level}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = levelColor,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            Text(
                level.description,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText,
                lineHeight = 16.sp,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun ContactItem(
    icon: ImageVector,
    iconBg: Color,
    label: String,
    value: String,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(Brand.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(BrandBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
