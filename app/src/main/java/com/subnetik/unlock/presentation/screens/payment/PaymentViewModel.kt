package com.subnetik.unlock.presentation.screens.payment

import android.content.Context
import android.net.Uri
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.subnetik.unlock.util.ErrorMapper
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean? = true,
    val isStudent: Boolean = true,
    val paymentInfo: StudentPaymentInfoResponse? = null,
    val uploadSuccess: Boolean = false,
    val uploadError: String? = null,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentApi: PaymentApi,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
        // isStudent defaults to true, profile screen only shows Paynet for students
    }

    fun uploadReceipt(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, uploadSuccess = false, uploadError = null) }
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val bytes = inputStream.readBytes()
                inputStream.close()

                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                val part = MultipartBody.Part.createFormData("receipt", "receipt.jpg", requestBody)

                paymentApi.uploadReceipt(part)
                _uiState.update { it.copy(isLoading = false, uploadSuccess = true) }
            } catch (e: Exception) {
                android.util.Log.e("PaymentVM", "Upload failed", e)
                _uiState.update { it.copy(isLoading = false, uploadError = ErrorMapper.map(e, ErrorMapper.ErrorContext.PAYMENT)) }
            }
        }
    }
}
