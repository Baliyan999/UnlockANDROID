package com.subnetik.unlock.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.BuildConfig
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.StudentApi
import com.subnetik.unlock.data.remote.dto.student.StudentSupportBooking
import com.subnetik.unlock.data.remote.dto.student.SupportBookingCreateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.subnetik.unlock.util.ErrorMapper
import javax.inject.Inject

data class SupportBookingUiState(
    val isDarkTheme: Boolean? = true,
    val teachers: List<String> = listOf("Куат Бахитов", "Артур Гарифьянов"),
    val selectedTeacher: String = "Куат Бахитов",
    val selectedDate: Calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) },
    val selectedTime: String = "",
    val comment: String = "",
    val busySlots: Set<String> = emptySet(),
    val isLoadingSlots: Boolean = false,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val bookingCreated: Boolean = false,
)

@HiltViewModel
class SupportBookingViewModel @Inject constructor(
    private val studentApi: StudentApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportBookingUiState())
    val uiState: StateFlow<SupportBookingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadBusySlots()
    }

    fun selectTeacher(teacher: String) {
        _uiState.update { it.copy(selectedTeacher = teacher, selectedTime = "") }
        loadBusySlots()
    }

    fun selectDate(year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        _uiState.update { it.copy(selectedDate = cal, selectedTime = "") }
        loadBusySlots()
    }

    fun selectTime(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun updateComment(text: String) {
        _uiState.update { it.copy(comment = text) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    private fun loadBusySlots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSlots = true) }
            try {
                val dateStr = formatDate(_uiState.value.selectedDate)
                val response = studentApi.getBusySlots(
                    teacher = _uiState.value.selectedTeacher,
                    date = dateStr,
                )
                _uiState.update { it.copy(busySlots = response.busySlots.toSet(), isLoadingSlots = false) }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.w("SupportBookingVM", "Failed to load busy slots: ${e.message}")
                _uiState.update { it.copy(busySlots = emptySet(), isLoadingSlots = false) }
            }
        }
    }

    fun submitBooking() {
        val state = _uiState.value
        if (state.selectedTime.isBlank() || state.comment.length < 5) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                studentApi.createSupportBooking(
                    SupportBookingCreateRequest(
                        supportTeacher = state.selectedTeacher,
                        bookingDate = formatDate(state.selectedDate),
                        bookingTime = state.selectedTime,
                        comment = state.comment.trim(),
                    )
                )
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = "Запись создана успешно!",
                        bookingCreated = true,
                        comment = "",
                        selectedTime = "",
                    )
                }
                loadBusySlots()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = ErrorMapper.map(e),
                    )
                }
            }
        }
    }

    private fun formatDate(cal: Calendar): String {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return df.format(cal.time)
    }

    companion object {
        fun generateTimeSlots(): List<String> {
            val slots = mutableListOf<String>()
            var hour = 15
            var minute = 30
            while (hour < 20 || (hour == 20 && minute <= 30)) {
                slots.add(String.format("%02d:%02d", hour, minute))
                minute += 10
                if (minute >= 60) {
                    minute = 0
                    hour++
                }
            }
            return slots
        }
    }
}
