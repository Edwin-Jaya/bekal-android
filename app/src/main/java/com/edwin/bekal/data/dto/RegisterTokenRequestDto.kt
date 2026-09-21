package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterTokenRequest(
    @SerialName("customerId") val customerId: String,
    @SerialName("fcmToken") val fcmToken: String,
    @SerialName("deviceInfo") val deviceInfo: String
)