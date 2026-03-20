package com.subnetik.unlock.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.dto.auth.AuthLoginResponse
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val loginEmail: String = "",
    val loginPassword: String = "",
    val registerName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val referralCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val authSuccess: Boolean = false,
    val needs2FA: Boolean = false,
    val needs2FAEmail: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginEmailChange(value: String) = _uiState.update { it.copy(loginEmail = value, error = null) }
    fun onLoginPasswordChange(value: String) = _uiState.update { it.copy(loginPassword = value, error = null) }
    fun onRegisterNameChange(value: String) = _uiState.update { it.copy(registerName = value, error = null) }
    fun onRegisterEmailChange(value: String) = _uiState.update { it.copy(registerEmail = value, error = null) }
    fun onRegisterPasswordChange(value: String) = _uiState.update { it.copy(registerPassword = value, error = null) }
    fun onRegisterConfirmPasswordChange(value: String) = _uiState.update { it.copy(registerConfirmPassword = value, error = null) }
    fun onReferralCodeChange(value: String) = _uiState.update { it.copy(referralCode = value) }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(_uiState.value.loginEmail, _uiState.value.loginPassword)) {
                is Resource.Success -> {
                    val response = result.data
                    if (response.requires2FA == true) {
                        _uiState.update {
                            it.copy(isLoading = false, needs2FA = true, needs2FAEmail = response.email ?: it.loginEmail)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.registerPassword != state.registerConfirmPassword) {
            _uiState.update { it.copy(error = "Пароли не совпадают") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(
                email = state.registerEmail,
                password = state.registerPassword,
                displayName = state.registerName,
                referralCode = state.referralCode.ifBlank { null },
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.verifyCode(email, code)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, authSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingSeen()
        }
    }

    fun resetAuthSuccess() = _uiState.update { it.copy(authSuccess = false) }
    fun reset2FA() = _uiState.update { it.copy(needs2FA = false, needs2FAEmail = null) }
}
