package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCheckResponse(
    @SerialName("exists") val exists: Boolean,
    @SerialName("message") val message: String
)