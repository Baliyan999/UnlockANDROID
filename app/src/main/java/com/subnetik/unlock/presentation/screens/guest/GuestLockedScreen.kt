package com.subnetik.unlock.presentation.screens.guest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import javax.inject.Inject

@HiltViewModel
class GuestLockedViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val isDarkTheme = settingsDataStore.isDarkTheme
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestLockedScreen(
    title: String,
    message: String,
    onLogin: () -> Unit,
    viewModel: GuestLockedViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val bgColor = if (isDark) DarkNavy else Color(0xFFF5F6FA)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ---- Top Bar ----
            TopAppBar(
                title = {
                    Text(
                        title,
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Brand.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
            ) {
                // ---- Header Card: Icon + Title + Subtitle ----
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Brand.Shapes.extraLarge,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, strokeColor),
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1A2151).copy(alpha = if (isDark) 1f else 0.1f),
                                        Color(0xFF0F1429).copy(alpha = if (isDark) 1f else 0.1f),
                                    )
                                )
                            )
                            .padding(Brand.Spacing.xl),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        BrandBlue.copy(alpha = 0.15f),
                                        RoundedCornerShape(14.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(26.dp),
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A1A2E),
                                )
                                Text(
                                    "\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u043B\u044C\u043D\u044B\u0439 \u043A\u0430\u0431\u0438\u043D\u0435\u0442 \u0443\u0447\u0435\u043D\u0438\u043A\u0430",
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280),
                                )
                            }
                        }
                    }
                }

                // ---- Description Card ----
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Brand.Shapes.extraLarge,
                    color = cardColor,
                    border = BorderStroke(1.dp, strokeColor),
                ) {
                    Column(
                        modifier = Modifier.padding(Brand.Spacing.xl),
                    ) {
                        Text(
                            "\u041F\u043E\u0441\u043B\u0435 \u0432\u0445\u043E\u0434\u0430 \u0437\u0434\u0435\u0441\u044C \u0431\u0443\u0434\u0443\u0442 \u0437\u0430\u0434\u0430\u043D\u0438\u044F \u0438 \u0437\u0430\u043F\u0438\u0441\u044C \u043A Support-\u043F\u0440\u0435\u043F\u043E\u0434\u0430\u0432\u0430\u0442\u0435\u043B\u044E.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                            textAlign = TextAlign.Start,
                            lineHeight = 22.sp,
                        )
                    }
                }

                // ---- Features Section ----
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Brand.Shapes.extraLarge,
                    color = cardColor,
                    border = BorderStroke(1.dp, strokeColor),
                ) {
                    Column(
                        modifier = Modifier.padding(Brand.Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
                    ) {
                        Text(
                            "\u0427\u0442\u043E \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u043E \u043D\u0430 \u044D\u0442\u043E\u0439 \u0441\u0442\u0440\u0430\u043D\u0438\u0446\u0435",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = primaryText,
                        )

                        FeatureItem(
                            text = "\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430 \u0434\u043E\u043C\u0430\u0448\u043D\u0435\u0439 \u0440\u0430\u0431\u043E\u0442\u044B \u0444\u0430\u0439\u043B\u0430\u043C\u0438 \u0434\u043E 20 \u041C\u0411",
                            isDark = isDark,
                            primaryText = primaryText,
                        )

                        FeatureItem(
                            text = "\u0420\u0430\u0431\u043E\u0442\u0430 \u0441 \u0433\u0440\u0443\u043F\u043F\u043E\u0439, \u0434\u0435\u0434\u043B\u0430\u0439\u043D\u0430\u043C\u0438 \u0438 \u043E\u0446\u0435\u043D\u043A\u0430\u043C\u0438",
                            isDark = isDark,
                            primaryText = primaryText,
                        )

                        FeatureItem(
                            text = "\u0417\u0430\u043F\u0438\u0441\u044C \u043A Support-\u043F\u0440\u0435\u043F\u043E\u0434\u0430\u0432\u0430\u0442\u0435\u043B\u044F\u043C",
                            isDark = isDark,
                            primaryText = primaryText,
                        )
                    }
                }

                // Login button removed per request

                Spacer(Modifier.height(Brand.Spacing.xxl))
            }
        }
    }
}

@Composable
private fun FeatureItem(
    text: String,
    isDark: Boolean,
    primaryText: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    BrandGreen.copy(alpha = 0.15f),
                    RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(18.dp),
            )
        }

        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = primaryText,
            lineHeight = 20.sp,
        )
    }
}
