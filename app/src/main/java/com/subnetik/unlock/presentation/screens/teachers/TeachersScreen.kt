package com.subnetik.unlock.presentation.screens.teachers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
// FlowRow from foundation.layout.* (imported via wildcard above)
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject

// ─── Data ────────────────────────────────────────────────────

data class TeacherInfo(
    val name: String,
    val specialty: String,
    val avatar: String?,
    val accentColor: Color,
    val badge: String? = null,
) {
    val tags: List<String>
        get() = specialty.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val primaryFocus: String
        get() = tags.firstOrNull() ?: specialty

    val initials: String
        get() = name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull() }
            .joinToString("")
            .uppercase()

    val fullAvatarUrl: String?
        get() = avatar?.let { url ->
            if (url.startsWith("http")) url else "https://unlocklingua.com$url"
        }
}

val defaultTeachers = listOf(
    TeacherInfo(
        name = "Марьям Гапур",
        specialty = "HSK 1, технический китайский, разговорная практика",
        avatar = "/images/oogway-turtle.jpg",
        accentColor = BrandCoral,
    ),
    TeacherInfo(
        name = "Рухсана",
        specialty = "HSK 1–6, технический китайский, уникальный метод",
        avatar = "/images/shifu.jpg",
        accentColor = BrandIndigo,
        badge = "Уникальный метод",
    ),
    TeacherInfo(
        name = "Юлиана",
        specialty = "HSK 2–3, медицинский китайский",
        avatar = "/images/tigritsa.jpg",
        accentColor = BrandTeal,
    ),
)

// ─── ViewModel ───────────────────────────────────────────────

@HiltViewModel
class TeachersViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val isDarkTheme = settingsDataStore.isDarkTheme
}

// ─── Screen ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersScreen(
    onBack: () -> Unit = {},
    viewModel: TeachersViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()

    val primaryText = if (isDark) Color.White else Color.Black.copy(alpha = 0.9f)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        "Преподаватели",
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
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Header card
                TeachersHeaderCard(
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                )

                // Teacher cards
                defaultTeachers.forEach { teacher ->
                    TeacherCard(
                        teacher = teacher,
                        isDark = isDark,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        cardColor = cardColor,
                        strokeColor = strokeColor,
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Header Card ─────────────────────────────────────────────

@Composable
private fun TeachersHeaderCard(
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    cardColor: Color,
    strokeColor: Color,
) {
    val gradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF1C1D2B), Color(0xFF14151D)))
    } else {
        Brush.linearGradient(listOf(Color.White, MistBlue.copy(alpha = 0.7f)))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.extraLarge,
        color = Color.Transparent,
        border = BorderStroke(1.dp, strokeColor),
        shadowElevation = if (isDark) 0.dp else 4.dp,
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(18.dp),
        ) {
            Text(
                text = "Эксперты Unlock",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Подберем преподавателя под ваш уровень и цели.",
                fontSize = 14.sp,
                color = secondaryText,
            )
        }
    }
}

// ─── Teacher Card ────────────────────────────────────────────

@Composable
private fun TeacherCard(
    teacher: TeacherInfo,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
    cardColor: Color,
    strokeColor: Color,
) {
    val gradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF1E1F2E), Color(0xFF17181F)))
    } else {
        Brush.linearGradient(listOf(Color.White, MistBlue.copy(alpha = 0.5f)))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = Brand.Shapes.extraLarge,
        color = Color.Transparent,
        border = BorderStroke(1.dp, strokeColor),
        shadowElevation = if (isDark) 0.dp else 4.dp,
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top row: avatar + name + optional badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Avatar
                TeacherAvatar(
                    initials = teacher.initials,
                    avatarUrl = teacher.fullAvatarUrl,
                    accentColor = teacher.accentColor,
                )

                // Name & level
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = teacher.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Text(
                        text = teacher.primaryFocus,
                        fontSize = 12.sp,
                        color = secondaryText,
                    )
                }

                // Optional badge
                if (teacher.badge != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = teacher.accentColor,
                    ) {
                        Text(
                            text = teacher.badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            // Competencies
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Компетенции",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryText,
                )

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    teacher.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = teacher.accentColor.copy(alpha = 0.14f),
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = teacher.accentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Teacher Avatar ──────────────────────────────────────────

@Composable
private fun TeacherAvatar(
    initials: String,
    avatarUrl: String?,
    accentColor: Color,
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = initials,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initials,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
    }
}
