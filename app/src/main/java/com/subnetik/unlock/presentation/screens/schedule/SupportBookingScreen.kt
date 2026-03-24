package com.subnetik.unlock.presentation.screens.schedule

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportBookingScreen(
    onBack: () -> Unit,
    viewModel: SupportBookingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme ?: true

    val bgColor = if (isDark) DarkNavy else Color(0xFFF5F6FA)
    val cardColor = if (isDark) Color(0xFF1A2151) else Color.White
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val primaryText = if (isDark) Color.White else Color(0xFF1A1A2E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280)

    val timeSlots = remember { SupportBookingViewModel.generateTimeSlots() }
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = bgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Запись к Support", color = primaryText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = primaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Запись к Support-преподавателю", fontWeight = FontWeight.Bold, color = primaryText)
                        Text("Выберите дату, время и преподавателя", style = MaterialTheme.typography.bodySmall, color = secondaryText)
                    }
                }
            }

            // Teacher picker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Преподаватель", fontWeight = FontWeight.SemiBold, color = primaryText)
                    }
                    Spacer(Modifier.height(12.dp))
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = uiState.selectedTeacher,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            uiState.teachers.forEach { teacher ->
                                DropdownMenuItem(
                                    text = { Text(teacher) },
                                    onClick = {
                                        viewModel.selectTeacher(teacher)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Date picker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Дата", fontWeight = FontWeight.SemiBold, color = primaryText)
                    }
                    Spacer(Modifier.height(12.dp))
                    val dateStr = remember(uiState.selectedDate) {
                        val df = SimpleDateFormat("d MMMM yyyy г.", Locale("ru"))
                        df.format(uiState.selectedDate.time)
                    }
                    OutlinedButton(
                        onClick = {
                            val cal = uiState.selectedDate
                            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
                            DatePickerDialog(
                                context,
                                { _, y, m, d -> viewModel.selectDate(y, m, d) },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH),
                            ).apply {
                                datePicker.minDate = tomorrow.timeInMillis
                            }.show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(dateStr, color = primaryText)
                    }
                }
            }

            // Time grid
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = BrandCoral, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Время", fontWeight = FontWeight.SemiBold, color = primaryText)
                    }
                    Spacer(Modifier.height(12.dp))

                    if (uiState.isLoadingSlots) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandBlue)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.heightIn(max = 400.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(timeSlots) { time ->
                                val isBusy = time in uiState.busySlots
                                val isSelected = time == uiState.selectedTime

                                Surface(
                                    onClick = { if (!isBusy) viewModel.selectTime(time) },
                                    enabled = !isBusy,
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isBusy -> BrandCoral.copy(alpha = 0.15f)
                                        isSelected -> BrandGold
                                        else -> if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF0F0F0)
                                    },
                                    border = if (isSelected) BorderStroke(1.dp, BrandGold) else null,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            time,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isBusy -> BrandCoral.copy(alpha = 0.5f)
                                                isSelected -> Color.Black
                                                else -> primaryText
                                            },
                                        )
                                        if (isBusy) {
                                            Text("Занято", fontSize = 9.sp, color = BrandCoral.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Comment
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, strokeColor),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Комментарий", fontWeight = FontWeight.SemiBold, color = primaryText)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.comment,
                        onValueChange = { viewModel.updateComment(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Что бы вы хотели попрактиковать?") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                    )
                    if (uiState.comment.isNotEmpty() && uiState.comment.length < 5) {
                        Text("Минимум 5 символов", fontSize = 12.sp, color = BrandCoral, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Submit button
            Button(
                onClick = { viewModel.submitBooking() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = uiState.selectedTime.isNotBlank() && uiState.comment.length >= 5 && !uiState.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Отправка...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Записаться", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
