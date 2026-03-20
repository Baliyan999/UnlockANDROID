package com.subnetik.unlock.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnetik.unlock.presentation.components.UnlockButton
import com.subnetik.unlock.presentation.components.UnlockTextField
import com.subnetik.unlock.presentation.theme.Brand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var displayName by remember { mutableStateOf(uiState.displayName ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать профиль", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Brand.Spacing.lg)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(Brand.Spacing.sm))

            UnlockTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Имя",
                leadingIcon = Icons.Default.Person,
            )

            Spacer(Modifier.height(Brand.Spacing.md))

            UnlockTextField(
                value = uiState.email ?: "",
                onValueChange = {},
                label = "Email",
                leadingIcon = Icons.Default.Email,
                enabled = false,
            )

            Spacer(Modifier.height(Brand.Spacing.xl))

            UnlockButton(
                text = "Сохранить",
                onClick = {
                    isSaving = true
                    viewModel.updateProfile(displayName = displayName, email = null, currentPassword = null, newPassword = null)
                },
                isLoading = isSaving,
                enabled = displayName.isNotBlank(),
            )
        }
    }
}
