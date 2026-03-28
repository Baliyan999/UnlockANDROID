package com.subnetik.unlock.presentation.screens.guest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

// ─── Data Models ──────────────────────────────────────────────

private data class CalcFormat(
    val id: String,
    val name: String,
    val description: String,
)

private data class CalcGroupSize(
    val id: String,
    val name: String,
    val description: String,
    val lessonsPerMonth: Int,
)

private data class CalcTeacher(
    val id: String,
    val name: String,
    val description: String,
    val monthlyPrice: Int,
)

private data class CalcLevel(
    val id: String,
    val name: String,
)

private data class CalcPrice(
    val levelId: String,
    val groupSizeId: String,
    val monthlyPrice: Int,
)

// ─── Static Data (matches iOS AppData) ───────────────────────

private val formats = listOf(
    CalcFormat("group", "Групповые занятия", "8-12 уроков в месяц, 80 минут"),
    CalcFormat("individual", "Индивидуальные занятия", "12 уроков в месяц, 60 минут"),
)

private val groupSizes = listOf(
    CalcGroupSize("small", "Маленькая группа", "От 3 до 6 человек", 12),
    CalcGroupSize("large", "Большая группа", "От 8 до 16 человек", 12),
)

private val teachers = listOf(
    CalcTeacher("rukhsana", "Рухсана", "Основатель школы, HSK 1–6, уникальная методика", 3_600_000),
    CalcTeacher("native", "Носитель языка", "Опытный преподаватель из Китая", 2_800_000),
    CalcTeacher("regular", "Обычный преподаватель", "Квалифицированный преподаватель", 2_100_000),
)

private val levels = listOf(
    CalcLevel("hsk1", "HSK 1"),
    CalcLevel("hsk2", "HSK 2"),
    CalcLevel("hsk3", "HSK 3"),
    CalcLevel("hsk4", "HSK 4"),
    CalcLevel("hsk5", "HSK 5"),
    CalcLevel("hsk6", "HSK 6"),
)

private val prices = listOf(
    // Large group
    CalcPrice("hsk1", "large", 825_000),
    CalcPrice("hsk2", "large", 825_000),
    CalcPrice("hsk3", "large", 860_000),
    CalcPrice("hsk4", "large", 1_050_000),
    CalcPrice("hsk5", "large", 1_115_000),
    CalcPrice("hsk6", "large", 1_440_000),
    // Small group
    CalcPrice("hsk1", "small", 935_000),
    CalcPrice("hsk2", "small", 935_000),
    CalcPrice("hsk3", "small", 980_000),
    CalcPrice("hsk4", "small", 1_050_000),
    CalcPrice("hsk5", "small", 1_115_000),
    CalcPrice("hsk6", "small", 1_440_000),
)

private val levelSubtitles = mapOf(
    "hsk1" to "Начальный",
    "hsk2" to "Базовый",
    "hsk3" to "Средний",
    "hsk4" to "Продвинутый",
    "hsk5" to "Высокий",
    "hsk6" to "Экспертный",
)

private fun formatSum(value: Int): String {
    val nf = NumberFormat.getNumberInstance(Locale("ru", "RU"))
    return nf.format(value).replace(',', ' ')
}

// ─── ViewModel ───────────────────────────────────────────────

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val isDarkTheme = settingsDataStore.isDarkTheme
}

