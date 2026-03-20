package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

@HiltViewModel
class AdminNotificationsViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {
    data class UiState(
        val isLoadingOptions: Boolean = true,
        val title: String = "",
        val body: String = "",
        val targetType: String = "all",
        val selectedRole: String? = null,
        val selectedGroupId: Int? = null,
        val selectedUserId: Int? = null,
        val isSending: Boolean = false,
        val successMessage: String? = null,
        val error: String? = null,
        // Dispatch options from API
        val availableRoles: List<String> = emptyList(),
        val availableUsers: List<AdminNotificationTargetUser> = emptyList(),
        val availableGroups: List<AdminNotificationTargetGroup> = emptyList(),
        val pushConfigured: Boolean = false,
        // Dispatch result
        val lastResult: AdminNotificationDispatchResponse? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadOptions() }

    fun loadOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingOptions = true, error = null) }
            try {
                val options = adminApi.getNotificationDispatchOptions()
                _uiState.update {
                    it.copy(
                        isLoadingOptions = false,
                        availableRoles = options.roles,
                        availableUsers = options.users,
                        availableGroups = options.groups,
                        pushConfigured = options.pushConfigured,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingOptions = false, error = e.message) }
            }
        }
    }

    fun updateTitle(v: String) { _uiState.update { it.copy(title = v) } }
    fun updateBody(v: String) { _uiState.update { it.copy(body = v) } }
    fun selectTargetType(t: String) {
        _uiState.update { it.copy(targetType = t, selectedRole = null, selectedGroupId = null, selectedUserId = null) }
    }
    fun selectRole(r: String) { _uiState.update { it.copy(selectedRole = r) } }
    fun selectGroup(id: Int) { _uiState.update { it.copy(selectedGroupId = id) } }
    fun selectUser(id: Int) { _uiState.update { it.copy(selectedUserId = id) } }

    fun sendNotification() {
        val state = _uiState.value
        if (state.title.isBlank() || state.body.isBlank()) return
        // Validate target selection
        when (state.targetType) {
            "role" -> if (state.selectedRole == null) return
            "group" -> if (state.selectedGroupId == null) return
            "user" -> if (state.selectedUserId == null) return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null, successMessage = null, lastResult = null) }
            try {
                val response = adminApi.dispatchNotification(AdminNotificationDispatchRequest(
                    title = state.title.trim(),
                    message = state.body.trim(),
                    targetType = state.targetType,
                    targetRole = if (state.targetType == "role") state.selectedRole else null,
                    targetGroupId = if (state.targetType == "group") state.selectedGroupId else null,
                    targetUserId = if (state.targetType == "user") state.selectedUserId else null,
                ))
                _uiState.update {
                    it.copy(
                        isSending = false,
                        successMessage = "Отправлено ${response.recipientsCount} получателям (push: ${response.pushSent})",
                        title = "", body = "",
                        lastResult = response,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.message) }
            }
        }
    }
}

@Composable
fun AdminNotificationsSection(isDark: Boolean, viewModel: AdminNotificationsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item { AdminSectionHeader(Icons.Default.Notifications, "Уведомления", isDark) }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadOptions() }) } }

        if (uiState.isLoadingOptions) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else {
            item {
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                        Text("Отправить уведомление", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)

                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Заголовок") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = uiState.body,
                            onValueChange = { viewModel.updateBody(it) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            label = { Text("Текст уведомления") },
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 6,
                        )

                        // Target type selector
                        Text("Кому отправить:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textColor)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            listOf("all" to "Все", "role" to "По роли", "group" to "Группа", "user" to "Пользователь").forEach { (key, label) ->
                                FilterChip(
                                    selected = uiState.targetType == key,
                                    onClick = { viewModel.selectTargetType(key) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        if (uiState.targetType == key) Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp))
                                    },
                                )
                            }
                        }

                        // Role selection
                        if (uiState.targetType == "role" && uiState.availableRoles.isNotEmpty()) {
                            Text("Роль:", style = MaterialTheme.typography.labelMedium, color = subtextColor)
                            Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                                uiState.availableRoles.forEach { role ->
                                    val roleLabel = when (role.lowercase()) {
                                        "admin" -> "Админ"; "teacher" -> "Учитель"
                                        "student" -> "Студент"; "user" -> "Пользователь"
                                        else -> role
                                    }
                                    FilterChip(
                                        selected = uiState.selectedRole == role,
                                        onClick = { viewModel.selectRole(role) },
                                        label = { Text(roleLabel, style = MaterialTheme.typography.labelSmall) },
                                    )
                                }
                            }
                        }

                        // Group selection
                        if (uiState.targetType == "group" && uiState.availableGroups.isNotEmpty()) {
                            Text("Группа:", style = MaterialTheme.typography.labelMedium, color = subtextColor)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                uiState.availableGroups.forEach { group ->
                                    val isSelected = uiState.selectedGroupId == group.id
                                    Surface(
                                        onClick = { viewModel.selectGroup(group.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) BrandBlue.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(group.name, style = MaterialTheme.typography.bodySmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) BrandBlue else textColor)
                                            if (isSelected) Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = BrandBlue)
                                        }
                                    }
                                }
                            }
                        }

                        // User selection
                        if (uiState.targetType == "user" && uiState.availableUsers.isNotEmpty()) {
                            Text("Пользователь:", style = MaterialTheme.typography.labelMedium, color = subtextColor)
                            Column(Modifier.heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                uiState.availableUsers.forEach { user ->
                                    val isSelected = uiState.selectedUserId == user.id
                                    val roleLabel = when (user.role.lowercase()) {
                                        "admin" -> "Админ"; "teacher" -> "Учитель"
                                        "student" -> "Студент"; "user" -> "Пользователь"
                                        else -> user.role
                                    }
                                    val roleColor = when (user.role.lowercase()) {
                                        "admin" -> BrandCoral; "teacher" -> BrandIndigo
                                        "student" -> BrandTeal; "user" -> BrandGold
                                        else -> BrandBlue
                                    }
                                    Surface(
                                        onClick = { viewModel.selectUser(user.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) BrandBlue.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    user.displayName.ifBlank { user.email },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) BrandBlue else textColor,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                                )
                                                Text(user.email, style = MaterialTheme.typography.labelSmall, color = subtextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            AdminStatusTag(roleLabel, roleColor)
                                            if (isSelected) Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = BrandBlue)
                                        }
                                    }
                                }
                            }
                        }

                        // Success message
                        uiState.successMessage?.let { msg ->
                            Surface(color = BrandGreen.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = BrandGreen)
                                    Text(msg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = BrandGreen)
                                }
                            }
                        }

                        // Send button
                        val canSend = uiState.title.isNotBlank() && uiState.body.isNotBlank() && !uiState.isSending && when (uiState.targetType) {
                            "role" -> uiState.selectedRole != null
                            "group" -> uiState.selectedGroupId != null
                            "user" -> uiState.selectedUserId != null
                            else -> true
                        }
                        Button(
                            onClick = { viewModel.sendNotification() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = canSend,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        ) {
                            if (uiState.isSending) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Отправить уведомление", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}
