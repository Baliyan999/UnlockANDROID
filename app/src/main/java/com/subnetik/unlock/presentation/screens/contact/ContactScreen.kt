package com.subnetik.unlock.presentation.screens.contact

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import com.subnetik.unlock.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val isDarkTheme = settingsDataStore.isDarkTheme
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onBack: () -> Unit,
    viewModel: ContactViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: (MaterialTheme.colorScheme.surface.luminance() < 0.5f)
    val context = LocalContext.current

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Контакты",
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
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(Brand.Spacing.sm))

                // Header icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(BrandBlue.copy(alpha = 0.2f), BrandTeal.copy(alpha = 0.2f)),
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = BrandBlue,
                    )
                }
                Text(
                    "Связаться с нами",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = primaryText,
                )
                Text(
                    "Мы всегда на связи!\nВыберите удобный способ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Brand.Spacing.sm))

                // Phone
                ContactCard(
                    icon = Icons.Default.Phone,
                    title = "Телефон",
                    subtitle = "+998772686886",
                    tint = BrandBlue,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+998772686886")))
                    },
                )

                // WhatsApp
                ContactCard(
                    icon = Icons.Default.Textsms,
                    title = "WhatsApp",
                    subtitle = "+998772686886",
                    tint = BrandTeal,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/998772686886")))
                    },
                )

                // Telegram
                ContactCard(
                    icon = Icons.AutoMirrored.Filled.Send,
                    title = "Telegram",
                    subtitle = "@unlock_language",
                    tint = BrandIndigo,
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/unlock_language")))
                    },
                )

                Spacer(Modifier.height(Brand.Spacing.sm))

                // Google Maps section
                GoogleMapSection(
                    cardColor = cardColor,
                    strokeColor = strokeColor,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                    context = context,
                )

                Spacer(Modifier.height(Brand.Spacing.sm))

                // Footer social icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SocialIcon(icon = Icons.AutoMirrored.Filled.Send, tint = BrandIndigo)
                    SocialIcon(icon = Icons.Default.Textsms, tint = BrandTeal)
                    SocialIcon(icon = Icons.Default.Phone, tint = BrandBlue)
                }
                Text(
                    "UNLOCK Language Studio",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = secondaryText.copy(alpha = 0.5f),
                )

                Spacer(Modifier.height(Brand.Spacing.xxl))
            }
        }
    }
}

@Composable
private fun ContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(tint.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = tint,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = secondaryText.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun GoogleMapSection(
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    context: android.content.Context,
) {
    val openMap = {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode("41.304608,69.267618(UNLOCK Language Studio)")}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback without package restriction
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = strokeColor,
                shape = RoundedCornerShape(18.dp),
            ),
    ) {
        // Static map image — tapping opens Google Maps / default map app
        Surface(
            onClick = openMap,
            color = Color(0xFF1A1E33),
            shape = RoundedCornerShape(0.dp),
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.unlock_map_dark),
                contentDescription = "Карта — UNLOCK Language Studio",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
            )
        }

        // Address bar
        Surface(
            onClick = openMap,
            color = cardColor,
            shape = RoundedCornerShape(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BrandCoral.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = BrandCoral,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Central Palace, 6 этаж",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Text(
                        "ул. Якуба Коласа, 2/1, Ташкент",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                    )
                }

                Surface(
                    shape = Brand.Shapes.full,
                    color = BrandBlue.copy(alpha = 0.12f),
                ) {
                    Text(
                        "Маршрут",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(tint.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = tint.copy(alpha = 0.5f),
        )
    }
}
