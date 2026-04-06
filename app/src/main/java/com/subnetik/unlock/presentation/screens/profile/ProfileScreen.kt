package com.subnetik.unlock.presentation.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.subnetik.unlock.R
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToContact: () -> Unit,
    onNavigateToShiFu: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {},
    onNavigateToPromocodes: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background with glow circles (like iOS LeadBackground)
        LeadBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = Brand.Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Settings button (top-right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                SettingsChipButton(
                    isDark = isDark,
                    onClick = { showSettings = true },
                )
            }

            // Avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { showAvatarSheet = true },
                ) {
                    val avatarUrl = uiState.avatarUrl
                    if (avatarUrl != null && avatarUrl.isNotBlank()) {
                        val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl
                            else "https://unlocklingua.com$avatarUrl"
                        var isError by remember(fullUrl) { mutableStateOf(false) }
                        if (isError) {
                            // Fallback: letter avatar
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(BrandBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = (uiState.displayName?.firstOrNull() ?: 'U').uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue,
                                )
                            }
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(fullUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                onError = { isError = true },
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape),
                            )
                        }
                    } else {
                        // No avatar — show letter
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(BrandBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (uiState.displayName?.firstOrNull() ?: 'U').uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue,
                            )
                        }
                    }
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier
                            .size(22.dp)
                            .offset(x = 4.dp, y = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                CircleShape,
                            )
                            .padding(3.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Welcome card
            GlassCard(isDark = isDark) {
                Column(
                    modifier = Modifier.padding(Brand.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
                ) {
                    val displayName = uiState.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: "Пользователь"
                    Text(
                        text = "Добро пожаловать, $displayName",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    )

                    // Role badge (only for admin)
                    if (uiState.role?.lowercase() == "admin") {
                        Surface(
                            shape = Brand.Shapes.full,
                            color = BrandCoral.copy(alpha = 0.2f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(BrandCoral, CircleShape),
                                )
                                Text(
                                    text = "Администратор",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCoral,
                                )
                            }
                        }
                    }

                    // Email
                    uiState.email?.takeIf { it.isNotEmpty() }?.let { email ->
                        Text(
                            text = "Логин: $email",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Logout button
                    // Paynet button for students
                    val isStudentRole = uiState.role?.lowercase() == "student"
                    if (isStudentRole) {
                        Button(
                            onClick = onNavigateToPayment,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(BrandGreen, BrandTeal)),
                                        RoundedCornerShape(12.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Оплата", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Promo code button (only for students and users)
                    if (uiState.role?.lowercase() in listOf("student", "user")) {
                        Button(
                            onClick = onNavigateToPromocodes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGold,
                                contentColor = Color.White,
                            ),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Промокод", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.85f),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            "Выйти",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Shi Fu card (admin only)
            val isAdmin = uiState.role?.lowercase() == "admin"
            if (isAdmin) {
                GlassCard(isDark = isDark, onClick = onNavigateToShiFu) {
                    Row(
                        modifier = Modifier.padding(Brand.Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.snow_leopard_wave),
                            contentDescription = "Ши Фу",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Помощник Ши Фу",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "AI ассистент по китайскому языку",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Contact Us card
            GlassCard(isDark = isDark, onClick = onNavigateToContact) {
                Row(
                    modifier = Modifier.padding(Brand.Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        BrandBlue.copy(alpha = 0.18f),
                                        BrandTeal.copy(alpha = 0.12f),
                                    ),
                                ),
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = BrandBlue,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Связаться с нами",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Телефон, WhatsApp, Telegram",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Footer text
            Text(
                text = "На главной вы всегда сможете вернуться к курсам\nи заданиям.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // Settings bottom sheet
    if (showSettings) {
        SettingsSheet(
            uiState = uiState,
            isDark = isDark,
            onDismiss = { showSettings = false },
            onToggleTheme = { viewModel.toggleTheme(it) },
            onSaveProfile = { name, email, currentPassword, newPassword ->
                viewModel.updateProfile(name, email, currentPassword, newPassword)
            },
            onClearMessages = { viewModel.clearMessages() },
        )
    }

    // Avatar action sheet
    if (showAvatarSheet) {
        AvatarActionSheet(
            isDark = isDark,
            hasAvatar = uiState.avatarUrl != null,
            onDismiss = { showAvatarSheet = false },
            onPickImage = {
                showAvatarSheet = false
                imagePickerLauncher.launch("image/*")
            },
            onDeleteAvatar = {
                showAvatarSheet = false
                viewModel.deleteAvatar()
            },
        )
    }
}

// ─── Avatar Action Sheet ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarActionSheet(
    isDark: Boolean,
    hasAvatar: Boolean,
    onDismiss: () -> Unit,
    onPickImage: () -> Unit,
    onDeleteAvatar: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Фото профиля",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Change / Set photo
            Surface(
                onClick = onPickImage,
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = BrandBlue,
                    )
                    Text(
                        if (hasAvatar) "Изменить фото" else "Выбрать фото",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                    )
                }
            }

            // Delete photo (only if has avatar)
            if (hasAvatar) {
                Surface(
                    onClick = onDeleteAvatar,
                    shape = RoundedCornerShape(14.dp),
                    color = BrandCoral.copy(alpha = 0.08f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = BrandCoral,
                        )
                        Text(
                            "Удалить фото",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandCoral,
                        )
                    }
                }
            }

            // Cancel
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Отмена",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = subtextColor,
                    )
                }
            }
        }
    }
}

// ─── LeadBackground ───────────────────────────────────────────

@Composable
private fun LeadBackground(isDark: Boolean) {
    val bgTop = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC)
    val bgBottom = if (isDark) Color(0xFF1A1E33) else Color(0xFFE6EDF8)
    val glowBlue = if (isDark) BrandBlue.copy(alpha = 0.18f) else BrandBlue.copy(alpha = 0.10f)
    val glowPurple = if (isDark) Color(0xFF8552DB).copy(alpha = 0.18f) else Color(0xFF8552DB).copy(alpha = 0.10f)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(bgTop, bgBottom))),
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-70).dp, y = (-60).dp)
                .background(glowBlue, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .background(glowPurple, CircleShape),
        )
    }
}

