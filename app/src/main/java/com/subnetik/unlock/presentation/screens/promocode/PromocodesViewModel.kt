package com.subnetik.unlock.presentation.screens.promocode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.BuildConfig
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.MarketApi
import com.subnetik.unlock.data.remote.dto.admin.CouponRedeemRequest
import com.subnetik.unlock.data.remote.dto.admin.CouponValidateRequest
import com.subnetik.unlock.data.remote.dto.admin.PromocodeRedemptionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PromocodesUiState(
    val code: String = "",
    val isValidating: Boolean = false,
    val statusMessage: String? = null,
    val isSuccess: Boolean = false,
    val redemptions: List<PromocodeRedemptionResponse> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val isDarkTheme: Boolean? = true,
)

@HiltViewModel
class PromocodesViewModel @Inject constructor(
    private val marketApi: MarketApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromocodesUiState())
    val uiState: StateFlow<PromocodesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadRedemptions()
    }

    fun onCodeChange(code: String) {
        _uiState.update { it.copy(code = code, statusMessage = null) }
    }

    fun validateAndRedeem() {
        val code = _uiState.value.code.trim().uppercase()
        if (code.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, statusMessage = null, isSuccess = false) }
            try {
                val validateResponse = marketApi.validateCoupon(
                    CouponValidateRequest(code = code, context = "any")
                )
                if (!validateResponse.valid) {
                    _uiState.update { it.copy(isValidating = false, statusMessage = validateResponse.message, isSuccess = false) }
                    return@launch
                }

                val redeemResponse = marketApi.redeemCoupon(
                    CouponRedeemRequest(code = code, context = "any")
                )
                _uiState.update { it.copy(
                    isValidating = false,
                    statusMessage = redeemResponse.message,
                    isSuccess = redeemResponse.success,
                    code = if (redeemResponse.success) "" else code,
                ) }

                if (redeemResponse.success) loadRedemptions()
            } catch (e: Exception) {
                _uiState.update { it.copy(isValidating = false, statusMessage = "Ошибка: ${e.message}", isSuccess = false) }
            }
        }
    }

    private fun loadRedemptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true) }
            try {
                val wrapper = marketApi.getMyRedemptions()
                if (BuildConfig.DEBUG) android.util.Log.d("PromoVM", "Loaded ${wrapper.redemptions.size} redemptions, total=${wrapper.total}")
                _uiState.update { it.copy(redemptions = wrapper.redemptions, isLoadingHistory = false) }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.e("PromoVM", "Failed to load redemptions", e)
                _uiState.update { it.copy(isLoadingHistory = false) }
            }
        }
    }
}
