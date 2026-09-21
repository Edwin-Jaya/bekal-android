package com.edwin.bekal.presentation.auth

import com.edwin.bekal.core.error.AppFailure
import com.edwin.bekal.data.dto.AuthUser

enum class AuthStatus {
    UNKNOWN,
    AUTHENTICATED,
    UNAUTHENTICATED,
}

data class AuthUiState(
    val status: AuthStatus = AuthStatus.UNKNOWN,
    val user: AuthUser? = null,
    val isSubmitting: Boolean = false,
    val failure: AppFailure? = null,
    val errorMessage: String? = null,
    val pendingGoogleEmail: String? = null  // ← tambah ini
) {
    val isLoggedIn: Boolean get() = status == AuthStatus.AUTHENTICATED
    val isRestoringSession: Boolean get() = status == AuthStatus.UNKNOWN
    val isLoggedOut: Boolean get() = status == AuthStatus.UNAUTHENTICATED
}