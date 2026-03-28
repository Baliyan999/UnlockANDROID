package com.subnetik.unlock.presentation.screens.homework

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.data.remote.dto.student.HomeworkAssignmentStudent
import com.subnetik.unlock.data.remote.dto.student.StudentSupportBooking
import com.subnetik.unlock.data.remote.dto.admin.HomeworkStudentGroupOverview
import com.subnetik.unlock.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeworkScreen(
    initialTab: String = "",
    onNavigateToSupportBooking: () -> Unit = {},
    viewModel: StudentHomeworkViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: isSystemInDarkTheme()

    // Auto-select Support tab if navigated from home button
    LaunchedEffect(initialTab) {
        if (initialTab == "support") {
            viewModel.selectTab(HomeworkTab.SUPPORT)
        }
    }

    val bgColor = if (isDark) DarkNavy else Color(0xFFF5F6FA)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        containerColor = bgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Учебный кабинет", color = primaryText, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Stats header
            StatsHeader(uiState, isDark, cardColor, strokeColor, primaryText, secondaryText)

            // Tab selector
            TabSelector(uiState.selectedTab, isDark) { viewModel.selectTab(it) }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandBlue)
                }
            } else {
                when (uiState.selectedTab) {
                    HomeworkTab.HOMEWORK -> HomeworkContent(uiState, viewModel, isDark, cardColor, strokeColor, primaryText, secondaryText)
                    HomeworkTab.SUPPORT -> SupportContent(uiState, viewModel, isDark, cardColor, strokeColor, primaryText, secondaryText)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatsHeader(
    uiState: StudentHomeworkUiState,
    isDark: Boolean,
    cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    val assignments = uiState.assignments
    val grades = assignments.mapNotNull { it.mySubmission?.grade }
    val avgGrade = if (grades.isNotEmpty()) String.format("%.1f", grades.average()) else "–"
    val submitted = assignments.count { it.mySubmission != null }
    val total = assignments.size

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1A2151).copy(alpha = if (isDark) 0.5f else 0.1f),
                        Color(0xFF0F1429).copy(alpha = if (isDark) 0.5f else 0.1f),
                    )
                )
            ).padding(16.dp),
        ) {
            // Icon + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (uiState.selectedTab == HomeworkTab.HOMEWORK) Icons.Default.MenuBook else Icons.Default.Groups,
                    contentDescription = null,
                    tint = if (uiState.selectedTab == HomeworkTab.HOMEWORK) BrandBlue else BrandCoral,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (uiState.selectedTab == HomeworkTab.HOMEWORK) "Домашние задания" else "Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else Color(0xFF1A1A2E),
                    )
                    Text(
                        if (uiState.selectedTab == HomeworkTab.HOMEWORK) "Смотрите задания, дедлайны и оценки." else "Запись к Support-преподавателям.",
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)).padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem("СР. БАЛЛ", avgGrade, if (isDark) Color.White else Color(0xFF1A1A2E))
                StatItem("СДАНО", "$submitted/$total", if (isDark) Color.White else Color(0xFF1A1A2E))
                StatItem("SUPPORT", "${uiState.supportBookings.size}", if (isDark) Color.White else Color(0xFF1A1A2E))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
        Text(label, fontSize = 11.sp, color = textColor.copy(alpha = 0.6f))
    }
}

@Composable
private fun TabSelector(selectedTab: HomeworkTab, isDark: Boolean, onSelect: (HomeworkTab) -> Unit) {
    val activeColor = if (isDark) BrandBlue else BrandBlue
    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE5E7EB)
    val activeText = Color.White
    val inactiveText = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF6B7280)

    Row(
        modifier = Modifier.fillMaxWidth().background(inactiveColor, RoundedCornerShape(12.dp)).padding(4.dp),
    ) {
        listOf(HomeworkTab.HOMEWORK to "Домашние задания", HomeworkTab.SUPPORT to "Support").forEach { (tab, label) ->
            val isActive = selectedTab == tab
            Surface(
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (isActive) activeColor else Color.Transparent,
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().wrapContentWidth(),
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (isActive) activeText else inactiveText,
                )
            }
        }
    }
}

@Composable
private fun HomeworkContent(
    uiState: StudentHomeworkUiState,
    viewModel: StudentHomeworkViewModel,
    isDark: Boolean, cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    // Groups
    if (uiState.groups.isNotEmpty()) {
        GroupsSection(uiState.groups, isDark, cardColor, strokeColor, primaryText, secondaryText)
    }

    // Assignments
    if (uiState.assignments.isEmpty()) {
        EmptyHomeworkState(isDark, cardColor, strokeColor, primaryText, secondaryText)
    } else {
        uiState.assignments.forEach { assignment ->
            AssignmentCard(assignment, uiState, viewModel, isDark, cardColor, strokeColor, primaryText, secondaryText)
        }
    }
}

