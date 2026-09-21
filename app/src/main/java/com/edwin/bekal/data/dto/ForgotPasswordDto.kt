package com.edwin.bekal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDto(val email: String)

@Serializable
data class VerifyOtpRequestDto(val email: String, val otp: String)

@Serializable
data class ResetPasswordRequestDto(val email: String, val otp: String, val newPassword: String)