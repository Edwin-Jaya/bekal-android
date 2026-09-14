package com.edwin.bekal.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edwin.bekal.core.error.CommonFailure
import com.edwin.bekal.core.network.AppResult
import com.edwin.bekal.data.auth.AuthRepository
import com.edwin.bekal.data.dto.LoginRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value.isSubmitting) return

        val credentials = LoginRequestDto(email = email, password = password)

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    errorMessage = "Email dan password tidak boleh kosong",
                )
            }
            return
        }

        viewModelScope.launch {
            markSubmitting()
            when (val result = authRepository.login(credentials)) {
                is AppResult.Success -> {
                    _uiState.update{ state ->
                        state.copy(
                            errorMessage = null,
                            status = AuthStatus.AUTHENTICATED
                        )
                    }
                }
                is AppResult.Failure -> {
                    val message = when (val appFailure = result.failure) {
                        is CommonFailure.ApiError -> {
                            appFailure.details.joinToString(", ").ifBlank {
                                appFailure.message ?: "Terjadi kesalahan pada API"
                            }
                        }
                        is CommonFailure.Unauthorized -> "Sesi telah berakhir, silakan login kembali."
                        is CommonFailure.Network -> "Koneksi internet bermasalah."
                        is CommonFailure.Unexpected -> "Terjadi kesalahan tidak terduga."
                        else -> "Login gagal: ${appFailure.javaClass.simpleName}"
                    }

                    _uiState.update { state ->
                        state.copy(
                            errorMessage = message,
                            status = AuthStatus.UNAUTHENTICATED
                        )
                    }
                }
            }

            markIdle()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
    private fun markSubmitting() {
        _uiState.update { it.copy(isSubmitting = true, failure = null) }
    }

    private fun markIdle() {
        _uiState.update { it.copy(isSubmitting = false) }
    }

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.observeSession().collect { session ->
                _uiState.update { state ->
                    state.copy(
                        status = if (session == null) {
                            AuthStatus.UNAUTHENTICATED
                        } else {
                            AuthStatus.AUTHENTICATED
                        },
                        user = session?.user,
                    )
                }
            }
        }
    }
}