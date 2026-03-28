package com.subnetik.unlock.presentation.screens.reviews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.remote.dto.reviews.PublicReviewDto
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ReviewsScreen(
    onBack: () -> Unit,
    isLoggedIn: Boolean = false,
    viewModel: ReviewsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Top Bar ─────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryText,
                        )
                    }
                    Text(
                        text = "\u041E\u0442\u0437\u044B\u0432\u044B",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                }
            }

            // ─── Content ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header section
                ReviewsHeader(
                    reviews = uiState.reviews,
                    isLoggedIn = isLoggedIn,
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )

                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = BrandBlue)
                        }
                    }
                    uiState.errorMessage != null -> {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    uiState.reviews.isEmpty() -> {
                        ReviewsEmptyState(
                            isLoggedIn = isLoggedIn,
                            isDark = isDark,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                        )
                    }
                    else -> {
                        uiState.reviews.forEach { review ->
                            ReviewCard(
                                review = review,
                                isDark = isDark,
                                cardColor = cardColor,
                                strokeColor = strokeColor,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xxxl))
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────

@Composable
private fun ReviewsHeader(
    reviews: List<PublicReviewDto>,
    isLoggedIn: Boolean,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "\u0427\u0442\u043E \u0433\u043E\u0432\u043E\u0440\u044F\u0442 \u043D\u0430\u0448\u0438 \u0443\u0447\u0435\u043D\u0438\u043A\u0438 \u043E \u043A\u0443\u0440\u0441\u0430\u0445 \u043A\u0438\u0442\u0430\u0439\u0441\u043A\u043E\u0433\u043E \u044F\u0437\u044B\u043A\u0430 UNLOCK",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryText,
            textAlign = TextAlign.Center,
        )

        if (reviews.isNotEmpty()) {
            val averageRating = reviews.map { it.rating }.average()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RatingStars(rating = averageRating)
                Text(
                    text = reviewsCountText(reviews.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                )
            }
        }

        if (!isLoggedIn) {
            LoginRequiredBanner(isDark = isDark)
        }
    }
}

// ─── Empty State ─────────────────────────────────────────────

@Composable
private fun ReviewsEmptyState(
    isLoggedIn: Boolean,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "\u041F\u043E\u043A\u0430 \u043D\u0435\u0442 \u043E\u0442\u0437\u044B\u0432\u043E\u0432",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = primaryText,
        )
        Text(
            text = "\u0421\u0442\u0430\u043D\u044C\u0442\u0435 \u043F\u0435\u0440\u0432\u044B\u043C, \u043A\u0442\u043E \u043F\u043E\u0434\u0435\u043B\u0438\u0442\u0441\u044F \u0441\u0432\u043E\u0438\u043C \u043E\u043F\u044B\u0442\u043E\u043C!",
            style = MaterialTheme.typography.bodySmall,
            color = secondaryText,
            textAlign = TextAlign.Center,
        )
        if (!isLoggedIn) {
            LoginRequiredBanner(isDark = isDark)
        }
    }
}

// ─── Login Required Banner ───────────────────────────────────

@Composable
private fun LoginRequiredBanner(isDark: Boolean) {
    val background = if (isDark) BrandBlue.copy(alpha = 0.18f) else BrandBlue.copy(alpha = 0.12f)
    val stroke = if (isDark) BrandBlue.copy(alpha = 0.5f) else BrandBlue.copy(alpha = 0.25f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = background,
        border = BorderStroke(1.dp, stroke),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "\u0422\u0440\u0435\u0431\u0443\u0435\u0442\u0441\u044F \u0432\u0445\u043E\u0434 \u0432 \u0430\u043A\u043A\u0430\u0443\u043D\u0442",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                )
            }
            Text(
                text = "\u0412\u043E\u0439\u0434\u0438\u0442\u0435, \u0447\u0442\u043E\u0431\u044B \u043E\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043E\u0442\u0437\u044B\u0432 \u0438 \u0432\u0438\u0434\u0435\u0442\u044C \u0441\u0442\u0430\u0442\u0443\u0441 \u043C\u043E\u0434\u0435\u0440\u0430\u0446\u0438\u0438.",
                style = MaterialTheme.typography.bodySmall,
                color = BrandBlue.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Review Card ─────────────────────────────────────────────

@Composable
private fun ReviewCard(
    review: PublicReviewDto,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
        shadowElevation = if (isDark) 0.dp else 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Author row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = review.author,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (review.isStudent) {
                            Text(
                                text = "\uD83D\uDC51",
                                fontSize = 14.sp,
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RatingStars(rating = review.rating.toDouble(), size = 12)
                        review.createdAt?.let { dateStr ->
                            Text(
                                text = formatShortDate(dateStr),
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryText,
                            )
                        }
                    }
                }
            }

            // Optional image
            if (!review.imageUrl.isNullOrBlank()) {
                val imgUrl = review.imageUrl.trim()
                if (imgUrl.startsWith("data:")) {
                    // Decode base64 data URI
                    val bitmap = remember(imgUrl) {
                        try {
                            val base64 = imgUrl.substringAfter("base64,")
                            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (_: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Review image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                } else {
                    val resolvedUrl = if (imgUrl.startsWith("http")) imgUrl else "https://unlocklingua.com$imgUrl"
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = "Review image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }

            // Review text
            Text(
                text = review.text,
                style = MaterialTheme.typography.bodySmall,
                color = primaryText,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─── Rating Stars ────────────────────────────────────────────

@Composable
private fun RatingStars(rating: Double, size: Int = 13) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating.roundToInt()) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = BrandGold,
                modifier = Modifier.size(size.dp),
            )
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────

private fun reviewsCountText(count: Int): String {
    val lastDigit = count % 10
    val lastTwoDigits = count % 100
    val word = when {
        lastTwoDigits in 11..19 -> "\u043E\u0442\u0437\u044B\u0432\u043E\u0432"
        lastDigit == 1 -> "\u043E\u0442\u0437\u044B\u0432"
        lastDigit in 2..4 -> "\u043E\u0442\u0437\u044B\u0432\u0430"
        else -> "\u043E\u0442\u0437\u044B\u0432\u043E\u0432"
    }
    return "$count $word"
}

private fun formatShortDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val formatter = SimpleDateFormat("d MMM yyyy", Locale("ru"))
        val date = parser.parse(isoDate.substringBefore(".").substringBefore("+"))
        date?.let { formatter.format(it) } ?: isoDate
    } catch (_: Exception) {
        isoDate
    }
}

private fun resolveReviewImageUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val trimmed = url.trim()
    // Skip data: URLs (base64 inline images not supported in Coil easily)
    if (trimmed.startsWith("data:")) return null
    return if (trimmed.startsWith("http")) trimmed else "https://unlocklingua.com$trimmed"
}
