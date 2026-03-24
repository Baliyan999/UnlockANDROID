package com.subnetik.unlock.presentation.screens.promocode

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.data.remote.dto.admin.PromocodeRedemptionResponse
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodesScreen(
    onBack: () -> Unit,
    viewModel: PromocodesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0D0D1A) else Color(0xFFF5F5FA)
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF6B6B80)
    val cardColor = if (isDark) Color(0xFF1A1A2E) else Color.White
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Промокод", fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Input section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, strokeColor),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Введите промокод", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = uiState.code,
                                onValueChange = { viewModel.onCodeChange(it.uppercase()) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Код промокода") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                            )
                            Button(
                                onClick = { viewModel.validateAndRedeem() },
                                enabled = uiState.code.isNotBlank() && !uiState.isValidating,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            ) {
                                if (uiState.isValidating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("OK", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        // Status message
                        uiState.statusMessage?.let { msg ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                msg,
                                fontSize = 13.sp,
                                color = if (uiState.isSuccess) BrandGreen else BrandCoral,
                            )
                        }
                    }
                }

                // History section
                if (uiState.isLoadingHistory) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp),
                    )
                } else if (uiState.redemptions.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                            Text("История промокодов", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)
                        }
                        Surface(shape = CircleShape, color = BrandGold) {
                            Text(
                                "${uiState.redemptions.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    uiState.redemptions.forEach { redemption ->
                        RedemptionCard(redemption = redemption, isDark = isDark)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RedemptionCard(
    redemption: PromocodeRedemptionResponse,
    isDark: Boolean,
) {
    val cardColor = if (isDark) Color(0xFF1A1A2E) else Color.White
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF6B6B80)

    val iconColor = when (redemption.couponType) {
        "tokens" -> BrandGold
        "discount_percent", "discount_amount" -> BrandIndigo
        "market_item" -> BrandTeal
        else -> BrandBlue
    }
    val icon = when (redemption.couponType) {
        "tokens" -> Icons.Default.MonetizationOn
        "discount_percent", "discount_amount" -> Icons.Default.Percent
        "market_item" -> Icons.Default.CardGiftcard
        else -> Icons.Default.ConfirmationNumber
    }

    // Format date: "2026-03-21T23:58:53" -> "21 марта 2026"
    val formattedDate = formatRedemptionDate(redemption.effectiveDate)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(redemption.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryText)
                if (redemption.effectiveDescription.isNotBlank()) {
                    Text(redemption.effectiveDescription, fontSize = 12.sp, color = secondaryText)
                }
            }

            if (formattedDate.isNotEmpty()) {
                Text(formattedDate, fontSize = 11.sp, color = secondaryText)
            }
        }
    }
}

private fun formatRedemptionDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    val months = arrayOf("", "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря")
    // "2026-03-21T23:58:53" -> extract date part
    val datePart = dateStr.substringBefore("T")
    val parts = datePart.split("-")
    if (parts.size != 3) return datePart
    val year = parts[0]
    val month = parts[1].toIntOrNull() ?: return datePart
    val day = parts[2].toIntOrNull() ?: return datePart
    val monthName = if (month in 1..12) months[month] else ""
    return "$day $monthName $year"
}
