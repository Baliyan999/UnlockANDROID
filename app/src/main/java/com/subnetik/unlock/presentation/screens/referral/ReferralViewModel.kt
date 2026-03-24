package com.subnetik.unlock.presentation.screens.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.AuthApi
import com.subnetik.unlock.data.remote.dto.auth.ReferralInfoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferralUiState(
    val isDarkTheme: Boolean? = null,
    val isLoading: Boolean = true,
    val referralInfo: ReferralInfoResponse? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadReferralInfo()
    }

    private fun loadReferralInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val info = authApi.getReferralInfo()
                _uiState.update { it.copy(isLoading = false, referralInfo = info) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Ошибка загрузки") }
            }
        }
    }
}
