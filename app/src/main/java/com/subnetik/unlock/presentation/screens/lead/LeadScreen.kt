package com.subnetik.unlock.presentation.screens.lead

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subnetik.unlock.presentation.screens.admin.components.AdminBackground
import com.subnetik.unlock.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadScreen(
    onBack: () -> Unit,
    viewModel: LeadViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themePreference by viewModel.isDarkTheme.collectAsStateWithLifecycle(initialValue = null)
    val isDark = themePreference ?: isSystemInDarkTheme()

    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.85f)
    val strokeColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val fieldBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
    val fieldStroke = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
    val placeholderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.35f)
    val errorColor = Color(0xFFFF6B6B)
    val successColor = Color(0xFF4CAF50)

    val submitGradient = Brush.linearGradient(listOf(Color(0xFF3A82F5), Color(0xFF7C5CFC)))
    val promoGradient = Brush.linearGradient(listOf(Color(0xFFE8754A), Color(0xFFF5B038)))

    // Success dialog
    if (state.submitSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog(); onBack() },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSuccessDialog(); onBack() }) {
                    Text("OK")
                }
            },
            title = { Text("Заявка отправлена") },
            text = { Text("Мы свяжемся с вами.") },
        )
    }

    // Error dialog
    state.submitError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissErrorDialog() }) {
                    Text("OK")
                }
            },
            title = { Text("Ошибка") },
            text = { Text(error) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Заявка",
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = primaryText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            AdminBackground(isDark = isDark)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Title
                Text(
                    "Заявка на пробный урок",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )

                // Form card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, strokeColor),
                    shadowElevation = if (isDark) 0.dp else 12.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        // Name
                        LeadField(
                            label = "Имя",
                            required = true,
                            value = state.name,
                            onValueChange = viewModel::onNameChange,
                            placeholder = "Введите имя",
                            error = if (state.showErrors && state.name.isBlank()) "Введите имя" else null,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next,
                            ),
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                            errorColor = errorColor,
                        )

                        // Phone
                        LeadField(
                            label = "Телефон",
                            required = true,
                            value = state.phone,
                            onValueChange = viewModel::onPhoneChange,
                            placeholder = "+998 xx xxx xx xx",
                            error = if (state.showErrors && state.phone.isBlank()) "Введите телефон" else null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next,
                            ),
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                            errorColor = errorColor,
                        )

                        // Email
                        LeadField(
                            label = "Email",
                            required = false,
                            value = state.email,
                            onValueChange = viewModel::onEmailChange,
                            placeholder = "Введите email",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                            errorColor = errorColor,
                        )

                        // Level dropdown
                        LeadDropdownField(
                            label = "Уровень",
                            selectedValue = state.selectedLevel,
                            options = LeadViewModel.levels,
                            onSelect = viewModel::onLevelChange,
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                        )

                        // Format dropdown
                        LeadDropdownField(
                            label = "Формат",
                            selectedValue = state.selectedFormat,
                            options = LeadViewModel.formats,
                            onSelect = viewModel::onFormatChange,
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                        )

                        // Promo code
                        LeadPromoRow(
                            value = state.promoCode,
                            onValueChange = viewModel::onPromoCodeChange,
                            onApply = viewModel::applyPromoCode,
                            isValidating = state.isValidatingPromo,
                            promoMessage = state.promoMessage,
                            promoIsSuccess = state.promoIsSuccess,
                            promoBonusDescription = state.promoBonusDescription,
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                            successColor = successColor,
                            errorColor = errorColor,
                            promoGradient = promoGradient,
                        )

                        // Comment
                        LeadCommentField(
                            value = state.comment,
                            onValueChange = viewModel::onCommentChange,
                            primaryText = primaryText,
                            labelColor = labelColor,
                            fieldBg = fieldBg,
                            fieldStroke = fieldStroke,
                            placeholderColor = placeholderColor,
                        )

                        // Submit button
                        Button(
                            onClick = viewModel::submitForm,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSubmitting,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(submitGradient, RoundedCornerShape(16.dp))
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (state.isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        "Отправить",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ─── Field components ────────────────────────────────────────

@Composable
private fun LeadFieldLabel(
    label: String,
    required: Boolean,
    labelColor: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
        )
        if (required) {
            Text(
                "*",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
            )
        }
    }
}

@Composable
private fun LeadField(
    label: String,
    required: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    primaryText: Color,
    labelColor: Color,
    fieldBg: Color,
    fieldStroke: Color,
    placeholderColor: Color,
    errorColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LeadFieldLabel(label = label, required = required, labelColor = labelColor)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = placeholderColor, fontSize = 14.sp)
            },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = primaryText,
                unfocusedTextColor = primaryText,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                focusedBorderColor = fieldStroke,
                unfocusedBorderColor = fieldStroke,
                cursorColor = BrandBlue,
            ),
            shape = RoundedCornerShape(12.dp),
        )
        if (error != null) {
            Text(
                error,
                fontSize = 11.sp,
                color = errorColor,
            )
        }
    }
}

@Composable
private fun LeadDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    primaryText: Color,
    labelColor: Color,
    fieldBg: Color,
    fieldStroke: Color,
    placeholderColor: Color,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LeadFieldLabel(label = label, required = false, labelColor = labelColor)

        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = fieldBg,
                border = BorderStroke(1.dp, fieldStroke),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selectedValue,
                        fontSize = 14.sp,
                        color = primaryText,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = placeholderColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadPromoRow(
    value: String,
    onValueChange: (String) -> Unit,
    onApply: () -> Unit,
    isValidating: Boolean,
    promoMessage: String?,
    promoIsSuccess: Boolean,
    promoBonusDescription: String?,
    primaryText: Color,
    labelColor: Color,
    fieldBg: Color,
    fieldStroke: Color,
    placeholderColor: Color,
    successColor: Color,
    errorColor: Color,
    promoGradient: Brush,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LeadFieldLabel(label = "Промокод", required = false, labelColor = labelColor)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Введите промокод", color = placeholderColor, fontSize = 14.sp)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    focusedBorderColor = fieldStroke,
                    unfocusedBorderColor = fieldStroke,
                    cursorColor = BrandBlue,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Button(
                onClick = onApply,
                enabled = !isValidating,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
            ) {
                Box(
                    modifier = Modifier
                        .background(promoGradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            "Применить",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Promo status message
        if (promoMessage != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (promoIsSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (promoIsSuccess) successColor else errorColor,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    promoMessage,
                    fontSize = 12.sp,
                    color = if (promoIsSuccess) successColor else errorColor,
                )
            }
            if (promoBonusDescription != null) {
                Text(
                    promoBonusDescription,
                    fontSize = 11.sp,
                    color = successColor,
                    modifier = Modifier.padding(start = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun LeadCommentField(
    value: String,
    onValueChange: (String) -> Unit,
    primaryText: Color,
    labelColor: Color,
    fieldBg: Color,
    fieldStroke: Color,
    placeholderColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LeadFieldLabel(label = "Комментарий", required = false, labelColor = labelColor)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            placeholder = {
                Text(
                    "Расскажите о ваших целях изучения китайского языка...",
                    color = placeholderColor,
                    fontSize = 13.sp,
                )
            },
            maxLines = 6,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = primaryText,
                unfocusedTextColor = primaryText,
                focusedContainerColor = fieldBg,
                unfocusedContainerColor = fieldBg,
                focusedBorderColor = fieldStroke,
                unfocusedBorderColor = fieldStroke,
                cursorColor = BrandBlue,
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}
