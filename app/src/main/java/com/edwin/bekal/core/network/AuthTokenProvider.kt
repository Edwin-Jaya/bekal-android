package com.edwin.bekal.core.network

fun interface AuthTokenProvider {
    suspend fun currentToken(): String?
}