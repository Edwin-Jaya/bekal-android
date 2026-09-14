package com.edwin.bekal.core.error

fun AppFailure.toErrorMessage(): String {
    return when (this) {
        is CommonFailure.Network -> "Gagal terhubung ke server. Periksa koneksi internetmu."
        is CommonFailure.Unauthorized -> "Sesi telah berakhir atau kamu tidak memiliki akses."
        is CommonFailure.ApiError -> {
            when {
                !message.isNullOrBlank() -> message
                details.isNotEmpty() -> details.joinToString("\n")
                else -> "Terjadi kesalahan pada server (${code ?: "API Error"})"
            }
        }
        is CommonFailure.Unexpected -> cause?.localizedMessage ?: "Terjadi kesalahan yang tidak terduga."
        else -> "Terjadi kesalahan. Silakan coba lagi."
    }
}