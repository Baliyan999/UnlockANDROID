package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.*
import com.subnetik.unlock.presentation.screens.admin.components.*
import com.subnetik.unlock.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LedgerDay(val shortTitle: String, val token: String) {
    MON("Пн", "пн"), TUE("Вт", "вт"), WED("Ср", "ср"),
    THU("Чт", "чт"), FRI("Пт", "пт"), SAT("Сб", "сб"), SUN("Вс", "вс");

    fun matches(schedule: String?): Boolean {
        if (schedule == null) return false
        return schedule.lowercase().contains(token)
    }

    companion object {
        fun today(): LedgerDay {
            val cal = java.util.Calendar.getInstance()
            return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> MON; java.util.Calendar.TUESDAY -> TUE
                java.util.Calendar.WEDNESDAY -> WED; java.util.Calendar.THURSDAY -> THU
                java.util.Calendar.FRIDAY -> FRI; java.util.Calendar.SATURDAY -> SAT
                else -> SUN
            }
        }
    }
}

@HiltViewModel
class AdminGroupsViewModel @Inject constructor(
    private val adminApi: AdminApi,
    private val progressApi: com.subnetik.unlock.data.remote.api.ProgressApi,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val groups: List<AdminGroup> = emptyList(),
        val selectedDay: LedgerDay? = LedgerDay.today(),
        val error: String? = null,
        // Create dialog
        val showCreateDialog: Boolean = false,
        val createName: String = "",
        val createHskLevel: String = "1",
        val createTeacher: String = "",
        val createClassroom: String = "",
        val createScheduleTime: String = "",
        val createScheduleDays: String = "",
        // Detail sheet
        val detailGroup: AdminGroup? = null,
        val detailStudents: List<AdminStudent> = emptyList(),
        val isLoadingStudents: Boolean = false,
        // Edit group dialog
        val showEditDialog: Boolean = false,
        val editName: String = "",
        val editHskLevel: String = "",
        val editTeacher: String = "",
        val editClassroom: String = "",
        val editScheduleTime: String = "",
        val editScheduleDays: String = "",
        // Add student dialog
        val showAddStudentDialog: Boolean = false,
        val addFirstName: String = "",
        val addLastName: String = "",
        val addAge: String = "",
        val addPhone: String = "",
        val addGuardianFirstName: String = "",
        val addGuardianLastName: String = "",
        val addGuardianPhone: String = "",
        // Edit student dialog
        val editStudentId: Int? = null,
        val editStudentFirstName: String = "",
        val editStudentLastName: String = "",
        val editStudentAge: String = "",
        val editStudentPhone: String = "",
        val editGuardianFirstName: String = "",
        val editGuardianLastName: String = "",
        val editGuardianPhone: String = "",
        // Payment screen
        val paymentStudentId: Int? = null,
        val paymentAmount: String = "",
        val paymentMethod: String = "cash",
        val editPaymentIndex: Int? = null,
        val editPaymentAmount: String = "",
        val editPaymentMethod: String = "cash",
        // Attendance screen
        val attendanceStudentId: Int? = null,
        // Progress screen
        val progressStudentId: Int? = null,
        val progressData: com.subnetik.unlock.data.remote.dto.progress.StudentFullProgressResponse? = null,
        val isLoadingProgress: Boolean = false,
    ) {
        val filtered: List<AdminGroup>
            get() {
                var result = groups
                if (selectedDay != null) result = result.filter { selectedDay.matches(it.scheduleDays) }
                return result
            }
        val groupedByHsk: Map<Int, List<AdminGroup>>
            get() = filtered.groupBy { it.hskLevel }.toSortedMap()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    fun selectDay(day: LedgerDay?) { _uiState.update { it.copy(selectedDay = day) } }
    fun selectToday() { _uiState.update { it.copy(selectedDay = LedgerDay.today()) } }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val groups = adminApi.getGroups()
                _uiState.update { it.copy(isLoading = false, groups = groups) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Create group ──
    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true, createName = "", createHskLevel = "1", createTeacher = "", createClassroom = "", createScheduleTime = "", createScheduleDays = "") }
    }
    fun dismissCreateDialog() { _uiState.update { it.copy(showCreateDialog = false) } }
    fun updateCreateName(v: String) { _uiState.update { it.copy(createName = v) } }
    fun updateCreateHskLevel(v: String) { _uiState.update { it.copy(createHskLevel = v) } }
    fun updateCreateTeacher(v: String) { _uiState.update { it.copy(createTeacher = v) } }
    fun updateCreateClassroom(v: String) { _uiState.update { it.copy(createClassroom = v) } }
    fun updateCreateScheduleTime(v: String) { _uiState.update { it.copy(createScheduleTime = v) } }
    fun updateCreateScheduleDays(v: String) { _uiState.update { it.copy(createScheduleDays = v) } }

    fun createGroup() {
        val state = _uiState.value
        if (state.createName.isBlank()) return
        viewModelScope.launch {
            try {
                adminApi.createGroup(AdminGroupCreateRequest(
                    name = state.createName.trim(),
                    hskLevel = state.createHskLevel.toIntOrNull() ?: 1,
                    teacher = state.createTeacher.takeIf { it.isNotBlank() },
                    classroom = state.createClassroom.takeIf { it.isNotBlank() },
                    scheduleTime = state.createScheduleTime.takeIf { it.isNotBlank() },
                    scheduleDays = state.createScheduleDays.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showCreateDialog = false) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ── Group detail ──
    fun openGroupDetail(group: AdminGroup) {
        _uiState.update { it.copy(detailGroup = group, isLoadingStudents = true, detailStudents = emptyList()) }
        viewModelScope.launch {
            try {
                val students = adminApi.getGroupStudents(group.id)
                _uiState.update { it.copy(isLoadingStudents = false, detailStudents = students) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingStudents = false, error = e.message) }
            }
        }
    }
    fun closeGroupDetail() { _uiState.update { it.copy(detailGroup = null, detailStudents = emptyList()) } }

    fun reloadStudents() {
        val group = _uiState.value.detailGroup ?: return
        _uiState.update { it.copy(isLoadingStudents = true) }
        viewModelScope.launch {
            try {
                val students = adminApi.getGroupStudents(group.id)
                _uiState.update { it.copy(isLoadingStudents = false, detailStudents = students) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingStudents = false, error = e.message) }
            }
        }
    }

    // ── Edit group ──
    fun showEditDialog() {
        val g = _uiState.value.detailGroup ?: return
        _uiState.update { it.copy(showEditDialog = true, editName = g.name, editHskLevel = g.hskLevel.toString(), editTeacher = g.teacher ?: "", editClassroom = g.classroom ?: "", editScheduleTime = g.scheduleTime ?: "", editScheduleDays = g.scheduleDays ?: "") }
    }
    fun dismissEditDialog() { _uiState.update { it.copy(showEditDialog = false) } }
    fun updateEditName(v: String) { _uiState.update { it.copy(editName = v) } }
    fun updateEditHskLevel(v: String) { _uiState.update { it.copy(editHskLevel = v) } }
    fun updateEditTeacher(v: String) { _uiState.update { it.copy(editTeacher = v) } }
    fun updateEditClassroom(v: String) { _uiState.update { it.copy(editClassroom = v) } }
    fun updateEditScheduleTime(v: String) { _uiState.update { it.copy(editScheduleTime = v) } }
    fun updateEditScheduleDays(v: String) { _uiState.update { it.copy(editScheduleDays = v) } }

    fun saveGroupEdit() {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        viewModelScope.launch {
            try {
                val updated = adminApi.updateGroup(group.id, AdminGroupCreateRequest(
                    name = state.editName.trim(),
                    hskLevel = state.editHskLevel.toIntOrNull() ?: group.hskLevel,
                    teacher = state.editTeacher.takeIf { it.isNotBlank() },
                    classroom = state.editClassroom.takeIf { it.isNotBlank() },
                    scheduleTime = state.editScheduleTime.takeIf { it.isNotBlank() },
                    scheduleDays = state.editScheduleDays.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showEditDialog = false, detailGroup = updated) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteGroup() {
        val group = _uiState.value.detailGroup ?: return
        viewModelScope.launch {
            try {
                adminApi.deleteGroup(group.id)
                _uiState.update { it.copy(detailGroup = null) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ── Add student ──
    fun showAddStudentDialog() { _uiState.update { it.copy(showAddStudentDialog = true, addFirstName = "", addLastName = "", addAge = "", addPhone = "", addGuardianFirstName = "", addGuardianLastName = "", addGuardianPhone = "") } }
    fun dismissAddStudentDialog() { _uiState.update { it.copy(showAddStudentDialog = false) } }
    fun updateAddFirstName(v: String) { _uiState.update { it.copy(addFirstName = v) } }
    fun updateAddLastName(v: String) { _uiState.update { it.copy(addLastName = v) } }
    fun updateAddAge(v: String) { _uiState.update { it.copy(addAge = v) } }
    fun updateAddPhone(v: String) { _uiState.update { it.copy(addPhone = v) } }
    fun updateAddGuardianFirstName(v: String) { _uiState.update { it.copy(addGuardianFirstName = v) } }
    fun updateAddGuardianLastName(v: String) { _uiState.update { it.copy(addGuardianLastName = v) } }
    fun updateAddGuardianPhone(v: String) { _uiState.update { it.copy(addGuardianPhone = v) } }

    fun addStudent() {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        if (state.addFirstName.isBlank()) return
        viewModelScope.launch {
            try {
                adminApi.createStudent(group.id, AdminStudentCreateRequest(
                    firstName = state.addFirstName.trim(),
                    lastName = state.addLastName.trim(),
                    age = state.addAge.toIntOrNull(),
                    phone = state.addPhone.takeIf { it.isNotBlank() },
                    guardianFirstName = state.addGuardianFirstName.takeIf { it.isNotBlank() },
                    guardianLastName = state.addGuardianLastName.takeIf { it.isNotBlank() },
                    guardianPhone = state.addGuardianPhone.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(showAddStudentDialog = false) }
                reloadStudents()
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ── Edit student ──
    fun showEditStudentDialog(student: AdminStudent) {
        _uiState.update { it.copy(editStudentId = student.id, editStudentFirstName = student.firstName, editStudentLastName = student.lastName, editStudentAge = student.age?.toString() ?: "", editStudentPhone = student.phone ?: "", editGuardianFirstName = student.guardianFirstName ?: "", editGuardianLastName = student.guardianLastName ?: "", editGuardianPhone = student.guardianPhone ?: "") }
    }
    fun dismissEditStudentDialog() { _uiState.update { it.copy(editStudentId = null) } }
    fun updateEditStudentFirstName(v: String) { _uiState.update { it.copy(editStudentFirstName = v) } }
    fun updateEditStudentLastName(v: String) { _uiState.update { it.copy(editStudentLastName = v) } }
    fun updateEditStudentAge(v: String) { _uiState.update { it.copy(editStudentAge = v) } }
    fun updateEditStudentPhone(v: String) { _uiState.update { it.copy(editStudentPhone = v) } }
    fun updateEditGuardianFirstName(v: String) { _uiState.update { it.copy(editGuardianFirstName = v) } }
    fun updateEditGuardianLastName(v: String) { _uiState.update { it.copy(editGuardianLastName = v) } }
    fun updateEditGuardianPhone(v: String) { _uiState.update { it.copy(editGuardianPhone = v) } }

    fun saveStudentEdit() {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        val studentId = state.editStudentId ?: return
        viewModelScope.launch {
            try {
                adminApi.updateStudent(group.id, studentId, AdminStudentUpdateRequest(
                    firstName = state.editStudentFirstName.trim(),
                    lastName = state.editStudentLastName.trim(),
                    age = state.editStudentAge.toIntOrNull(),
                    phone = state.editStudentPhone.takeIf { it.isNotBlank() },
                    guardianFirstName = state.editGuardianFirstName.takeIf { it.isNotBlank() },
                    guardianLastName = state.editGuardianLastName.takeIf { it.isNotBlank() },
                    guardianPhone = state.editGuardianPhone.takeIf { it.isNotBlank() },
                ))
                _uiState.update { it.copy(editStudentId = null) }
                reloadStudents()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteStudent(studentId: Int) {
        val group = _uiState.value.detailGroup ?: return
        viewModelScope.launch {
            try {
                adminApi.deleteStudent(group.id, studentId)
                reloadStudents()
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ── Payment ──
    fun showPaymentDialog(studentId: Int) { _uiState.update { it.copy(paymentStudentId = studentId, paymentAmount = "", paymentMethod = "cash", editPaymentIndex = null) } }
    fun dismissPaymentDialog() { _uiState.update { it.copy(paymentStudentId = null) } }
    fun updatePaymentAmount(v: String) { _uiState.update { it.copy(paymentAmount = v) } }
    fun updatePaymentMethod(v: String) { _uiState.update { it.copy(paymentMethod = v) } }

    fun startEditPayment(index: Int, payment: PaymentEntry) {
        _uiState.update { it.copy(editPaymentIndex = index, editPaymentAmount = payment.amount?.toString() ?: "", editPaymentMethod = payment.paymentMethod ?: "cash") }
    }
    fun cancelEditPayment() { _uiState.update { it.copy(editPaymentIndex = null) } }
    fun updateEditPaymentAmount(v: String) { _uiState.update { it.copy(editPaymentAmount = v) } }
    fun updateEditPaymentMethod(v: String) { _uiState.update { it.copy(editPaymentMethod = v) } }

    fun savePayment() {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        val studentId = state.paymentStudentId ?: return
        val amount = state.paymentAmount.toIntOrNull() ?: return
        val student = state.detailStudents.find { it.id == studentId } ?: return
        val cal = java.util.Calendar.getInstance()
        val newPayment = PaymentEntry(
            month = cal.get(java.util.Calendar.MONTH) + 1,
            year = cal.get(java.util.Calendar.YEAR),
            date = String.format("%02d.%02d.%04d", cal.get(java.util.Calendar.DAY_OF_MONTH), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR)),
            paymentMethod = state.paymentMethod,
            amount = amount,
        )
        val updatedPayments = (student.payments ?: emptyList()) + newPayment
        viewModelScope.launch {
            try {
                adminApi.updateStudent(group.id, studentId, AdminStudentUpdateRequest(payments = updatedPayments))
                _uiState.update { it.copy(paymentAmount = "", paymentMethod = "cash") }
                reloadStudents()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun saveEditPayment() {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        val studentId = state.paymentStudentId ?: return
        val index = state.editPaymentIndex ?: return
        val amount = state.editPaymentAmount.toIntOrNull() ?: return
        val student = state.detailStudents.find { it.id == studentId } ?: return
        val payments = student.payments?.toMutableList() ?: return
        if (index >= payments.size) return
        payments[index] = payments[index].copy(amount = amount, paymentMethod = state.editPaymentMethod)
        viewModelScope.launch {
            try {
                adminApi.updateStudent(group.id, studentId, AdminStudentUpdateRequest(payments = payments))
                _uiState.update { it.copy(editPaymentIndex = null) }
                reloadStudents()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deletePayment(index: Int) {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        val studentId = state.paymentStudentId ?: return
        val student = state.detailStudents.find { it.id == studentId } ?: return
        val payments = student.payments?.toMutableList() ?: return
        if (index >= payments.size) return
        payments.removeAt(index)
        viewModelScope.launch {
            try {
                adminApi.updateStudent(group.id, studentId, AdminStudentUpdateRequest(payments = payments))
                reloadStudents()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ── Attendance ──
    fun showAttendanceDialog(studentId: Int) { _uiState.update { it.copy(attendanceStudentId = studentId) } }
    fun dismissAttendanceDialog() { _uiState.update { it.copy(attendanceStudentId = null) } }

    fun toggleAttendance(studentId: Int, date: String) {
        val state = _uiState.value
        val group = state.detailGroup ?: return
        val student = state.detailStudents.find { it.id == studentId } ?: return
        val currentAttendance = student.attendance?.toMutableList() ?: mutableListOf()
        val existingIndex = currentAttendance.indexOfFirst { it.date == date }
        if (existingIndex >= 0) {
            val current = currentAttendance[existingIndex].status
            if (current == "present") currentAttendance[existingIndex] = AttendanceEntry(date, "absent")
            else if (current == "absent") currentAttendance.removeAt(existingIndex)
        } else {
            currentAttendance.add(AttendanceEntry(date, "present"))
        }
        viewModelScope.launch {
            try {
                adminApi.updateStudent(group.id, studentId, AdminStudentUpdateRequest(attendance = currentAttendance))
                reloadStudents()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    // ── Progress ──
    fun showProgressDialog(student: AdminStudent) {
        val userId = student.userId
        if (userId == null) {
            _uiState.update { it.copy(error = "У ученика нет привязанного аккаунта") }
            return
        }
        _uiState.update { it.copy(progressStudentId = student.id, isLoadingProgress = true, progressData = null) }
        viewModelScope.launch {
            try {
                val progress = progressApi.getStudentProgress(userId)
                _uiState.update { it.copy(isLoadingProgress = false, progressData = progress) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingProgress = false, progressData = null, error = e.message) }
            }
        }
    }
    fun dismissProgressDialog() { _uiState.update { it.copy(progressStudentId = null, progressData = null) } }
}

// ═══════════════════════════════════════════════════════════════
//  MAIN SECTION
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGroupsSection(isDark: Boolean, viewModel: AdminGroupsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    // ── Create group dialog ──
    if (uiState.showCreateDialog) {
        GroupEditorSheet(isDark, "Новая группа", uiState.createName, uiState.createHskLevel, uiState.createTeacher, uiState.createClassroom, uiState.createScheduleTime, uiState.createScheduleDays,
            onNameChange = { viewModel.updateCreateName(it) }, onHskChange = { viewModel.updateCreateHskLevel(it) }, onTeacherChange = { viewModel.updateCreateTeacher(it) }, onClassroomChange = { viewModel.updateCreateClassroom(it) }, onTimeChange = { viewModel.updateCreateScheduleTime(it) }, onDaysChange = { viewModel.updateCreateScheduleDays(it) },
            onDismiss = { viewModel.dismissCreateDialog() }, onSave = { viewModel.createGroup() }, saveLabel = "Создать группу", enabled = uiState.createName.isNotBlank())
    }

    // ── Edit group dialog ──
    if (uiState.showEditDialog) {
        GroupEditorSheet(isDark, "Редактировать группу", uiState.editName, uiState.editHskLevel, uiState.editTeacher, uiState.editClassroom, uiState.editScheduleTime, uiState.editScheduleDays,
            onNameChange = { viewModel.updateEditName(it) }, onHskChange = { viewModel.updateEditHskLevel(it) }, onTeacherChange = { viewModel.updateEditTeacher(it) }, onClassroomChange = { viewModel.updateEditClassroom(it) }, onTimeChange = { viewModel.updateEditScheduleTime(it) }, onDaysChange = { viewModel.updateEditScheduleDays(it) },
            onDismiss = { viewModel.dismissEditDialog() }, onSave = { viewModel.saveGroupEdit() }, saveLabel = "Сохранить", enabled = uiState.editName.isNotBlank())
    }

    // ── Add student dialog ──
    if (uiState.showAddStudentDialog) {
        StudentEditorSheet(isDark, "Добавить ученика",
            uiState.addFirstName, uiState.addLastName, uiState.addAge, uiState.addPhone,
            uiState.addGuardianFirstName, uiState.addGuardianLastName, uiState.addGuardianPhone,
            onFirstNameChange = { viewModel.updateAddFirstName(it) }, onLastNameChange = { viewModel.updateAddLastName(it) },
            onAgeChange = { viewModel.updateAddAge(it) }, onPhoneChange = { viewModel.updateAddPhone(it) },
            onGuardianFirstNameChange = { viewModel.updateAddGuardianFirstName(it) }, onGuardianLastNameChange = { viewModel.updateAddGuardianLastName(it) }, onGuardianPhoneChange = { viewModel.updateAddGuardianPhone(it) },
            onDismiss = { viewModel.dismissAddStudentDialog() }, onSave = { viewModel.addStudent() }, saveLabel = "Добавить", enabled = uiState.addFirstName.isNotBlank())
    }

    // ── Edit student dialog ──
    if (uiState.editStudentId != null) {
        StudentEditorSheet(isDark, "Редактировать учащегося",
            uiState.editStudentFirstName, uiState.editStudentLastName, uiState.editStudentAge, uiState.editStudentPhone,
            uiState.editGuardianFirstName, uiState.editGuardianLastName, uiState.editGuardianPhone,
            onFirstNameChange = { viewModel.updateEditStudentFirstName(it) }, onLastNameChange = { viewModel.updateEditStudentLastName(it) },
            onAgeChange = { viewModel.updateEditStudentAge(it) }, onPhoneChange = { viewModel.updateEditStudentPhone(it) },
            onGuardianFirstNameChange = { viewModel.updateEditGuardianFirstName(it) }, onGuardianLastNameChange = { viewModel.updateEditGuardianLastName(it) }, onGuardianPhoneChange = { viewModel.updateEditGuardianPhone(it) },
            onDismiss = { viewModel.dismissEditStudentDialog() }, onSave = { viewModel.saveStudentEdit() }, saveLabel = "Сохранить изменения", enabled = uiState.editStudentFirstName.isNotBlank())
    }

    // ── Payment screen ──
    if (uiState.paymentStudentId != null) {
        val student = uiState.detailStudents.find { it.id == uiState.paymentStudentId }
        PaymentScreen(isDark, student, uiState, viewModel)
        return
    }

    // ── Attendance screen ──
    if (uiState.attendanceStudentId != null) {
        val student = uiState.detailStudents.find { it.id == uiState.attendanceStudentId }
        AttendanceScreen(isDark, student, uiState, viewModel)
        return
    }

    // ── Progress screen ──
    if (uiState.progressStudentId != null) {
        val student = uiState.detailStudents.find { it.id == uiState.progressStudentId }
        ProgressScreen(isDark, student, uiState, viewModel)
        return
    }

    // ── Group detail sheet ──
    if (uiState.detailGroup != null) {
        GroupDetailSheet(isDark, uiState, viewModel)
        return
    }

    // ── Main list ──
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        // Header with create button
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    Icon(Icons.Default.TableChart, null, Modifier.size(24.dp), tint = BrandIndigo)
                    Text("Учетная таблица\nучебного центра", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor, lineHeight = 20.sp)
                }
                Button(
                    onClick = { viewModel.showCreateDialog() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Создать группу", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Filters card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(Brand.Spacing.md), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Text("Фильтры:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = subtextColor)
                        Button(
                            onClick = { viewModel.selectToday() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Сегодня", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    // Day chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LedgerDay.entries.toList()) { day ->
                            val selected = uiState.selectedDay == day
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) BrandIndigo else Color.Transparent,
                                border = BorderStroke(1.dp, if (selected) BrandIndigo else subtextColor.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { viewModel.selectDay(if (selected) null else day) },
                            ) {
                                Text(
                                    day.shortTitle,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color.White else textColor,
                                )
                            }
                        }
                    }
                }
            }
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.filtered.isEmpty()) {
            item { AdminEmptyState(Icons.Default.TableChart, "Нет групп", if (uiState.selectedDay != null) "Нет групп в этот день" else "Группы появятся здесь", isDark = isDark) }
        } else {
            // Group by HSK level
            uiState.groupedByHsk.forEach { (hskLevel, groupsInLevel) ->
                // HSK section header like iOS
                item {
                    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(hskLevelColor(hskLevel)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HSK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                                Text("$hskLevel", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }
                        Column {
                            Text("Уровень HSK $hskLevel", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Text("${groupsInLevel.size} ${pluralGroups(groupsInLevel.size)}", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                        }
                    }
                }
                items(groupsInLevel, key = { it.id }) { group ->
                    AdminGlassCard(isDark = isDark, onClick = { viewModel.openGroupDetail(group) }) {
                        Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(group.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                                AdminStatusTag("${group.studentsCount} учеников", BrandTeal)
                            }
                            group.scheduleTime?.let {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = subtextColor)
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                }
                            }
                            group.scheduleDays?.let {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(14.dp), tint = subtextColor)
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                }
                            }
                            group.teacher?.let {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = subtextColor)
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                                }
                            }
                            group.classroom?.let {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.MeetingRoom, null, Modifier.size(14.dp), tint = subtextColor)
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  GROUP DETAIL SHEET
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailSheet(isDark: Boolean, uiState: AdminGroupsViewModel.UiState, viewModel: AdminGroupsViewModel) {
    val group = uiState.detailGroup ?: return
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val infoBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.5f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        // Title bar
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(group.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { viewModel.closeGroupDetail() }, shape = RoundedCornerShape(10.dp)) {
                    Text("Закрыть")
                }
            }
        }

        // HSK badge + name
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(hskLevelColor(group.hskLevel)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HSK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${group.hskLevel}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Column {
                    Text(group.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Text("Уровень HSK ${group.hskLevel}", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                }
            }
        }

        // Info cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                if (group.scheduleTime != null || group.scheduleDays != null) {
                    InfoCard("Расписание", buildString { group.scheduleTime?.let { append(it) }; group.scheduleDays?.let { if (isNotEmpty()) append("\n"); append(it) } }, infoBg, textColor, subtextColor)
                }
                group.teacher?.let { InfoCard("Преподаватель", it, infoBg, textColor, subtextColor) }
                group.classroom?.let { InfoCard("Кабинет", it, infoBg, textColor, subtextColor) }
                InfoCard("Учеников", "${group.studentsCount}", infoBg, textColor, subtextColor)
            }
        }

        // Action buttons
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                Button(
                    onClick = { viewModel.showEditDialog() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Редактировать группу", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { viewModel.deleteGroup() },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCoral),
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Удалить группу", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Students header
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                    Text("Учащиеся группы", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                    Surface(shape = CircleShape, color = BrandBlue.copy(alpha = 0.2f)) {
                        Text("${uiState.detailStudents.size}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                }
                OutlinedButton(onClick = { viewModel.showAddStudentDialog() }, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        // Students list
        if (uiState.isLoadingStudents) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.detailStudents.isEmpty()) {
            item { AdminEmptyState(Icons.Default.People, "Нет учеников", "Добавьте учеников в группу", isDark = isDark) }
        } else {
            items(uiState.detailStudents, key = { it.id }) { student ->
                StudentCard(student, isDark, viewModel)
            }
        }

        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}

// ═══════════════════════════════════════════════════════════════
//  STUDENT CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StudentCard(student: AdminStudent, isDark: Boolean, viewModel: AdminGroupsViewModel) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    AdminGlassCard(isDark = isDark) {
        Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
            // Name
            Text(student.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)

            // Age & phone
            val details = buildList {
                student.age?.let { add("Возраст: $it") }
                student.phone?.let { add(it) }
            }
            if (details.isNotEmpty()) {
                details.forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                }
            }

            // Payment status
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Оплата:", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                if (student.isPaid) {
                    Text("Оплачено (${formatAmount(student.paidAmount)} сум)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandGreen)
                } else {
                    Text("Не оплачено", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandCoral)
                }
            }

            // Attendance
            if (student.totalLessons > 0) {
                Text("Посещаемость:  ${student.attendanceCount}/${student.totalLessons} • ${student.attendancePercent}%", style = MaterialTheme.typography.bodySmall, color = subtextColor)
            }

            // Action buttons row 1: Payment, Attendance, Progress
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.xs)) {
                SmallActionButton("Оплата", Icons.Default.Payment, BrandGreen, Modifier.weight(1f)) { viewModel.showPaymentDialog(student.id) }
                SmallActionButton("Посещение", Icons.Default.CheckCircle, BrandBlue, Modifier.weight(1f)) { viewModel.showAttendanceDialog(student.id) }
                SmallActionButton("Прогресс", Icons.Default.BarChart, BrandIndigo, Modifier.weight(1f)) { viewModel.showProgressDialog(student) }
            }
            // Action buttons row 2: Edit, Delete
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.xs)) {
                SmallActionButton("Редактировать", Icons.Default.Edit, BrandBlue, Modifier.weight(1f)) { viewModel.showEditStudentDialog(student) }
                SmallActionButton("Удалить", Icons.Default.Delete, BrandCoral, Modifier.weight(1f)) { viewModel.deleteStudent(student.id) }
            }
        }
    }
}

@Composable
private fun SmallActionButton(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
    ) {
        Icon(icon, null, Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ═══════════════════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun InfoCard(label: String, value: String, bg: Color, textColor: Color, subtextColor: Color) {
    Surface(shape = RoundedCornerShape(12.dp), color = bg, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Brand.Spacing.md)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = subtextColor)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditorSheet(
    isDark: Boolean, title: String,
    name: String, hskLevel: String, teacher: String, classroom: String, scheduleTime: String, scheduleDays: String,
    onNameChange: (String) -> Unit, onHskChange: (String) -> Unit, onTeacherChange: (String) -> Unit, onClassroomChange: (String) -> Unit, onTimeChange: (String) -> Unit, onDaysChange: (String) -> Unit,
    onDismiss: () -> Unit, onSave: () -> Unit, saveLabel: String, enabled: Boolean,
) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
            OutlinedTextField(name, onNameChange, Modifier.fillMaxWidth(), label = { Text("Название группы") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            OutlinedTextField(hskLevel, onHskChange, Modifier.fillMaxWidth(), label = { Text("HSK уровень") }, shape = RoundedCornerShape(12.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(teacher, onTeacherChange, Modifier.fillMaxWidth(), label = { Text("Учитель") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            OutlinedTextField(classroom, onClassroomChange, Modifier.fillMaxWidth(), label = { Text("Кабинет") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                OutlinedTextField(scheduleDays, onDaysChange, Modifier.weight(1f), label = { Text("Дни") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(scheduleTime, onTimeChange, Modifier.weight(1f), label = { Text("Время") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            }
            Button(onSave, Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), enabled = enabled) { Text(saveLabel, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun StudentEditorSheet(
    isDark: Boolean, title: String,
    firstName: String, lastName: String, age: String, phone: String,
    guardianFirstName: String, guardianLastName: String, guardianPhone: String,
    onFirstNameChange: (String) -> Unit, onLastNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit, onPhoneChange: (String) -> Unit,
    onGuardianFirstNameChange: (String) -> Unit, onGuardianLastNameChange: (String) -> Unit, onGuardianPhoneChange: (String) -> Unit,
    onDismiss: () -> Unit, onSave: () -> Unit, saveLabel: String, enabled: Boolean,
) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Отмена") }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = cardBg, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                    LabeledField("Имя *", firstName, onFirstNameChange)
                    LabeledField("Фамилия *", lastName, onLastNameChange)
                    LabeledField("Возраст *", age, onAgeChange, keyboardType = KeyboardType.Number)
                    LabeledField("Телефон *", phone, onPhoneChange, keyboardType = KeyboardType.Phone)
                    LabeledField("Имя опекуна *", guardianFirstName, onGuardianFirstNameChange)
                    LabeledField("Фамилия опекуна *", guardianLastName, onGuardianLastNameChange)
                    LabeledField("Телефон опекуна", guardianPhone, onGuardianPhoneChange, keyboardType = KeyboardType.Phone)
                    Button(onSave, Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), enabled = enabled) { Text(saveLabel, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    val subtextColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = subtextColor)
        OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboardType))
    }
}

// ═══════════════════════════════════════════════════════════════
//  PAYMENT SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PaymentScreen(isDark: Boolean, student: AdminStudent?, uiState: AdminGroupsViewModel.UiState, viewModel: AdminGroupsViewModel) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val payments = student?.payments?.sortedByDescending { (it.year ?: 0) * 100 + (it.month ?: 0) } ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(student?.fullName ?: "", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedButton(onClick = { viewModel.dismissPaymentDialog() }, shape = RoundedCornerShape(10.dp)) { Text("Закрыть") }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = cardBg, border = BorderStroke(1.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Платежи", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                        OutlinedButton(onClick = {}, shape = RoundedCornerShape(10.dp), enabled = false) {
                            // Placeholder — add handled below
                        }
                    }
                    // Payment list
                    payments.forEachIndexed { index, payment ->
                        val monthName = monthName(payment.month ?: 0)
                        val year = payment.year ?: 0
                        Surface(shape = RoundedCornerShape(12.dp), color = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White, border = BorderStroke(1.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(Brand.Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("$monthName $year", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                                    Text("${formatAmount(payment.amount)} сум", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandGreen)
                                    val methodLabel = when (payment.paymentMethod) { "cash" -> "Наличные"; "card" -> "Карта"; "terminal" -> "Терминал"; "transfer" -> "Перевод"; else -> payment.paymentMethod ?: "" }
                                    Text("$methodLabel  ${payment.date ?: ""}", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                                }
                                // Edit button
                                IconButton(onClick = { viewModel.startEditPayment(index, payment) }, modifier = Modifier.size(36.dp)) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = BrandBlue) { Icon(Icons.Default.Edit, null, Modifier.padding(6.dp).size(16.dp), tint = Color.White) }
                                }
                                Spacer(Modifier.width(4.dp))
                                // Delete button
                                IconButton(onClick = { viewModel.deletePayment(index) }, modifier = Modifier.size(36.dp)) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = BrandCoral) { Icon(Icons.Default.Delete, null, Modifier.padding(6.dp).size(16.dp), tint = Color.White) }
                                }
                            }
                        }
                    }
                    if (payments.isEmpty()) {
                        Text("Нет платежей", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                    }
                    // Add new payment
                    HorizontalDivider(color = cardBorder)
                    Text("Добавить платёж", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = textColor)
                    OutlinedTextField(uiState.paymentAmount, { viewModel.updatePaymentAmount(it) }, Modifier.fillMaxWidth(), label = { Text("Сумма") }, shape = RoundedCornerShape(10.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.xs)) {
                        listOf("cash" to "Наличные", "card" to "Карта", "terminal" to "Терминал").forEach { (m, l) ->
                            FilterChip(selected = uiState.paymentMethod == m, onClick = { viewModel.updatePaymentMethod(m) }, label = { Text(l, fontSize = 11.sp) })
                        }
                    }
                    Button({ viewModel.savePayment() }, Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), enabled = uiState.paymentAmount.toIntOrNull() != null,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) { Text("Сохранить", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  ATTENDANCE SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AttendanceScreen(isDark: Boolean, student: AdminStudent?, uiState: AdminGroupsViewModel.UiState, viewModel: AdminGroupsViewModel) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val group = uiState.detailGroup
    val studentId = student?.id ?: return

    // Calculate lesson dates for current month based on scheduleDays
    val lessonDates = getLessonDatesForCurrentMonth(group?.scheduleDays)

    val attendanceMap = student.attendance?.associate { it.date to it.status } ?: emptyMap()

    val presentCount = attendanceMap.count { it.value == "present" }
    val totalCount = lessonDates.size
    val percent = if (totalCount > 0) presentCount * 100 / totalCount else 0

    val cal = java.util.Calendar.getInstance()
    val monthName = monthName(cal.get(java.util.Calendar.MONTH) + 1)
    val year = cal.get(java.util.Calendar.YEAR)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedButton(onClick = { viewModel.dismissAttendanceDialog() }, shape = RoundedCornerShape(10.dp)) { Text("Закрыть") }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = cardBg, border = BorderStroke(1.dp, cardBorder), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                    Text("Посещаемость", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                    Text("$monthName $year", style = MaterialTheme.typography.bodySmall, color = subtextColor)

                    // Date grid
                    val rows = lessonDates.chunked(4)
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            row.forEach { date ->
                                val status = attendanceMap[date]
                                val bgColor = when (status) {
                                    "present" -> BrandTeal
                                    "absent" -> BrandCoral
                                    else -> if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                                }
                                val borderColor = when (status) {
                                    "present" -> BrandTeal
                                    "absent" -> BrandCoral
                                    else -> subtextColor.copy(alpha = 0.3f)
                                }
                                // Format date dd.MM from yyyy-MM-dd
                                val displayDate = if (date.length >= 10) "${date.substring(8, 10)}.${date.substring(5, 7)}" else date

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = bgColor,
                                    border = BorderStroke(1.dp, borderColor),
                                    modifier = Modifier.clickable { viewModel.toggleAttendance(studentId, date) },
                                ) {
                                    Text(
                                        displayDate,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (status != null) Color.White else textColor,
                                    )
                                }
                            }
                        }
                    }

                    Text("Итого: $presentCount/$totalCount • $percent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = textColor)

                    Button(
                        onClick = { viewModel.dismissAttendanceDialog() },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Сохранить", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  PROGRESS SCREEN (iOS-matching design)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ProgressScreen(isDark: Boolean, student: AdminStudent?, uiState: AdminGroupsViewModel.UiState, viewModel: AdminGroupsViewModel) {
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    var expandedTestLevel by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Прогресс", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                OutlinedButton(onClick = { viewModel.dismissProgressDialog() }, shape = RoundedCornerShape(10.dp)) { Text("Закрыть") }
            }
        }

        // Student info
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                val initial = (student?.firstName?.firstOrNull() ?: '?').uppercase()
                Box(Modifier.size(44.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text(initial, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = BrandBlue)
                }
                Column {
                    Text(student?.fullName ?: "", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = textColor)
                    Text("Академический прогресс", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                }
            }
        }

        if (uiState.isLoadingProgress) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.progressData != null) {
            // HSK Tests
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = BrandBlue)
                    Spacer(Modifier.width(Brand.Spacing.sm))
                    Text("Тесты HSK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
            items((1..6).toList()) { level ->
                val test = uiState.progressData.tests.find { it.levelId == "hsk$level" || it.levelId == "$level" }
                val prevTest = if (level > 1) uiState.progressData.tests.find { it.levelId == "hsk${level - 1}" || it.levelId == "${level - 1}" } else null
                val isLocked = level > 1 && (prevTest == null || prevTest.bestScore < 8)
                val isCompleted = test != null && test.bestScore > 0

                AdminTestLevelCard(
                    level = level,
                    testResult = test,
                    isLocked = isLocked,
                    isCompleted = isCompleted,
                    isDark = isDark,
                    cardBg = cardBg,
                    strokeColor = strokeColor,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    isExpanded = expandedTestLevel == (test?.levelId ?: ""),
                    onToggleExpand = {
                        val id = test?.levelId ?: ""
                        expandedTestLevel = if (expandedTestLevel == id) null else id
                    },
                )
            }

            // Vocabulary
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, null, Modifier.size(20.dp), tint = BrandTeal)
                    Spacer(Modifier.width(Brand.Spacing.sm))
                    Text("Словарь", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
            if (uiState.progressData.vocabulary.isEmpty()) {
                item { Text("Ученик ещё не изучал словарь", style = MaterialTheme.typography.bodySmall, color = subtextColor, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            } else {
                items(uiState.progressData.vocabulary) { vocab ->
                    AdminVocabLevelCard(
                        vocab = vocab,
                        isDark = isDark,
                        cardBg = cardBg,
                        strokeColor = strokeColor,
                        textColor = textColor,
                        subtextColor = subtextColor,
                    )
                }
            }
        } else {
            item { Text("Нет данных о прогрессе", style = MaterialTheme.typography.bodySmall, color = subtextColor) }
        }
    }
}

@Composable
private fun AdminTestLevelCard(
    level: Int,
    testResult: com.subnetik.unlock.data.remote.dto.progress.TestProgressSyncItem?,
    isLocked: Boolean,
    isCompleted: Boolean,
    isDark: Boolean,
    cardBg: Color,
    strokeColor: Color,
    textColor: Color,
    subtextColor: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    val alpha = if (isLocked) 0.5f else 1f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cardBg.copy(alpha = alpha),
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).background(BrandBlue.copy(alpha = if (isLocked) 0.08f else 0.18f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$level", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isLocked) subtextColor else BrandBlue)
                }
                Spacer(Modifier.width(Brand.Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text("HSK $level", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isLocked) subtextColor else textColor)
                    Text(
                        when {
                            isCompleted -> {
                                val t = testResult!!
                                val attText = adminRussianPlural(t.attempts, "попытка", "попытки", "попыток")
                                "${t.bestScore}/${t.totalQuestions} (${t.bestPercent}%) \u2022 ${t.attempts} $attText"
                            }
                            isLocked -> "Пройдите HSK ${level - 1}"
                            else -> "Не начат"
                        },
                        style = MaterialTheme.typography.bodySmall, color = subtextColor,
                    )
                }
                if (isLocked) {
                    Icon(Icons.Default.Lock, null, Modifier.size(14.dp), tint = subtextColor.copy(alpha = 0.5f))
                } else if (isCompleted && testResult != null) {
                    Surface(shape = RoundedCornerShape(50), color = if (testResult.bestScore >= 8) BrandTeal else BrandCoral) {
                        Text(
                            if (testResult.bestScore >= 8) "Пройден" else "Не сдан",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp,
                        )
                    }
                }
            }

            // Expandable answers
            if (testResult != null && !testResult.bestAttemptDetails.isNullOrEmpty() && !isLocked) {
                Spacer(Modifier.height(8.dp))
                Surface(onClick = onToggleExpand, shape = RoundedCornerShape(50), color = BrandBlue.copy(alpha = 0.08f)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, Modifier.size(14.dp), tint = BrandBlue)
                        Text(if (isExpanded) "Скрыть ответы" else "Показать ответы", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandBlue, fontSize = 11.sp)
                    }
                }

                androidx.compose.animation.AnimatedVisibility(visible = isExpanded, enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(), exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()) {
                    Column(Modifier.padding(top = 8.dp)) {
                        val details = testResult.bestAttemptDetails!!
                        val correctCount = details.count { it.isCorrect == true }
                        val wrongCount = details.size - correctCount

                        Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(8.dp).background(BrandTeal, CircleShape))
                                Text("Верно: $correctCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandTeal, fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(8.dp).background(BrandCoral, CircleShape))
                                Text("Ошибки: $wrongCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandCoral, fontSize = 11.sp)
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Surface(shape = RoundedCornerShape(12.dp), color = Color.Transparent, border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f))) {
                            Column {
                                details.forEachIndexed { idx, detail ->
                                    AdminQuestionDetailRow(index = idx + 1, detail = detail, isDark = isDark, primaryText = textColor)
                                    if (idx < details.lastIndex) {
                                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminQuestionDetailRow(
    index: Int,
    detail: com.subnetik.unlock.data.remote.dto.progress.TestAttemptDetail,
    isDark: Boolean,
    primaryText: Color,
) {
    val isCorrect = detail.isCorrect == true
    val accent = if (isCorrect) BrandTeal else BrandCoral

    Row(
        Modifier.fillMaxWidth().background(accent.copy(alpha = 0.04f)).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(26.dp).background(accent.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
            Text("$index", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
        }
        Column(Modifier.weight(1f)) {
            Text(detail.prompt ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = primaryText, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            if (isCorrect) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = BrandTeal)
                    Text(detail.correctAnswer ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandTeal, fontSize = 11.sp)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Close, null, Modifier.size(12.dp), tint = BrandCoral)
                    Text(detail.selectedAnswer ?: "", style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough), color = BrandCoral, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = BrandTeal)
                    Text(detail.correctAnswer ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandTeal, fontSize = 11.sp)
                }
            }
        }
        Icon(if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel, null, Modifier.size(16.dp), tint = accent)
    }
}

@Composable
private fun AdminVocabLevelCard(
    vocab: com.subnetik.unlock.data.remote.dto.progress.VocabProgressSyncItem,
    isDark: Boolean,
    cardBg: Color,
    strokeColor: Color,
    textColor: Color,
    subtextColor: Color,
) {
    val notStudied = maxOf(0, vocab.totalWords - vocab.knownCount - vocab.reviewCount)
    val barFraction = if (vocab.totalWords > 0) vocab.knownCount.toFloat() / vocab.totalWords else 0f

    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = cardBg, border = BorderStroke(1.dp, strokeColor)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("HSK ${vocab.level}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text("${vocab.percent.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandTeal)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { barFraction },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = BrandTeal,
                trackColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AdminVocabStatPill("Выучено", vocab.knownCount, BrandTeal, subtextColor, Modifier.weight(1f))
                AdminVocabStatPill("Повторить", vocab.reviewCount, BrandCoral, subtextColor, Modifier.weight(1f))
                AdminVocabStatPill("Не изучено", notStudied, subtextColor, subtextColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AdminVocabStatPill(label: String, count: Int, color: Color, subtextColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp)).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = subtextColor, fontSize = 9.sp)
    }
}

private fun adminRussianPlural(count: Int, one: String, few: String, many: String): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..19 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

private fun pluralGroups(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..14 -> "групп"
        mod10 == 1 -> "группа"
        mod10 in 2..4 -> "группы"
        else -> "групп"
    }
}

private fun formatAmount(amount: Int?): String {
    if (amount == null) return "0"
    return amount.toString().reversed().chunked(3).joinToString(" ").reversed()
}

private fun monthName(month: Int): String = when (month) {
    1 -> "Январь"; 2 -> "Февраль"; 3 -> "Март"; 4 -> "Апрель"
    5 -> "Май"; 6 -> "Июнь"; 7 -> "Июль"; 8 -> "Август"
    9 -> "Сентябрь"; 10 -> "Октябрь"; 11 -> "Ноябрь"; 12 -> "Декабрь"
    else -> ""
}

private fun getLessonDatesForCurrentMonth(scheduleDays: String?): List<String> {
    if (scheduleDays == null) return emptyList()
    val days = scheduleDays.lowercase()
    val dayMap = mapOf("пн" to java.util.Calendar.MONDAY, "вт" to java.util.Calendar.TUESDAY, "ср" to java.util.Calendar.WEDNESDAY, "чт" to java.util.Calendar.THURSDAY, "пт" to java.util.Calendar.FRIDAY, "сб" to java.util.Calendar.SATURDAY, "вс" to java.util.Calendar.SUNDAY)
    val activeDays = dayMap.filter { days.contains(it.key) }.values.toSet()
    if (activeDays.isEmpty()) return emptyList()

    val cal = java.util.Calendar.getInstance()
    val currentMonth = cal.get(java.util.Calendar.MONTH)
    val currentYear = cal.get(java.util.Calendar.YEAR)
    val today = cal.get(java.util.Calendar.DAY_OF_MONTH)
    cal.set(currentYear, currentMonth, 1)
    val maxDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val dates = mutableListOf<String>()
    for (day in 1..minOf(maxDay, today)) {
        cal.set(currentYear, currentMonth, day)
        if (cal.get(java.util.Calendar.DAY_OF_WEEK) in activeDays) {
            dates.add(String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day))
        }
    }
    return dates
}
