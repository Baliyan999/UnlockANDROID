package com.subnetik.unlock.presentation.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.local.datastore.SettingsDataStore
import com.subnetik.unlock.data.remote.api.ReviewsApi
import com.subnetik.unlock.data.remote.dto.reviews.PublicReviewDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.subnetik.unlock.util.ErrorMapper
import javax.inject.Inject

data class ReviewsUiState(
    val reviews: List<PublicReviewDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewsApi: ReviewsApi,
    settingsDataStore: SettingsDataStore,
) : ViewModel() {

    val isDarkTheme = settingsDataStore.isDarkTheme

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

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
}
