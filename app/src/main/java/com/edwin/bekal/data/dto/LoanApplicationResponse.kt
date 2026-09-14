package com.edwin.bekal.data.dto

import com.edwin.bekal.utils.BigDecimalSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class LoanApplicationResponse(
    val id: String,

    @SerialName("applicationNumber")
    val applicationNumber: String,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amountRequested")
    val amountRequested: BigDecimal,

    @SerialName("tenorMonths")
    val tenorMonths: Int,

    @SerialName("purpose")
    val purpose: String? = null,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("interestRate")
    val interestRate: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("monthlyInstallment")
    val monthlyInstallment: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("totalRepayment")
    val totalRepayment: BigDecimal,

    @SerialName("status")
    val status: String,

    @SerialName("submittedAt")
    val submittedAt: String // ISO Instant string, format at UI layer
)