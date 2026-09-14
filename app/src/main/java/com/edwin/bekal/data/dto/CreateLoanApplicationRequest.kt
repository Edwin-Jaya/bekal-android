package com.edwin.bekal.data.dto

import com.edwin.bekal.utils.BigDecimalSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Minimal reference wrapper so Jackson on the backend can resolve
 * Customer/Branch/Plafond entities by id alone, e.g. {"id": "..."}.
 */
@Serializable
data class IdRef(val id: String)

@Serializable
data class CreateLoanApplicationRequest(
    val customer: IdRef,
    val branch: IdRef,
    val plafond: IdRef,
    @Serializable(with = BigDecimalSerializer::class)
    val amountRequested: BigDecimal,
    val tenorMonths: Int,
    val purpose: String? = null
)