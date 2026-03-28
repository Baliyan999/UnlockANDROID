package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.remote.api.CalendarApi
import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventCreate
import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventResponse
import com.subnetik.unlock.presentation.screens.admin.components.AdminGlassCard
import com.subnetik.unlock.presentation.screens.admin.components.AdminSectionHeader
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════════════

@HiltViewModel
class AdminCalendarViewModel @Inject constructor(private val calendarApi: CalendarApi) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val events: List<CalendarEventResponse> = emptyList(),
        val error: String? = null,
        val showCreateSheet: Boolean = false,
        val deleteConfirmId: Int? = null,
        // Create form
        val newTitle: String = "",
        val newDescription: String = "",
        val newEventType: String = "holiday",
        val newVisibility: String = "all",
        val newCancelsLessons: Boolean = true,
        val newNoCompensation: Boolean = true,
        val newDateMillis: Long = System.currentTimeMillis(),
        val newHasEndDate: Boolean = false,
        val newEndDateMillis: Long = System.currentTimeMillis(),
        val isSaving: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val events = calendarApi.getEvents()
                _uiState.update { it.copy(isLoading = false, events = events.sortedBy { e -> e.date }) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun showCreate() {
        _uiState.update {
            it.copy(
                showCreateSheet = true, newTitle = "", newDescription = "",
                newEventType = "holiday", newCancelsLessons = true, newNoCompensation = true,
                newDateMillis = System.currentTimeMillis(), newHasEndDate = false,
                newEndDateMillis = System.currentTimeMillis(),
            )
        }
    }
    fun dismissCreate() { _uiState.update { it.copy(showCreateSheet = false) } }
    fun updateNewTitle(v: String) { _uiState.update { it.copy(newTitle = v) } }
    fun updateNewDescription(v: String) { _uiState.update { it.copy(newDescription = v) } }
    fun updateNewEventType(v: String) { _uiState.update { it.copy(newEventType = v, newCancelsLessons = v == "holiday") } }
    fun updateNewVisibility(v: String) { _uiState.update { it.copy(newVisibility = v) } }
    fun updateNewCancelsLessons(v: Boolean) { _uiState.update { it.copy(newCancelsLessons = v) } }
    fun updateNewNoCompensation(v: Boolean) { _uiState.update { it.copy(newNoCompensation = v) } }
    fun updateNewDateMillis(v: Long) { _uiState.update { it.copy(newDateMillis = v) } }
    fun updateNewHasEndDate(v: Boolean) { _uiState.update { it.copy(newHasEndDate = v) } }
    fun updateNewEndDateMillis(v: Long) { _uiState.update { it.copy(newEndDateMillis = v) } }

    private fun formatDate(millis: Long): String {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return df.format(Date(millis))
    }

    fun createEvent() {
        val s = _uiState.value
        if (s.newTitle.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val color = when (s.newEventType) { "holiday" -> "red"; "note" -> "yellow"; else -> "blue" }
                calendarApi.createEvent(
                    CalendarEventCreate(
                        date = formatDate(s.newDateMillis),
                        endDate = if (s.newHasEndDate) formatDate(s.newEndDateMillis) else null,
                        title = s.newTitle,
                        description = s.newDescription.ifBlank { null },
                        eventType = s.newEventType, color = color,
                        visibility = s.newVisibility,
                        cancelsLessons = s.newCancelsLessons,
                        noCompensation = s.newNoCompensation,
                    )
                )
                _uiState.update { it.copy(isSaving = false, showCreateSheet = false) }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun confirmDelete(id: Int) { _uiState.update { it.copy(deleteConfirmId = id) } }
    fun dismissDelete() { _uiState.update { it.copy(deleteConfirmId = null) } }
    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            try { calendarApi.deleteEvent(id); _uiState.update { it.copy(deleteConfirmId = null) }; loadData() }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message, deleteConfirmId = null) } }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UI — Main Section
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCalendarSection(isDark: Boolean, refreshTrigger: Int = 0, viewModel: AdminCalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(refreshTrigger) { if (refreshTrigger > 0) viewModel.loadData() }

    // Delete confirmation
    uiState.deleteConfirmId?.let { id ->
        val event = uiState.events.find { it.id == id }
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Удалить событие?", fontWeight = FontWeight.Bold) },
            text = { Text("«${event?.title ?: ""}» будет удалено") },
            confirmButton = { TextButton(onClick = { viewModel.deleteEvent(id) }) { Text("Удалить", color = BrandCoral) } },
            dismissButton = { TextButton(onClick = { viewModel.dismissDelete() }) { Text("Отмена") } },
        )
    }

    // Create event full-screen sheet
    if (uiState.showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissCreate() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            CreateEventForm(uiState = uiState, viewModel = viewModel, isDark = isDark, textColor = textColor, subtextColor = subtextColor)
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                AdminSectionHeader(Icons.Default.CalendarMonth, "Календарь событий", isDark)
                Surface(onClick = { viewModel.showCreate() }, shape = RoundedCornerShape(50), color = BrandGreen) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = Color.White)
                        Text("Добавить", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        if (uiState.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.events.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(40.dp), tint = subtextColor)
                    Spacer(Modifier.height(8.dp))
                    Text("Нет событий", style = MaterialTheme.typography.bodyMedium, color = subtextColor)
                }
            }
        } else {
            items(uiState.events, key = { it.id }) { event ->
                CalendarEventCard(event = event, isDark = isDark, textColor = textColor, subtextColor = subtextColor, onDelete = { viewModel.confirmDelete(event.id) })
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
// Event Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CalendarEventCard(
    event: CalendarEventResponse, isDark: Boolean, textColor: Color, subtextColor: Color, onDelete: () -> Unit,
) {
    val accentColor = when (event.color) {
        "red" -> BrandCoral; "green" -> BrandGreen; "yellow" -> BrandGold; "orange" -> Color(0xFFFF9800); else -> BrandBlue
    }
    val typeLabel = when (event.eventType) { "holiday" -> "Выходной"; "note" -> "Заметка"; else -> "Событие" }
    val typeIcon = when (event.eventType) { "holiday" -> Icons.Default.WbSunny; "note" -> Icons.Default.EditNote; else -> Icons.Default.Event }

    AdminGlassCard(isDark = isDark) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(48.dp).clip(RoundedCornerShape(2.dp)).background(accentColor))
            Spacer(Modifier.width(10.dp))
            Icon(typeIcon, null, Modifier.size(20.dp), tint = accentColor)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(event.date, style = MaterialTheme.typography.labelSmall, color = subtextColor)
                    Text("·", color = subtextColor)
                    Text(typeLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accentColor)
                }
                event.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = subtextColor, maxLines = 2)
                }
                if (event.cancelsLessons) {
                    Surface(color = BrandCoral.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Text("Занятия отменены", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrandCoral)
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = BrandCoral.copy(alpha = 0.7f))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Create Event Form (iOS-style)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventForm(
    uiState: AdminCalendarViewModel.UiState,
    viewModel: AdminCalendarViewModel,
    isDark: Boolean,
    textColor: Color,
    subtextColor: Color,
) {
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    val eventTypes = listOf(
        "holiday" to "🔴 Выходной / Праздник",
        "event" to "🔵 Событие",
        "note" to "🟡 Заметка",
    )
    val visibilityOptions = listOf("all" to "Всем", "teachers" to "Только учителям", "students" to "Только учащимся")

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Новое событие", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
            TextButton(onClick = { viewModel.dismissCreate() }) {
                Text("Отмена", color = BrandBlue, fontWeight = FontWeight.SemiBold)
            }
        }

        // Type section
        Text("Тип", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subtextColor)
        eventTypes.forEach { (type, label) ->
            Surface(
                onClick = { viewModel.updateNewEventType(type) },
                shape = RoundedCornerShape(10.dp),
                color = if (uiState.newEventType == type) BrandBlue.copy(alpha = 0.12f) else fieldBg,
                border = if (uiState.newEventType == type) androidx.compose.foundation.BorderStroke(1.dp, BrandBlue.copy(alpha = 0.3f)) else null,
            ) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = textColor)
                    if (uiState.newEventType == type) Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = BrandGreen)
                }
            }
        }

        // Date picker
        Text("Дата", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subtextColor)
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.newDateMillis)
        LaunchedEffect(datePickerState.selectedDateMillis) {
            datePickerState.selectedDateMillis?.let { viewModel.updateNewDateMillis(it) }
        }
        DatePicker(
            state = datePickerState,
            modifier = Modifier.fillMaxWidth(),
            title = null,
            headline = null,
            showModeToggle = false,
        )

        // Multi-day toggle
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Несколько дней", style = MaterialTheme.typography.bodyMedium, color = textColor)
            Switch(checked = uiState.newHasEndDate, onCheckedChange = { viewModel.updateNewHasEndDate(it) })
        }

        if (uiState.newHasEndDate) {
            val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.newEndDateMillis)
            LaunchedEffect(endDatePickerState.selectedDateMillis) {
                endDatePickerState.selectedDateMillis?.let { viewModel.updateNewEndDateMillis(it) }
            }
            DatePicker(
                state = endDatePickerState,
                modifier = Modifier.fillMaxWidth(),
                title = { Text("До", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subtextColor, modifier = Modifier.padding(start = 16.dp, top = 8.dp)) },
                headline = null,
                showModeToggle = false,
            )
        }

        // Title
        Text("Название *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subtextColor)
        OutlinedTextField(
            uiState.newTitle, { viewModel.updateNewTitle(it) },
            Modifier.fillMaxWidth(),
            placeholder = { Text("Навруз") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )

        // Description
        Text("Описание", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subtextColor)
        OutlinedTextField(
            uiState.newDescription, { viewModel.updateNewDescription(it) },
            Modifier.fillMaxWidth(),
            placeholder = { Text("Праздник весны и обновления") },
            shape = RoundedCornerShape(12.dp),
        )

        // Visibility
        Text("Кому показывать", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subtextColor)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            visibilityOptions.forEach { (vis, label) ->
                Surface(
                    onClick = { viewModel.updateNewVisibility(vis) },
                    shape = RoundedCornerShape(50),
                    color = if (uiState.newVisibility == vis) BrandBlue else fieldBg,
                ) {
                    Text(
                        label,
                        Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.newVisibility == vis) Color.White else textColor,
                    )
                }
            }
        }

        // Cancels lessons
        if (uiState.newEventType == "holiday" || uiState.newEventType == "event") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Отменяет занятия", style = MaterialTheme.typography.bodyMedium, color = textColor)
                Switch(
                    checked = uiState.newCancelsLessons,
                    onCheckedChange = { viewModel.updateNewCancelsLessons(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = BrandCoral),
                )
            }

            // No compensation
            if (uiState.newCancelsLessons) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Без компенсации", style = MaterialTheme.typography.bodyMedium, color = textColor)
                    Switch(
                        checked = uiState.newNoCompensation,
                        onCheckedChange = { viewModel.updateNewNoCompensation(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = BrandGold),
                    )
                }
            }
        }

        // Error
        uiState.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = BrandCoral) }

        // Create button
        Button(
            onClick = { viewModel.createEvent() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = uiState.newTitle.isNotBlank() && !uiState.isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
        ) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Создать", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}
