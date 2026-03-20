package com.subnetik.unlock.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.components.UnlockButton
import com.subnetik.unlock.presentation.components.UnlockTextField
import com.subnetik.unlock.presentation.theme.Brand
import com.subnetik.unlock.presentation.theme.GradientIndigo

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

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(GradientIndigo)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "解",
                    fontSize = 48.sp,
                    color = Color.White.copy(alpha = 0.15f),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "UNLOCK",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 4.sp,
                )
                Text(
                    text = "Language Studio",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }

        // White card overlapping gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp)
                .padding(horizontal = Brand.Spacing.lg),
            shape = Brand.Shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = Brand.Elevation.large),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(Brand.Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Custom segmented tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            Brand.Shapes.medium
                        )
                        .padding(4.dp),
                ) {
                    listOf("Вход", "Регистрация").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            onClick = { selectedTab = index },
                            modifier = Modifier.weight(1f),
                            shape = Brand.Shapes.small,
                            color = if (isSelected) MaterialTheme.colorScheme.surface
                            else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Brand.Spacing.xl))

                if (selectedTab == 0) {
                    LoginForm(
                        email = uiState.loginEmail,
                        password = uiState.loginPassword,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onEmailChange = viewModel::onLoginEmailChange,
                        onPasswordChange = viewModel::onLoginPasswordChange,
                        onLogin = viewModel::login,
                    )
                } else {
                    RegisterForm(
                        name = uiState.registerName,
                        email = uiState.registerEmail,
                        password = uiState.registerPassword,
                        confirmPassword = uiState.registerConfirmPassword,
                        referralCode = uiState.referralCode,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        onNameChange = viewModel::onRegisterNameChange,
                        onEmailChange = viewModel::onRegisterEmailChange,
                        onPasswordChange = viewModel::onRegisterPasswordChange,
                        onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChange,
                        onReferralCodeChange = viewModel::onReferralCodeChange,
                        onRegister = viewModel::register,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    isLoading: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        UnlockTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(Modifier.height(Brand.Spacing.md))

        UnlockTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Пароль",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onTrailingIconClick = { passwordVisible = !passwordVisible },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
        )

        error?.let {
            Spacer(Modifier.height(Brand.Spacing.sm))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(Brand.Spacing.xl))

        UnlockButton(
            text = "Войти",
            onClick = onLogin,
            isLoading = isLoading,
            enabled = email.isNotBlank() && password.isNotBlank(),
        )
    }
}

@Composable
private fun RegisterForm(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    referralCode: String,
    isLoading: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onReferralCodeChange: (String) -> Unit,
    onRegister: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        UnlockTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Имя",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(Brand.Spacing.md))

        UnlockTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(Modifier.height(Brand.Spacing.md))

        UnlockTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Пароль",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onTrailingIconClick = { passwordVisible = !passwordVisible },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(Modifier.height(Brand.Spacing.md))

        UnlockTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Подтвердите пароль",
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(Modifier.height(Brand.Spacing.md))

        UnlockTextField(
            value = referralCode,
            onValueChange = onReferralCodeChange,
            label = "Реферальный код (необязательно)",
            leadingIcon = Icons.Default.CardGiftcard,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )

        error?.let {
            Spacer(Modifier.height(Brand.Spacing.sm))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(Brand.Spacing.xl))

        UnlockButton(
            text = "Зарегистрироваться",
            onClick = onRegister,
            isLoading = isLoading,
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
        )
    }
}
