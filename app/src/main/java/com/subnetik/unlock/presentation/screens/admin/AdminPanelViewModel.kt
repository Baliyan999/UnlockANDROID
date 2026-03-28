package com.subnetik.unlock.presentation.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminPanelUiState(
    val selectedSection: AdminSection = AdminSection.LEADS,
    val isDarkTheme: Boolean? = true,
    val refreshTrigger: Int = 0,
)

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPanelUiState())
    val uiState: StateFlow<AdminPanelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    fun selectSection(section: AdminSection) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun refresh() {
        _uiState.update { it.copy(refreshTrigger = it.refreshTrigger + 1) }
    }
}
