package com.subnetik.unlock.presentation.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.domain.model.Resource
import com.subnetik.unlock.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.subnetik.unlock.util.ErrorMapper
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String? = null,
    val email: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isDarkTheme: Boolean? = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getDisplayName().collect { name ->
                _uiState.update { it.copy(displayName = name) }
            }
        }
        viewModelScope.launch {
            authRepository.getUserEmail().collect { email ->
                _uiState.update { it.copy(email = email) }
            }
        }
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _uiState.update { it.copy(role = role.name) }
            }
        }
        viewModelScope.launch {
            authRepository.getAvatarUrl().collect { url ->
                _uiState.update { it.copy(avatarUrl = url) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        // Refresh avatar from server (DataStore may be stale)
        refreshAvatarFromServer()
    }

    private fun refreshAvatarFromServer() {
        viewModelScope.launch {
            try {
                when (val result = authRepository.getProfile()) {
                    is Resource.Success -> {
                        val serverAvatar = result.data.avatar
                        _uiState.update { it.copy(avatarUrl = serverAvatar) }
                    }
                    else -> {}
                }
            } catch (_: Exception) {}
        }
    }

    fun updateProfile(
        displayName: String?,
        email: String?,
        currentPassword: String?,
        newPassword: String?,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            when (val result = authRepository.updateProfile(
                displayName = displayName,
                email = email,
                currentPassword = currentPassword,
                newPassword = newPassword,
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            displayName = result.data.displayName,
                            email = result.data.email,
                            successMessage = "Профиль успешно обновлён",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkTheme(isDark)
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null || bytes.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "Не удалось прочитать файл") }
                    return@launch
                }
                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                when (val result = authRepository.uploadAvatar(bytes, fileName)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                avatarUrl = result.data,
                                successMessage = "Аватар обновлён",
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = ErrorMapper.map(e, ErrorMapper.ErrorContext.PROFILE)) }
            }
        }
    }

    fun deleteAvatar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            when (val result = authRepository.deleteAvatar()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            avatarUrl = null,
                            successMessage = "Аватар удалён",
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
