package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.AdminPromocode
import com.subnetik.unlock.data.remote.dto.admin.AdminPromocodeCreateRequest
import com.subnetik.unlock.data.remote.dto.admin.AdminPromocodeUpdateRequest
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
class AdminPromocodesViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val promocodes: List<AdminPromocode> = emptyList(),
        val selectedFilter: String = "all",
        val error: String? = null,
        val showCreateDialog: Boolean = false,
        val createCode: String = "",
        val createDiscountPercent: String = "",
        val createDiscountAmount: String = "",
        val createUsageLimit: String = "",
        val createExpiresAt: String = "",
    ) {
        val filtered: List<AdminPromocode>
            get() = when (selectedFilter) {
                "active" -> promocodes.filter { it.isActive && it.status.lowercase() != "deleted" }
                "inactive" -> promocodes.filter { !it.isActive && it.status.lowercase() != "deleted" }
                "deleted" -> promocodes.filter { it.status.lowercase() == "deleted" }
                else -> promocodes
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val codes = adminApi.getPromocodes()
                _uiState.update { it.copy(isLoading = false, promocodes = codes) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectFilter(f: String) { _uiState.update { it.copy(selectedFilter = f) } }

    fun showCreateDialog() { _uiState.update { it.copy(showCreateDialog = true, createCode = "", createDiscountPercent = "", createDiscountAmount = "", createUsageLimit = "", createExpiresAt = "") } }
    fun dismissCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun updateCreateCode(v: String) { _uiState.update { it.copy(createCode = v) } }
    fun updateCreateDiscountPercent(v: String) { _uiState.update { it.copy(createDiscountPercent = v) } }
    fun updateCreateDiscountAmount(v: String) { _uiState.update { it.copy(createDiscountAmount = v) } }
    fun updateCreateUsageLimit(v: String) { _uiState.update { it.copy(createUsageLimit = v) } }
    fun updateCreateExpiresAt(v: String) { _uiState.update { it.copy(createExpiresAt = v) } }

    fun createPromocode() {
        val state = _uiState.value
        if (state.createCode.isBlank()) return
        viewModelScope.launch {
            try {
                adminApi.createPromocode(AdminPromocodeCreateRequest(
                    code = state.createCode.trim(),
                    discountPercent = state.createDiscountPercent.toIntOrNull(),
                    discountAmount = state.createDiscountAmount.toIntOrNull(),
                    usageLimit = state.createUsageLimit.toIntOrNull(),
                    expiresAt = state.createExpiresAt.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showCreateDialog = false) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun toggleActive(id: Int, currentlyActive: Boolean) {
        viewModelScope.launch {
            try { adminApi.updatePromocode(id, AdminPromocodeUpdateRequest(isActive = !currentlyActive)); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deletePromocode(id: Int) {
        viewModelScope.launch {
            try { adminApi.deletePromocode(id); loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPromocodesSection(isDark: Boolean, viewModel: AdminPromocodesViewModel = hiltViewModel()) {
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
                Text("Новый промокод", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedTextField(uiState.createCode, { viewModel.updateCreateCode(it) }, Modifier.fillMaxWidth(), label = { Text("Код промокода") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    OutlinedTextField(uiState.createDiscountPercent, { viewModel.updateCreateDiscountPercent(it) }, Modifier.weight(1f), label = { Text("Скидка %") }, shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(uiState.createDiscountAmount, { viewModel.updateCreateDiscountAmount(it) }, Modifier.weight(1f), label = { Text("Скидка сум") }, shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(uiState.createUsageLimit, { viewModel.updateCreateUsageLimit(it) }, Modifier.fillMaxWidth(), label = { Text("Лимит использования") }, shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(uiState.createExpiresAt, { viewModel.updateCreateExpiresAt(it) }, Modifier.fillMaxWidth(), label = { Text("Действует до (ГГГГ-ММ-ДД)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Button({ viewModel.createPromocode() }, Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), enabled = uiState.createCode.isNotBlank()) { Text("Создать", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item { AdminSectionHeader(Icons.Default.LocalOffer, "Управление промокодами", isDark) }

        // Create button
        item {
            Button(
                onClick = { viewModel.showCreateDialog() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Создать промокод", fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            val active = uiState.promocodes.count { it.isActive && it.status.lowercase() != "deleted" }
            val inactive = uiState.promocodes.count { !it.isActive && it.status.lowercase() != "deleted" }
            val deleted = uiState.promocodes.count { it.status.lowercase() == "deleted" }
            AdminFilterTabs(
                filters = listOf(
                    AdminFilter("all", "Все", uiState.promocodes.size),
                    AdminFilter("active", "Активные", active),
                    AdminFilter("inactive", "Неактивные", inactive),
                    AdminFilter("deleted", "Удалённые", deleted),
                ),
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.selectFilter(it) },
                isDark = isDark,
            )
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.filtered.isEmpty()) {
            item { AdminEmptyState(Icons.Default.LocalOffer, "Нет промокодов", "Промокоды появятся здесь", isDark = isDark) }
        } else {
            items(uiState.filtered, key = { it.id }) { code ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(code.code, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                            AdminStatusTag(if (code.status.lowercase() == "deleted") "Удалён" else if (code.isActive) "Активен" else "Неактивен", if (code.status.lowercase() == "deleted") BrandCoral else if (code.isActive) BrandTeal else BrandCoral)
                        }
                        val discount = when {
                            code.discountPercent != null -> "${code.discountPercent}%"
                            code.discountAmount != null -> "${code.discountAmount} сум"
                            else -> "—"
                        }
                        Text("Скидка: $discount", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        Text("Использовано: ${code.usageCount}${code.usageLimit?.let { " / $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                        code.expiresAt?.let { Text("Действует до: ${it.take(10)}", style = MaterialTheme.typography.labelSmall, color = subtextColor) }
                        // Actions
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            if (code.status.lowercase() != "deleted") {
                                if (code.isActive) {
                                    AdminActionButton("Деактивировать", Icons.Default.Block, BrandCoral, onClick = { viewModel.toggleActive(code.id, true) })
                                } else {
                                    AdminActionButton("Активировать", Icons.Default.CheckCircle, BrandTeal, onClick = { viewModel.toggleActive(code.id, false) })
                                }
                                AdminActionButton("Удалить", Icons.Default.Delete, BrandCoral, onClick = { viewModel.deletePromocode(code.id) })
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}