// ─── Screen ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onBack: () -> Unit = {},
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    var selectedFormatId by remember { mutableStateOf<String?>(null) }
    var selectedGroupSizeId by remember { mutableStateOf<String?>(null) }
    var selectedTeacherId by remember { mutableStateOf<String?>(null) }
    var selectedLevelId by remember { mutableStateOf<String?>(null) }

    val selectedFormat = formats.firstOrNull { it.id == selectedFormatId }
    val selectedGroupSize = groupSizes.firstOrNull { it.id == selectedGroupSizeId }
    val selectedTeacher = teachers.firstOrNull { it.id == selectedTeacherId }
    val selectedLevel = levels.firstOrNull { it.id == selectedLevelId }

    val lessonsPerMonth = when {
        selectedFormatId == "group" && selectedGroupSize != null -> selectedGroupSize.lessonsPerMonth
        selectedFormatId == "individual" -> 12
        else -> 8
    }

    val monthlyPrice: Int = when {
        selectedLevel == null -> 0
        selectedFormatId == "group" && selectedGroupSize != null -> {
            prices.firstOrNull { it.levelId == selectedLevel.id && it.groupSizeId == selectedGroupSize.id }
                ?.monthlyPrice ?: 0
        }
        selectedFormatId == "individual" && selectedTeacher != null -> {
            selectedTeacher.monthlyPrice
        }
        else -> 0
    }

    val showResult = selectedLevelId != null && selectedFormatId != null &&
            (selectedFormatId != "group" || selectedGroupSizeId != null) &&
            (selectedFormatId != "individual" || selectedTeacherId != null)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Top Bar ─────────────────────────
            TopAppBar(
                title = {
                    Text(
                        "Калькулятор",
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = primaryText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // ─── Header ──────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = BrandTeal,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            "Калькулятор стоимости",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Рассчитайте стоимость обучения китайскому языку",
                        fontSize = 14.sp,
                        color = secondaryText,
                        textAlign = TextAlign.Center,
                    )
                }

                // ─── Step 1: Format ──────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CalcSectionHeader(number = 1, title = "Формат обучения", primaryText = primaryText)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CalcFormatCard(
                            format = formats[0],
                            isSelected = selectedFormatId == "group",
                            icon = Icons.Default.Groups,
                            accentColor = BrandBlue,
                            isDark = isDark,
                            cardColor = cardColor,
                            strokeColor = strokeColor,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedFormatId = "group"
                                if (selectedGroupSizeId == null) {
                                    selectedGroupSizeId = groupSizes.first().id
                                }
                                selectedTeacherId = null
                            },
                        )
                        CalcFormatCard(
                            format = formats[1],
                            isSelected = selectedFormatId == "individual",
                            icon = Icons.Default.Person,
                            accentColor = BrandCoral,
                            isDark = isDark,
                            cardColor = cardColor,
                            strokeColor = strokeColor,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedFormatId = "individual"
                                if (selectedTeacherId == null) {
                                    selectedTeacherId = teachers.first().id
                                }
                                selectedGroupSizeId = null
                            },
                        )
                    }
                }

                // ─── Step 2: Group Size (for group) ──
                AnimatedVisibility(
                    visible = selectedFormatId == "group",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CalcSectionHeader(number = 2, title = "Размер группы", primaryText = primaryText)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            groupSizes.forEach { gs ->
                                CalcGroupSizeCard(
                                    groupSize = gs,
                                    isSelected = selectedGroupSizeId == gs.id,
                                    accentColor = BrandIndigo,
                                    isDark = isDark,
                                    cardColor = cardColor,
                                    strokeColor = strokeColor,
                                    primaryText = primaryText,
                                    secondaryText = secondaryText,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedGroupSizeId = gs.id },
                                )
                            }
                        }
                    }
                }

                // ─── Step 2: Teacher (for individual) ─
                AnimatedVisibility(
                    visible = selectedFormatId == "individual",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CalcSectionHeader(number = 2, title = "Преподаватель", primaryText = primaryText)

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            teachers.forEach { teacher ->
                                CalcTeacherCard(
                                    teacher = teacher,
                                    isSelected = selectedTeacherId == teacher.id,
                                    accentColor = BrandGold,
                                    isDark = isDark,
                                    cardColor = cardColor,
                                    strokeColor = strokeColor,
                                    primaryText = primaryText,
                                    secondaryText = secondaryText,
                                    onClick = { selectedTeacherId = teacher.id },
                                )
                            }
                        }
                    }
                }

                // ─── Step 3: HSK Level ───────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CalcSectionHeader(number = 3, title = "Уровень HSK", primaryText = primaryText)

                    // 3x2 grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        levels.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                row.forEach { level ->
                                    CalcHskLevelCard(
                                        level = level,
                                        isSelected = selectedLevelId == level.id,
                                        accentColor = BrandTeal,
                                        isDark = isDark,
                                        cardColor = cardColor,
                                        strokeColor = strokeColor,
                                        primaryText = primaryText,
                                        secondaryText = secondaryText,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedLevelId = level.id },
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Result ──────────────────────────
                AnimatedVisibility(
                    visible = showResult,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    CalcResultCard(
                        formatName = selectedFormat?.name ?: "",
                        groupSizeName = selectedGroupSize?.name,
                        teacherName = selectedTeacher?.name,
                        levelName = selectedLevel?.name ?: "",
                        lessonsPerMonth = lessonsPerMonth,
                        monthlyPrice = monthlyPrice,
                        isDark = isDark,
                        cardColor = cardColor,
                        strokeColor = strokeColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                    )
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

// ─── Section Header ──────────────────────────────────────────

@Composable
private fun CalcSectionHeader(
    number: Int,
    title: String,
    primaryText: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(BrandTeal.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$number",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrandTeal,
            )
        }
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText,
        )
    }
}