// ─── Glass Card ───────────────────────────────────────────────

@Composable
private fun GlassCard(
    isDark: Boolean,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF171E33).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.98f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, strokeColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        content()
    }
}

// ─── Settings Chip Button ─────────────────────────────────────

@Composable
private fun SettingsChipButton(isDark: Boolean, onClick: () -> Unit) {
    val bg = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val stroke = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = Brand.Shapes.full,
        color = bg,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(stroke, stroke)),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor,
            )
            Text(
                "Настройки",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}

// ─── Settings Bottom Sheet ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    uiState: ProfileUiState,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onSaveProfile: (String?, String?, String?, String?) -> Unit,
    onClearMessages: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            onClearMessages()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
    ) {
        SettingsSheetContent(
            uiState = uiState,
            isDark = isDark,
            onDismiss = {
                onClearMessages()
                onDismiss()
            },
            onToggleTheme = onToggleTheme,
            onSaveProfile = onSaveProfile,
        )
    }
}

// ─── Language data ───────────────────────────────────────────

private data class AppLanguage(
    val code: String,
    val flag: String,
    val shortLabel: String,
    val title: String,
)

private val languages = listOf(
    AppLanguage("ru", "\uD83C\uDDF7\uD83C\uDDFA", "RU", "Русский"),
    AppLanguage("en", "\uD83C\uDDFA\uD83C\uDDF8", "EN", "English"),
    AppLanguage("uz", "\uD83C\uDDFA\uD83C\uDDFF", "UZ", "Uzbek"),
    AppLanguage("zh", "\uD83C\uDDE8\uD83C\uDDF3", "ZH", "中文"),
    AppLanguage("ko", "\uD83C\uDDF0\uD83C\uDDF7", "KO", "한국어"),
)

