package com.edwin.bekal.core.error

sealed interface CommonFailure : AppFailure {

    data class Network(val cause: Throwable? = null) : CommonFailure

    data object Unauthorized : CommonFailure

    data class ApiError(
        val code: String? = null,
        val details: List<String> = emptyList(),
        override val message: String? = null,
    ) : CommonFailure

    data class Unexpected(val cause: Throwable? = null) : CommonFailure
}