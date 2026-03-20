package com.subnetik.unlock.presentation.screens.admin.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnetik.unlock.data.remote.api.AdminApi
import com.subnetik.unlock.data.remote.dto.admin.AdminLead
import com.subnetik.unlock.data.remote.dto.admin.AdminLeadStats
import com.subnetik.unlock.data.remote.dto.admin.AdminLeadUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LeadFilter { PENDING, PROCESSED, DELETED }

data class AdminLeadsUiState(
    val isLoading: Boolean = true,
    val leads: List<AdminLead> = emptyList(),
    val leadStats: AdminLeadStats? = null,
    val selectedFilter: LeadFilter = LeadFilter.PENDING,
    val error: String? = null,
    val actionInProgress: Int? = null,
    val noteDialogLeadId: Int? = null,
    val noteText: String = "",
) {
    val filteredLeads: List<AdminLead>
        get() {
            val statusFilter = when (selectedFilter) {
                LeadFilter.PENDING -> "pending"
                LeadFilter.PROCESSED -> "processed"
                LeadFilter.DELETED -> "deleted"
            }
            return leads.filter { it.status.lowercase() == statusFilter }
        }
}

@HiltViewModel
class AdminLeadsViewModel @Inject constructor(
    private val adminApi: AdminApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminLeadsUiState())
    val uiState: StateFlow<AdminLeadsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val leads = adminApi.getLeads()
                val stats = adminApi.getLeadStats()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        leads = leads,
                        leadStats = stats,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
                }
            }
        }
    }

    fun selectFilter(filter: LeadFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun processLead(leadId: Int) {
        updateLeadStatus(leadId, "processed")
    }

    fun returnLead(leadId: Int) {
        updateLeadStatus(leadId, "pending")
    }

    fun softDeleteLead(leadId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = leadId) }
            try {
                adminApi.deleteLead(leadId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, actionInProgress = null) }
            }
        }
    }

    fun hardDeleteLead(leadId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = leadId) }
            try {
                adminApi.hardDeleteLead(leadId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, actionInProgress = null) }
            }
        }
    }

    fun showNoteDialog(leadId: Int) {
        val lead = _uiState.value.leads.find { it.id == leadId }
        _uiState.update {
            it.copy(
                noteDialogLeadId = leadId,
                noteText = lead?.adminNote ?: "",
            )
        }
    }

    fun dismissNoteDialog() {
        _uiState.update { it.copy(noteDialogLeadId = null, noteText = "") }
    }

    fun updateNoteText(text: String) {
        _uiState.update { it.copy(noteText = text) }
    }

    fun saveNote() {
        val leadId = _uiState.value.noteDialogLeadId ?: return
        val noteText = _uiState.value.noteText
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = leadId) }
            try {
                adminApi.updateLead(leadId, AdminLeadUpdateRequest(adminNote = noteText))
                _uiState.update { it.copy(noteDialogLeadId = null, noteText = "") }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, actionInProgress = null) }
            }
        }
    }

    private fun updateLeadStatus(leadId: Int, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = leadId) }
            try {
                adminApi.updateLead(leadId, AdminLeadUpdateRequest(status = status))
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, actionInProgress = null) }
            }
        }
    }
}
