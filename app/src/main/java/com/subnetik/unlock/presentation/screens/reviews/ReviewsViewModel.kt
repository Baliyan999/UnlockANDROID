package com.subnetik.unlock.presentation.screens.reviews

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.AuthDataStore
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.ReviewsApi
import com.subnetik.unlock.data.remote.dto.reviews.PublicReviewDto
import com.subnetik.unlock.data.remote.dto.reviews.SubmitReviewRequest
import com.subnetik.unlock.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewsUiState(
    val reviews: List<PublicReviewDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class ReviewFormState(
    val showForm: Boolean = false,
    val author: String = "",
    val text: String = "",
    val rating: Int = 0,
    val isStudent: Boolean = false,
    val imageUri: Uri? = null,
    val imageBase64: String? = null,
    val imageError: String? = null,
    val isSubmitting: Boolean = false,
    val showValidation: Boolean = false,
    val submitError: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewsApi: ReviewsApi,
    private val authDataStore: AuthDataStore,
    settingsDataStore: SettingsDataStore,
) : ViewModel() {

    val isDarkTheme = settingsDataStore.isDarkTheme

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ReviewFormState())
    val formState: StateFlow<ReviewFormState> = _formState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val reviews = reviewsApi.getPublicReviews()
                _uiState.value = _uiState.value.copy(reviews = reviews, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ErrorMapper.map(e),
                )
            }
        }
    }

    // ─── Review Form ────────────────────────────────────────────

    fun openReviewForm() {
        viewModelScope.launch {
            val displayName = authDataStore.displayName.first()
            val email = authDataStore.email.first()
            val defaultAuthor = when {
                !displayName.isNullOrBlank() -> displayName.trim()
                !email.isNullOrBlank() -> email.substringBefore("@")
                else -> ""
            }
            _formState.value = ReviewFormState(showForm = true, author = defaultAuthor)
        }
    }

    fun closeReviewForm() {
        _formState.value = ReviewFormState()
    }

    fun onAuthorChange(value: String) {
        if (value.length <= 100) {
            _formState.value = _formState.value.copy(author = value)
        }
    }

    fun onTextChange(value: String) {
        if (value.length <= 1000) {
            _formState.value = _formState.value.copy(text = value)
        }
    }

    fun onRatingChange(value: Int) {
        _formState.value = _formState.value.copy(rating = value)
    }

    fun onIsStudentChange(value: Boolean) {
        _formState.value = _formState.value.copy(isStudent = value)
    }

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver

                // Validate MIME type
                val mimeType = contentResolver.getType(uri)
                if (mimeType != "image/jpeg" && mimeType != "image/png") {
                    _formState.value = _formState.value.copy(
                        imageError = "Поддерживаются только JPG и PNG"
                    )
                    return@launch
                }

                // Read bytes and validate size
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    _formState.value = _formState.value.copy(
                        imageError = "Не удалось прочитать файл"
                    )
                    return@launch
                }

                val maxSize = 2 * 1024 * 1024 // 2 MB
                if (bytes.size > maxSize) {
                    _formState.value = _formState.value.copy(
                        imageError = "Файл слишком большой (макс. 2 МБ)"
                    )
                    return@launch
                }

                // Encode to base64 data URL
                val encoded = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val dataUrl = "data:$mimeType;base64,$encoded"

                _formState.value = _formState.value.copy(
                    imageUri = uri,
                    imageBase64 = dataUrl,
                    imageError = null,
                )
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    imageError = "Ошибка при загрузке изображения"
                )
            }
        }
    }

    fun clearImage() {
        _formState.value = _formState.value.copy(
            imageUri = null,
            imageBase64 = null,
            imageError = null,
        )
    }

    fun submitReview() {
        _formState.value = _formState.value.copy(showValidation = true)
        val state = _formState.value
        val trimmedAuthor = state.author.trim()
        val trimmedText = state.text.trim()

        // Validate
        if (trimmedAuthor.isEmpty() || trimmedText.isEmpty() || trimmedText.length < 10 || state.rating == 0) {
            return
        }
        if (state.isSubmitting) return

        _formState.value = state.copy(isSubmitting = true, submitError = null)

        viewModelScope.launch {
            try {
                val request = SubmitReviewRequest(
                    author = trimmedAuthor,
                    text = trimmedText,
                    rating = state.rating,
                    isStudent = state.isStudent,
                    imageUrl = state.imageBase64,
                )
                reviewsApi.submitReview(request)
                _formState.value = _formState.value.copy(isSubmitting = false, isSuccess = true)
                loadReviews()
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSubmitting = false,
                    submitError = ErrorMapper.map(e),
                )
            }
        }
    }
}
