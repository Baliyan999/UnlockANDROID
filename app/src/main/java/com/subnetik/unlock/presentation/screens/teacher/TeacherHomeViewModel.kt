package com.subnetik.unlock.presentation.screens.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.api.NotificationApi
import com.subnetik.unlock.data.remote.api.ProgressApi
import com.subnetik.unlock.data.remote.dto.admin.*
import com.subnetik.unlock.data.remote.dto.notification.InboxNotification
import com.subnetik.unlock.data.remote.dto.progress.StudentFullProgressResponse
import com.subnetik.unlock.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.*
import javax.inject.Inject

data class TeacherHomeUiState(
    val displayName: String? = null,
    val isLoading: Boolean = true,
    val groups: List<AdminGroup> = emptyList(),
    val totalStudents: Int = 0,
    val todayLessonsCount: Int = 0,
    val unreadNotifications: Int = 0,
    val isDarkTheme: Boolean? = true,
    // Group detail
    val selectedGroup: AdminGroup? = null,
    val groupStudents: List<AdminStudent> = emptyList(),
    val isLoadingStudents: Boolean = false,
    // Student attendance
    val selectedStudent: AdminStudent? = null,
    val showAttendance: Boolean = false,
    val attendanceMap: Map<String, Boolean> = emptyMap(),
    val lessonDates: List<String> = emptyList(),
    val isSavingAttendance: Boolean = false,
    // Student progress
    val showProgress: Boolean = false,
    val studentProgress: StudentFullProgressResponse? = null,
    val isLoadingProgress: Boolean = false,
    // New homework
    val showNewHomework: Boolean = false,
    val homeworkGroupId: Int? = null,
    val homeworkTitle: String = "",
    val homeworkDescription: String = "",
    val homeworkHasDeadline: Boolean = false,
    val homeworkDueDate: Long? = null,
    val homeworkDueHour: Int = 16,
    val homeworkDueMinute: Int = 0,
    val isCreatingHomework: Boolean = false,
    // Error
    val error: String? = null,
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val adminApi: AdminApi,
    private val notificationApi: NotificationApi,
    private val progressApi: ProgressApi,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherHomeUiState())
    val uiState: StateFlow<TeacherHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getDisplayName().collect { name ->
                _uiState.update { it.copy(displayName = name) }
            }
        }
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
                val groupsD = async { runCatching { adminApi.getGroups() }.getOrDefault(emptyList()) }
                val unreadD = async { runCatching { notificationApi.getUnreadCount().unreadCount }.getOrDefault(0) }

                val groups = groupsD.await()
                val todayToken = todayToken()
                val todayGroups = groups.filter { matchesSchedule(it.scheduleDays, todayToken) }
                val totalStudents = groups.sumOf { it.studentsCount }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groups = groups,
                        totalStudents = totalStudents,
                        todayLessonsCount = todayGroups.size,
                        unreadNotifications = unreadD.await(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ─── Group Detail ──────────────────────────────────────────

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

    // ─── Attendance ────────────────────────────────────────────

    fun openAttendance(student: AdminStudent) {
        val group = _uiState.value.selectedGroup ?: return
        val dates = getLessonDatesForCurrentMonth(group.scheduleDays)
        val map = mutableMapOf<String, Boolean>()
        dates.forEach { date ->
            val entry = student.attendance?.find { it.date == date }
            map[date] = entry?.status == "present"
        }
        _uiState.update {
            it.copy(
                selectedStudent = student,
                showAttendance = true,
                lessonDates = dates,
                attendanceMap = map,
            )
        }
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
                adminApi.updateStudent(
                    groupId = group.id,
                    studentId = student.id,
                    request = AdminStudentUpdateRequest(attendance = entries),
                )
                // Reload students
                val students = adminApi.getGroupStudents(group.id)
                _uiState.update {
                    it.copy(
                        isSavingAttendance = false,
                        showAttendance = false,
                        selectedStudent = null,
                        groupStudents = students,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingAttendance = false, error = e.message) }
            }
        }
    }

    fun closeAttendance() {
        _uiState.update { it.copy(showAttendance = false, selectedStudent = null) }
    }

    // ─── Progress ──────────────────────────────────────────────

    fun openProgress(student: AdminStudent) {
        val userId = student.userId ?: return
        _uiState.update {
            it.copy(
                selectedStudent = student,
                showProgress = true,
                isLoadingProgress = true,
                studentProgress = null,
            )
        }
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

    // ─── Homework ──────────────────────────────────────────────

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
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreatingHomework = false, error = e.message) }
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────────

    companion object {
        private val DAY_TOKENS = mapOf(
            Calendar.MONDAY to "пн",
            Calendar.TUESDAY to "вт",
            Calendar.WEDNESDAY to "ср",
            Calendar.THURSDAY to "чт",
            Calendar.FRIDAY to "пт",
            Calendar.SATURDAY to "сб",
            Calendar.SUNDAY to "вс",
        )

        private val DAY_SHORT_TITLES = mapOf(
            Calendar.MONDAY to "Пн",
            Calendar.TUESDAY to "Вт",
            Calendar.WEDNESDAY to "Ср",
            Calendar.THURSDAY to "Чт",
            Calendar.FRIDAY to "Пт",
            Calendar.SATURDAY to "Сб",
            Calendar.SUNDAY to "Вс",
        )

        private val ALL_DAYS_ORDERED = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,
        )

        fun todayToken(): String {
            val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return DAY_TOKENS[dow] ?: "пн"
        }

        fun todayShortTitle(): String {
            val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return DAY_SHORT_TITLES[dow] ?: "Пн"
        }

        fun matchesSchedule(scheduleDays: String?, token: String): Boolean {
            if (scheduleDays.isNullOrEmpty()) return false
            return scheduleDays.lowercase().contains(token)
        }

        fun parseStartTime(time: String?): String {
            if (time == null) return "99:99"
            return time.take(5)
        }

        fun parseTimeRange(time: String?): Pair<String, String> {
            if (time == null) return "--:--" to "--:--"
            val parts = time.split("-")
            val start = parts.firstOrNull()?.trim() ?: "--:--"
            val end = if (parts.size > 1) parts[1].trim() else "--:--"
            return start to end
        }

        fun countLessonsPerDay(groups: List<AdminGroup>): Map<String, Int> {
            val result = mutableMapOf<String, Int>()
            for (entry in ALL_DAYS_ORDERED) {
                val token = DAY_TOKENS[entry] ?: continue
                val label = DAY_SHORT_TITLES[entry] ?: continue
                val count = groups.count { matchesSchedule(it.scheduleDays, token) }
                result[label] = count
            }
            return result
        }

        fun parseMinutes(time: String): Int? {
            val parts = time.split(":").mapNotNull { it.toIntOrNull() }
            if (parts.size < 2) return null
            return parts[0] * 60 + parts[1]
        }
    }

    private fun getLessonDatesForCurrentMonth(scheduleDays: String?): List<String> {
        if (scheduleDays.isNullOrEmpty()) return emptyList()

        val dayMap = mapOf(
            "пн" to Calendar.MONDAY,
            "вт" to Calendar.TUESDAY,
            "ср" to Calendar.WEDNESDAY,
            "чт" to Calendar.THURSDAY,
            "пт" to Calendar.FRIDAY,
            "сб" to Calendar.SATURDAY,
            "вс" to Calendar.SUNDAY,
        )

        val activeDays = scheduleDays.lowercase().split(Regex("[\\s,]+"))
            .mapNotNull { dayMap[it.trim()] }
            .toSet()

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
            if (cal.get(Calendar.DAY_OF_WEEK) in activeDays) {
                dates.add(sdf.format(cal.time))
            }
        }
        return dates
    }
}
