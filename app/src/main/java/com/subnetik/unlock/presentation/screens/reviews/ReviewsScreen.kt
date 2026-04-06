package com.subnetik.unlock.presentation.screens.reviews

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val formState by viewModel.formState.collectAsStateWithLifecycle()
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
                    onLeaveReview = { viewModel.openReviewForm() },
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
                            onLeaveReview = { viewModel.openReviewForm() },
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

    // ─── Review Form Dialog ──────────────────────────────────
    if (formState.showForm) {
        ReviewFormDialog(
            formState = formState,
            isDark = isDark,
            onAuthorChange = viewModel::onAuthorChange,
            onTextChange = viewModel::onTextChange,
            onRatingChange = viewModel::onRatingChange,
            onIsStudentChange = viewModel::onIsStudentChange,
            onImageSelected = viewModel::onImageSelected,
            onClearImage = viewModel::clearImage,
            onSubmit = viewModel::submitReview,
            onDismiss = viewModel::closeReviewForm,
        )
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
    onLeaveReview: () -> Unit,
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

        if (isLoggedIn) {
            LeaveReviewButton(isDark = isDark, onClick = onLeaveReview)
        } else {
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
    onLeaveReview: () -> Unit,
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
        if (isLoggedIn) {
            LeaveReviewButton(isDark = isDark, onClick = onLeaveReview)
        } else {
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

// ─── Leave Review Button ────────────────────────────────────

@Composable
private fun LeaveReviewButton(isDark: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isDark) 0.dp else 6.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(GradientIndigo),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = BrandGold,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "\u041E\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043E\u0442\u0437\u044B\u0432",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

// ─── Review Form Dialog ─────────────────────────────────────

@Composable
private fun ReviewFormDialog(
    formState: ReviewFormState,
    isDark: Boolean,
    onAuthorChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onIsStudentChange: (Boolean) -> Unit = {},
    onImageSelected: (Uri, android.content.Context) -> Unit,
    onClearImage: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cardBg = if (isDark) Color(0xFF1E2338) else Color.White
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF5F5F5)
    val primaryText = if (isDark) Color.White else Color.Black
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            shadowElevation = if (isDark) 0.dp else 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Transparent),
            ) {
                // ─── Header ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(GradientIndigo))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "\u041E\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043E\u0442\u0437\u044B\u0432",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        onClick = onDismiss,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "\u2715",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }

                // ─── Form Body ───────────────────────────
                Column(
                    modifier = Modifier
                        .background(cardBg)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    if (formState.isSuccess) {
                        ReviewSuccessContent(isDark = isDark, onDone = onDismiss)
                    } else {
                        ReviewFormFields(
                            formState = formState,
                            isDark = isDark,
                            fieldBg = fieldBg,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            onAuthorChange = onAuthorChange,
                            onTextChange = onTextChange,
                            onRatingChange = onRatingChange,
                            onIsStudentChange = onIsStudentChange,
                            onImageSelected = onImageSelected,
                            onClearImage = onClearImage,
                            onSubmit = onSubmit,
                        )
                    }
                }
            }
        }
    }
}

// ─── Form Fields ────────────────────────────────────────────

