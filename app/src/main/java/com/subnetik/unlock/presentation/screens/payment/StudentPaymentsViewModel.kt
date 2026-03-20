package com.subnetik.unlock.presentation.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.PaymentApi
import com.subnetik.unlock.data.remote.dto.payment.StudentPaymentInfoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentPaymentsUiState(
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean? = null,
    val paymentInfo: StudentPaymentInfoResponse? = null,
    val error: String? = null,
)

@HiltViewModel
class StudentPaymentsViewModel @Inject constructor(
    private val paymentApi: PaymentApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentPaymentsUiState())
    val uiState: StateFlow<StudentPaymentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val info = paymentApi.getMyPaymentInfo()
                _uiState.update { it.copy(isLoading = false, paymentInfo = info) }
            } catch (e: Exception) {
                android.util.Log.e("StudentPaymentsVM", "Failed to load", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
