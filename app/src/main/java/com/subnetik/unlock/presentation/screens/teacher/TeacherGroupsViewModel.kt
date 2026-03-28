package com.subnetik.unlock.presentation.screens.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.api.ProgressApi
import com.subnetik.unlock.data.remote.dto.admin.*
import com.subnetik.unlock.data.remote.dto.progress.StudentFullProgressResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TeacherGroupsUiState(
    val isDarkTheme: Boolean? = true,
    val isLoading: Boolean = true,
    val groups: List<AdminGroup> = emptyList(),
    val totalStudents: Int = 0,
    // Tabs
    val selectedTab: Int = 0, // 0=Мои группы, 1=Домашние задания
    val selectedHomeworkTab: Int = 0, // 0=Задания, 1=Рейтинг
    // Group detail
    val selectedGroup: AdminGroup? = null,
    val groupStudents: List<AdminStudent> = emptyList(),
    val isLoadingStudents: Boolean = false,
    // Attendance
    val selectedStudent: AdminStudent? = null,
    val showAttendance: Boolean = false,
    val attendanceMap: Map<String, Boolean> = emptyMap(),
    val lessonDates: List<String> = emptyList(),
    val isSavingAttendance: Boolean = false,
    // Progress
    val showProgress: Boolean = false,
    val studentProgress: StudentFullProgressResponse? = null,
    val isLoadingProgress: Boolean = false,
    // Homework
    val assignments: List<AdminHomeworkAssignment> = emptyList(),
    val isLoadingHomework: Boolean = false,
    val showCompleted: Boolean = false,
    val showNewHomework: Boolean = false,
    val homeworkGroupId: Int? = null,
    val homeworkTitle: String = "",
    val homeworkDescription: String = "",
    val homeworkHasDeadline: Boolean = false,
    val homeworkDueDate: Long? = null,
    val homeworkDueHour: Int = 16,
    val homeworkDueMinute: Int = 0,
    val isCreatingHomework: Boolean = false,
    // Rating
    val ratingGroups: List<HomeworkStudentGroupOverview> = emptyList(),
    val ratingSelectedGroupId: Int? = null,
    val isLoadingRating: Boolean = false,
    // Error
    val error: String? = null,
)