// ─── Format Card ─────────────────────────────────────────────

@Composable
private fun CalcFormatCard(
    format: CalcFormat,
    isSelected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val border = if (isSelected) BorderStroke(2.dp, accentColor) else BorderStroke(1.dp, strokeColor)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else secondaryText,
                    modifier = Modifier.size(24.dp),
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                format.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                format.description,
                fontSize = 12.sp,
                color = secondaryText,
                lineHeight = 16.sp,
            )
        }
    }
}

// ─── Group Size Card ─────────────────────────────────────────

@Composable
private fun CalcGroupSizeCard(
    groupSize: CalcGroupSize,
    isSelected: Boolean,
    accentColor: Color,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val border = if (isSelected) BorderStroke(2.dp, accentColor) else BorderStroke(1.dp, strokeColor)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Spacer(Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                groupSize.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                groupSize.description,
                fontSize = 11.sp,
                color = secondaryText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Teacher Card ────────────────────────────────────────────

@Composable
private fun CalcTeacherCard(
    teacher: CalcTeacher,
    isSelected: Boolean,
    accentColor: Color,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit,
) {
    val border = if (isSelected) BorderStroke(2.dp, accentColor) else BorderStroke(1.dp, strokeColor)

    val teacherIcon = when (teacher.id) {
        "rukhsana" -> Icons.Default.Star
        "native" -> Icons.Default.Flag
        else -> Icons.Default.School
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    teacherIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    teacher.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    teacher.description,
                    fontSize = 12.sp,
                    color = secondaryText,
                )
            }

            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ─── HSK Level Card ──────────────────────────────────────────

@Composable
private fun CalcHskLevelCard(
    level: CalcLevel,
    isSelected: Boolean,
    accentColor: Color,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) accentColor else cardColor
    val border = if (isSelected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    val titleColor = if (isSelected) Color.White else primaryText
    val subtitleColor = if (isSelected) Color.White.copy(alpha = 0.9f) else secondaryText

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Spacer(Modifier.size(16.dp))
                }
            }

            Text(
                level.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                levelSubtitles[level.id] ?: "",
                fontSize = 11.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Result Card ─────────────────────────────────────────────

@Composable
private fun CalcResultCard(
    formatName: String,
    groupSizeName: String?,
    teacherName: String?,
    levelName: String,
    lessonsPerMonth: Int,
    monthlyPrice: Int,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
        shadowElevation = if (isDark) 0.dp else 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = BrandGold,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    "Результат расчета",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
            }

            // Details
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CalcResultRow("Формат:", formatName, primaryText, secondaryText)
                if (groupSizeName != null) {
                    CalcResultRow("Размер группы:", groupSizeName, primaryText, secondaryText)
                }
                if (teacherName != null) {
                    CalcResultRow("Преподаватель:", teacherName, primaryText, secondaryText)
                }
                CalcResultRow("Уровень:", levelName, primaryText, secondaryText)
                CalcResultRow("Уроков в месяц:", "$lessonsPerMonth", primaryText, secondaryText)
            }

            // Divider
            HorizontalDivider(
                color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f),
            )

            // Price
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.3f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "${formatSum(monthlyPrice)} UZS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "в месяц",
                        fontSize = 14.sp,
                        color = secondaryText,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcResultRow(
    label: String,
    value: String,
    primaryText: Color,
    secondaryText: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = secondaryText,
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText,
        )
    }
}
