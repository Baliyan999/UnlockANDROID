package com.subnetik.unlock.presentation.screens.market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.subnetik.unlock.R
import com.subnetik.unlock.data.remote.dto.market.MarketItemResponse
import com.subnetik.unlock.data.remote.dto.market.MarketPurchaseResponse
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MarketScreen(
    onBack: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel(),
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
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Brand.Spacing.sm, vertical = Brand.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText) }
                Spacer(Modifier.weight(1f))
                Image(painter = painterResource(R.drawable.unlock_token_logo), contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape))
                Spacer(Modifier.width(7.dp))
                Text("Unlock Market", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.loadData() }) {
                    Box(
                        modifier = Modifier.size(34.dp).background(BrandGold.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", modifier = Modifier.size(16.dp), tint = BrandGold)
                    }
                }
            }

            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BrandGold)
                        Spacer(Modifier.height(16.dp))
                        Text("Загрузка магазина...", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Brand.Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
                ) {
                    Spacer(Modifier.height(Brand.Spacing.sm))

                    // ─── Header ──────────────────────────
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(110.dp).background(BrandGold.copy(alpha = 0.06f), CircleShape))
                        Box(modifier = Modifier.size(80.dp).background(BrandGold.copy(alpha = 0.10f), CircleShape))
                        Image(
                            painter = painterResource(R.drawable.unlock_token_logo),
                            contentDescription = "Unlock Token",
                            modifier = Modifier.size(56.dp).clip(CircleShape),
                        )
                    }
                    Text("Магазин наград", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = primaryText)
                    Text("Обменивай токены на крутые призы", style = MaterialTheme.typography.bodySmall, color = secondaryText)

                    // ─── Messages ─────────────────────────
                    uiState.errorMessage?.let { msg ->
                        MarketBanner(Icons.Default.Warning, msg, BrandCoral)
                    }
                    uiState.successMessage?.let { msg ->
                        MarketBanner(Icons.Default.CheckCircle, msg, BrandGreen)
                    }

                    // ─── Wallet Card ─────────────────────
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        shadowElevation = 20.dp,
                    ) {
                        Column {
                            // Balance section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF333868), Color(0xFF1F2451)),
                                        ),
                                        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                                    )
                                    .padding(vertical = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Мой баланс", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(formatBalance(uiState.balance), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Image(painter = painterResource(R.drawable.unlock_token_logo), contentDescription = null, modifier = Modifier.size(26.dp).clip(CircleShape))
                                }
                            }

                            // Stats strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFF2A2F5A),
                                        RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                                    )
                                    .padding(vertical = 10.dp),
                            ) {
                                WalletStat(Icons.Default.ShoppingCart, "Покупки", "${uiState.purchases.size}", Modifier.weight(1f))
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                                WalletStat(Icons.Default.ShoppingBag, "Товары", "${uiState.items.size}", Modifier.weight(1f))
                            }
                        }
                    }

                    // ─── Items Section ────────────────────
                    if (uiState.items.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp).background(BrandGold.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(32.dp), tint = BrandGold.copy(alpha = 0.5f))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Товаров пока нет", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = secondaryText)
                            Spacer(Modifier.height(4.dp))
                            Text("Загляни позже — скоро появятся крутые призы!", style = MaterialTheme.typography.bodySmall, color = secondaryText.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        }
                    } else {
                        // Available items header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandGold)
                            Spacer(Modifier.width(8.dp))
                            Text("Доступные товары", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                            Spacer(Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(50), color = BrandGold.copy(alpha = 0.12f)) {
                                Text("${uiState.items.size}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandGold)
                            }
                        }

                        uiState.items.forEach { item ->
                            MarketProductCard(item, uiState.balance, item.code in uiState.buyingCodes, isDark, primaryText, secondaryText, cardColor, strokeColor) { qty ->
                                viewModel.buyItem(item.code, qty)
                            }
                        }
                    }

                    // ─── History Section ──────────────────
                    if (uiState.purchases.isNotEmpty()) {
                        Surface(
                            onClick = { viewModel.toggleHistory() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            color = Color.Transparent,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandIndigo)
                                Spacer(Modifier.width(8.dp))
                                Text("История покупок", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText)
                                Spacer(Modifier.weight(1f))
                                Surface(shape = RoundedCornerShape(50), color = BrandIndigo.copy(alpha = 0.12f)) {
                                    Text("${uiState.purchases.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandIndigo)
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.ExpandMore, contentDescription = null,
                                    modifier = Modifier.size(16.dp).rotate(if (uiState.showHistory) 180f else 0f),
                                    tint = secondaryText,
                                )
                            }
                        }

                        AnimatedVisibility(visible = uiState.showHistory, enter = expandVertically(), exit = shrinkVertically()) {
                            Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                                uiState.purchases.take(20).forEach { purchase ->
                                    HistoryRow(purchase, isDark, primaryText, secondaryText, cardColor, strokeColor)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Brand.Spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun WalletStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(11.dp), tint = BrandGold)
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun MarketBanner(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.12f)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = color)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}

@Composable
private fun MarketProductCard(
    item: MarketItemResponse, balance: Int, isBuying: Boolean,
    isDark: Boolean, primaryText: Color, secondaryText: Color, cardColor: Color, strokeColor: Color,
    onBuy: (Int) -> Unit,
) {
    var quantity by remember { mutableIntStateOf(1) }
    val totalPrice = item.tokenPrice * quantity
    val canAfford = balance >= totalPrice

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
        shadowElevation = 8.dp,
    ) {
        Column {
            // Image
            if (item.imageUrl != null) {
                val imgUrl = if (item.imageUrl.startsWith("http")) item.imageUrl else "https://unlocklingua.com${item.imageUrl}"
                AsyncImage(
                    model = imgUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp).background(if (isDark) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(28.dp), tint = secondaryText.copy(alpha = 0.4f))
                }
            }

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = primaryText, maxLines = 2)

                if (item.description != null) {
                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = secondaryText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                HorizontalDivider(color = strokeColor)

                // Quantity + Buy
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quantity control
                    Surface(shape = RoundedCornerShape(10.dp), color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (quantity > 1) primaryText else secondaryText.copy(alpha = 0.4f))
                            }
                            Text("$quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp), color = primaryText)
                            IconButton(onClick = { if (quantity < 100) quantity++ }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = primaryText)
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Buy button
                    Button(
                        onClick = { onBuy(quantity) },
                        enabled = !isBuying && canAfford,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canAfford) BrandGold else Color.Gray.copy(alpha = 0.3f),
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    ) {
                        if (isBuying) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(6.dp))
                        } else {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(if (isBuying) "Покупка..." else "Купить", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Total price hint
                if (quantity > 1) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Итого:", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                        Image(painter = painterResource(R.drawable.unlock_token_logo), contentDescription = null, modifier = Modifier.size(13.dp).clip(CircleShape))
                        Text("$totalPrice", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (canAfford) BrandGold else BrandCoral)
                        if (!canAfford) {
                            Text("(не хватает ${totalPrice - balance})", style = MaterialTheme.typography.labelSmall, color = BrandCoral.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(purchase: MarketPurchaseResponse, isDark: Boolean, primaryText: Color, secondaryText: Color, cardColor: Color, strokeColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(0.5.dp, strokeColor),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (purchase.itemImageUrl != null) {
                    val imgUrl = if (purchase.itemImageUrl.startsWith("http")) purchase.itemImageUrl else "https://unlocklingua.com${purchase.itemImageUrl}"
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = purchase.itemName,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp), tint = secondaryText)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(purchase.itemName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = primaryText, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(formatDate(purchase.createdAt), style = MaterialTheme.typography.labelSmall, color = secondaryText)
                    if (purchase.quantity > 1) {
                        Surface(shape = RoundedCornerShape(50), color = BrandIndigo.copy(alpha = 0.12f)) {
                            Text("x${purchase.quantity}", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandIndigo, fontSize = 10.sp)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("-${purchase.totalTokens}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandCoral)
                Image(painter = painterResource(R.drawable.unlock_token_logo), contentDescription = null, modifier = Modifier.size(15.dp).clip(CircleShape))
            }
        }
    }
}

private fun formatBalance(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ru"))
    return formatter.format(value)
}

private fun formatDate(raw: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val output = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        output.format(input.parse(raw.take(19))!!)
    } catch (_: Exception) { raw.take(10) }
}
