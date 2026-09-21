package com.edwin.bekal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeDashboardResponseDto(
    @SerialName("loanStatus") val loanStatus: String? = null,
    @SerialName("creditTier") val creditTier: String? = null,
    @SerialName("maxLimit") val maxLimit: Double? = null,
    @SerialName("availableLimit") val availableLimit: Double? = null,
    @SerialName("activeBillAmount") val activeBillAmount: Double? = null,
    @SerialName("successfulLoansCount") val successfulLoansCount: Int? = null,
    @SerialName("requiredForNextTier") val requiredForNextTier: Int? = null,
    @SerialName("dueDate") val dueDate: String? = null,
    @SerialName("activeLoanId") val activeLoanId: String? = null
)