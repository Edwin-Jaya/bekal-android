package com.edwin.bekal.presentation.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.data.dto.LoanReviewDetail
import com.edwin.bekal.data.loan.remote.LoanApplicationApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoanDetailUiState {
    data object Loading : LoanDetailUiState
    data class Success(val loanDetail: LoanReviewDetail) : LoanDetailUiState
    data class Error(val message: String) : LoanDetailUiState
}

@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    private val loanApplicationApi: LoanApplicationApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoanDetailUiState>(LoanDetailUiState.Loading)
    val uiState: StateFlow<LoanDetailUiState> = _uiState.asStateFlow()

    fun loadLoanDetail(id: String) {
        viewModelScope.launch {
            _uiState.value = LoanDetailUiState.Loading

            try {
                val response = loanApplicationApi.getLoanApplicationDetail(id)
                _uiState.value = LoanDetailUiState.Success(response)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = LoanDetailUiState.Error(
                    message = e.localizedMessage ?: "Gagal memuat detail pinjaman."
                )
            }
        }
    }
}