package com.subnetik.unlock.presentation.screens.contact

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onBack: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val bgTop = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC)
    val bgBottom = if (isDark) Color(0xFF1A1E33) else Color(0xFFE6EDF8)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контакты", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(bgTop, bgBottom))),
            )

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

                // Header icon + title
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
                )
                Text(
                    "Мы всегда на связи!\nВыберите удобный способ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Brand.Spacing.sm))

                // Phone card
                ContactCard(
                    icon = Icons.Default.Phone,
                    title = "Телефон",
                    subtitle = "+998772686886",
                    tint = BrandBlue,
                    isDark = isDark,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+998772686886")))
                    },
                )

                // WhatsApp card
                ContactCard(
                    icon = Icons.Default.Textsms,
                    title = "WhatsApp",
                    subtitle = "+998772686886",
                    tint = BrandTeal,
                    isDark = isDark,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/998772686886")))
                    },
                )

                // Telegram card
                ContactCard(
                    icon = Icons.AutoMirrored.Filled.Send,
                    title = "Telegram",
                    subtitle = "@unlock_language",
                    tint = BrandIndigo,
                    isDark = isDark,
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/unlock_language")))
                    },
                )

                Spacer(Modifier.height(Brand.Spacing.sm))

                // Google Maps section
                GoogleMapSection(isDark = isDark, context = context)

                Spacer(Modifier.height(Brand.Spacing.sm))

                // Footer
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )

                Spacer(Modifier.height(Brand.Spacing.xl))
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
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF171E33).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.98f)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, tint.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(tint.copy(alpha = 0.2f), tint.copy(alpha = 0.08f)),
                        ),
                        RoundedCornerShape(14.dp),
                    ),
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
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun GoogleMapSection(isDark: Boolean, context: android.content.Context) {
    val cardBg = if (isDark) Color(0xFF171E33).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.98f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BrandCoral.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)),
    ) {
        // Google Maps WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    // Disable scrolling inside the WebView
                    setOnTouchListener { _, _ -> true }
                    loadUrl("https://www.google.com/maps/@41.304608,69.267618,17z")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )

        // Address bar
        Surface(
            onClick = {
                val uri = Uri.parse("geo:41.304608,69.267618?q=41.304608,69.267618(UNLOCK Language Studio)")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            },
            color = cardBg,
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
                    )
                    Text(
                        "ул. Якуба Коласа, 2/1, Ташкент",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
