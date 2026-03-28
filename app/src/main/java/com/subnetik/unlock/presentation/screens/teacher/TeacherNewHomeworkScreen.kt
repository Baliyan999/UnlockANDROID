package com.subnetik.unlock.presentation.screens.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNewHomeworkScreen(
    viewModel: TeacherHomeViewModel,
    isDark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.03f)

    Box(modifier = Modifier.fillMaxSize()) {
        AdminBackground(isDark = isDark)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { viewModel.closeNewHomework() },
                    shape = Brand.Shapes.full,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                ) {
                    Text(
                        "Отмена",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                    )
                }

                Text(
                    "Новое задание",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )

                Surface(
                    onClick = { viewModel.createHomework() },
                    shape = Brand.Shapes.full,
                    color = if (uiState.homeworkTitle.isNotBlank()) BrandBlue.copy(alpha = 0.15f) else Color.Transparent,
                    enabled = uiState.homeworkTitle.isNotBlank() && !uiState.isCreatingHomework,
                ) {
                    Text(
                        "Создать",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.homeworkTitle.isNotBlank()) BrandBlue else secondaryText,
                    )
                }
            }

            Spacer(Modifier.height(Brand.Spacing.md))

            Column(modifier = Modifier.padding(horizontal = Brand.Spacing.lg)) {
                // Group selector
                Text(
                    "Группа",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.height(Brand.Spacing.sm))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm),
                ) {
                    uiState.groups.forEach { group ->
                        val isSelected = group.id == uiState.homeworkGroupId
                        Surface(
                            onClick = { viewModel.updateHomeworkGroupId(group.id) },
                            shape = Brand.Shapes.full,
                            color = if (isSelected) BrandBlue else Color.Transparent,
                            border = if (isSelected) null else BorderStroke(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                            ),
                        ) {
                            Text(
                                group.name,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else primaryText,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))

                // Title field
                Text(
                    "Название задания *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.height(Brand.Spacing.sm))
                OutlinedTextField(
                    value = uiState.homeworkTitle,
                    onValueChange = { viewModel.updateHomeworkTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Например: Урок 5, упражнения 1-3") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = fieldBg,
                        focusedContainerColor = fieldBg,
                    ),
                )

                Spacer(Modifier.height(Brand.Spacing.xl))

                // Description field
                Text(
                    "Описание",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(Modifier.height(Brand.Spacing.sm))
                OutlinedTextField(
                    value = uiState.homeworkDescription,
                    onValueChange = { viewModel.updateHomeworkDescription(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = fieldBg,
                        focusedContainerColor = fieldBg,
                    ),
                )

                Spacer(Modifier.height(Brand.Spacing.xl))

                // Deadline toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Установить дедлайн",
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryText,
                    )
                    Switch(
                        checked = uiState.homeworkHasDeadline,
                        onCheckedChange = { viewModel.toggleHomeworkDeadline(it) },
                    )
                }

                if (uiState.homeworkHasDeadline) {
                    Spacer(Modifier.height(Brand.Spacing.md))

                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = uiState.homeworkDueDate ?: System.currentTimeMillis(),
                    )

                    LaunchedEffect(datePickerState.selectedDateMillis) {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.updateHomeworkDueDate(it)
                        }
                    }

                    // Styled date picker card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF1A2540) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2A3B5A) else Color(0xFFD1D5DB)),
                    ) {
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier.fillMaxWidth(),
                            showModeToggle = false,
                            title = null,
                            headline = null,
                        )
                    }

                    Spacer(Modifier.height(Brand.Spacing.md))

                    // Time picker row
                    var showTimePicker by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF1A2540) else Color.White,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2A3B5A) else Color(0xFFD1D5DB)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Время", style = MaterialTheme.typography.bodyLarge, color = primaryText)
                            Surface(
                                onClick = { showTimePicker = true },
                                shape = RoundedCornerShape(10.dp),
                                color = BrandBlue.copy(alpha = 0.12f),
                            ) {
                                Text(
                                    "%02d:%02d".format(uiState.homeworkDueHour, uiState.homeworkDueMinute),
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue,
                                )
                            }
                        }
                    }

                    if (showTimePicker) {
                        val timePickerState = rememberTimePickerState(
                            initialHour = uiState.homeworkDueHour,
                            initialMinute = uiState.homeworkDueMinute,
                            is24Hour = true,
                        )
                        AlertDialog(
                            onDismissRequest = { showTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.updateHomeworkDueTime(timePickerState.hour, timePickerState.minute)
                                    showTimePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
                            },
                            title = { Text("Выберите время") },
                            text = { TimePicker(state = timePickerState) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Brand.Spacing.xxxl))
        }

        // Loading overlay
        if (uiState.isCreatingHomework) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = BrandBlue)
            }
        }
    }
}
