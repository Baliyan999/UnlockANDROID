package com.subnetik.unlock.presentation.screens.referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    onBack: () -> Unit = {},
    viewModel: ReferralViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val context = LocalContext.current

    val bgColor = if (isDark) Color(0xFF0D0D1A) else Color(0xFFF5F5FA)
    val cardColor = if (isDark) Color(0xFF1A1A2E) else Color.White
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B6B80)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Реферальная программа", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = bgColor,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ─── Hero Card ──────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2A2A4A), Color(0xFF1A1A35))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(BrandGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = BrandGold, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Приглашай друзей", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                            Text("Получай токены за каждого нового ученика", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandGold)
                }
            } else if (uiState.referralInfo != null) {
                val info = uiState.referralInfo!!

                // ─── Referral Code Card ─────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, strokeColor),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Ваш реферальный код", fontSize = 14.sp, color = secondaryText)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            // Code
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    info.referralCode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = BrandGold,
                                    letterSpacing = 2.sp,
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Copy code button
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("referral", info.referralCode))
                                            Toast.makeText(context, "Код скопирован!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Код", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }

                                    // Share link button
                                    OutlinedButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Присоединяйся к Unlock! Используй мой реферальный код: ${info.referralCode}\n${info.effectiveLink}")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Поделиться"))
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, BrandGold),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandGold)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Ссылка", color = BrandGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    }
                                }
                            }

                            // QR Code
                            val qrBitmap = remember(info.effectiveLink) {
                                generateQrCode(info.effectiveLink, 400)
                            }
                            qrBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR код",
                                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                    }
                }

                // ─── Stats Row ──────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        value = "${info.effectiveInvited}",
                        label = "ПРИГЛАШЕНО",
                        color = BrandBlue,
                        cardColor = cardColor,
                        strokeColor = strokeColor,
                        primaryText = primaryText,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = "${info.rewardedTotal}",
                        label = "СТАЛИ УЧЕНИКАМИ",
                        color = BrandGreen,
                        cardColor = cardColor,
                        strokeColor = strokeColor,
                        primaryText = primaryText,
                        modifier = Modifier.weight(1f),
                    )
                }

                // ─── How it Works ───────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, strokeColor),
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Как это работает", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)

                        HowItWorksStep(
                            icon = Icons.Default.Link,
                            iconColor = BrandGold,
                            text = "Поделитесь ссылкой с другом",
                            primaryText = primaryText,
                        )
                        HowItWorksStep(
                            icon = Icons.Default.PersonAdd,
                            iconColor = BrandCoral,
                            text = "Друг регистрируется по вашей ссылке",
                            primaryText = primaryText,
                        )
                        HowItWorksStep(
                            icon = Icons.Default.CardGiftcard,
                            iconColor = BrandGold,
                            text = "Когда друг становится учеником, вы получаете ${info.rewardForReferrer} токенов, друг – ${info.rewardForInvited}",
                            primaryText = primaryText,
                        )
                    }
                }
            } else if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = BrandCoral.copy(alpha = 0.1f),
                ) {
                    Text(
                        uiState.errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = BrandCoral,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    value: String, label: String, color: Color,
    cardColor: Color, strokeColor: Color, primaryText: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = primaryText)
            Text(label, fontSize = 11.sp, color = primaryText.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HowItWorksStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    text: String,
    primaryText: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = primaryText, modifier = Modifier.weight(1f))
    }
}

private fun generateQrCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (_: Exception) {
        null
    }
}