@HiltViewModel
class TeacherGroupsViewModel @Inject constructor(
    private val adminApi: AdminApi,
    private val progressApi: ProgressApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherGroupsUiState())
    val uiState: StateFlow<TeacherGroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val groups = runCatching { adminApi.getGroups() }.getOrDefault(emptyList())
                val totalStudents = groups.sumOf { it.studentsCount }
                _uiState.update { it.copy(isLoading = false, groups = groups, totalStudents = totalStudents) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ─── Tabs ──────────────────────────────────────────────

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == 1 && _uiState.value.assignments.isEmpty()) {
            loadHomework()
        }
    }

    fun selectHomeworkTab(tab: Int) {
        _uiState.update { it.copy(selectedHomeworkTab = tab) }
        if (tab == 1 && _uiState.value.ratingGroups.isEmpty()) {
            loadRating()
        }
    }

    // ─── Homework ──────────────────────────────────────────

    fun loadHomework() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHomework = true) }
            try {
                val assignments = adminApi.getHomeworkAssignments(includeCompleted = true)
                _uiState.update { it.copy(assignments = assignments, isLoadingHomework = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingHomework = false, error = e.message) }
            }
        }
    }

    fun toggleShowCompleted() {
        _uiState.update { it.copy(showCompleted = !it.showCompleted) }
    }

    // ─── Rating ────────────────────────────────────────────

    private fun loadRating() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRating = true) }
            try {
                val groups = adminApi.getHomeworkStudentGroups()
                _uiState.update {
                    it.copy(
                        ratingGroups = groups,
                        ratingSelectedGroupId = groups.firstOrNull()?.id,
                        isLoadingRating = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingRating = false, error = e.message) }
            }
        }
    }

    fun selectRatingGroup(groupId: Int) {
        _uiState.update { it.copy(ratingSelectedGroupId = groupId) }
    }

    // ─── Group Detail ──────────────────────────────────────

    fun selectGroup(group: AdminGroup) {
        _uiState.update { it.copy(selectedGroup = group, isLoadingStudents = true) }
        viewModelScope.launch {
            try {
                val students = adminApi.getGroupStudents(group.id)
                _uiState.update { it.copy(groupStudents = students, isLoadingStudents = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingStudents = false, error = e.message) }
            }
        }
    }

    fun closeGroupDetail() {
        _uiState.update { it.copy(selectedGroup = null, groupStudents = emptyList()) }
    }

    // ─── Attendance ────────────────────────────────────────

    fun openAttendance(student: AdminStudent) {
        val group = _uiState.value.selectedGroup ?: return
        val dates = getLessonDatesForCurrentMonth(group.scheduleDays)
        val map = mutableMapOf<String, Boolean>()
        dates.forEach { date ->
            val entry = student.attendance?.find { it.date == date }
            map[date] = entry?.status == "present"
        }
        _uiState.update { it.copy(selectedStudent = student, showAttendance = true, lessonDates = dates, attendanceMap = map) }
    }

    fun toggleAttendance(date: String) {
        val current = _uiState.value.attendanceMap.toMutableMap()
        current[date] = !(current[date] ?: false)
        _uiState.update { it.copy(attendanceMap = current) }
    }

    fun saveAttendance() {
        val state = _uiState.value
        val student = state.selectedStudent ?: return
        val group = state.selectedGroup ?: return
        _uiState.update { it.copy(isSavingAttendance = true) }
        viewModelScope.launch {
            try {
                val entries = state.attendanceMap.map { (date, present) ->
                    AttendanceEntry(date = date, status = if (present) "present" else "absent")
                }
                adminApi.updateStudent(groupId = group.id, studentId = student.id, request = AdminStudentUpdateRequest(attendance = entries))
                val students = adminApi.getGroupStudents(group.id)
                _uiState.update { it.copy(isSavingAttendance = false, showAttendance = false, selectedStudent = null, groupStudents = students) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingAttendance = false, error = e.message) }
            }
        }
    }

    fun closeAttendance() {
        _uiState.update { it.copy(showAttendance = false, selectedStudent = null) }
    }

    // ─── Progress ──────────────────────────────────────────

    fun openProgress(student: AdminStudent) {
        val userId = student.userId ?: return
        _uiState.update { it.copy(selectedStudent = student, showProgress = true, isLoadingProgress = true, studentProgress = null) }
        viewModelScope.launch {
            try {
                val progress = progressApi.getStudentProgress(userId)
                _uiState.update { it.copy(studentProgress = progress, isLoadingProgress = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingProgress = false, error = e.message) }
            }
        }
    }

    fun closeProgress() {
        _uiState.update { it.copy(showProgress = false, selectedStudent = null, studentProgress = null) }
    }

    // ─── New Homework ──────────────────────────────────────

    fun openNewHomework(groupId: Int? = null) {
        _uiState.update {
            it.copy(
                showNewHomework = true,
                homeworkGroupId = groupId ?: it.groups.firstOrNull()?.id,
                homeworkTitle = "",
                homeworkDescription = "",
                homeworkHasDeadline = false,
                homeworkDueDate = null,
            )
        }
    }

    fun closeNewHomework() {
        _uiState.update { it.copy(showNewHomework = false) }
    }

    fun updateHomeworkTitle(title: String) {
        _uiState.update { it.copy(homeworkTitle = title) }
    }

    fun updateHomeworkDescription(desc: String) {
        _uiState.update { it.copy(homeworkDescription = desc) }
    }

    fun updateHomeworkGroupId(id: Int) {
        _uiState.update { it.copy(homeworkGroupId = id) }
    }

    fun toggleHomeworkDeadline(enabled: Boolean) {
        _uiState.update { it.copy(homeworkHasDeadline = enabled) }
    }

    fun updateHomeworkDueDate(millis: Long?) {
        _uiState.update { it.copy(homeworkDueDate = millis) }
    }

    fun updateHomeworkDueTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(homeworkDueHour = hour, homeworkDueMinute = minute) }
    }

    fun createHomework() {
        val state = _uiState.value
        val groupId = state.homeworkGroupId ?: return
        if (state.homeworkTitle.isBlank()) return

        _uiState.update { it.copy(isCreatingHomework = true) }
        viewModelScope.launch {
            try {
                val dueDate = if (state.homeworkHasDeadline && state.homeworkDueDate != null) {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = state.homeworkDueDate
                    cal.set(Calendar.HOUR_OF_DAY, state.homeworkDueHour)
                    cal.set(Calendar.MINUTE, state.homeworkDueMinute)
                    cal.set(Calendar.SECOND, 0)
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    sdf.format(cal.time)
                } else null

                adminApi.createHomeworkAssignment(
                    AdminHomeworkCreateRequest(
                        title = state.homeworkTitle,
                        description = state.homeworkDescription.takeIf { it.isNotBlank() },
                        groupId = groupId,
                        dueDate = dueDate,
                    )
                )
                _uiState.update { it.copy(isCreatingHomework = false, showNewHomework = false) }
                loadHomework() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreatingHomework = false, error = e.message) }
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────

    private fun getLessonDatesForCurrentMonth(scheduleDays: String?): List<String> {
        if (scheduleDays.isNullOrEmpty()) return emptyList()
        val dayMap = mapOf(
            "пн" to Calendar.MONDAY, "вт" to Calendar.TUESDAY, "ср" to Calendar.WEDNESDAY,
            "чт" to Calendar.THURSDAY, "пт" to Calendar.FRIDAY, "сб" to Calendar.SATURDAY, "вс" to Calendar.SUNDAY,
        )
        val activeDays = scheduleDays.lowercase().split(Regex("[\\s,]+")).mapNotNull { dayMap[it.trim()] }.toSet()
        if (activeDays.isEmpty()) return emptyList()
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        cal.set(year, month, 1)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dates = mutableListOf<String>()
        for (day in 1..maxDay) {
            cal.set(year, month, day)
            if (cal.get(Calendar.DAY_OF_WEEK) in activeDays) dates.add(sdf.format(cal.time))
        }
        return dates
    }
}
