package com.subnetik.unlock.presentation.screens.homework

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.StudentApi
import com.subnetik.unlock.data.remote.dto.admin.HomeworkStudentGroupOverview
import com.subnetik.unlock.data.remote.dto.student.HomeworkAssignmentStudent
import com.subnetik.unlock.data.remote.dto.student.StudentSupportBooking
import com.subnetik.unlock.data.remote.dto.student.SupportBookingCreateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class HomeworkTab { HOMEWORK, SUPPORT }

data class StudentHomeworkUiState(
    val isDarkTheme: Boolean? = null,
    val selectedTab: HomeworkTab = HomeworkTab.HOMEWORK,
    val isLoading: Boolean = true,
    val assignments: List<HomeworkAssignmentStudent> = emptyList(),
    val groups: List<HomeworkStudentGroupOverview> = emptyList(),
    val supportBookings: List<StudentSupportBooking> = emptyList(),
    // File upload per assignment
    val uploadingAssignmentId: Int? = null,
    val selectedFileUri: Map<Int, Uri> = emptyMap(),
    val selectedFileName: Map<Int, String> = emptyMap(),
    // Support booking form
    val supportTeachers: List<String> = listOf("Куат Бахитов", "Артур Гарифьянов"),
    val supportTeacher: String = "Куат Бахитов",
    val supportDate: Calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) },
    val supportTime: String = "",
    val supportComment: String = "",
    val busySlots: Set<String> = emptySet(),
    val isLoadingSlots: Boolean = false,
    val isSubmittingBooking: Boolean = false,
    // Messages
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class StudentHomeworkViewModel @Inject constructor(
    private val studentApi: StudentApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentHomeworkUiState())
    val uiState: StateFlow<StudentHomeworkUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadData()
        loadBusySlots()
    }

    fun selectTab(tab: HomeworkTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refresh() { loadData(); loadBusySlots() }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    // ─── Homework ──────────────────────────────

    fun selectFile(assignmentId: Int, uri: Uri, fileName: String) {
        _uiState.update {
            it.copy(
                selectedFileUri = it.selectedFileUri + (assignmentId to uri),
                selectedFileName = it.selectedFileName + (assignmentId to fileName),
            )
        }
    }

    fun submitHomework(assignmentId: Int, fileBytes: ByteArray, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(uploadingAssignmentId = assignmentId, errorMessage = null) }
            try {
                val requestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                studentApi.submitHomework(assignmentId, part)
                _uiState.update {
                    it.copy(
                        uploadingAssignmentId = null,
                        successMessage = "Домашнее задание отправлено!",
                        selectedFileUri = it.selectedFileUri - assignmentId,
                        selectedFileName = it.selectedFileName - assignmentId,
                    )
                }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(uploadingAssignmentId = null, errorMessage = e.message ?: "Ошибка при отправке") }
            }
        }
    }

    // ─── Support Booking ────────────────────────

    fun selectSupportTeacher(teacher: String) {
        _uiState.update { it.copy(supportTeacher = teacher, supportTime = "") }
        loadBusySlots()
    }

    fun selectSupportDate(year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, day) }
        _uiState.update { it.copy(supportDate = cal, supportTime = "") }
        loadBusySlots()
    }

    fun selectSupportTime(time: String) {
        _uiState.update { it.copy(supportTime = time) }
    }

    fun updateSupportComment(text: String) {
        _uiState.update { it.copy(supportComment = text) }
    }

    fun submitBooking() {
        val state = _uiState.value
        if (state.supportTime.isBlank() || state.supportComment.trim().length < 5) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingBooking = true, errorMessage = null) }
            try {
                studentApi.createSupportBooking(
                    SupportBookingCreateRequest(
                        supportTeacher = state.supportTeacher,
                        bookingDate = formatDate(state.supportDate),
                        bookingTime = state.supportTime,
                        comment = state.supportComment.trim(),
                    )
                )
                _uiState.update {
                    it.copy(
                        isSubmittingBooking = false,
                        successMessage = "Запись создана успешно!",
                        supportComment = "",
                        supportTime = "",
                    )
                }
                loadData()
                loadBusySlots()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmittingBooking = false, errorMessage = e.message ?: "Ошибка при создании записи") }
            }
        }
    }

    private fun loadBusySlots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSlots = true) }
            try {
                val dateStr = formatDate(_uiState.value.supportDate)
                val response = studentApi.getBusySlots(teacher = _uiState.value.supportTeacher, date = dateStr)
                _uiState.update { it.copy(busySlots = response.busySlots.toSet(), isLoadingSlots = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(busySlots = emptySet(), isLoadingSlots = false) }
            }
        }
    }

    // ─── Data Loading ───────────────────────────

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val assignments = try { studentApi.getMyAssignments() } catch (_: Exception) { emptyList() }
                val groups = try { studentApi.getMyGroups() } catch (_: Exception) { emptyList() }
                val bookings = try { studentApi.getMySupportBookings() } catch (_: Exception) { emptyList() }
                _uiState.update { it.copy(isLoading = false, assignments = assignments, groups = groups, supportBookings = bookings) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun formatDate(cal: Calendar): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

    companion object {
        fun generateTimeSlots(): List<String> {
            val slots = mutableListOf<String>()
            var hour = 15; var minute = 30
            while (hour < 20 || (hour == 20 && minute <= 20)) {
                slots.add(String.format("%02d:%02d", hour, minute))
                minute += 10
                if (minute >= 60) { minute = 0; hour++ }
            }
            return slots
        }
    }
}
