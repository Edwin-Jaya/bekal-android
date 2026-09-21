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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class LoanApplicationViewModel @Inject constructor(
    private val api: LoanApplicationApi,
    private val plafondApi: PlafondApi,
    private val branchApi: BranchApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Reactive Auth State observed by LoansScreen
    val isLoggedIn: StateFlow<Boolean> = authRepository.observeSession()
        .map { session -> session?.user?.id != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _uiState = MutableStateFlow<LoanApplicationUiState>(LoanApplicationUiState.Idle)
    val uiState: StateFlow<LoanApplicationUiState> = _uiState

    private val _historyState = MutableStateFlow<LoanHistoryUiState>(LoanHistoryUiState.Loading)
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

    init {
        loadBranches()
    }

    fun loadActivePlafond() {
        viewModelScope.launch {
            _plafondState.value = PlafondUiState.Loading
            try {
                val customerId = authRepository.observeSession().first()?.user?.id
                if (customerId.isNullOrBlank()) {
                    _plafondState.value = PlafondUiState.Unauthenticated
                    return@launch
                }
                val plafond = plafondApi.getActivePlafond(customerId)
                _plafondState.value = PlafondUiState.Success(plafond)
            } catch (e: HttpException) {
                _plafondState.value = when (e.code()) {
                    404 -> PlafondUiState.NotAvailable
                    401, 403 -> PlafondUiState.Unauthenticated
                    else -> PlafondUiState.Error("Gagal memuat plafond (HTTP ${e.code()})")
                }
            } catch (e: SerializationException) {
                _plafondState.value = PlafondUiState.Error("Format data plafond tidak sesuai")
            } catch (e: IOException) {
                _plafondState.value = PlafondUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                _plafondState.value = PlafondUiState.Error(e.message ?: "Gagal memuat plafond")
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

    fun loadLoanHistory() {
        viewModelScope.launch {
            _historyState.value = LoanHistoryUiState.Loading
            try {
                val customerId = authRepository.observeSession().first()?.user?.id
                if (customerId.isNullOrBlank()) {
                    _historyState.value = LoanHistoryUiState.Unauthorized
                    return@launch
                }
                val response = api.getLoanApplicationHistory(customerId)
                _historyState.value = LoanHistoryUiState.Success(response.content)
            } catch (e: HttpException) {
                _historyState.value = when (e.code()) {
                    401, 403 -> LoanHistoryUiState.Unauthorized
                    else -> LoanHistoryUiState.Error("Gagal memuat riwayat (HTTP ${e.code()})")
                }
            } catch (e: IOException) {
                _historyState.value = LoanHistoryUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                _historyState.value = LoanHistoryUiState.Error(e.message ?: "Gagal memuat riwayat pengajuan")
            }
        }
    }

    fun submitLoanApplication(
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

            val currentPlafond = (_plafondState.value as? PlafondUiState.Success)?.plafond
            if (currentPlafond == null) {
                _uiState.value = LoanApplicationUiState.Error("Plafond aktif tidak ditemukan")
                return@launch
            }

            if (amountRequested > currentPlafond.availableAmount) {
                _uiState.value = LoanApplicationUiState.Error("Jumlah pengajuan melebihi sisa plafond aktif Anda")
                return@launch
            }

            if (tenorMonths > currentPlafond.maxTenorMonths) {
                _uiState.value = LoanApplicationUiState.Error("Tenor melebihi batas maksimum plafond (${currentPlafond.maxTenorMonths} bulan)")
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
                    plafond = IdRef(currentPlafond.id),
                    amountRequested = amountRequested,
                    tenorMonths = tenorMonths,
                    purpose = purpose?.take(150)
                )
                val response = api.createLoanApplication(request)
                _uiState.value = LoanApplicationUiState.Success(response)

                loadActivePlafond()
            } catch (e: HttpException) {
                _uiState.value = LoanApplicationUiState.Error(
                    e.response()?.errorBody()?.string() ?: "Gagal memproses pengajuan pinjaman (HTTP ${e.code()})"
                )
            } catch (e: IOException) {
                _uiState.value = LoanApplicationUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                _uiState.value = LoanApplicationUiState.Error(e.message ?: "Gagal memproses pengajuan pinjaman")
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

sealed interface LoanApplicationUiState {
    data object Idle : LoanApplicationUiState
    data object Loading : LoanApplicationUiState
    data class Success(val response: LoanApplicationResponse) : LoanApplicationUiState
    data class Error(val message: String) : LoanApplicationUiState
}

sealed interface LoanHistoryUiState {
    data object Loading : LoanHistoryUiState
    data object Unauthorized : LoanHistoryUiState
    data class Success(val applications: List<LoanApplicationResponse>) : LoanHistoryUiState
    data class Error(val message: String) : LoanHistoryUiState
}

sealed interface PlafondUiState {
    data object Loading : PlafondUiState
    data object NotAvailable : PlafondUiState
    data object Unauthenticated : PlafondUiState
    data class Success(val plafond: PlafondResponse) : PlafondUiState
    data class Error(val message: String) : PlafondUiState
}

sealed interface BranchUiState {
    data object Loading : BranchUiState
    data class Success(val branches: List<BranchResponse>) : BranchUiState
    data class Error(val message: String) : BranchUiState
}