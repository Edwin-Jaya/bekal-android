package com.edwin.bekal.data.dto

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val tipe: String? = null,
    val roles: List<String> = emptyList(),
)