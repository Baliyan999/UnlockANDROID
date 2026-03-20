package com.subnetik.unlock.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.StudentApi
import com.subnetik.unlock.data.remote.dto.student.StudentScheduleData
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.repository.AuthRepository
import com.subnetik.unlock.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val displayName: String? = null,
    val unreadCount: Int = 0,
    val tokenBalance: Int = 0,
    val isDarkTheme: Boolean? = null,
    val schedule: StudentScheduleData? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val studentApi: StudentApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
        loadUnreadCount()
        loadWallet()
        loadSchedule()
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is Resource.Success -> _uiState.update { it.copy(unreadCount = result.data) }
                else -> {}
            }
        }
    }

    private fun loadWallet() {
        viewModelScope.launch {
            try {
                val wallet = studentApi.getWallet()
                _uiState.update { it.copy(tokenBalance = wallet.balance) }
            } catch (_: Exception) {}
        }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            try {
                val schedule = studentApi.getSchedule()
                android.util.Log.d("HomeVM", "Schedule OK: group=${schedule.groupName}, days=${schedule.scheduleDays}, time=${schedule.scheduleTime}, dur=${schedule.lessonDurationMinutes}")
                _uiState.update { it.copy(schedule = schedule) }
            } catch (e: Exception) {
                android.util.Log.e("HomeVM", "Schedule FAIL: ${e::class.simpleName}: ${e.message}")
            }
        }
    }
}
