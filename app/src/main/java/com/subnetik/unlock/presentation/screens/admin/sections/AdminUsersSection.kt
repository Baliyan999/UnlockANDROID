package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.AdminUser
import com.subnetik.unlock.data.remote.dto.admin.AdminUserUpdateRequest
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
class AdminUsersViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {
    data class UiState(
        val isLoading: Boolean = true,
        val users: List<AdminUser> = emptyList(),
        val selectedFilter: String = "all",
        val searchQuery: String = "",
        val error: String? = null,
        val roleDialogUserId: Int? = null,
        val roleDialogCurrentRole: String = "",
    ) {
        val filtered: List<AdminUser>
            get() {
                val byRole = when (selectedFilter) {
                    "all" -> users
                    "admin" -> users.filter { it.role.lowercase() == "admin" || it.role.lowercase() == "manager" }
                    else -> users.filter { it.role.lowercase() == selectedFilter }
                }
                return if (searchQuery.isBlank()) byRole
                else byRole.filter {
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
                }
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val users = adminApi.getUsers()
                _uiState.update { it.copy(isLoading = false, users = users) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectFilter(f: String) { _uiState.update { it.copy(selectedFilter = f) } }
    fun updateSearch(q: String) { _uiState.update { it.copy(searchQuery = q) } }

    fun showRoleDialog(userId: Int, currentRole: String) {
        _uiState.update { it.copy(roleDialogUserId = userId, roleDialogCurrentRole = currentRole) }
    }
    fun dismissRoleDialog() { _uiState.update { it.copy(roleDialogUserId = null) } }

    fun changeRole(userId: Int, newRole: String) {
        viewModelScope.launch {
            try {
                adminApi.updateUserRole(userId, AdminUserUpdateRequest(role = newRole))
                _uiState.update { it.copy(roleDialogUserId = null) }
                loadData()
            } catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersSection(isDark: Boolean, viewModel: AdminUsersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant

    // Role change dialog
    if (uiState.roleDialogUserId != null) {
        val user = uiState.users.find { it.id == uiState.roleDialogUserId }
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissRoleDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (isDark) Color(0xFF0D1120) else Color(0xFFF7F7FC),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md)) {
                Text("Изменить роль", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                user?.let { Text("${it.displayName.ifBlank { it.email }}", style = MaterialTheme.typography.bodySmall, color = subtextColor) }
                val roles = listOf("user" to "Пользователь", "student" to "Студент", "teacher" to "Учитель", "admin" to "Админ")
                roles.forEach { (role, label) ->
                    val isCurrentRole = role == uiState.roleDialogCurrentRole.lowercase()
                    val roleColor = when (role) { "admin" -> BrandCoral; "teacher" -> BrandIndigo; "user" -> BrandGold; else -> BrandTeal }
                    Surface(
                        onClick = {
                            if (!isCurrentRole) viewModel.changeRole(uiState.roleDialogUserId!!, role)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrentRole) roleColor.copy(alpha = 0.15f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isCurrentRole) FontWeight.Bold else FontWeight.Normal, color = if (isCurrentRole) roleColor else textColor)
                            if (isCurrentRole) Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = roleColor)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Brand.Spacing.lg, vertical = Brand.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Brand.Spacing.md),
    ) {
        item { AdminSectionHeader(Icons.Default.People, "Пользователи", isDark) }

        // Search bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearch(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по имени или email...") },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(20.dp)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearch("") }) {
                            Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White,
                    unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White,
                ),
            )
        }

        // Stats row
        item {
            val students = uiState.users.count { it.role.lowercase() == "student" }
            val teachers = uiState.users.count { it.role.lowercase() == "teacher" }
            val admins = uiState.users.count { it.role.lowercase() == "admin" || it.role.lowercase() == "manager" }
            val regularUsers = uiState.users.count { it.role.lowercase() == "user" }
            AdminFilterTabs(
                filters = listOf(
                    AdminFilter("all", "Все", uiState.users.size),
                    AdminFilter("student", "Студенты", students),
                    AdminFilter("teacher", "Учителя", teachers),
                    AdminFilter("admin", "Админы", admins),
                    AdminFilter("user", "Пользователи", regularUsers),
                ),
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.selectFilter(it) },
                isDark = isDark,
            )
        }

        uiState.error?.let { error -> item { AdminErrorBanner(error, isDark, onRetry = { viewModel.loadData() }) } }

        if (uiState.isLoading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = Brand.Spacing.xxxl), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(32.dp), color = BrandBlue, strokeWidth = 3.dp) } }
        } else if (uiState.filtered.isEmpty()) {
            item { AdminEmptyState(Icons.Default.People, "Нет пользователей", if (uiState.searchQuery.isNotBlank()) "Попробуйте другой запрос" else "Пользователи появятся здесь", isDark = isDark) }
        } else {
            items(uiState.filtered, key = { it.id }) { user ->
                AdminGlassCard(isDark = isDark) {
                    Column(Modifier.fillMaxWidth().padding(Brand.Spacing.lg), verticalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            // Avatar with fallback
                            val avatarUrl = user.avatar?.let { url ->
                                if (url.startsWith("http")) url else "https://unlocklingua.com$url"
                            }
                            val initial = (user.displayName.firstOrNull() ?: user.email.firstOrNull() ?: '?').uppercase()
                            if (avatarUrl != null) {
                                val isError = remember { mutableStateOf(false) }
                                if (!isError.value) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        onError = { isError.value = true },
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(initial, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandBlue)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(initial, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = BrandBlue)
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    user.displayName.ifBlank { "Без имени" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = subtextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            val roleColor = when (user.role.lowercase()) {
                                "admin", "manager" -> BrandCoral
                                "teacher" -> BrandIndigo
                                "student" -> BrandTeal
                                "user" -> BrandGold
                                else -> BrandBlue
                            }
                            val roleLabel = when (user.role.lowercase()) {
                                "admin" -> "Админ"
                                "manager" -> "Менеджер"
                                "teacher" -> "Учитель"
                                "student" -> "Студент"
                                "user" -> "Пользователь"
                                else -> user.role
                            }
                            AdminStatusTag(roleLabel, roleColor)
                        }
                        user.teacherName?.let { teacher ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.School, null, Modifier.size(14.dp), tint = subtextColor)
                                Text("Учитель: $teacher", style = MaterialTheme.typography.bodySmall, color = subtextColor)
                            }
                        }
                        user.lastLoginAt?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = subtextColor)
                                Text("Последний вход: ${it.take(10)}", style = MaterialTheme.typography.labelSmall, color = subtextColor)
                            }
                        }
                        // Actions
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        Row(horizontalArrangement = Arrangement.spacedBy(Brand.Spacing.sm)) {
                            AdminActionButton("Роль", Icons.Default.ManageAccounts, BrandIndigo, onClick = { viewModel.showRoleDialog(user.id, user.role) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(Brand.Spacing.xl)) }
    }
}