@Composable
private fun SettingsSheetContent(
    uiState: ProfileUiState,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onSaveProfile: (String?, String?, String?, String?) -> Unit,
) {
    var isAccountExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var editableName by remember(uiState.displayName) { mutableStateOf(uiState.displayName ?: "") }
    var editableLogin by remember(uiState.email) { mutableStateOf(uiState.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("ru") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isAdmin = uiState.role?.lowercase() == "admin"
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f)
    val fieldStroke = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

    // Clear password fields on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Настройки аккаунта",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }

        // Success message
        uiState.successMessage?.let { msg ->
            Surface(
                color = BrandGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = BrandGreen)
                }
            }
        }

        // Error message
        (validationError ?: uiState.error)?.let { msg ->
            Surface(
                color = BrandCoral.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Error, null, tint = BrandCoral, modifier = Modifier.size(18.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = BrandCoral)
                }
            }
        }

        // ─── Account Settings Card (expandable) ─────────────
        GlassCard(isDark = isDark) {
            Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
                // Expandable header
                Surface(
                    onClick = { isAccountExpanded = !isAccountExpanded },
                    color = fieldBg,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BrandBlue,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Настройки аккаунта",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            )
                            Text(
                                if (isAccountExpanded) "Имя, логин и пароль"
                                else "Нажмите, чтобы открыть",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtextColor,
                            )
                        }
                        Icon(
                            if (isAccountExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = subtextColor,
                        )
                    }
                }

                // Expanded content
                AnimatedVisibility(
                    visible = isAccountExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier.padding(top = Brand.Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                    ) {
                        // Name field
                        OutlinedTextField(
                            value = editableName,
                            onValueChange = { editableName = it },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        // Login/email field (now editable)
                        OutlinedTextField(
                            value = editableLogin,
                            onValueChange = { editableLogin = it.trim() },
                            label = { Text("Логин") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )

                        // Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = fieldStroke,
                        )

                        // Current password
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Текущий пароль") },
                            supportingText = {
                                Text(
                                    "Нужен только если меняете логин или пароль",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )

                        // New password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Новый пароль") },
                            supportingText = {
                                Text(
                                    "Минимум 8 символов, буквы и цифры",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )

                        // Confirm password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Повторите новый пароль") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )

                        Spacer(Modifier.height(4.dp))

                        // Save button
                        Button(
                            onClick = {
                                validationError = null
                                // Validate
                                val nameChanged = editableName != (uiState.displayName ?: "")
                                val loginChanged = editableLogin != (uiState.email ?: "")
                                val passwordChanging = newPassword.isNotEmpty()

                                if (!nameChanged && !loginChanged && !passwordChanging) {
                                    validationError = "Нет изменений для сохранения"
                                    return@Button
                                }
                                if (editableName.length < 2 || editableName.length > 20) {
                                    validationError = "Имя должно быть от 2 до 20 символов"
                                    return@Button
                                }
                                if (loginChanged && (editableLogin.length < 3 || editableLogin.contains(" "))) {
                                    validationError = "Логин: минимум 3 символа, без пробелов"
                                    return@Button
                                }
                                if ((loginChanged || passwordChanging) && currentPassword.isEmpty()) {
                                    validationError = "Введите текущий пароль для смены логина или пароля"
                                    return@Button
                                }
                                if (passwordChanging) {
                                    if (newPassword.length < 8) {
                                        validationError = "Новый пароль: минимум 8 символов"
                                        return@Button
                                    }
                                    val hasLetter = newPassword.any { it.isLetter() }
                                    val hasDigit = newPassword.any { it.isDigit() }
                                    if (!hasLetter || !hasDigit) {
                                        validationError = "Пароль должен содержать буквы и цифры"
                                        return@Button
                                    }
                                    if (newPassword != confirmPassword) {
                                        validationError = "Пароли не совпадают"
                                        return@Button
                                    }
                                }

                                onSaveProfile(
                                    editableName.takeIf { nameChanged },
                                    editableLogin.takeIf { loginChanged },
                                    currentPassword.takeIf { it.isNotEmpty() },
                                    newPassword.takeIf { passwordChanging },
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading && editableName.isNotBlank(),
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Сохранить профиль", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // ─── Theme & Language Card ───────────────────────────
        GlassCard(isDark = isDark) {
            Column(
                modifier = Modifier.padding(Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                Text(
                    "Тема и язык",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )

                // Theme toggle
                Surface(
                    color = fieldBg,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.linearGradient(listOf(fieldStroke, fieldStroke)),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BrandBlue,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Тема интерфейса",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                            )
                            Text(
                                if (isDark) "Темная" else "Светлая",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtextColor,
                            )
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { onToggleTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandBlue,
                            ),
                        )
                    }
                }

                // Language section
                Surface(
                    onClick = { if (isAdmin) isLanguageExpanded = !isLanguageExpanded },
                    color = fieldBg,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.linearGradient(listOf(fieldStroke, fieldStroke)),
                    ),
                ) {
                    Column {
                        if (isAdmin) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = BrandBlue,
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Язык интерфейса",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                )
                                Text(
                                    run {
                                        val lang = languages.find { it.code == selectedLanguage }
                                        "${lang?.flag} ${lang?.title}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtextColor,
                                )
                            }
                                Icon(
                                    if (isLanguageExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = subtextColor,
                                )
                        }

                        // Expandable language grid (admin only)
                        AnimatedVisibility(
                            visible = isLanguageExpanded && isAdmin,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                languages.forEach { lang ->
                                    val isSelected = lang.code == selectedLanguage
                                    Surface(
                                        onClick = { selectedLanguage = lang.code },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) {
                                            BrandBlue.copy(alpha = 0.15f)
                                        } else {
                                            if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)
                                        },
                                        border = if (isSelected) {
                                            ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                                brush = Brush.linearGradient(listOf(BrandBlue.copy(alpha = 0.4f), BrandTeal.copy(alpha = 0.4f))),
                                            )
                                        } else null,
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Text(lang.flag, fontSize = 20.sp)
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                lang.shortLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) BrandBlue else subtextColor,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        } // end if (isAdmin) for language
                    }
                }
            }
        }

        // ─── Documents Card (hidden for plain users) ─────────────────────────────────
        val isUserRole = uiState.role?.lowercase() == "user" || uiState.role == null
        val isTeacherOrAdmin = uiState.role?.lowercase() in listOf("teacher", "admin")
        if (!isUserRole) {
        val allStudentDocs = com.subnetik.unlock.presentation.screens.terms.getStudentDocuments()
        val teacherDocs = com.subnetik.unlock.presentation.screens.terms.getTeacherDocuments()
        val isAdmin = uiState.role?.lowercase() == "admin"
        // Teachers see only privacy + termsOfUse in general section; admins and students see all 3
        val generalDocs = if (isAdmin || !isTeacherOrAdmin) allStudentDocs
            else allStudentDocs.filter { it.id in listOf("privacy", "termsOfUse") }
        var activeViewerDoc by remember { mutableStateOf<com.subnetik.unlock.presentation.screens.terms.TermsDocument?>(null) }

        GlassCard(isDark = isDark) {
            Column(
                modifier = Modifier.padding(Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
            ) {
                // General documents section
                Text(
                    if (isTeacherOrAdmin) "Общие документы" else "Документы",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )

                generalDocs.forEach { doc ->
                    Surface(
                        onClick = { activeViewerDoc = doc },
                        color = fieldBg,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                doc.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = doc.tint,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                doc.title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = subtextColor,
                            )
                        }
                    }
                }

                // Teacher/Admin: employment documents section
                if (isTeacherOrAdmin) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Трудовые документы",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )

                    teacherDocs.forEach { doc ->
                        Surface(
                            onClick = { activeViewerDoc = doc },
                            color = fieldBg,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    doc.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = doc.tint,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    doc.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor,
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = subtextColor,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Read-only document viewer dialog
        activeViewerDoc?.let { doc ->
            com.subnetik.unlock.presentation.screens.terms.ReadOnlyDocumentViewer(
                doc = doc,
                onDismiss = { activeViewerDoc = null },
            )
        }

        } // end if (!isUserRole) documents

        // ─── Danger Zone ────────────────────────────────────
        GlassCard(isDark = isDark) {
            Column(
                modifier = Modifier.padding(Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
            ) {
                Text(
                    "Опасная зона",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandCoral,
                )

                var showDeleteConfirm by remember { mutableStateOf(false) }

                Button(
                    onClick = { showDeleteConfirm = !showDeleteConfirm },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandCoral.copy(alpha = 0.12f),
                        contentColor = BrandCoral,
                    ),
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Удалить аккаунт", fontWeight = FontWeight.SemiBold)
                }

                Text(
                    "Аккаунт и все связанные данные будут удалены навсегда.",
                    style = MaterialTheme.typography.labelSmall,
                    color = subtextColor,
                )
            }
        }
    }
}
