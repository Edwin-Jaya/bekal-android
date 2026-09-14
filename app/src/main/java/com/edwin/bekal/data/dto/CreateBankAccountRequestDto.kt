package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBankAccountRequestDto(
    @SerialName("customerId")
    val customerId: String,
    @SerialName("bankName")
    val bankName: String,
    @SerialName("bankAccountNumber")
    val bankAccountNumber: String,
    @SerialName("bankAccountHolder")
    val bankAccountHolder: String,
    @SerialName("isPrimary")
    val isPrimary: Boolean? = null
)