package com.edwin.bekal.data.dto

import com.edwin.bekal.utils.BigDecimalSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class PlafondResponse(
    val id: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("plafondAmount")
    val plafondAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("usedAmount")
    val usedAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("availableAmount")
    val availableAmount: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("interestRate")
    val interestRate: BigDecimal,

    @SerialName("maxTenorMonths")
    val maxTenorMonths: Int,

    @SerialName("status")
    val status: String,

    @SerialName("validFrom")
    val validFrom: String? = null,

    @SerialName("validUntil")
    val validUntil: String? = null
)