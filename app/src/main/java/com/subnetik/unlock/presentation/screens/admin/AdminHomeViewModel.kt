package com.subnetik.unlock.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.*
import com.subnetik.unlock.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AdminHomeUiState(
    val displayName: String? = null,
    val isLoading: Boolean = true,
    val groups: List<AdminGroup> = emptyList(),
    val leads: List<AdminLead> = emptyList(),
    val users: List<AdminUser> = emptyList(),
    val leadStats: AdminLeadStats? = null,
    val promocodeStats: AdminPromocodeStats? = null,
    val isDarkTheme: Boolean? = null,
)

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    private val adminApi: AdminApi,
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getDisplayName().collect { name ->
                _uiState.update { it.copy(displayName = name) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val groupsD = async { runCatching { adminApi.getGroups() }.getOrDefault(emptyList()) }
                val leadsD = async { runCatching { adminApi.getLeads() }.getOrDefault(emptyList()) }
                val usersD = async { runCatching { adminApi.getUsers() }.getOrDefault(emptyList()) }
                val leadStatsD = async { runCatching { adminApi.getLeadStats() }.getOrNull() }
                val promoStatsD = async { runCatching { adminApi.getPromocodeStats() }.getOrNull() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groups = groupsD.await(),
                        leads = leadsD.await(),
                        users = usersD.await(),
                        leadStats = leadStatsD.await(),
                        promocodeStats = promoStatsD.await(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Day-of-week helpers
    companion object {
        private val DAY_TOKENS = mapOf(
            Calendar.MONDAY to "пн",
            Calendar.TUESDAY to "вт",
            Calendar.WEDNESDAY to "ср",
            Calendar.THURSDAY to "чт",
            Calendar.FRIDAY to "пт",
            Calendar.SATURDAY to "сб",
            Calendar.SUNDAY to "вс",
        )

        private val DAY_SHORT_TITLES = mapOf(
            Calendar.MONDAY to "Пн",
            Calendar.TUESDAY to "Вт",
            Calendar.WEDNESDAY to "Ср",
            Calendar.THURSDAY to "Чт",
            Calendar.FRIDAY to "Пт",
            Calendar.SATURDAY to "Сб",
            Calendar.SUNDAY to "Вс",
        )

        fun todayToken(): String {
            val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return DAY_TOKENS[dow] ?: "пн"
        }

        fun todayShortTitle(): String {
            val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            return DAY_SHORT_TITLES[dow] ?: "Пн"
        }

        fun matchesSchedule(scheduleDays: String?, token: String): Boolean {
            if (scheduleDays.isNullOrEmpty()) return false
            return scheduleDays.lowercase().contains(token)
        }

        fun parseStartTime(time: String?): String {
            if (time == null) return "99:99"
            return time.take(5)
        }

        fun parseTimeRange(time: String?): Pair<String, String> {
            if (time == null) return "--:--" to "--:--"
            val parts = time.split("-")
            val start = parts.firstOrNull()?.trim() ?: "--:--"
            val end = if (parts.size > 1) parts[1].trim() else "--:--"
            return start to end
        }
    }
}
