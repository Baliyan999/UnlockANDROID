package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.data.remote.dto.admin.AdminLead
import com.subnetik.unlock.presentation.screens.admin.components.*
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLeadsSection(
    isDark: Boolean,
    viewModel: AdminLeadsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Note dialog
    if (uiState.noteDialogLeadId != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissNoteDialog() },
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
            ) {
                Text(
                    "Заметка администратора",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                OutlinedTextField(
                    value = uiState.noteText,
                    onValueChange = { viewModel.updateNoteText(it) },
                    label = { Text("Заметка") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6,
                )
                Button(
                    onClick = { viewModel.saveNote() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.actionInProgress == null,
                ) {
                    if (uiState.actionInProgress != null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Сохранить", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Brand.Spacing.lg,
            vertical = Brand.Spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        // Section header
        item {
            AdminSectionHeader(
                icon = Icons.Default.Description,
                title = "Управление заявками",
                isDark = isDark,
            )
        }

        // Filter tabs
        item {
            val stats = uiState.leadStats
            AdminFilterTabs(
                filters = listOf(
                    AdminFilter(LeadFilter.PENDING, "Заявки", stats?.pending ?: 0),
                    AdminFilter(LeadFilter.PROCESSED, "Обработанные", stats?.processed ?: 0),
                    AdminFilter(LeadFilter.DELETED, "Удаленные", stats?.deleted ?: 0),
                ),
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.selectFilter(it) },
                isDark = isDark,
            )
        }

        // Loading
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Brand.Spacing.xxxl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = BrandBlue,
                        strokeWidth = 3.dp,
                    )
                }
            }
        }

        // Error
        uiState.error?.let { error ->
            item {
                AdminGlassCard(isDark = isDark) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Brand.Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Error, null, tint = BrandCoral, modifier = Modifier.size(18.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = BrandCoral)
                    }
                }
            }
        }

        // Lead cards
        if (!uiState.isLoading) {
            val filtered = uiState.filteredLeads
            if (filtered.isEmpty()) {
                item {
                    AdminEmptyState(
                        icon = Icons.Default.FindInPage,
                        title = "Нет заявок",
                        description = "Заявки появятся здесь после отправки пользователями",
                        isDark = isDark,
                    )
                }
            } else {
                items(filtered, key = { it.id }) { lead ->
                    AdminLeadCard(
                        lead = lead,
                        filter = uiState.selectedFilter,
                        isDark = isDark,
                        isActionInProgress = uiState.actionInProgress == lead.id,
                        onProcess = { viewModel.processLead(lead.id) },
                        onReturn = { viewModel.returnLead(lead.id) },
                        onNote = { viewModel.showNoteDialog(lead.id) },
                        onDelete = { viewModel.softDeleteLead(lead.id) },
                        onHardDelete = { viewModel.hardDeleteLead(lead.id) },
                    )
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ─── Lead Card ───────────────────────────────────────────────

@Composable
private fun AdminLeadCard(
    lead: AdminLead,
    filter: LeadFilter,
    isDark: Boolean,
    isActionInProgress: Boolean,
    onProcess: () -> Unit,
    onReturn: () -> Unit,
    onNote: () -> Unit,
    onDelete: () -> Unit,
    onHardDelete: () -> Unit,
) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    AdminGlassCard(isDark = isDark) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Brand.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
        ) {
            // Header: name + tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    lead.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdminStatusTag(
                        text = statusLabel(lead.status),
                        color = statusColor(lead.status),
                    )
                    if (lead.source.isNotBlank()) {
                        AdminStatusTag(
                            text = lead.source,
                            color = BrandBlue,
                        )
                    }
                }
            }

            // Contact info
            if (lead.email.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = subtextColor)
                    Text(lead.email, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                }
            }
            lead.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = subtextColor)
                    Text(phone, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                }
            }

            // Details row
            val details = buildList {
                lead.format?.takeIf { it.isNotBlank() }?.let { add("Формат: $it") }
                lead.languageLevel?.takeIf { it.isNotBlank() }?.let { add("Уровень: $it") }
                lead.promocode?.takeIf { it.isNotBlank() }?.let { add("Промокод: $it") }
                lead.preferredTime?.takeIf { it.isNotBlank() }?.let { add("Время: $it") }
            }
            if (details.isNotEmpty()) {
                Text(
                    details.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = subtextColor,
                )
            }

            // Message
            lead.message?.takeIf { it.isNotBlank() }?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor,
                    maxLines = 3,
                )
            }

            // Admin note
            lead.adminNote?.takeIf { it.isNotBlank() }?.let { note ->
                Surface(
                    color = BrandBlue.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.StickyNote2,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = BrandBlue,
                        )
                        Text(
                            note,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // Created date
            lead.createdAt?.let { date ->
                Text(
                    formatDate(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }

            // Action buttons
            if (!isActionInProgress) {
                HorizontalDivider(
                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                ) {
                    when (filter) {
                        LeadFilter.PENDING -> {
                            AdminActionButton("Обработать", Icons.Default.CheckCircle, BrandTeal, onProcess)
                            AdminActionButton("Заметка", Icons.AutoMirrored.Filled.StickyNote2, BrandBlue, onNote)
                            AdminActionButton("Удалить", Icons.Default.Delete, BrandCoral, onDelete)
                        }
                        LeadFilter.PROCESSED -> {
                            AdminActionButton("Вернуть", Icons.AutoMirrored.Filled.Undo, BrandGold, onReturn)
                            AdminActionButton("Заметка", Icons.AutoMirrored.Filled.StickyNote2, BrandBlue, onNote)
                            AdminActionButton("Удалить", Icons.Default.Delete, BrandCoral, onDelete)
                        }
                        LeadFilter.DELETED -> {
                            AdminActionButton("Заметка", Icons.AutoMirrored.Filled.StickyNote2, BrandBlue, onNote)
                            AdminActionButton("Удалить навсегда", Icons.Default.DeleteForever, BrandCoral, onHardDelete)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Brand.Spacing.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BrandBlue,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) {
            val day = parts[2]
            val month = when (parts[1]) {
                "01" -> "янв"
                "02" -> "фев"
                "03" -> "мар"
                "04" -> "апр"
                "05" -> "май"
                "06" -> "июн"
                "07" -> "июл"
                "08" -> "авг"
                "09" -> "сен"
                "10" -> "окт"
                "11" -> "ноя"
                "12" -> "дек"
                else -> parts[1]
            }
            "$day $month ${parts[0]}"
        } else {
            isoDate.take(10)
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
}