@Composable
private fun GroupsSection(
    groups: List<HomeworkStudentGroupOverview>,
    isDark: Boolean, cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Мои группы", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)
            Spacer(Modifier.height(8.dp))
            groups.forEach { group ->
                Text(group.name, fontWeight = FontWeight.SemiBold, color = primaryText)
                group.teacher?.let {
                    Text("Преподаватель: ${it.displayName}", fontSize = 13.sp, color = secondaryText)
                }
                val myRank = group.rating.firstOrNull()
                myRank?.let {
                    Text("Ваше место в рейтинге: ${it.rank}", fontSize = 13.sp, color = secondaryText)
                }
                if (group != groups.last()) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyHomeworkState(
    isDark: Boolean, cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Активных заданий пока нет", fontWeight = FontWeight.SemiBold, color = primaryText)
                Text("Когда преподаватель добавит новое ДЗ, оно сразу появится здесь.", fontSize = 13.sp, color = secondaryText)
            }
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: HomeworkAssignmentStudent,
    uiState: StudentHomeworkUiState,
    viewModel: StudentHomeworkViewModel,
    isDark: Boolean, cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    val submission = assignment.mySubmission
    val statusColor = when {
        submission == null -> BrandCoral
        submission.grade != null -> BrandGold
        else -> BrandIndigo
    }
    val statusText = when {
        submission == null -> "Не сдано"
        submission.grade != null -> "Оценено"
        else -> "На проверке"
    }

    val context = LocalContext.current
    val isUploading = uiState.uploadingAssignmentId == assignment.id
    val selectedName = uiState.selectedFileName[assignment.id]

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            val name = cursor?.use { c ->
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                c.moveToFirst()
                if (idx >= 0) c.getString(idx) else "file"
            } ?: "file"
            viewModel.selectFile(assignment.id, it, name)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Status bar
            Box(
                Modifier.width(4.dp).fillMaxHeight().background(statusColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Column(modifier = Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(assignment.title, fontWeight = FontWeight.Bold, color = primaryText)
                        Text(assignment.groupName, fontSize = 13.sp, color = secondaryText)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.15f),
                    ) {
                        Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                }

                // Due date
                assignment.dueDate?.let { due ->
                    val formatted = formatDateRu(due)
                    if (formatted.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = secondaryText)
                            Spacer(Modifier.width(4.dp))
                            Text("Дедлайн: $formatted", fontSize = 12.sp, color = secondaryText)
                        }
                    }
                }

                // Description
                if (!assignment.description.isNullOrBlank()) {
                    Text(assignment.description, fontSize = 13.sp, color = secondaryText, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }

                // Submission info
                if (submission != null) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = secondaryText)
                        Spacer(Modifier.width(4.dp))
                        Text("Отправлено: ${formatDateRu(submission.submittedAt)}", fontSize = 12.sp, color = secondaryText)
                    }

                    if (submission.grade != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandGold)
                            Spacer(Modifier.width(4.dp))
                            Text("Оценка: ${submission.grade}", fontWeight = FontWeight.Bold, color = BrandGold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = BrandIndigo)
                            Spacer(Modifier.width(4.dp))
                            Text("Оценка ожидается", fontSize = 13.sp, color = BrandIndigo)
                        }
                    }

                    submission.teacherComment?.let { comment ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandIndigo)
                            Spacer(Modifier.width(4.dp))
                            Text(comment, fontSize = 13.sp, color = BrandIndigo)
                        }
                    }
                }

                // File upload (only if not submitted yet)
                if (submission == null) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))

                    if (selectedName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandTeal)
                            Spacer(Modifier.width(6.dp))
                            Text(selectedName, fontSize = 13.sp, color = primaryText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { launcher.launch("*/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Выбрать файл", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val uri = uiState.selectedFileUri[assignment.id] ?: return@Button
                                val name = uiState.selectedFileName[assignment.id] ?: "file"
                                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@Button
                                if (bytes.size > 20 * 1024 * 1024) {
                                    viewModel.clearMessages()
                                    return@Button
                                }
                                viewModel.submitHomework(assignment.id, bytes, name, mime)
                            },
                            shape = RoundedCornerShape(10.dp),
                            enabled = selectedName != null && !isUploading,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
                            modifier = Modifier.weight(1f),
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Отправка...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Отправить", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportContent(
    uiState: StudentHomeworkUiState,
    viewModel: StudentHomeworkViewModel,
    isDark: Boolean, cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    val context = LocalContext.current
    val timeSlots = remember { StudentHomeworkViewModel.generateTimeSlots() }

    // ─── Booking form (inline, like iOS) ────────
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Запись к Support-преподавателю", fontWeight = FontWeight.Bold, color = primaryText)
                    Text("Выберите дату, время и преподавателя", fontSize = 13.sp, color = secondaryText)
                }
            }

            HorizontalDivider(color = strokeColor)

            // Teacher picker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Преподаватель", fontWeight = FontWeight.SemiBold, color = primaryText)
                Spacer(Modifier.weight(1f))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    Text(
                        uiState.supportTeacher,
                        modifier = Modifier.menuAnchor(),
                        color = BrandGold,
                        fontWeight = FontWeight.SemiBold,
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        uiState.supportTeachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { Text(teacher) },
                                onClick = { viewModel.selectSupportTeacher(teacher); expanded = false },
                            )
                        }
                    }
                }
            }

            // Date picker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Дата", fontWeight = FontWeight.SemiBold, color = primaryText)
                Spacer(Modifier.weight(1f))
                val dateStr = remember(uiState.supportDate) {
                    SimpleDateFormat("d MMMM yyyy г.", Locale("ru")).format(uiState.supportDate.time)
                }
                TextButton(onClick = {
                    val cal = uiState.supportDate
                    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
                    DatePickerDialog(context, { _, y, m, d -> viewModel.selectSupportDate(y, m, d) },
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                    ).apply { datePicker.minDate = tomorrow.timeInMillis }.show()
                }) {
                    Text(dateStr, color = primaryText)
                }
            }

            // Time grid
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Время", fontWeight = FontWeight.SemiBold, color = primaryText)
            }

            if (uiState.isLoadingSlots) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandBlue)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.heightIn(max = 350.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(timeSlots) { time ->
                        val isBusy = time in uiState.busySlots
                        val isSelected = time == uiState.supportTime
                        Surface(
                            onClick = { if (!isBusy) viewModel.selectSupportTime(time) },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isBusy -> BrandCoral.copy(alpha = 0.12f)
                                isSelected -> BrandGold
                                else -> if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF0F0F0)
                            },
                        ) {
                            Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(time, fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isBusy -> BrandCoral.copy(alpha = 0.5f)
                                        isSelected -> Color.Black
                                        else -> primaryText
                                    },
                                )
                                if (isBusy) Text("Занято", fontSize = 9.sp, color = BrandCoral.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // Comment
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Комментарий", fontWeight = FontWeight.SemiBold, color = primaryText)
            }
            OutlinedTextField(
                value = uiState.supportComment,
                onValueChange = { viewModel.updateSupportComment(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Что бы вы хотели попрактиковать?") },
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
            )
            if (uiState.supportComment.isNotEmpty() && uiState.supportComment.trim().length < 5) {
                Text("Минимум 5 символов", fontSize = 12.sp, color = BrandCoral)
            }

            // Submit
            Button(
                onClick = { viewModel.submitBooking() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.supportTime.isNotBlank() && uiState.supportComment.trim().length >= 5 && !uiState.isSubmittingBooking,
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                if (uiState.isSubmittingBooking) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Отправка...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Отправить заявку", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ─── Booking history ────────────────────────
    Text("История заявок", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = primaryText)

    if (uiState.supportBookings.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cardColor,
            border = BorderStroke(1.dp, strokeColor),
        ) {
            Text("У вас пока нет заявок на Support.", modifier = Modifier.padding(16.dp), color = secondaryText)
        }
    } else {
        uiState.supportBookings.forEach { booking ->
            BookingCard(booking, isDark, cardColor, strokeColor, primaryText, secondaryText)
        }
    }
}

@Composable
private fun BookingCard(
    booking: StudentSupportBooking,
    isDark: Boolean, cardColor: Color, strokeColor: Color, primaryText: Color, secondaryText: Color,
) {
    val statusColor = when (booking.status) {
        "pending" -> BrandGold
        "processed", "confirmed" -> BrandGreen
        "cancelled" -> BrandCoral
        else -> secondaryText
    }
    val statusText = when (booking.status) {
        "pending" -> "Ожидает"
        "processed", "confirmed" -> "Обработана"
        "cancelled" -> "Отменена"
        else -> booking.status
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, strokeColor),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandBlue)
                Spacer(Modifier.width(6.dp))
                Text(booking.supportTeacher, fontWeight = FontWeight.SemiBold, color = primaryText, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(statusText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp), tint = secondaryText)
                Spacer(Modifier.width(4.dp))
                Text(formatDateRu(booking.sessionDatetime), fontSize = 13.sp, color = secondaryText)
            }
            if (booking.comment.isNotBlank()) {
                Text(booking.comment, fontSize = 13.sp, color = secondaryText, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            booking.adminNote?.let { note ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandIndigo)
                    Spacer(Modifier.width(4.dp))
                    Text(note, fontSize = 13.sp, color = BrandIndigo)
                }
            }
        }
    }
}

private fun formatDateRu(isoDate: String): String {
    return try {
        val formats = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd")
        var date: Date? = null
        for (fmt in formats) {
            try {
                date = SimpleDateFormat(fmt, Locale.US).parse(isoDate)
                if (date != null) break
            } catch (_: Exception) {}
        }
        date?.let {
            SimpleDateFormat("d MMMM yyyy 'г.' 'в' HH:mm", Locale("ru")).format(it)
        } ?: isoDate
    } catch (_: Exception) { isoDate }
}
