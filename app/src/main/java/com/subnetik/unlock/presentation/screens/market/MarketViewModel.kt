package com.subnetik.unlock.presentation.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.MarketApi
import com.subnetik.unlock.data.remote.dto.market.MarketItemResponse
import com.subnetik.unlock.data.remote.dto.market.MarketPurchaseRequest
import com.subnetik.unlock.data.remote.dto.market.MarketPurchaseResponse
import com.subnetik.unlock.data.remote.dto.admin.CouponValidateRequest
import com.subnetik.unlock.data.remote.dto.admin.CouponRedeemRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean? = null,
    val balance: Int = 0,
    val items: List<MarketItemResponse> = emptyList(),
    val purchases: List<MarketPurchaseResponse> = emptyList(),
    val buyingCodes: Set<String> = emptySet(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val showHistory: Boolean = false,
    val showCouponSection: Boolean = false,
    val couponCode: String = "",
    val couponValidating: Boolean = false,
    val couponRedeeming: Boolean = false,
    val couponMessage: String? = null,
    val couponSuccess: Boolean = false,
    val couponBonusDescription: String? = null,
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketApi: MarketApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val walletD = async { marketApi.getWallet() }
                val itemsD = async { marketApi.getMarketItems() }
                val purchasesD = async { runCatching { marketApi.getMyPurchases() }.getOrDefault(emptyList()) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        balance = walletD.await().balance,
                        items = itemsD.await(),
                        purchases = purchasesD.await(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun buyItem(itemCode: String, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(buyingCodes = it.buyingCodes + itemCode, errorMessage = null, successMessage = null) }
            try {
                marketApi.buyItem(MarketPurchaseRequest(itemCode, quantity))
                _uiState.update { it.copy(buyingCodes = it.buyingCodes - itemCode, successMessage = "Покупка оформлена!") }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(buyingCodes = it.buyingCodes - itemCode, errorMessage = e.message) }
            }
        }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }

    fun toggleCouponSection() {
        _uiState.update { it.copy(showCouponSection = !it.showCouponSection, couponCode = "", couponMessage = null, couponSuccess = false, couponBonusDescription = null) }
    }

    fun updateCouponCode(code: String) {
        _uiState.update { it.copy(couponCode = code, couponMessage = null, couponSuccess = false) }
    }

    fun validateAndRedeemCoupon() {
        val code = _uiState.value.couponCode.trim().uppercase()
        if (code.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(couponValidating = true, couponMessage = null, couponSuccess = false) }
            try {
                val validateResponse = marketApi.validateCoupon(CouponValidateRequest(code = code, context = "wallet"))
                if (validateResponse.valid) {
                    _uiState.update { it.copy(couponValidating = false, couponRedeeming = true, couponBonusDescription = validateResponse.bonusDescription) }
                    val redeemResponse = marketApi.redeemCoupon(CouponRedeemRequest(code = code, context = "wallet"))
                    if (redeemResponse.success) {
                        _uiState.update { it.copy(couponRedeeming = false, couponSuccess = true, couponMessage = redeemResponse.message, couponCode = "") }
                        loadData() // refresh balance
                    } else {
                        _uiState.update { it.copy(couponRedeeming = false, couponMessage = redeemResponse.message) }
                    }
                } else {
                    _uiState.update { it.copy(couponValidating = false, couponMessage = validateResponse.message ?: "Купон недействителен") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(couponValidating = false, couponRedeeming = false, couponMessage = e.message ?: "Ошибка") }
            }
        }
    }
}
