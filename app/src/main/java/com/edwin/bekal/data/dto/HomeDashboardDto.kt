package com.edwin.bekal.data.dto

import android.util.Log
import com.edwin.bekal.presentation.home.HomeLoanState
import java.math.BigDecimal

data class HomeDashboardData(
    val loanState: HomeLoanState,
    val creditTier: String,
    val tierNumber: Int,
    val maxLimit: BigDecimal,
    val availableLimit: BigDecimal,
    val activeBillAmount: BigDecimal,
    val successfulLoansCount: Int,
    val requiredForNextTier: Int,
    val dueDate: String?,
    val rawStatus: String,
    val activeLoanId: String? = null
)

fun HomeDashboardResponseDto.toDomain(): HomeDashboardData {
    val state = when (this.loanStatus?.uppercase()) {
        "IN_REVIEW", "IN_APPROVAL", "IN_DISBURSEMENT" -> HomeLoanState.IN_REVIEW
        "DISBURSED" -> HomeLoanState.ACTIVE_REPAYMENT
        "CLOSED", "CANCELLED", "REVIEW_REJECTED", "APPROVAL_REJECTED", null -> HomeLoanState.PRE_APPLICATION
        else -> HomeLoanState.PRE_APPLICATION
    }

    val tierStr = this.creditTier?.uppercase() ?: "TIER_1"
    val tierNum = when (tierStr) {
        "TIER_2" -> 2
        "TIER_3" -> 3
        else -> 1
    }

    return HomeDashboardData(
        loanState = state,
        creditTier = tierStr,
        tierNumber = tierNum,
        maxLimit = this.maxLimit?.toBigDecimal() ?: BigDecimal.ZERO,
        availableLimit = this.availableLimit?.toBigDecimal() ?: BigDecimal.ZERO,
        activeBillAmount = this.activeBillAmount?.toBigDecimal() ?: BigDecimal.ZERO,
        successfulLoansCount = this.successfulLoansCount ?: 0,
        requiredForNextTier = this.requiredForNextTier ?: 0,
        dueDate = this.dueDate,
        rawStatus = this.loanStatus ?: "CLOSED",
        activeLoanId = this.activeLoanId   // ⬅️ tambahkan ini
    )
}