package com.edwin.bekal.presentation.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.BranchResponse
import com.edwin.bekal.data.dto.CreateLoanApplicationRequest
import com.edwin.bekal.data.dto.IdRef
import com.edwin.bekal.data.dto.LoanApplicationResponse
import com.edwin.bekal.data.dto.PlafondResponse
import com.edwin.bekal.data.loan.remote.BranchApi
import com.edwin.bekal.data.loan.remote.LoanApplicationApi
import com.edwin.bekal.data.loan.remote.PlafondApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class LoanApplicationViewModel @Inject constructor(
    private val api: LoanApplicationApi,
    private val plafondApi: PlafondApi,
    private val branchApi: BranchApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoanApplicationUiState>(
        LoanApplicationUiState.Idle
    )
    val uiState: StateFlow<LoanApplicationUiState> = _uiState

    private val _historyState = MutableStateFlow<LoanHistoryUiState>(
        LoanHistoryUiState.Loading
    )
    val historyState: StateFlow<LoanHistoryUiState> = _historyState

    private val _plafondState = MutableStateFlow<PlafondUiState>(PlafondUiState.Loading)
    val plafondState: StateFlow<PlafondUiState> = _plafondState

    private val _branchState = MutableStateFlow<BranchUiState>(BranchUiState.Loading)
    val branchState: StateFlow<BranchUiState> = _branchState

    // Form inputs
    val amountRequested = MutableStateFlow("")
    val tenorMonths = MutableStateFlow("")
    val purpose = MutableStateFlow("")
    val selectedBranchId = MutableStateFlow<String?>(null)

    fun loadActivePlafond() {
        viewModelScope.launch {
            _plafondState.value = PlafondUiState.Loading
            try {
                val customerId = authRepository.observeSession().first()?.user?.id
                if (customerId.isNullOrBlank()) {
                    _plafondState.value = PlafondUiState.Error("Sesi tidak valid")
                    return@launch
                }
                val plafond = plafondApi.getActivePlafond(customerId)
                _plafondState.value = PlafondUiState.Success(plafond)
            } catch (e: Exception) {
                // Customer belum punya plafond aktif — bukan error fatal, sembunyikan tombol ajukan
                _plafondState.value = PlafondUiState.NotAvailable
            }
        }
    }

    fun loadBranches() {
        viewModelScope.launch {
            _branchState.value = BranchUiState.Loading
            try {
                val branches = branchApi.getActiveBranches()
                _branchState.value = BranchUiState.Success(branches)
            } catch (e: Exception) {
                _branchState.value = BranchUiState.Error(e.message ?: "Gagal memuat daftar cabang")
            }
        }
    }

    fun submitLoanApplication(
        plafondId: String,
        amountRequested: BigDecimal,
        tenorMonths: Int,
        purpose: String?
    ) {
        viewModelScope.launch {
            val branchId = selectedBranchId.value
            if (branchId.isNullOrBlank()) {
                _uiState.value = LoanApplicationUiState.Error("Pilih cabang terlebih dahulu")
                return@launch
            }

            _uiState.value = LoanApplicationUiState.Loading
            try {
                val session = authRepository.observeSession().first()
                val customerId = session?.user?.id

                if (customerId.isNullOrBlank()) {
                    _uiState.value = LoanApplicationUiState.Error("Sesi tidak valid, silakan login ulang")
                    return@launch
                }

                val request = CreateLoanApplicationRequest(
                    customer = IdRef(customerId),
                    branch = IdRef(branchId),
                    plafond = IdRef(plafondId),
                    amountRequested = amountRequested,
                    tenorMonths = tenorMonths,
                    purpose = purpose
                )
                val response = api.createLoanApplication(request)
                _uiState.value = LoanApplicationUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = LoanApplicationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadLoanHistory() {
        viewModelScope.launch {
            _historyState.value = LoanHistoryUiState.Loading
            try {
                val customerId = authRepository.observeSession().first()?.user?.id
                if (customerId.isNullOrBlank()) {
                    _historyState.value = LoanHistoryUiState.Error("Sesi tidak valid, silakan login ulang")
                    return@launch
                }
                val response = api.getLoanApplicationHistory(customerId)
                _historyState.value = LoanHistoryUiState.Success(response.content)
            } catch (e: Exception) {
                _historyState.value = LoanHistoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetFormState() {
        amountRequested.value = ""
        tenorMonths.value = ""
        purpose.value = ""
        selectedBranchId.value = null
        _uiState.value = LoanApplicationUiState.Idle
    }
}

sealed class LoanApplicationUiState {
    object Idle : LoanApplicationUiState()
    object Loading : LoanApplicationUiState()
    data class Success(val response: LoanApplicationResponse) : LoanApplicationUiState()
    data class Error(val message: String) : LoanApplicationUiState()
}

sealed class LoanHistoryUiState {
    object Loading : LoanHistoryUiState()
    data class Success(val applications: List<LoanApplicationResponse>) : LoanHistoryUiState()
    data class Error(val message: String) : LoanHistoryUiState()
}

sealed class PlafondUiState {
    object Loading : PlafondUiState()
    object NotAvailable : PlafondUiState()
    data class Success(val plafond: PlafondResponse) : PlafondUiState()
    data class Error(val message: String) : PlafondUiState()
}

sealed class BranchUiState {
    object Loading : BranchUiState()
    data class Success(val branches: List<BranchResponse>) : BranchUiState()
    data class Error(val message: String) : BranchUiState()
}