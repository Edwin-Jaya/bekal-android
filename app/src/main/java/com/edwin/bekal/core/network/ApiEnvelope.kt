package com.edwin.bekal.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    @SerialName("timestamp") val timestamp: String? = null,
    @SerialName("status") val status: Int,
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T? = null
)

@Serializable
data class ApiMetaDto(
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
)

@Serializable
data class ApiErrorDto(
    val code: String? = null,
    val details: List<String> = emptyList(),
)