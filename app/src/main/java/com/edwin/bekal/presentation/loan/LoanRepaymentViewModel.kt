package com.edwin.bekal.presentation.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.loan.remote.LoanBalanceResponse
import com.edwin.bekal.data.loan.remote.PaymentHistoryResponse
import com.edwin.bekal.data.loan.remote.RepaymentApi
import com.edwin.bekal.data.loan.remote.RepaymentRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

sealed interface BalanceUiState {
    data object Loading : BalanceUiState
    data class Success(val balance: LoanBalanceResponse) : BalanceUiState
    data class Error(val message: String) : BalanceUiState
}

sealed interface RepaymentSubmitState {
    data object Idle : RepaymentSubmitState
    data object Loading : RepaymentSubmitState
    data class Success(val updatedBalance: LoanBalanceResponse) : RepaymentSubmitState
    data class Error(val message: String) : RepaymentSubmitState
}

@HiltViewModel
class LoanRepaymentViewModel @Inject constructor(
    private val repaymentApi: RepaymentApi,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _balanceState = MutableStateFlow<BalanceUiState>(BalanceUiState.Loading)
    val balanceState: StateFlow<BalanceUiState> = _balanceState

    private val _submitState = MutableStateFlow<RepaymentSubmitState>(RepaymentSubmitState.Idle)
    val submitState: StateFlow<RepaymentSubmitState> = _submitState

    private val _historyState = MutableStateFlow<List<PaymentHistoryResponse>>(emptyList())
    val historyState: StateFlow<List<PaymentHistoryResponse>> = _historyState

    val paymentInput = MutableStateFlow("")
    val selectedPaymentMethod = MutableStateFlow("VA_BANK_TRANSFER")

    fun loadLoanBalance(loanId: String) {
        viewModelScope.launch {
            _balanceState.value = BalanceUiState.Loading
            try {
                val balance = repaymentApi.getLoanBalance(loanId)
                _balanceState.value = BalanceUiState.Success(balance)
            } catch (e: Exception) {
                _balanceState.value = BalanceUiState.Error(e.message ?: "Gagal memuat saldo pinjaman")
            }
        }
    }

    fun executeRepayment(loanId: String, amountPaid: BigDecimal) {
        viewModelScope.launch {
            _submitState.value = RepaymentSubmitState.Loading
            try {
                // Generate unique client-side UUID to guarantee idempotency in backend TRX_PAYMENTS
                val clientTxnRef = "TRX-${UUID.randomUUID().toString().take(8).uppercase()}"

                val request = RepaymentRequest(
                    loanId = loanId,
                    amountPaid = amountPaid,
                    paymentMethod = selectedPaymentMethod.value,
                    transactionReference = clientTxnRef
                )

                val response = repaymentApi.submitRepayment(request)
                _submitState.value = RepaymentSubmitState.Success(response)
                // Refresh balance state immediately
                _balanceState.value = BalanceUiState.Success(response)
                paymentInput.value = ""
            } catch (e: Exception) {
                _submitState.value = RepaymentSubmitState.Error(e.message ?: "Gagal memproses pembayaran")
            }
        }
    }

    fun loadCustomerHistory() {
        viewModelScope.launch {
            try {
                val customerId = authRepository.observeSession().first()?.user?.id ?: return@launch
                _historyState.value = repaymentApi.getPaymentHistory(customerId)
            } catch (_: Exception) { }
        }
    }
}