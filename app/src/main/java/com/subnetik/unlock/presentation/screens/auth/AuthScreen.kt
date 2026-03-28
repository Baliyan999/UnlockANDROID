package com.subnetik.unlock.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.BrandBlue
import com.subnetik.unlock.presentation.theme.BrandIndigo
import com.subnetik.unlock.presentation.theme.BrandTeal
import com.subnetik.unlock.presentation.theme.DarkNavy
import com.subnetik.unlock.presentation.theme.DeepBlue

// Auth screen adaptive colors
@Composable private fun authBackground(isDark: Boolean) = if (isDark) DarkNavy else Color(0xFFF0F2F8)
@Composable private fun authCardBackground(isDark: Boolean) = if (isDark) Color(0xFF1A2040) else Color.White
@Composable private fun authCardStroke(isDark: Boolean) = if (isDark) Color(0xFF2A3060) else Color(0xFFE0E4EE)
@Composable private fun authFieldBackground(isDark: Boolean) = if (isDark) Color(0xFF141A35) else Color(0xFFF5F6FA)
@Composable private fun authFieldStroke(isDark: Boolean) = if (isDark) Color(0xFF252D55) else Color(0xFFD8DCE8)
@Composable private fun authLabelColor(isDark: Boolean) = if (isDark) Color(0xFFB0B8D0) else Color(0xFF4A5068)
@Composable private fun authPlaceholderColor(isDark: Boolean) = if (isDark) Color(0xFF5A6280) else Color(0xFF9CA3B8)
@Composable private fun authTitleColor(isDark: Boolean) = if (isDark) Color.White else Color(0xFF1A1E33)
@Composable private fun authSubtextColor(isDark: Boolean) = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF6B7088)
private val AuthGlowBlue = Color(0xFF3A82F5).copy(alpha = 0.15f)
private val AuthGlowPurple = Color(0xFF6C63FF).copy(alpha = 0.12f)
private val GradientSubmit = listOf(BrandBlue, BrandIndigo)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToVerifyCode: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.authSuccess) {
        if (uiState.authSuccess) {
            onAuthSuccess()
            viewModel.resetAuthSuccess()
        }
    }

    LaunchedEffect(uiState.needs2FA) {
        val email = uiState.needs2FAEmail
        if (uiState.needs2FA && email != null) {
            onNavigateToVerifyCode(email)
            viewModel.reset2FA()
        }
    }

    var isSignUp by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val isDark = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(authBackground(isDark)),
    ) {
        // Decorative glow circles (matching iOS LeadBackground)
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-100).dp, y = (-180).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AuthGlowBlue, Color.Transparent),
                    ),
                    shape = CircleShape,
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AuthGlowPurple, Color.Transparent),
                    ),
                    shape = CircleShape,
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // Lock/PersonAdd icon in gradient circle
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        brush = Brush.linearGradient(GradientSubmit),
                        shape = RoundedCornerShape(22.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = if (isSignUp) "Создать аккаунт" else "Вход в аккаунт",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = authTitleColor(isDark),
            )

            Spacer(Modifier.height(8.dp))

            // Toggle link text (iOS style)
            if (isSignUp) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Уже есть аккаунт? ",
                        fontSize = 14.sp,
                        color = authLabelColor(isDark),
                    )
                    Text(
                        text = "Войти",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { isSignUp = false },
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Или ",
                        fontSize = 14.sp,
                        color = authLabelColor(isDark),
                    )
                    Text(
                        text = "создайте новый аккаунт",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { isSignUp = true },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Form card (matching iOS LeadFormCard)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = authCardBackground(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, authCardStroke(isDark)),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                ) {
                    AnimatedVisibility(
                        visible = !isSignUp,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        LoginFormContent(
                            email = uiState.loginEmail,
                            password = uiState.loginPassword,
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            rememberMe = rememberMe,
                            onRememberMeChange = { rememberMe = it },
                            onEmailChange = viewModel::onLoginEmailChange,
                            onPasswordChange = viewModel::onLoginPasswordChange,
                            onLogin = viewModel::login,
                            isDark = isDark,
                        )
                    }
                    AnimatedVisibility(
                        visible = isSignUp,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        RegisterFormContent(
                            name = uiState.registerName,
                            email = uiState.registerEmail,
                            password = uiState.registerPassword,
                            confirmPassword = uiState.registerConfirmPassword,
                            referralCode = uiState.referralCode,
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            rememberMe = rememberMe,
                            onRememberMeChange = { rememberMe = it },
                            onNameChange = viewModel::onRegisterNameChange,
                            onEmailChange = viewModel::onRegisterEmailChange,
                            onPasswordChange = viewModel::onRegisterPasswordChange,
                            onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChange,
                            onReferralCodeChange = viewModel::onReferralCodeChange,
                            onRegister = viewModel::register,
                            isDark = isDark,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bottom helper text (matching iOS)
            Text(
                text = "На главной вы всегда сможете вернуться к курсам и заданиям.",
                fontSize = 12.sp,
                color = authPlaceholderColor(isDark),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ──────────────────────────────────────────────
// Styled input field matching iOS LeadInputField
// ──────────────────────────────────────────────
@Composable
private fun AuthInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    helperText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label above field (iOS style)
        Text(
            text = buildAnnotatedString {
                append(label)
                if (required) {
                    withStyle(SpanStyle(color = Color(0xFFF57840))) {
                        append(" *")
                    }
                }
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = authLabelColor(isDark),
        )
        Spacer(Modifier.height(6.dp))

        // Field container
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = authFieldBackground(isDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, authFieldStroke(isDark)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val visualTransformation = if (isPassword && !passwordVisible)
                    PasswordVisualTransformation() else VisualTransformation.None

                BasicAuthTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = placeholder,
                    isDark = isDark,
                    modifier = Modifier.weight(1f),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                )

                if (isPassword && onTogglePasswordVisibility != null) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = authPlaceholderColor(isDark),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Helper text below field
        if (helperText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = helperText,
                fontSize = 11.sp,
                color = authPlaceholderColor(isDark),
            )
        }
    }
}

@Composable
private fun BasicAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 15.sp,
            color = authTitleColor(isDark),
        ),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = Brush.verticalGradient(listOf(BrandBlue, BrandBlue)),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        color = authPlaceholderColor(isDark),
                    )
                }
                innerTextField()
            }
        },
    )
}

