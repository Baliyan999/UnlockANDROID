package com.subnetik.unlock.presentation.screens.payment

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.data.remote.dto.payment.PaymentEntryDto
import com.subnetik.unlock.data.remote.dto.payment.ReceiptDto
import com.subnetik.unlock.data.remote.dto.payment.StudentPaymentInfoResponse
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StudentPaymentsScreen(
    onBack: () -> Unit,
    viewModel: StudentPaymentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText) }
                Text("Мои оплаты", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.width(48.dp))
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (uiState.error != null) {
                ErrorCard(uiState.error!!, primaryText, secondaryText, cardColor) { viewModel.reload() }
            } else {
                val info = uiState.paymentInfo
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Brand.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
                ) {
                    Spacer(Modifier.height(Brand.Spacing.sm))

                    if (info != null) {
                        ObligationCard(info, isDark, primaryText, secondaryText, cardColor, strokeColor)
                    }

                    if (info != null && info.pendingReceipts.isNotEmpty()) {
                        PendingSection(info.pendingReceipts, isDark, primaryText, secondaryText, cardColor)
                    }

                    HistorySection(info?.payments ?: emptyList(), isDark, primaryText, secondaryText, cardColor)

                    Spacer(Modifier.height(Brand.Spacing.xl))
                }
            }
        }
    }
}

@Composable
private fun ObligationCard(info: StudentPaymentInfoResponse, isDark: Boolean, primaryText: Color, secondaryText: Color, cardColor: Color, strokeColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = cardColor,
        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(Brush.linearGradient(listOf(BrandBlue.copy(alpha = 0.2f), BrandIndigo.copy(alpha = 0.15f))), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HSK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                        Text("${info.hskLevel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandIndigo)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.groupName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText)
                    Text(info.studentName, style = MaterialTheme.typography.bodySmall, color = secondaryText)
                }
                // Status badge
                val (statusText, statusColor) = when {
                    info.currentMonthPaid -> "Оплачено" to BrandGreen
                    info.totalPaidCurrentMonth > 0 && info.remainingBalance > 0 -> "Частично" to Color(0xFFFF9800)
                    else -> "Не оплачено" to BrandGold
                }
                Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.12f)) {
                    Text(statusText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = strokeColor)
            Spacer(Modifier.height(12.dp))

            // Prices
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PriceColumn("К оплате", formatSum(if (info.expectedPrice > 0) info.expectedPrice else info.monthlyPrice), BrandBlue, secondaryText)
                PriceColumn("За урок", formatSum(info.pricePerLesson), BrandTeal, secondaryText)
                PriceColumn("Уроков/мес", "${info.lessonsThisMonth}", BrandIndigo, secondaryText)
            }

            // First month note
            if (info.isFirstMonth) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(11.dp), tint = BrandIndigo)
                    Text("Первый месяц – оплата пропорционально оставшимся занятиям", style = MaterialTheme.typography.labelSmall, color = secondaryText)
                }
            }

            // Schedule
            if (info.scheduleDays != null && info.scheduleTime != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(13.dp), tint = secondaryText)
                    Text("${info.scheduleDays} • ${info.scheduleTime}", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                }
            }
        }
    }
}

@Composable
private fun PriceColumn(title: String, value: String, color: Color, secondaryText: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = secondaryText)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PendingSection(receipts: List<ReceiptDto>, isDark: Boolean, primaryText: Color, secondaryText: Color, cardColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.15f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandGold)
                Spacer(Modifier.width(8.dp))
                Text("На проверке", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(50), color = BrandGold.copy(alpha = 0.12f)) {
                    Text("${receipts.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandGold)
                }
            }
            Spacer(Modifier.height(12.dp))
            receipts.forEach { receipt ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(BrandGold.copy(alpha = 0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (receipt.extractedAmount != null && receipt.extractedAmount > 0) {
                                Text(formatSum(receipt.extractedAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText)
                            } else {
                                Text("Сумма определяется...", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = secondaryText)
                            }
                            Text(monthYearLabel(receipt.month, receipt.year), style = MaterialTheme.typography.bodySmall, color = secondaryText)
                        }
                        Text("Ожидание", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandGold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySection(payments: List<PaymentEntryDto>, isDark: Boolean, primaryText: Color, secondaryText: Color, cardColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandBlue)
                Text("История оплат", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
            }
            Spacer(Modifier.height(12.dp))

            if (payments.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(28.dp), tint = secondaryText.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))
                    Text("Пока нет оплат", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                }
            } else {
                payments.forEach { payment ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f),
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).background(BrandGreen.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (payment.amount != null) {
                                    Text(formatSum(payment.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryText)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(monthYearLabel(payment.month, payment.year), style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                    if (payment.paymentMethod != null) {
                                        Text("•", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                        Text(payment.paymentMethod.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = secondaryText)
                                    }
                                }
                            }
                            if (payment.date != null) {
                                Text(shortDate(payment.date), style = MaterialTheme.typography.labelSmall, color = secondaryText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, primaryText: Color, secondaryText: Color, cardColor: Color, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(Brand.Spacing.lg), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(22.dp), color = cardColor) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(26.dp), tint = BrandCoral)
                Spacer(Modifier.height(16.dp))
                Text("Нет связи с сервером", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.height(6.dp))
                Text(message, style = MaterialTheme.typography.bodySmall, color = secondaryText, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onRetry) { Text("Повторить", color = BrandBlue) }
            }
        }
    }
}

private fun formatSum(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ru"))
    return "${formatter.format(amount)} сум"
}

private fun monthYearLabel(month: Int?, year: Int?): String {
    if (month == null || year == null) return "—"
    val months = arrayOf("", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    val name = if (month in 1..12) months[month] else "—"
    return "$name $year"
}

private fun shortDate(dateStr: String): String {
    val parts = dateStr.take(10).split("-")
    return if (parts.size >= 3) "${parts[2]}.${parts[1]}" else dateStr.take(10)
}
