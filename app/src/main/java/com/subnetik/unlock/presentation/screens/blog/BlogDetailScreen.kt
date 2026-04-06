package com.subnetik.unlock.presentation.screens.blog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.BlogApi
import com.subnetik.unlock.data.remote.dto.blog.BlogPostDto
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────

@HiltViewModel
class BlogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    settingsDataStore: SettingsDataStore,
    private val blogApi: BlogApi,
    private val authDataStore: AuthDataStore,
) : ViewModel() {

    val isDarkTheme = settingsDataStore.isDarkTheme

    private val postId: Int = (savedStateHandle.get<String>("articleId") ?: "0").toIntOrNull() ?: 0

    private val _post = MutableStateFlow<BlogPostDto?>(null)
    val post = _post.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked = _isLiked.asStateFlow()

    private val _likesCount = MutableStateFlow(0)
    val likesCount = _likesCount.asStateFlow()

    private val _viewsCount = MutableStateFlow(0)
    val viewsCount = _viewsCount.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = blogApi.getPost(postId)
                _post.value = result
                _likesCount.value = result.likes
                _viewsCount.value = result.views

                // Increment view count
                try {
                    val viewResp = blogApi.incrementView(postId)
                    _viewsCount.value = viewResp.viewsCount
                } catch (_: Exception) { /* best-effort */ }

                // Check login status and like status
                val loggedIn = authDataStore.isLoggedIn.first()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    try {
                        val likeStatus = blogApi.getLikeStatus(postId)
                        _isLiked.value = likeStatus.liked
                    } catch (_: Exception) { /* best-effort */ }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            try {
                val resp = blogApi.toggleLike(postId)
                _isLiked.value = resp.liked
                _likesCount.value = resp.likesCount
            } catch (_: Exception) { /* ignore */ }
        }
    }

    fun retry() = loadPost()
}

// ─── Blog Detail Screen ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogDetailScreen(
    articleId: String,
    onBack: () -> Unit,
    viewModel: BlogDetailViewModel = hiltViewModel(),
) {
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()
    val post by viewModel.post.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLiked by viewModel.isLiked.collectAsStateWithLifecycle()
    val likesCount by viewModel.likesCount.collectAsStateWithLifecycle()
    val viewsCount by viewModel.viewsCount.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "Статья",
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

                error != null && post == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Не удалось загрузить статью",
                                style = MaterialTheme.typography.titleMedium,
                                color = secondaryText,
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.retry() }) {
                                Text("Попробовать снова", color = BrandBlue)
                            }
                        }
                    }
                }

                post != null -> {
                    val article = post!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = Brand.Spacing.lg),
                    ) {
                        // Cover image
                        if (!article.imageUrl.isNullOrBlank()) {
                            val coverUrl = if (article.imageUrl.startsWith("http")) article.imageUrl
                                else "https://unlocklingua.com${article.imageUrl}"
                            coil3.compose.AsyncImage(
                                model = coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.height(Brand.Spacing.lg))
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = cardColor,
                            border = BorderStroke(1.dp, strokeColor),
                        ) {
                            Column(modifier = Modifier.padding(Brand.Spacing.xl)) {
                                // Title
                                Text(
                                    article.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText,
                                    lineHeight = 28.sp,
                                )

                                Spacer(Modifier.height(Brand.Spacing.sm))

                                // Author + Date
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (!article.author.isNullOrBlank()) {
                                        Text(
                                            article.author,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandBlue,
                                        )
                                    }
                                    Text(
                                        formatBlogDate(article.createdAt),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = secondaryText,
                                    )
                                }

                                Spacer(Modifier.height(Brand.Spacing.md))

                                // Views + Likes row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = "Просмотры",
                                            modifier = Modifier.size(16.dp),
                                            tint = secondaryText,
                                        )
                                        Text(
                                            viewsCount.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = secondaryText,
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Лайки",
                                            modifier = Modifier.size(16.dp),
                                            tint = secondaryText,
                                        )
                                        Text(
                                            likesCount.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = secondaryText,
                                        )
                                    }
                                }

                                Spacer(Modifier.height(Brand.Spacing.xl))

                                // Content – render markdown-like
                                RenderBlogContent(
                                    content = article.content,
                                    primaryText = primaryText,
                                    secondaryText = secondaryText,
                                )
                            }
                        }

                        Spacer(Modifier.height(Brand.Spacing.lg))

                        // Like button
                        if (isLoggedIn) {
                            Button(
                                onClick = { viewModel.toggleLike() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLiked) Color(0xFFE53935) else BrandBlue,
                                ),
                            ) {
                                Icon(
                                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isLiked) "Вам нравится" else "Нравится",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = cardColor,
                                border = BorderStroke(1.dp, strokeColor),
                            ) {
                                Text(
                                    "Войдите чтобы поставить лайк",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryText,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }

                        Spacer(Modifier.height(Brand.Spacing.xxxl))
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Статья не найдена",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = secondaryText,
                        )
                    }
                }
            }
        }
    }
}

// ─── Simple Markdown Renderer ────────────────────────────

@Composable
private fun RenderBlogContent(
    content: String,
    primaryText: Color,
    secondaryText: Color,
) {
    val blocks = content.split("\n\n").filter { it.isNotBlank() }

    blocks.forEachIndexed { index, block ->
        val trimmed = block.trim()
        when {
            // ## Header 2
            trimmed.startsWith("## ") -> {
                Text(
                    trimmed.removePrefix("## "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    lineHeight = 24.sp,
                )
            }
            // # Header 1
            trimmed.startsWith("# ") -> {
                Text(
                    trimmed.removePrefix("# "),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    lineHeight = 28.sp,
                )
            }
            // Bullet points block
            trimmed.lines().all { it.trimStart().startsWith("- ") || it.trimStart().startsWith("* ") || it.isBlank() } -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    trimmed.lines()
                        .filter { it.isNotBlank() }
                        .forEach { line ->
                            val bulletText = line.trimStart().removePrefix("- ").removePrefix("* ")
                            Row {
                                Text(
                                    "\u2022  ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = BrandBlue,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = renderInlineMarkdown(bulletText),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryText,
                                    lineHeight = 24.sp,
                                )
                            }
                        }
                }
            }
            // Regular paragraph with inline bold support
            else -> {
                Text(
                    text = renderInlineMarkdown(trimmed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = primaryText,
                    lineHeight = 24.sp,
                )
            }
        }
        if (index < blocks.lastIndex) {
            Spacer(Modifier.height(Brand.Spacing.lg))
        }
    }
}

/**
 * Renders **bold** text within a string using AnnotatedString.
 */
private fun renderInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldStart = remaining.indexOf("**")
            if (boldStart == -1) {
                append(remaining)
                break
            }
            // text before bold
            append(remaining.substring(0, boldStart))
            remaining = remaining.substring(boldStart + 2)
            val boldEnd = remaining.indexOf("**")
            if (boldEnd == -1) {
                // no closing **, just append the rest
                append("**$remaining")
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(remaining.substring(0, boldEnd))
            }
            remaining = remaining.substring(boldEnd + 2)
        }
    }
}