// ──────────────────────────────────────────────
// Remember Me checkbox row (matching iOS)
// ──────────────────────────────────────────────
@Composable
private fun RememberMeRow(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!isChecked) },
        shape = RoundedCornerShape(12.dp),
        color = authFieldBackground(isDark).copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, authFieldStroke(isDark)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = BrandBlue,
                    uncheckedColor = authPlaceholderColor(isDark),
                    checkmarkColor = Color.White,
                ),
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Запомнить вход",
                fontSize = 13.sp,
                color = authLabelColor(isDark),
            )
        }
    }
}

// ──────────────────────────────────────────────
// Gradient submit button (matching iOS)
// ──────────────────────────────────────────────
@Composable
private fun AuthSubmitButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
) {
    val colors = if (enabled && !isLoading) GradientSubmit
    else GradientSubmit.map { it.copy(alpha = 0.4f) }

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        contentPadding = PaddingValues(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(colors),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "\u2192",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Login form content
// ──────────────────────────────────────────────
@Composable
private fun LoginFormContent(
    email: String,
    password: String,
    isLoading: Boolean,
    error: String?,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    isDark: Boolean,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuthInputField(
            label = "Логин",
            placeholder = "Введите логин",
            value = email,
            onValueChange = onEmailChange,
            isDark = isDark,
            required = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        AuthInputField(
            label = "Пароль",
            placeholder = "Минимум 8 символов",
            value = password,
            onValueChange = onPasswordChange,
            isDark = isDark,
            required = true,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            helperText = "Минимум 8 символов, должен содержать буквы и цифры",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onLogin() },
            ),
        )

        error?.let {
            Text(
                text = it,
                color = Color(0xFFF57840),
                fontSize = 13.sp,
            )
        }

        RememberMeRow(
            isChecked = rememberMe,
            onCheckedChange = onRememberMeChange,
            isDark = isDark,
        )

        AuthSubmitButton(
            text = "Войти",
            onClick = onLogin,
            isLoading = isLoading,
            enabled = email.isNotBlank() && password.isNotBlank(),
        )
    }
}

// ──────────────────────────────────────────────
// Register form content
// ──────────────────────────────────────────────
@Composable
private fun RegisterFormContent(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    referralCode: String,
    isLoading: Boolean,
    error: String?,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onReferralCodeChange: (String) -> Unit,
    onRegister: () -> Unit,
    isDark: Boolean,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AuthInputField(
            label = "Имя",
            placeholder = "Введите ваше имя",
            value = name,
            onValueChange = onNameChange,
            isDark = isDark,
            required = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        AuthInputField(
            label = "Логин",
            placeholder = "Введите логин",
            value = email,
            onValueChange = onEmailChange,
            isDark = isDark,
            required = true,
            helperText = "Только латинские буквы, цифры и символы . _ @ -",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        AuthInputField(
            label = "Пароль",
            placeholder = "Минимум 8 символов",
            value = password,
            onValueChange = onPasswordChange,
            isDark = isDark,
            required = true,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            helperText = "Минимум 8 символов, должен содержать буквы и цифры",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        AuthInputField(
            label = "Подтвердите пароль",
            placeholder = "Повторите пароль",
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            isDark = isDark,
            required = true,
            isPassword = true,
            passwordVisible = confirmVisible,
            onTogglePasswordVisibility = { confirmVisible = !confirmVisible },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        AuthInputField(
            label = "Реферальный код",
            placeholder = "Необязательно",
            value = referralCode,
            onValueChange = onReferralCodeChange,
            isDark = isDark,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onRegister() },
            ),
        )

        error?.let {
            Text(
                text = it,
                color = Color(0xFFF57840),
                fontSize = 13.sp,
            )
        }

        RememberMeRow(
            isChecked = rememberMe,
            onCheckedChange = onRememberMeChange,
            isDark = isDark,
        )

        AuthSubmitButton(
            text = "Создать аккаунт",
            onClick = onRegister,
            isLoading = isLoading,
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
        )
    }
}
