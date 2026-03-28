package com.subnetik.unlock.presentation.screens.payment

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.R
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@Composable
fun PaymentScreen(
    onBack: () -> Unit,
    onNavigateToMyPayments: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    val paynetGreen = Color(0xFF00A650)
    val context = LocalContext.current

    var showReceiptUpload by remember { mutableStateOf(false) }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadReceipt(context, uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText)
                }
                Text(
                    "Оплата",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Brand.Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
            ) {
                Spacer(Modifier.height(Brand.Spacing.sm))

                // ─── Header ──────────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(listOf(paynetGreen.copy(alpha = 0.2f), BrandTeal.copy(alpha = 0.15f))),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(30.dp), tint = paynetGreen)
                }

                Text("Оплата обучения", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = primaryText)

                // region QR-payment section – hidden, not deleted
                if (false) {
                Text(
                    "Отсканируйте QR-код любым\nплатёжным приложением",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                )

                // ─── Info Section ────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = secondaryText)
                            Text("Как оплатить", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = secondaryText)
                        }
                        Spacer(Modifier.height(10.dp))
                        InfoRow("1", "Откройте платёжное приложение на телефоне", paynetGreen, secondaryText)
                        Spacer(Modifier.height(6.dp))
                        InfoRow("2", "Отсканируйте QR-код ниже", paynetGreen, secondaryText)
                        Spacer(Modifier.height(6.dp))
                        InfoRow("3", "Оплатите и загрузите квитанцию", paynetGreen, secondaryText)
                    }
                }

                // ─── QR Code Section ─────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, paynetGreen.copy(alpha = 0.12f)),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.payment_qr),
                            contentDescription = "QR код для оплаты",
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .shadow(12.dp, RoundedCornerShape(16.dp))
                                .background(Color.White, RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit,
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            "Сканируйте любым платёжным приложением",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaymentBadge("Paynet", paynetGreen)
                            PaymentBadge("Payme", Color(0xFF00BDD9))
                            PaymentBadge("Click", Color(0xFF2678DE))
                            PaymentBadge("Uzum", Color(0xFF8C3DD9))
                        }
                    }
                }
                } // end hidden QR-payment section
                // endregion

                // ─── Upload Button (students only) ───────
                if (uiState.isStudent) {
                    Button(
                        onClick = { showReceiptUpload = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(BrandGreen, BrandTeal)),
                                    RoundedCornerShape(18.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Я оплатил(а)", fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // My Payments Link
                    Surface(
                        onClick = onNavigateToMyPayments,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = cardColor,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandBlue)
                            Spacer(Modifier.width(10.dp))
                            Text("Мои оплаты", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = secondaryText.copy(alpha = 0.5f))
                        }
                    }
                }

                // Upload status
                if (uiState.uploadSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = BrandGreen.copy(alpha = 0.08f),
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Квитанция загружена и отправлена на проверку", style = MaterialTheme.typography.bodySmall, color = BrandGreen)
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))
            }
        }
    }

    // Receipt upload bottom sheet
    if (showReceiptUpload) {
        ReceiptUploadSheet(
            isDark = isDark,
            onDismiss = { showReceiptUpload = false },
            onPickFromGallery = {
                showReceiptUpload = false
                imagePickerLauncher.launch("image/*")
            },
        )
    }
}

@Composable
private fun InfoRow(number: String, text: String, accentColor: Color, textColor: Color) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(number, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
        Text(text, style = MaterialTheme.typography.bodySmall, color = textColor)
    }
}

@Composable
private fun PaymentBadge(name: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.1f)) {
        Text(
            name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptUploadSheet(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    val primaryText = if (isDark) Color.White else Color.Black
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1A1A2E) else Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Brand.Spacing.lg)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text("Закрыть", color = BrandGreen) }
                Text("Загрузить квитанцию", fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.width(60.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BrandGreen.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.FindInPage, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(30.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text("Подтверждение оплаты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
            Spacer(Modifier.height(8.dp))
            Text(
                "Загрузите скриншот или фото квитанции.\nШи Фу автоматически определит сумму.",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            // Gallery button
            Surface(
                onClick = onPickFromGallery,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Выбрать из галереи", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Скриншот или сохранённое фото", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = secondaryText)
                }
            }
        }
    }
}
