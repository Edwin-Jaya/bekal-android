package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("device_token") val deviceToken: String = "fcm_dummy_token_12345"
)

@Serializable
data class LoginResponseDto(
    @SerialName("token") val token: String,
    @SerialName("type") val type: String? = "Bearer"
)