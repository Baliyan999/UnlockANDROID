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
import com.subnetik.unlock.data.remote.dto.admin.AdminSupportBooking
import com.subnetik.unlock.data.remote.dto.admin.AdminSupportUpdateRequest
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
class AdminSupportViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val bookings: List<AdminSupportBooking> = emptyList(),
        val selectedFilter: String = "all",
        val error: String? = null,
        val noteDialogId: Int? = null,
        val noteText: String = "",
    ) {
        val filtered: List<AdminSupportBooking>
            get() = if (selectedFilter == "all") bookings else bookings.filter { it.status.lowercase() == selectedFilter }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try { _uiState.update { it.copy(isLoading = false, bookings = adminApi.getSupportBookings()) } }
            catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun selectFilter(f: String) { _uiState.update { it.copy(selectedFilter = f) } }

    fun processBooking(id: Int) { updateBooking(id, AdminSupportUpdateRequest(status = "processed")) }
    fun cancelBooking(id: Int) { updateBooking(id, AdminSupportUpdateRequest(status = "cancelled")) }

    fun showNoteDialog(id: Int) {
        val note = _uiState.value.bookings.find { it.id == id }?.adminNote ?: ""
        _uiState.update { it.copy(noteDialogId = id, noteText = note) }
    }
    fun dismissNote() { _uiState.update { it.copy(noteDialogId = null, noteText = "") } }
    fun updateNoteText(t: String) { _uiState.update { it.copy(noteText = t) } }
    fun saveNote() {
        val id = _uiState.value.noteDialogId ?: return
        updateBooking(id, AdminSupportUpdateRequest(adminNote = _uiState.value.noteText))
        _uiState.update { it.copy(noteDialogId = null, noteText = "") }
    }

    private fun updateBooking(id: Int, req: AdminSupportUpdateRequest) {
        viewModelScope.launch {
            try { adminApi.updateSupportBooking(id, req); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportSection(isDark: Boolean, viewModel: AdminSupportViewModel = hiltViewModel()) {
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
        item { AdminSectionHeader(Icons.Default.SupportAgent, "Управление Support", isDark) }
        item {
            val pending = uiState.bookings.count { it.status.lowercase() == "pending" }
            val processed = uiState.bookings.count { it.status.lowercase() == "processed" }
            val cancelled = uiState.bookings.count { it.status.lowercase() == "cancelled" }
            AdminFilterTabs(listOf(
                AdminFilter("all", "Все", uiState.bookings.size),
                AdminFilter("pending", "Ожидают", pending),
                AdminFilter("processed", "Обработанные", processed),
                AdminFilter("cancelled", "Отменённые", cancelled),
            ), uiState.selectedFilter, { viewModel.selectFilter(it) }, isDark)
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.filtered.isEmpty()) {
            item { AdminEmptyState(Icons.Default.SupportAgent, "Нет записей", "Записи на support появятся здесь", isDark) }
        } else {
            items(uiState.filtered, key = { it.id }) { b ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(b.studentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                            AdminStatusTag(statusLabel(b.status), statusColor(b.status))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = subtextColor)
                            Text("Учитель: ${b.supportTeacher}", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = subtextColor)
                            Text(b.sessionDatetime, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        }
                        if (b.comment.isNotBlank()) Text(b.comment, style = MaterialTheme.typography.bodySmall, color = subtextColor, maxLines = 3)
                        b.adminNote?.takeIf { it.isNotBlank() }?.let { note ->
                            Surface(color = BrandBlue.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.AutoMirrored.Filled.Note, null, Modifier.size(14.dp), tint = BrandBlue)
                                    Text(note, style = MaterialTheme.typography.labelSmall, color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                        // Actions
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            if (b.status.lowercase() == "pending") {
                                AdminActionButton("Обработать", Icons.Default.CheckCircle, BrandTeal, onClick = { viewModel.processBooking(b.id) })
                                AdminActionButton("Отменить", Icons.Default.Cancel, BrandCoral, onClick = { viewModel.cancelBooking(b.id) })
                            }
                            AdminActionButton("Заметка", Icons.AutoMirrored.Filled.Note, BrandBlue, onClick = { viewModel.showNoteDialog(b.id) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}
