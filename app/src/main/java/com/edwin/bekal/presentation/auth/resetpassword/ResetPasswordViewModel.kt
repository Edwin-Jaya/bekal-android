package com.edwin.bekal.presentation.auth.resetpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.toErrorMessage
import com.edwin.bekal.core.network.onFailure
import com.edwin.bekal.core.network.onSuccess
import com.edwin.bekal.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val repository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val email: String = checkNotNull(savedStateHandle["email"])

    private val _uiState = MutableStateFlow(ResetPasswordUiState(email = email))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onOtpChange(otp: String) {
        val digitsOnly = otp.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otp = digitsOnly, otpError = null, errorMessage = null) }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(newPassword = password, newPasswordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null, errorMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value

        val otpError = if (state.otp.length != 6) "Kode OTP harus 6 digit" else null
        val newPasswordError = when {
            state.newPassword.isBlank() -> "Kata sandi baru wajib diisi"
            state.newPassword.length < 8 -> "Kata sandi minimal 8 karakter"
            else -> null
        }
        val confirmPasswordError = if (state.confirmPassword != state.newPassword) {
            "Konfirmasi kata sandi tidak cocok"
        } else null

        if (listOf(otpError, newPasswordError, confirmPasswordError).any { it != null }) {
            _uiState.update {
                it.copy(otpError = otpError, newPasswordError = newPasswordError, confirmPasswordError = confirmPasswordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.resetPassword(email, state.otp, state.newPassword)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { failure -> _uiState.update { it.copy(isLoading = false, errorMessage = failure.toErrorMessage()) } }
        }
    }
}