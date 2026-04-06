package com.subnetik.unlock.presentation.screens.admin.sections

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.*
import com.subnetik.unlock.presentation.screens.admin.components.*
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════
//  BLOG
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminBlogViewModel @Inject constructor(private val adminApi: AdminApi) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val posts: List<AdminBlogPost> = emptyList(),
        val selectedFilter: String = "all",
        val error: String? = null,
        // Create/Edit dialog
        val showFormDialog: Boolean = false,
        val editingPostId: Int? = null,
        val formTitle: String = "",
        val formExcerpt: String = "",
        val formContent: String = "",
        val formSlug: String = "",
        val formLanguage: String = "ru",
        val formStatus: String = "draft",
        val formImageUrl: String = "",
        val coverImageUri: Uri? = null,
        val coverUploadedUrl: String? = null,
        val isUploadingCover: Boolean = false,
        val isSaving: Boolean = false,
        // Delete confirmation
        val deleteConfirmPostId: Int? = null,
    ) {
        val isEditing: Boolean get() = editingPostId != null
        val filtered: List<AdminBlogPost>
            get() = if (selectedFilter == "all") posts else posts.filter { it.status.lowercase() == selectedFilter }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try { _uiState.update { it.copy(isLoading = false, posts = adminApi.getBlogPosts()) } }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun selectFilter(f: String) { _uiState.update { it.copy(selectedFilter = f) } }

    // ─── Form: Create ───────────────────────────────────────
    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showFormDialog = true, editingPostId = null,
                formTitle = "", formExcerpt = "", formContent = "", formSlug = "",
                formLanguage = "ru", formStatus = "draft", formImageUrl = "",
                coverImageUri = null, coverUploadedUrl = null,
            )
        }
    }

    // ─── Form: Edit ─────────────────────────────────────────
    fun showEditDialog(post: AdminBlogPost) {
        _uiState.update {
            it.copy(
                showFormDialog = true, editingPostId = post.id,
                formTitle = post.title, formExcerpt = post.excerpt, formContent = post.content,
                formSlug = post.slug, formLanguage = post.language, formStatus = post.status,
                formImageUrl = post.imageUrl ?: post.cover ?: "",
                coverImageUri = null, coverUploadedUrl = null,
            )
        }
    }

    fun dismissFormDialog() { _uiState.update { it.copy(showFormDialog = false, editingPostId = null) } }

    fun updateFormTitle(v: String) { _uiState.update { it.copy(formTitle = v) } }
    fun updateFormExcerpt(v: String) { _uiState.update { it.copy(formExcerpt = v) } }
    fun updateFormContent(v: String) { _uiState.update { it.copy(formContent = v) } }
    fun updateFormSlug(v: String) { _uiState.update { it.copy(formSlug = v) } }
    fun updateFormLanguage(v: String) { _uiState.update { it.copy(formLanguage = v) } }
    fun updateFormStatus(v: String) { _uiState.update { it.copy(formStatus = v) } }
    fun updateFormImageUrl(v: String) { _uiState.update { it.copy(formImageUrl = v) } }

    /** Auto-generate slug from title (transliterate basic Cyrillic) */
    fun autoGenerateSlug() {
        val title = _uiState.value.formTitle
        val slug = title.lowercase()
            .replace(Regex("[а-яё]") ) { ch ->
                when (ch.value) {
                    "а" -> "a"; "б" -> "b"; "в" -> "v"; "г" -> "g"; "д" -> "d"
                    "е" -> "e"; "ё" -> "yo"; "ж" -> "zh"; "з" -> "z"; "и" -> "i"
                    "й" -> "y"; "к" -> "k"; "л" -> "l"; "м" -> "m"; "н" -> "n"
                    "о" -> "o"; "п" -> "p"; "р" -> "r"; "с" -> "s"; "т" -> "t"
                    "у" -> "u"; "ф" -> "f"; "х" -> "kh"; "ц" -> "ts"; "ч" -> "ch"
                    "ш" -> "sh"; "щ" -> "shch"; "ъ" -> ""; "ы" -> "y"; "ь" -> ""
                    "э" -> "e"; "ю" -> "yu"; "я" -> "ya"
                    else -> ch.value
                }
            }
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        _uiState.update { it.copy(formSlug = slug) }
    }

    fun selectCoverImage(uri: Uri, context: Context) {
        _uiState.update { it.copy(coverImageUri = uri, isUploadingCover = true) }
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot read image")
                val bytes = inputStream.readBytes()
                inputStream.close()
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = "cover_${System.currentTimeMillis()}.${if (mimeType.contains("png")) "png" else "jpg"}"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                val response = adminApi.uploadBlogImage(part)
                _uiState.update { it.copy(isUploadingCover = false, coverUploadedUrl = response.url, formImageUrl = response.url ?: "") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploadingCover = false, error = "Ошибка загрузки: ${e.message}") }
            }
        }
    }

    fun removeCoverImage() { _uiState.update { it.copy(coverImageUri = null, coverUploadedUrl = null, formImageUrl = "") } }

    // ─── Save (create or update) ────────────────────────────
    fun savePost() {
        val s = _uiState.value
        if (s.formTitle.isBlank()) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val imageUrl = s.coverUploadedUrl ?: s.formImageUrl.takeIf { it.isNotBlank() }
                if (s.isEditing) {
                    adminApi.updateBlogPost(
                        s.editingPostId!!,
                        AdminBlogUpdateRequest(
                            title = s.formTitle.trim(),
                            excerpt = s.formExcerpt.trim(),
                            content = s.formContent.trim(),
                            slug = s.formSlug.trim(),
                            language = s.formLanguage,
                            status = s.formStatus,
                            imageUrl = imageUrl,
                        ),
                    )
                } else {
                    adminApi.createBlogPost(
                        AdminBlogCreateRequest(
                            title = s.formTitle.trim(),
                            excerpt = s.formExcerpt.trim(),
                            content = s.formContent.trim(),
                            slug = s.formSlug.trim(),
                            language = s.formLanguage,
                            status = s.formStatus,
                            imageUrl = imageUrl,
                        ),
                    )
                }
                _uiState.update { it.copy(showFormDialog = false, editingPostId = null, isSaving = false) }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun publishPost(id: Int) { updatePostStatus(id, "published") }
    fun unpublishPost(id: Int) { updatePostStatus(id, "draft") }

    fun showDeleteConfirm(id: Int) { _uiState.update { it.copy(deleteConfirmPostId = id) } }
    fun dismissDeleteConfirm() { _uiState.update { it.copy(deleteConfirmPostId = null) } }
    fun confirmDeletePost() {
        val id = _uiState.value.deleteConfirmPostId ?: return
        _uiState.update { it.copy(deleteConfirmPostId = null) }
        viewModelScope.launch {
            try { adminApi.deleteBlogPost(id); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    private fun updatePostStatus(id: Int, status: String) {
        viewModelScope.launch {
            try { adminApi.updateBlogPostStatus(id, AdminBlogStatusRequest(status = status)); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlogSection(isDark: Boolean, viewModel: AdminBlogViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.selectCoverImage(it, context) }
    }

    // ─── Delete confirmation dialog ─────────────────────────
    if (uiState.deleteConfirmPostId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            title = { Text("Удалить пост?", fontWeight = FontWeight.Bold) },
            text = { Text("Это действие нельзя отменить. Пост будет удалён навсегда.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeletePost() }) {
                    Text("Удалить", color = BrandCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirm() }) {
                    Text("Отмена")
                }
            },
        )
    }

    // ─── Create / Edit bottom sheet ─────────────────────────
    if (uiState.showFormDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissFormDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                Text(
                    if (uiState.isEditing) "Редактировать пост" else "Новый пост",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )

                // Title
                OutlinedTextField(
                    uiState.formTitle,
                    { viewModel.updateFormTitle(it) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Заголовок") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )

                // Slug with auto-generate
                OutlinedTextField(
                    uiState.formSlug,
                    { viewModel.updateFormSlug(it) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Slug (URL)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.autoGenerateSlug() }) {
                            Icon(Icons.Default.AutoFixHigh, "Сгенерировать slug", Modifier.size(20.dp))
                        }
                    },
                )

                // Excerpt
                OutlinedTextField(
                    uiState.formExcerpt,
                    { viewModel.updateFormExcerpt(it) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Краткое описание") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3,
                )

                // Content
                OutlinedTextField(
                    uiState.formContent,
                    { viewModel.updateFormContent(it) },
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    label = { Text("Содержание") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 15,
                )

                // Cover image
                Text("Обложка:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                if (uiState.coverImageUri != null) {
                    Box(Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = uiState.coverImageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        if (uiState.isUploadingCover) {
                            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(32.dp), color = Color.White, strokeWidth = 3.dp)
                            }
                        }
                        IconButton(
                            onClick = { viewModel.removeCoverImage() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = BrandCoral) {
                                Icon(Icons.Default.Close, null, Modifier.size(20.dp).padding(2.dp), tint = Color.White)
                            }
                        }
                    }
                    if (uiState.coverUploadedUrl != null) {
                        Text("Загружено", style = MaterialTheme.typography.labelSmall, color = BrandGreen, fontWeight = FontWeight.Bold)
                    }
                } else if (uiState.formImageUrl.isNotBlank()) {
                    // Show existing image URL (for edit mode)
                    Box(Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = uiState.formImageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        IconButton(
                            onClick = { viewModel.removeCoverImage() },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = BrandCoral) {
                                Icon(Icons.Default.Close, null, Modifier.size(20.dp).padding(2.dp), tint = Color.White)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Выбрать обложку")
                    }
                }

                // Language selector
                Text("Язык:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    listOf("ru" to "Русский", "uz" to "Узбекский", "en" to "Английский").forEach { (code, label) ->
                        FilterChip(
                            selected = uiState.formLanguage == code,
                            onClick = { viewModel.updateFormLanguage(code) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                // Status selector
                Text("Статус:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    listOf("draft" to "Черновик", "published" to "Опубликован").forEach { (code, label) ->
                        FilterChip(
                            selected = uiState.formStatus == code,
                            onClick = { viewModel.updateFormStatus(code) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                // Save button
                Button(
                    onClick = { viewModel.savePost() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.formTitle.isNotBlank() && !uiState.isUploadingCover && !uiState.isSaving,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (uiState.isEditing) "Сохранить" else "Создать пост",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    // ─── Main list ──────────────────────────────────────────
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item { AdminSectionHeader(Icons.AutoMirrored.Filled.Article, "Управление блогом", isDark) }

        item {
            Button(
                onClick = { viewModel.showCreateDialog() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать пост", fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            val draft = uiState.posts.count { it.status.lowercase() == "draft" }
            val published = uiState.posts.count { it.status.lowercase() == "published" }
            AdminFilterTabs(
                filters = listOf(
                    AdminFilter("all", "Все", uiState.posts.size),
                    AdminFilter("draft", "Черновики", draft),
                    AdminFilter("published", "Опубликованные", published),
                ),
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.selectFilter(it) },
                isDark = isDark,
            )
        }

        uiState.error?.let { error ->
            item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp)
                }
            }
        } else if (uiState.filtered.isEmpty()) {
            item {
                AdminEmptyState(
                    Icons.AutoMirrored.Filled.Article,
                    "Нет постов",
                    "Посты блога появятся здесь",
                    isDark = isDark,
                )
            }
        } else {
            items(uiState.filtered, key = { it.id }) { post ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        // Cover image
                        val coverUrl = post.imageUrl ?: post.cover
                        coverUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        Column(
                            Modifier.fillMaxWidth().padding(Brand.Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                        ) {
                            // Title + status
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    post.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.weight(1f),
                                )
                                AdminStatusTag(statusLabel(post.status), statusColor(post.status))
                            }

                            // Excerpt
                            if (post.excerpt.isNotBlank()) {
                                Text(post.excerpt, style = MaterialTheme.typography.bodySmall, color = subtextColor, maxLines = 2)
                            }

                            // Slug
                            if (post.slug.isNotBlank()) {
                                Text(
                                    "/${post.slug}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandBlue.copy(alpha = 0.7f),
                                )
                            }

                            // Meta: language, views, likes, date
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AdminStatusTag(post.language.uppercase(), BrandBlue)
                                if (post.views > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Default.Visibility, null, Modifier.size(14.dp), tint = subtextColor)
                                        Text("${post.views}", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                    }
                                }
                                if (post.likes > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Default.Favorite, null, Modifier.size(14.dp), tint = BrandCoral)
                                        Text("${post.likes}", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                    }
                                }
                                post.createdAt?.let {
                                    Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                }
                            }

                            HorizontalDivider(
                                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                            )

                            // Action buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                                AdminActionButton("Изменить", Icons.Default.Edit, BrandBlue, onClick = { viewModel.showEditDialog(post) })
                                when (post.status.lowercase()) {
                                    "draft" -> AdminActionButton("Опубликовать", Icons.Default.Publish, BrandTeal, onClick = { viewModel.publishPost(post.id) })
                                    "published" -> AdminActionButton("Снять", Icons.Default.Unpublished, BrandGold, onClick = { viewModel.unpublishPost(post.id) })
                                }
                                AdminActionButton("Удалить", Icons.Default.Delete, BrandCoral, onClick = { viewModel.showDeleteConfirm(post.id) })
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TOKENS
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminTokensViewModel @Inject constructor(private val adminApi: AdminApi) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val students: List<AdminTokenStudent> = emptyList(),
        val transactions: List<AdminTokenTransaction> = emptyList(),
        val showTransactions: Boolean = false,
        val error: String? = null,
        val adjustDialogUserId: Int? = null,
        val adjustDialogUserName: String = "",
        val adjustAmount: String = "",
        val adjustReason: String = "",
        val isAdding: Boolean = true,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val students = adminApi.getTokenStudents()
                val transactions = adminApi.getTokenTransactions()
                _uiState.update { it.copy(isLoading = false, students = students, transactions = transactions) }
            } catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
    fun toggleView(showTx: Boolean) { _uiState.update { it.copy(showTransactions = showTx) } }

    fun showAdjustDialog(userId: Int, userName: String) {
        _uiState.update { it.copy(adjustDialogUserId = userId, adjustDialogUserName = userName, adjustAmount = "", adjustReason = "", isAdding = true) }
    }
    fun dismissAdjustDialog() { _uiState.update { it.copy(adjustDialogUserId = null) } }
    fun updateAdjustAmount(v: String) { _uiState.update { it.copy(adjustAmount = v) } }
    fun updateAdjustReason(v: String) { _uiState.update { it.copy(adjustReason = v) } }
    fun setIsAdding(adding: Boolean) { _uiState.update { it.copy(isAdding = adding) } }

    fun adjustTokens() {
        val state = _uiState.value
        val userId = state.adjustDialogUserId ?: return
        val amount = state.adjustAmount.toIntOrNull() ?: return
        val finalAmount = if (state.isAdding) amount else -amount
        viewModelScope.launch {
            try {
                adminApi.createTokenTransaction(AdminTokenTransactionCreateRequest(studentId = userId, amount = finalAmount, category = "admin_discretion", description = state.adjustReason.takeIf { it.isNotBlank() }))
                _uiState.update { it.copy(adjustDialogUserId = null) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTokensSection(isDark: Boolean, viewModel: AdminTokensViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Adjust dialog
    if (uiState.adjustDialogUserId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissAdjustDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                Text("Управление токенами", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                Text(uiState.adjustDialogUserName, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                // Add / Subtract toggle
                Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    FilterChip(
                        selected = uiState.isAdding,
                        onClick = { viewModel.setIsAdding(true) },
                        label = { Text("Пополнить") },
                        leadingIcon = { if (uiState.isAdding) Icon(Icons.Default.Add, null, Modifier.size(16.dp)) },
                    )
                    FilterChip(
                        selected = !uiState.isAdding,
                        onClick = { viewModel.setIsAdding(false) },
                        label = { Text("Списать") },
                        leadingIcon = { if (!uiState.isAdding) Icon(Icons.Default.Remove, null, Modifier.size(16.dp)) },
                    )
                }
                OutlinedTextField(uiState.adjustAmount, { viewModel.updateAdjustAmount(it) }, Modifier.fillMaxWidth(), label = { Text("Количество") }, shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(uiState.adjustReason, { viewModel.updateAdjustReason(it) }, Modifier.fillMaxWidth(), label = { Text("Причина") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Button(
                    onClick = { viewModel.adjustTokens() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.adjustAmount.toIntOrNull() != null && (uiState.adjustAmount.toIntOrNull() ?: 0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = if (uiState.isAdding) BrandGreen else BrandCoral),
                ) { Text(if (uiState.isAdding) "Пополнить" else "Списать", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        item { AdminSectionHeader(Icons.Default.Toll, "Управление токенами", isDark) }
        item {
            AdminFilterTabs(
                filters = listOf(AdminFilter(false, "Студенты", uiState.students.size), AdminFilter(true, "Транзакции", uiState.transactions.size)),
                selectedFilter = uiState.showTransactions, onFilterSelected = { viewModel.toggleView(it) }, isDark = isDark,
            )
        }
        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } } }
        else if (!uiState.showTransactions) {
            if (uiState.students.isEmpty()) { item { AdminEmptyState(Icons.Default.People, "Нет студентов", "Студенты с токенами появятся здесь", isDark = isDark) } }
            else items(uiState.students, key = { it.id }) { s ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.xs)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            if (!s.avatarUrl.isNullOrBlank()) {
                                val fullAvatarUrl = if (s.avatarUrl.startsWith("http")) s.avatarUrl else "https://unlocklingua.com${s.avatarUrl}"
                                AsyncImage(
                                    model = fullAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center,
                                ) { Text(s.displayName.firstOrNull()?.uppercase() ?: "?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandBlue) }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(s.displayName.ifBlank { s.email }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                                Text(s.email, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Баланс: ${s.tokenBalance}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandGold)
                        }
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            AdminActionButton("Пополнить", Icons.Default.Add, BrandGreen, onClick = { viewModel.showAdjustDialog(s.id, s.displayName.ifBlank { s.email }) })
                            AdminActionButton("Списать", Icons.Default.Remove, BrandCoral, onClick = {
                                viewModel.showAdjustDialog(s.id, s.displayName.ifBlank { s.email })
                                viewModel.setIsAdding(false)
                            })
                        }
                    }
                }
            }
        } else {
            if (uiState.transactions.isEmpty()) { item { AdminEmptyState(Icons.Default.Receipt, "Нет транзакций", "Транзакции появятся здесь", isDark = isDark) } }
            else items(uiState.transactions, key = { it.id }) { tx ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.xs)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tx.student?.displayName ?: "—", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            Text("${if (tx.amount >= 0) "+" else ""}${tx.amount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (tx.amount >= 0) BrandGreen else BrandCoral)
                        }
                        Text("${tx.transactionType}${tx.category?.let { " • $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        tx.description?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = subtextColor) }
                        tx.createdBy?.let { Text("Создал: ${it.displayName}", style = MaterialTheme.typography.labelSmall, color = subtextColor) }
                        tx.createdAt?.let { Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(alpha = 0.3f) else subtextColor.copy(alpha = 0.5f)) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  MARKET
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminMarketViewModel @Inject constructor(private val adminApi: AdminApi) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val items: List<AdminMarketItem> = emptyList(),
        val error: String? = null,
        val showCreateDialog: Boolean = false,
        val createName: String = "",
        val createDescription: String = "",
        val createTokenPrice: String = "",
        val createCategory: String = "",
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    init { loadData() }
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try { _uiState.update { it.copy(isLoading = false, items = adminApi.getMarketItems()) } }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun showCreateDialog() { _uiState.update { it.copy(showCreateDialog = true, createName = "", createDescription = "", createTokenPrice = "", createCategory = "") } }
    fun dismissCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun updateCreateName(v: String) { _uiState.update { it.copy(createName = v) } }
    fun updateCreateDescription(v: String) { _uiState.update { it.copy(createDescription = v) } }
    fun updateCreateTokenPrice(v: String) { _uiState.update { it.copy(createTokenPrice = v) } }
    fun updateCreateCategory(v: String) { _uiState.update { it.copy(createCategory = v) } }

    fun createItem() {
        val state = _uiState.value
        if (state.createName.isBlank() || state.createTokenPrice.toIntOrNull() == null) return
        viewModelScope.launch {
            try {
                adminApi.createMarketItem(AdminMarketItemCreateRequest(
                    name = state.createName.trim(),
                    description = state.createDescription.takeIf { it.isNotBlank() },
                    tokenPrice = state.createTokenPrice.toInt(),
                    category = state.createCategory.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showCreateDialog = false) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun toggleActive(id: Int, currentlyActive: Boolean) {
        viewModelScope.launch {
            try { adminApi.updateMarketItem(id, AdminMarketItemUpdateRequest(isActive = !currentlyActive)); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMarketSection(isDark: Boolean, viewModel: AdminMarketViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Create dialog
    if (uiState.showCreateDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                Text("Новый товар", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedTextField(uiState.createName, { viewModel.updateCreateName(it) }, Modifier.fillMaxWidth(), label = { Text("Название") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(uiState.createDescription, { viewModel.updateCreateDescription(it) }, Modifier.fillMaxWidth(), label = { Text("Описание") }, shape = RoundedCornerShape(12.dp), maxLines = 3)
                OutlinedTextField(uiState.createTokenPrice, { viewModel.updateCreateTokenPrice(it) }, Modifier.fillMaxWidth(), label = { Text("Цена (токены)") }, shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(uiState.createCategory, { viewModel.updateCreateCategory(it) }, Modifier.fillMaxWidth(), label = { Text("Категория") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Button(
                    onClick = { viewModel.createItem() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.createName.isNotBlank() && uiState.createTokenPrice.toIntOrNull() != null,
                ) { Text("Создать товар", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        item { AdminSectionHeader(Icons.Default.Storefront, "Маркет товаров", isDark) }

        item {
            Button(
                onClick = { viewModel.showCreateDialog() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Добавить товар", fontWeight = FontWeight.SemiBold)
            }
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } } }
        else if (uiState.items.isEmpty()) { item { AdminEmptyState(Icons.Default.Storefront, "Нет товаров", "Товары маркета появятся здесь", isDark = isDark) } }
        else {
            items(uiState.items, key = { it.id }) { item ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            AdminStatusTag(if (item.isActive) "Активен" else "Неактивен", if (item.isActive) BrandTeal else BrandCoral)
                        }
                        item.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = subtextColor, maxLines = 2) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${item.tokenPrice} токенов", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandGold)
                            item.category?.let { AdminStatusTag(it, BrandBlue) }
                        }
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            if (item.isActive) {
                                AdminActionButton("Деактивировать", Icons.Default.Block, BrandCoral, onClick = { viewModel.toggleActive(item.id, true) })
                            } else {
                                AdminActionButton("Активировать", Icons.Default.CheckCircle, BrandTeal, onClick = { viewModel.toggleActive(item.id, false) })
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LESSONS
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminLessonsViewModel @Inject constructor(private val adminApi: AdminApi) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val lessons: List<AdminLesson> = emptyList(),
        val groups: List<AdminGroup> = emptyList(),
        val error: String? = null,
        val showCreateDialog: Boolean = false,
        val createTitle: String = "",
        val createGroupId: Int? = null,
        val createStartsAt: String = "",
        val createMeetUrl: String = "",
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    init { loadData() }
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val lessons = adminApi.getLessons()
                val groups = adminApi.getGroups()
                _uiState.update { it.copy(isLoading = false, lessons = lessons, groups = groups) }
            }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun showCreateDialog() { _uiState.update { it.copy(showCreateDialog = true, createTitle = "", createGroupId = null, createStartsAt = "", createMeetUrl = "") } }
    fun dismissCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun updateCreateTitle(v: String) { _uiState.update { it.copy(createTitle = v) } }
    fun selectCreateGroup(id: Int) { _uiState.update { it.copy(createGroupId = id) } }
    fun updateCreateStartsAt(v: String) { _uiState.update { it.copy(createStartsAt = v) } }
    fun updateCreateMeetUrl(v: String) { _uiState.update { it.copy(createMeetUrl = v) } }

    fun createLesson() {
        val state = _uiState.value
        if (state.createTitle.isBlank() || state.createGroupId == null || state.createStartsAt.isBlank()) return
        viewModelScope.launch {
            try {
                adminApi.createLesson(AdminLessonCreateRequest(
                    title = state.createTitle.trim(),
                    groupId = state.createGroupId,
                    startsAt = state.createStartsAt.trim(),
                    meetUrl = state.createMeetUrl.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showCreateDialog = false) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLessonsSection(isDark: Boolean, viewModel: AdminLessonsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Create dialog
    if (uiState.showCreateDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                Text("Новый урок", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedTextField(uiState.createTitle, { viewModel.updateCreateTitle(it) }, Modifier.fillMaxWidth(), label = { Text("Название урока") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                // Group selector
                Text("Группа:", style = MaterialTheme.typography.labelMedium, color = subtextColor)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.groups.forEach { group ->
                        val isSelected = uiState.createGroupId == group.id
                        Surface(
                            onClick = { viewModel.selectCreateGroup(group.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BrandBlue.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                        ) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${group.name} (HSK ${group.hskLevel})", style = MaterialTheme.typography.bodySmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) BrandBlue else textColor)
                                if (isSelected) Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = BrandBlue)
                            }
                        }
                    }
                }
                OutlinedTextField(uiState.createStartsAt, { viewModel.updateCreateStartsAt(it) }, Modifier.fillMaxWidth(), label = { Text("Начало (ГГГГ-ММ-ДД ЧЧ:ММ)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(uiState.createMeetUrl, { viewModel.updateCreateMeetUrl(it) }, Modifier.fillMaxWidth(), label = { Text("Ссылка на Meet (опционально)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Button(
                    onClick = { viewModel.createLesson() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.createTitle.isNotBlank() && uiState.createGroupId != null && uiState.createStartsAt.isNotBlank(),
                ) { Text("Создать урок", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        item { AdminSectionHeader(Icons.Default.Videocam, "Уроки Live", isDark) }

        item {
            Button(
                onClick = { viewModel.showCreateDialog() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать урок", fontWeight = FontWeight.SemiBold)
            }
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } } }
        else if (uiState.lessons.isEmpty()) { item { AdminEmptyState(Icons.Default.Videocam, "Нет уроков", "Live уроки появятся здесь", isDark = isDark) } }
        else {
            items(uiState.lessons, key = { it.id }) { lesson ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(lesson.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            AdminStatusTag(statusLabel(lesson.status), statusColor(lesson.status))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Group, null, Modifier.size(14.dp), tint = subtextColor)
                            Text(lesson.groupName, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = subtextColor)
                            Text(lesson.startsAt.take(16).replace("T", " "), style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        }
                        lesson.meetUrl?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Link, null, Modifier.size(14.dp), tint = BrandBlue)
                                Text("Meet ссылка", style = MaterialTheme.typography.bodySmall, color = BrandBlue)
                            }
                        }
                        lesson.notes?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = subtextColor, maxLines = 2)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HOMEWORK
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminHomeworkViewModel @Inject constructor(private val adminApi: AdminApi) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val groups: List<AdminHomeworkGroup> = emptyList(),
        val assignments: List<AdminHomeworkAssignment> = emptyList(),
        val allGroups: List<AdminGroup> = emptyList(),
        val showAssignments: Boolean = false,
        val error: String? = null,
        val showCreateDialog: Boolean = false,
        val createTitle: String = "",
        val createDescription: String = "",
        val createGroupId: Int? = null,
        val createDueDate: String = "",
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    init { loadData() }
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val groups = adminApi.getHomeworkGroups()
                val assignments = adminApi.getHomeworkAssignments()
                val allGroups = adminApi.getGroups()
                _uiState.update { it.copy(isLoading = false, groups = groups, assignments = assignments, allGroups = allGroups) }
            }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun toggleView(show: Boolean) { _uiState.update { it.copy(showAssignments = show) } }

    fun showCreateDialog() { _uiState.update { it.copy(showCreateDialog = true, createTitle = "", createDescription = "", createGroupId = null, createDueDate = "") } }
    fun dismissCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun updateCreateTitle(v: String) { _uiState.update { it.copy(createTitle = v) } }
    fun updateCreateDescription(v: String) { _uiState.update { it.copy(createDescription = v) } }
    fun selectCreateGroup(id: Int) { _uiState.update { it.copy(createGroupId = id) } }
    fun updateCreateDueDate(v: String) { _uiState.update { it.copy(createDueDate = v) } }

    fun createAssignment() {
        val state = _uiState.value
        if (state.createTitle.isBlank() || state.createGroupId == null) return
        viewModelScope.launch {
            try {
                adminApi.createHomeworkAssignment(AdminHomeworkCreateRequest(
                    title = state.createTitle.trim(),
                    description = state.createDescription.takeIf { it.isNotBlank() },
                    groupId = state.createGroupId,
                    dueDate = state.createDueDate.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showCreateDialog = false) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeworkSection(isDark: Boolean, viewModel: AdminHomeworkViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Create dialog
    if (uiState.showCreateDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                Text("Новое задание", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedTextField(uiState.createTitle, { viewModel.updateCreateTitle(it) }, Modifier.fillMaxWidth(), label = { Text("Название") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(uiState.createDescription, { viewModel.updateCreateDescription(it) }, Modifier.fillMaxWidth().heightIn(min = 80.dp), label = { Text("Описание") }, shape = RoundedCornerShape(12.dp), maxLines = 5)
                // Group selector
                Text("Группа:", style = MaterialTheme.typography.labelMedium, color = subtextColor)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.allGroups.forEach { group ->
                        val isSelected = uiState.createGroupId == group.id
                        Surface(
                            onClick = { viewModel.selectCreateGroup(group.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BrandBlue.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                        ) {
                            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${group.name} (HSK ${group.hskLevel})", style = MaterialTheme.typography.bodySmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) BrandBlue else textColor)
                                if (isSelected) Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = BrandBlue)
                            }
                        }
                    }
                }
                OutlinedTextField(uiState.createDueDate, { viewModel.updateCreateDueDate(it) }, Modifier.fillMaxWidth(), label = { Text("Дедлайн (ГГГГ-ММ-ДД)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Button(
                    onClick = { viewModel.createAssignment() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.createTitle.isNotBlank() && uiState.createGroupId != null,
                ) { Text("Создать задание", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        item { AdminSectionHeader(Icons.AutoMirrored.Filled.Assignment, "Домашние задания", isDark) }

        item {
            Button(
                onClick = { viewModel.showCreateDialog() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать задание", fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            AdminFilterTabs(
                filters = listOf(AdminFilter(false, "Группы", uiState.groups.size), AdminFilter(true, "Задания", uiState.assignments.size)),
                selectedFilter = uiState.showAssignments, onFilterSelected = { viewModel.toggleView(it) }, isDark = isDark,
            )
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } } }
        else if (!uiState.showAssignments) {
            // Groups view
            if (uiState.groups.isEmpty()) { item { AdminEmptyState(Icons.Default.Group, "Нет групп", "Группы с заданиями появятся здесь", isDark = isDark) } }
            else items(uiState.groups, key = { it.id }) { group ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(group.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(6.dp), color = hskLevelColor(group.hskLevel).copy(alpha = 0.15f)) {
                                Text("HSK ${group.hskLevel}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = hskLevelColor(group.hskLevel))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, null, Modifier.size(14.dp), tint = BrandBlue)
                            Text("${group.assignmentsCount} заданий", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = BrandBlue)
                        }
                    }
                }
            }
        } else {
            // Assignments view
            if (uiState.assignments.isEmpty()) { item { AdminEmptyState(Icons.AutoMirrored.Filled.Assignment, "Нет заданий", "Домашние задания появятся здесь", isDark = isDark) } }
            else items(uiState.assignments, key = { it.id }) { hw ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(hw.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            AdminStatusTag(statusLabel(hw.status), statusColor(hw.status))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Group, null, Modifier.size(14.dp), tint = subtextColor)
                            Text(hw.groupName, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        }
                        hw.dueDate?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Event, null, Modifier.size(14.dp), tint = subtextColor)
                                Text("Дедлайн: ${it.take(10)}", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp), tint = BrandGreen)
                            Text("Сдали: ${hw.submissionsCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandGreen)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  RECEIPTS
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminReceiptsViewModel @Inject constructor(private val adminApi: AdminApi) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val receipts: List<AdminReceipt> = emptyList(),
        val selectedFilter: String = "all",
        val error: String? = null,
        val noteDialogId: Int? = null,
        val noteText: String = "",
    ) {
        val filtered: List<AdminReceipt>
            get() = if (selectedFilter == "all") receipts else receipts.filter { it.status.lowercase() == selectedFilter }
    }
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    init { loadData() }
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try { _uiState.update { it.copy(isLoading = false, receipts = adminApi.getReceipts()) } }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
    fun selectFilter(f: String) { _uiState.update { it.copy(selectedFilter = f) } }

    fun approveReceipt(id: Int) {
        viewModelScope.launch {
            try { adminApi.approveReceipt(id); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
    fun rejectReceipt(id: Int) {
        viewModelScope.launch {
            try {
                val note = _uiState.value.receipts.find { it.id == id }?.adminNote
                adminApi.rejectReceipt(id, AdminReceiptActionRequest(adminNote = note))
                loadData()
            }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun showNoteDialog(id: Int) {
        val note = _uiState.value.receipts.find { it.id == id }?.adminNote ?: ""
        _uiState.update { it.copy(noteDialogId = id, noteText = note) }
    }
    fun dismissNote() { _uiState.update { it.copy(noteDialogId = null, noteText = "") } }
    fun updateNoteText(t: String) { _uiState.update { it.copy(noteText = t) } }
    fun saveNote() {
        _uiState.update { it.copy(noteDialogId = null, noteText = "") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReceiptsSection(isDark: Boolean, refreshTrigger: Int = 0, viewModel: AdminReceiptsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Reload when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) viewModel.loadData()
    }
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Note dialog
    if (uiState.noteDialogId != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissNote() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                Text("Заметка", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedTextField(uiState.noteText, { viewModel.updateNoteText(it) }, Modifier.fillMaxWidth().heightIn(min = 100.dp), label = { Text("Заметка") }, shape = RoundedCornerShape(12.dp), maxLines = 5)
                Button({ viewModel.saveNote() }, Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp)) { Text("Сохранить", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
        item { AdminSectionHeader(Icons.Default.Receipt, "Квитанции", isDark) }
        item {
            val pending = uiState.receipts.count { it.status.lowercase() == "pending" }
            val approved = uiState.receipts.count { it.status.lowercase() == "approved" }
            val rejected = uiState.receipts.count { it.status.lowercase() == "rejected" }
            AdminFilterTabs(
                filters = listOf(
                    AdminFilter("all", "Все", uiState.receipts.size),
                    AdminFilter("pending", "Ожидают", pending),
                    AdminFilter("approved", "Подтверждённые", approved),
                    AdminFilter("rejected", "Отклонённые", rejected),
                ),
                selectedFilter = uiState.selectedFilter, onFilterSelected = { viewModel.selectFilter(it) }, isDark = isDark,
            )
        }
        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) { item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } } }
        else if (uiState.filtered.isEmpty()) { item { AdminEmptyState(Icons.Default.Receipt, "Нет квитанций", "Квитанции появятся здесь", isDark = isDark) } }
        else {
            items(uiState.filtered, key = { it.id }) { receipt ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(receipt.studentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            AdminStatusTag(statusLabel(receipt.status), statusColor(receipt.status))
                        }
                        receipt.groupName?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Group, null, Modifier.size(14.dp), tint = subtextColor)
                                Text("Группа: $it", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            }
                        }
                        // Receipt image toggle
                        if (receipt.imageUrl.isNotBlank()) {
                            var showImage by remember { mutableStateOf(false) }
                            Surface(
                                onClick = { showImage = !showImage },
                                shape = RoundedCornerShape(8.dp),
                                color = BrandBlue.copy(alpha = 0.10f),
                            ) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(if (showImage) Icons.Default.PhotoLibrary else Icons.Default.Photo, null, Modifier.size(14.dp), tint = BrandBlue)
                                    Text(if (showImage) "Скрыть чек" else "Показать чек", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandBlue)
                                }
                            }
                            if (showImage) {
                                val imgUrl = if (receipt.imageUrl.startsWith("http")) receipt.imageUrl else "https://unlocklingua.com${receipt.imageUrl}"
                                coil3.compose.AsyncImage(
                                    model = imgUrl,
                                    contentDescription = "Квитанция",
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                        // Shi Fu verdict
                        receipt.extractedAmount?.let { amount ->
                            Surface(color = BrandTeal.copy(alpha = 0.10f), shape = RoundedCornerShape(10.dp)) {
                                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🐉", fontSize = 18.sp)
                                    Column {
                                        Text("Вердикт Ши Фу", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandTeal)
                                        Text("$amount сум", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            receipt.finalAmount?.let { Text("Итого: $it сум", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandGreen) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            receipt.paymentMethod?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = subtextColor) }
                            Text("${receipt.month}/${receipt.year}", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                        }
                        receipt.reviewedByName?.let {
                            Text("Проверил: $it", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                        }
                        receipt.adminNote?.takeIf { it.isNotBlank() }?.let { note ->
                            Surface(color = BrandBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Note, null, Modifier.size(14.dp), tint = BrandBlue)
                                    Text(note, style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            if (receipt.status.lowercase() == "pending") {
                                AdminActionButton("Подтвердить", Icons.Default.CheckCircle, BrandTeal, onClick = { viewModel.approveReceipt(receipt.id) })
                                AdminActionButton("Отклонить", Icons.Default.Cancel, BrandCoral, onClick = { viewModel.rejectReceipt(receipt.id) })
                            }
                            AdminActionButton("Заметка", Icons.AutoMirrored.Filled.Note, BrandBlue, onClick = { viewModel.showNoteDialog(receipt.id) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}
