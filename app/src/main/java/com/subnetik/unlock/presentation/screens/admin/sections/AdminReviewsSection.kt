package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.AdminReview
import com.subnetik.unlock.data.remote.dto.admin.AdminReviewUpdateRequest
import com.subnetik.unlock.presentation.screens.admin.components.*
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminReviewsViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val reviews: List<AdminReview> = emptyList(),
        val selectedFilter: String = "all",
        val error: String? = null,
        val noteDialogId: Int? = null,
        val noteText: String = "",
    ) {
        val filtered: List<AdminReview>
            get() = if (selectedFilter == "all") reviews else reviews.filter { it.status.lowercase() == selectedFilter }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try { _uiState.update { it.copy(isLoading = false, reviews = adminApi.getReviews()) } }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun selectFilter(f: String) { _uiState.update { it.copy(selectedFilter = f) } }
    fun publishReview(id: Int) { updateStatus(id, "published") }
    fun rejectReview(id: Int) { updateStatus(id, "rejected") }
    fun deleteReview(id: Int) {
        viewModelScope.launch {
            try { adminApi.deleteReview(id); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
    fun showNoteDialog(id: Int) {
        val note = _uiState.value.reviews.find { it.id == id }?.adminNote ?: ""
        _uiState.update { it.copy(noteDialogId = id, noteText = note) }
    }
    fun dismissNote() { _uiState.update { it.copy(noteDialogId = null, noteText = "") } }
    fun updateNoteText(t: String) { _uiState.update { it.copy(noteText = t) } }
    fun saveNote() {
        val id = _uiState.value.noteDialogId ?: return
        viewModelScope.launch {
            try { adminApi.updateReviewNote(id, AdminReviewUpdateRequest(adminNote = _uiState.value.noteText)); _uiState.update { it.copy(noteDialogId = null, noteText = "") }; loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    private fun updateStatus(id: Int, status: String) {
        viewModelScope.launch {
            try {
                when (status) {
                    "approved" -> adminApi.approveReview(id)
                    "rejected" -> adminApi.rejectReview(id)
                }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewsSection(isDark: Boolean, viewModel: AdminReviewsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
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
        item { AdminSectionHeader(Icons.Default.RateReview, "Управление отзывами", isDark) }
        item {
            val published = uiState.reviews.count { it.status.lowercase() == "published" }
            val pending = uiState.reviews.count { it.status.lowercase() == "pending" }
            val rejected = uiState.reviews.count { it.status.lowercase() == "rejected" }
            val deleted = uiState.reviews.count { it.status.lowercase() == "deleted" }
            AdminFilterTabs(listOf(
                AdminFilter("all", "Все", uiState.reviews.size),
                AdminFilter("pending", "Ожидают", pending),
                AdminFilter("published", "Опубликованные", published),
                AdminFilter("rejected", "Отклонённые", rejected),
                AdminFilter("deleted", "Удалённые", deleted),
            ), uiState.selectedFilter, { viewModel.selectFilter(it) }, isDark)
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.filtered.isEmpty()) {
            item { AdminEmptyState(Icons.Default.RateReview, "Нет отзывов", "Отзывы появятся здесь", isDark) }
        } else {
            items(uiState.filtered, key = { it.id }) { review ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(review.author, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                                if (review.isStudent) {
                                    Icon(Icons.Default.WorkspacePremium, null, Modifier.size(18.dp), tint = BrandGold)
                                }
                            }
                            AdminStatusTag(statusLabel(review.status), statusColor(review.status))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(5) { i ->
                                Icon(if (i < review.rating) Icons.Default.Star else Icons.Default.StarOutline, null, Modifier.size(16.dp), tint = if (i < review.rating) BrandGold else subtextColor)
                            }
                        }
                        Text(review.text, style = MaterialTheme.typography.bodySmall, color = subtextColor, maxLines = 5)
                        review.adminNote?.takeIf { it.isNotBlank() }?.let { note ->
                            Surface(color = BrandBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Note, null, Modifier.size(14.dp), tint = BrandBlue)
                                    Text(note, style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            when (review.status.lowercase()) {
                                "pending" -> {
                                    AdminActionButton("Опубликовать", Icons.Default.CheckCircle, BrandTeal, onClick = { viewModel.publishReview(review.id) })
                                    AdminActionButton("Отклонить", Icons.Default.Cancel, BrandCoral, onClick = { viewModel.rejectReview(review.id) })
                                }
                                "published" -> {
                                    AdminActionButton("Отклонить", Icons.Default.Cancel, BrandCoral, onClick = { viewModel.rejectReview(review.id) })
                                }
                                "rejected" -> {
                                    AdminActionButton("Опубликовать", Icons.Default.CheckCircle, BrandTeal, onClick = { viewModel.publishReview(review.id) })
                                }
                            }
                            AdminActionButton("Заметка", Icons.AutoMirrored.Filled.Note, BrandBlue, onClick = { viewModel.showNoteDialog(review.id) })
                            if (review.status.lowercase() != "deleted") {
                                AdminActionButton("Удалить", Icons.Default.Delete, BrandCoral, onClick = { viewModel.deleteReview(review.id) })
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}
