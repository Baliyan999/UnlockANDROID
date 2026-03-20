package com.subnetik.unlock.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.StudentApi
import com.subnetik.unlock.data.remote.dto.student.StudentScheduleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentScheduleUiState(
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean? = null,
    val schedule: StudentScheduleData? = null,
    val error: String? = null,
)

@HiltViewModel
class StudentScheduleViewModel @Inject constructor(
    private val studentApi: StudentApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentScheduleUiState())
    val uiState: StateFlow<StudentScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val schedule = studentApi.getSchedule()
                _uiState.update { it.copy(isLoading = false, schedule = schedule) }
            } catch (e: Exception) {
                android.util.Log.e("ScheduleVM", "Failed to load schedule", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
