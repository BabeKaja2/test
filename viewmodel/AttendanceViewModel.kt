package com.babetech.ucb_admin_access.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babetech.ucb_admin_access.domain.usecase.AttendanceFilter
import com.babetech.ucb_admin_access.domain.usecase.AttendanceRecord
import com.babetech.ucb_admin_access.domain.usecase.GetFilteredAttendanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AttendanceUiState(
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val availablePromotions: List<String> = emptyList(),
    val availableFacultes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val selectedPromotion: String? = null,
    val selectedFaculte: String? = null
)

class AttendanceViewModel(
    private val getFilteredAttendanceUseCase: GetFilteredAttendanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            loadAvailableFilters()
            loadAttendanceForToday()
        }
    }

    private suspend fun loadAvailableFilters() {
        try {
            val promotions = getFilteredAttendanceUseCase.getAvailablePromotions()
            val facultes = getFilteredAttendanceUseCase.getAvailableFacultes()
            
            _uiState.value = _uiState.value.copy(
                availablePromotions = promotions,
                availableFacultes = facultes
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Erreur lors du chargement des filtres: ${e.message}"
            )
        }
    }

    private suspend fun loadAttendanceForToday() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadAttendance(AttendanceFilter(date = today))
    }

    fun updateDateFilter(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        applyFilters()
    }

    fun updatePromotionFilter(promotion: String?) {
        _uiState.value = _uiState.value.copy(selectedPromotion = promotion)
        applyFilters()
    }

    fun updateFaculteFilter(faculte: String?) {
        _uiState.value = _uiState.value.copy(selectedFaculte = faculte)
        applyFilters()
    }

    fun applyFilters() {
        val currentState = _uiState.value
        val filter = AttendanceFilter(
            date = currentState.selectedDate,
            promotion = currentState.selectedPromotion,
            faculte = currentState.selectedFaculte
        )
        
        viewModelScope.launch {
            loadAttendance(filter)
        }
    }

    private suspend fun loadAttendance(filter: AttendanceFilter) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        getFilteredAttendanceUseCase.execute(filter)
            .onSuccess { records ->
                _uiState.value = _uiState.value.copy(
                    attendanceRecords = records,
                    isLoading = false
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${error.message}"
                )
            }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refresh() {
        applyFilters()
    }
}