@Composable
private fun ReviewFormFields(
    formState: ReviewFormState,
    isDark: Boolean,
    fieldBg: Color,
    primaryText: Color,
    secondaryText: Color,
    onAuthorChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onIsStudentChange: (Boolean) -> Unit = {},
    onImageSelected: (Uri, android.content.Context) -> Unit,
    onClearImage: () -> Unit,
    onSubmit: () -> Unit,
) {
    val trimmedAuthor = formState.author.trim()
    val trimmedText = formState.text.trim()
    val authorError = if (formState.showValidation && trimmedAuthor.isEmpty()) "\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0432\u0430\u0448\u0435 \u0438\u043C\u044F" else null
    val textError = when {
        formState.showValidation && trimmedText.isEmpty() -> "\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0442\u0435\u043A\u0441\u0442 \u043E\u0442\u0437\u044B\u0432\u0430"
        formState.showValidation && trimmedText.length < 10 -> "\u041C\u0438\u043D\u0438\u043C\u0443\u043C 10 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432"
        else -> null
    }
    val ratingError = if (formState.showValidation && formState.rating == 0) "\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043E\u0446\u0435\u043D\u043A\u0443" else null

    // Author
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormFieldLabel(text = "\u0412\u0430\u0448\u0435 \u0438\u043C\u044F", required = true, color = primaryText)
        OutlinedTextField(
            value = formState.author,
            onValueChange = onAuthorChange,
            placeholder = {
                Text(
                    "\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0432\u0430\u0448\u0435 \u0438\u043C\u044F",
                    color = secondaryText,
                )
            },
            singleLine = true,
            isError = authorError != null,
            colors = reviewFieldColors(fieldBg, isDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            if (authorError != null) {
                Text(
                    text = authorError,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandCoral,
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "${trimmedAuthor.length}/100",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText,
                fontSize = 11.sp,
            )
        }
    }

    // Review text
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormFieldLabel(text = "\u0412\u0430\u0448 \u043E\u0442\u0437\u044B\u0432", required = true, color = primaryText)
        OutlinedTextField(
            value = formState.text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    "\u041F\u043E\u0434\u0435\u043B\u0438\u0442\u0435\u0441\u044C \u0441\u0432\u043E\u0438\u043C \u043E\u043F\u044B\u0442\u043E\u043C \u043E\u0431\u0443\u0447\u0435\u043D\u0438\u044F...",
                    color = secondaryText,
                )
            },
            minLines = 4,
            maxLines = 6,
            isError = textError != null,
            colors = reviewFieldColors(fieldBg, isDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            if (textError != null) {
                Text(
                    text = textError,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandCoral,
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "${trimmedText.length}/1000",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText,
                fontSize = 11.sp,
            )
        }
    }

    // Rating
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FormFieldLabel(text = "\u041E\u0446\u0435\u043D\u043A\u0430", required = true, color = primaryText)
        StarRatingPicker(
            rating = formState.rating,
            onRatingChange = onRatingChange,
        )
        if (ratingError != null) {
            Text(
                text = ratingError,
                style = MaterialTheme.typography.bodySmall,
                color = BrandCoral,
                fontSize = 11.sp,
            )
        }
    }

    // Image upload
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "Изображение (опционально)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = primaryText,
        )

        val context = LocalContext.current
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { onImageSelected(it, context) }
        }

        if (formState.imageUri != null) {
            // Image preview with remove button
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        AsyncImage(
                            model = formState.imageUri,
                            contentDescription = "Выбранное изображение",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Text(
                            "Изображение выбрано",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                            fontSize = 11.sp,
                        )
                    }
                }

                // Remove button (X) in top-right corner
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    onClick = onClearImage,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Удалить изображение",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        } else {
            // Placeholder - clickable to launch image picker
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = secondaryText,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        "Выберите изображение",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                    )
                    Text(
                        "JPG, PNG до 2MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                    )
                }
            }
        }

        // Image error message
        if (formState.imageError != null) {
            Text(
                text = formState.imageError,
                style = MaterialTheme.typography.bodySmall,
                color = BrandCoral,
                fontSize = 11.sp,
            )
        } else {
            Text(
                "JPG, PNG до 2MB",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText.copy(alpha = 0.5f),
                fontSize = 10.sp,
            )
        }
    }

    // Student toggle
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Статус", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.06f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Я ученик Unlock", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text(
                        "Рядом с именем будет иконка короны.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                    )
                }
                Switch(
                    checked = formState.isStudent,
                    onCheckedChange = onIsStudentChange,
                )
            }
        }
    }

    // Error message
    if (formState.submitError != null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = BrandCoral.copy(alpha = 0.12f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = BrandCoral,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = formState.submitError,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandCoral,
                    fontSize = 11.sp,
                )
            }
        }
    }

    // Submit button
    Button(
        onClick = onSubmit,
        enabled = !formState.isSubmitting,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(GradientIndigo),
                    shape = RoundedCornerShape(14.dp),
                    alpha = if (formState.isSubmitting) 0.7f else 1f,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(
                    text = if (formState.isSubmitting) "\u041E\u0442\u043F\u0440\u0430\u0432\u043A\u0430..." else "\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// ─── Star Rating Picker ─────────────────────────────────────

@Composable
private fun StarRatingPicker(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = BrandGold,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onRatingChange(i) },
            )
        }
    }
}

// ─── Form Field Label ───────────────────────────────────────

@Composable
private fun FormFieldLabel(text: String, required: Boolean, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        if (required) {
            Text(
                text = "*",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

// ─── Review Field Colors ────────────────────────────────────

@Composable
private fun reviewFieldColors(fieldBg: Color, isDark: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = fieldBg,
    unfocusedContainerColor = fieldBg,
    focusedBorderColor = BrandBlue,
    unfocusedBorderColor = Color.Transparent,
    errorBorderColor = BrandCoral,
    cursorColor = BrandBlue,
    focusedTextColor = if (isDark) Color.White else Color.Black,
    unfocusedTextColor = if (isDark) Color.White else Color.Black,
)

// ─── Success Content ────────────────────────────────────────

@Composable
private fun ReviewSuccessContent(isDark: Boolean, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Checkmark circle
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 8.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(GradientBlue),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }

        Text(
            text = "\u041E\u0442\u0437\u044B\u0432 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color.Black,
        )
        Text(
            text = "\u0421\u043F\u0430\u0441\u0438\u0431\u043E! \u041C\u044B \u043E\u043F\u0443\u0431\u043B\u0438\u043A\u0443\u0435\u043C \u0435\u0433\u043E \u043F\u043E\u0441\u043B\u0435 \u043C\u043E\u0434\u0435\u0440\u0430\u0446\u0438\u0438.",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
        )

        // Status pill
        Surface(
            shape = RoundedCornerShape(50),
            color = BrandBlue.copy(alpha = if (isDark) 0.18f else 0.12f),
            border = BorderStroke(1.dp, BrandBlue.copy(alpha = if (isDark) 0.45f else 0.2f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\u23F0",
                    fontSize = 11.sp,
                )
                Text(
                    text = "\u0421\u0442\u0430\u0442\u0443\u0441: \u043D\u0430 \u043C\u043E\u0434\u0435\u0440\u0430\u0446\u0438\u0438",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue,
                )
            }
        }

        // Done button
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(GradientBlue),
                        shape = RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u0413\u043E\u0442\u043E\u0432\u043E",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
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
