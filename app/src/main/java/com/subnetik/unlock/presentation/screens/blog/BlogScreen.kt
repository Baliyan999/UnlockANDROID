package com.subnetik.unlock.presentation.screens.blog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.BlogApi
import com.subnetik.unlock.data.remote.dto.blog.BlogPostDto
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────

@HiltViewModel
class BlogViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore,
    private val blogApi: BlogApi,
) : ViewModel() {

    val isDarkTheme = settingsDataStore.isDarkTheme

    private val _posts = MutableStateFlow<List<BlogPostDto>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _posts.value = blogApi.getPosts()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────

internal fun formatBlogDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val parsed = ZonedDateTime.parse(isoDate, DateTimeFormatter.ISO_DATE_TIME)
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("ru"))
        parsed.format(formatter)
    } catch (_: Exception) {
        try {
            val parsed = java.time.LocalDate.parse(isoDate.take(10))
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("ru"))
            parsed.format(formatter)
        } catch (_: Exception) {
            isoDate
        }
    }
}

// ─── Blog List Screen ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogScreen(
    onBack: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    viewModel: BlogViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Top bar ────────────────────────────
            TopAppBar(
                title = {
                    Text(
                        "Блог",
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

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = BrandBlue)
                    }
                }

                error != null && posts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Не удалось загрузить статьи",
                                style = MaterialTheme.typography.titleMedium,
                                color = secondaryText,
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.loadPosts() }) {
                                Text("Попробовать снова", color = BrandBlue)
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Brand.Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.lg),
                    ) {
                        Text(
                            "Полезные статьи и советы по изучению китайского языка",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                        )

                        posts.forEach { post ->
                            BlogArticleCard(
                                post = post,
                                isDark = isDark,
                                cardColor = cardColor,
                                strokeColor = strokeColor,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                                onClick = { onNavigateToArticle(post.id.toString()) },
                            )
                        }

                        Spacer(Modifier.height(Brand.Spacing.xxl))
                    }
                }
            }
        }
    }
}

// ─── Article Card ────────────────────────────────────────

@Composable
private fun BlogArticleCard(
    post: BlogPostDto,
    isDark: Boolean,
    cardColor: Color,
    strokeColor: Color,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
        shadowElevation = if (isDark) 0.dp else 3.dp,
    ) {
        Column(modifier = Modifier.padding(Brand.Spacing.lg)) {
            // Date + category badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    formatBlogDate(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText,
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BrandBlue.copy(alpha = 0.12f),
                ) {
                    Text(
                        "Статья",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue,
                        fontSize = 10.sp,
                    )
                }
            }

            Spacer(Modifier.height(Brand.Spacing.sm))

            Text(
                post.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )

            Spacer(Modifier.height(Brand.Spacing.sm))

            Text(
                post.excerpt,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Views + Likes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Просмотры",
                            modifier = Modifier.size(14.dp),
                            tint = secondaryText,
                        )
                        Text(
                            post.views.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Лайки",
                            modifier = Modifier.size(14.dp),
                            tint = secondaryText,
                        )
                        Text(
                            post.likes.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryText,
                        )
                    }
                }

                // "Читать далее →"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Читать далее",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
