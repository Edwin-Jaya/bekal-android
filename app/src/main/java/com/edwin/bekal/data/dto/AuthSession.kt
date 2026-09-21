package com.edwin.bekal.data.dto

data class AuthSession(
    val user: AuthUser? = null,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtMillis: Long = 100_000L,
) {
    fun isExpiredAt(nowMillis: Long): Boolean = nowMillis >= expiresAtMillis
}