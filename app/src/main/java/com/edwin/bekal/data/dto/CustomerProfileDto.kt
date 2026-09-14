package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerProfileDto(
    @SerialName("id") val id: String,
    @SerialName("customerFullName") val fullName: String,
    @SerialName("customerEmail") val email: String,
    @SerialName("customerPhoneNumber") val phoneNumber: String? = null,
    @SerialName("customerAddress") val address: String? = null,
    @SerialName("customerGender") val gender: String? = null,
    @SerialName("customerNik") val nik: String? = null,
    @SerialName("customerStatus") val status: String? = null
)