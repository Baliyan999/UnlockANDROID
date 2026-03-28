package com.subnetik.unlock.presentation.screens.lead

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.LeadApi
import com.subnetik.unlock.data.remote.api.MarketApi
import com.subnetik.unlock.data.remote.dto.admin.CouponValidateRequest
import com.subnetik.unlock.data.remote.dto.lead.LeadCreateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.subnetik.unlock.util.ErrorMapper
import javax.inject.Inject

data class LeadUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val comment: String = "",
    val promoCode: String = "",
    val selectedLevel: String = "Не знаю свой уровень",
    val selectedFormat: String = "Группа",
    val showErrors: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null,
    val promoMessage: String? = null,
    val promoIsSuccess: Boolean = false,
    val promoBonusDescription: String? = null,
    val isValidatingPromo: Boolean = false,
)

@HiltViewModel
class LeadViewModel @Inject constructor(
    private val leadApi: LeadApi,
    private val marketApi: MarketApi,
    settingsDataStore: SettingsDataStore,
) : ViewModel() {

    val isDarkTheme = settingsDataStore.isDarkTheme

    private val _uiState = MutableStateFlow(LeadUiState())
    val uiState: StateFlow<LeadUiState> = _uiState.asStateFlow()

    companion object {
        val levels = listOf(
            "Не знаю свой уровень",
            "Начинающий",
            "HSK1",
            "HSK2",
            "HSK3",
            "HSK4",
            "HSK5",
            "HSK6",
        )

        val formats = listOf(
            "Группа",
            "Индивидуально",
            "Интенсив",
        )
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onCommentChange(value: String) {
        _uiState.update { it.copy(comment = value) }
    }

    fun onPromoCodeChange(value: String) {
        _uiState.update { it.copy(promoCode = value) }
    }

    fun onLevelChange(value: String) {
        _uiState.update { it.copy(selectedLevel = value) }
    }

    fun onFormatChange(value: String) {
        _uiState.update { it.copy(selectedFormat = value) }
    }

    fun applyPromoCode() {
        val code = _uiState.value.promoCode.trim()
        if (code.isEmpty()) {
            _uiState.update {
                it.copy(
                    promoMessage = "Введите промокод",
                    promoIsSuccess = false,
                    promoBonusDescription = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isValidatingPromo = true) }
            try {
                val response = marketApi.validateCoupon(
                    CouponValidateRequest(code = code, context = "lead"),
                )
                if (response.valid) {
                    _uiState.update {
                        it.copy(
                            isValidatingPromo = false,
                            promoMessage = "Промокод применён",
                            promoIsSuccess = true,
                            promoBonusDescription = response.bonusDescription.ifBlank { null },
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isValidatingPromo = false,
                            promoMessage = response.message ?: "Промокод недействителен",
                            promoIsSuccess = false,
                            promoBonusDescription = null,
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isValidatingPromo = false,
                        promoMessage = "Ошибка проверки промокода",
                        promoIsSuccess = false,
                        promoBonusDescription = null,
                    )
                }
            }
        }
    }

    fun submitForm() {
        _uiState.update { it.copy(showErrors = true) }
        val state = _uiState.value
        val trimmedName = state.name.trim()
        val trimmedPhone = state.phone.trim()
        if (trimmedName.isEmpty() || trimmedPhone.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            try {
                leadApi.createLead(
                    LeadCreateRequest(
                        name = trimmedName,
                        email = state.email.ifBlank { null },
                        phone = trimmedPhone,
                        message = state.comment.ifBlank { null },
                        languageLevel = state.selectedLevel,
                        format = state.selectedFormat,
                        promocode = state.promoCode.ifBlank { null },
                        source = "lead",
                    ),
                )
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = ErrorMapper.map(e),
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(submitSuccess = false) }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(submitError = null) }
    }
}
