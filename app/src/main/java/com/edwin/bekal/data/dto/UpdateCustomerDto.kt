package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCustomerRequestDto(
    @SerialName("customerFullName")
    val fullName: String? = null,

    @SerialName("customerPhoneNumber")
    val phoneNumber: String? = null,

    @SerialName("customerAddress")
    val address: String? = null,

    @SerialName("customerGender")
    val gender: String? = null
)