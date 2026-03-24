package com.subnetik.unlock.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.CalendarApi
import com.subnetik.unlock.data.remote.api.StudentApi
import com.subnetik.unlock.data.remote.dto.calendar.CalendarEventResponse
import com.subnetik.unlock.data.remote.dto.student.StudentScheduleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class StudentScheduleUiState(
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean? = null,
    val schedule: StudentScheduleData? = null,
    val error: String? = null,
    val calendarEvents: List<CalendarEventResponse> = emptyList(),
)

@HiltViewModel
class StudentScheduleViewModel @Inject constructor(
    private val studentApi: StudentApi,
    private val calendarApi: CalendarApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentScheduleUiState())
    val uiState: StateFlow<StudentScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadData()
        loadCalendarEvents()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val schedule = studentApi.getSchedule()
                _uiState.update { it.copy(isLoading = false, schedule = schedule) }
            } catch (e: Exception) {
                android.util.Log.e("ScheduleVM", "Failed to load schedule", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadCalendarEvents() {
        viewModelScope.launch {
            try {
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val mondayOffset = if (dayOfWeek == Calendar.SUNDAY) -6 else (Calendar.MONDAY - dayOfWeek)
                cal.add(Calendar.DAY_OF_MONTH, mondayOffset)
                val fromDate = df.format(cal.time)
                val events = calendarApi.getEvents(fromDate = fromDate)
                _uiState.update { it.copy(calendarEvents = events) }
            } catch (_: Exception) {}
        }
    }
}
