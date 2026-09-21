package com.edwin.bekal.data.mapper

import com.edwin.bekal.data.dto.HomeDashboardResponseDto
import com.edwin.bekal.data.dto.PlafondResponse
import com.edwin.bekal.data.dto.HomeDashboardData
import com.edwin.bekal.core.database.entity.HomeDashboardEntity
import com.edwin.bekal.core.database.entity.PlafondEntity
import com.edwin.bekal.presentation.home.HomeLoanState
import java.math.BigDecimal

// --- Home Dashboard Mappers ---

fun HomeDashboardResponseDto.toEntity(): HomeDashboardEntity {
    return HomeDashboardEntity(
        id = 1,
        loanStatus = this.loanStatus,
        creditTier = this.creditTier,
        maxLimit = this.maxLimit,
        availableLimit = this.availableLimit,
        activeBillAmount = this.activeBillAmount,
        successfulLoansCount = this.successfulLoansCount,
        requiredForNextTier = this.requiredForNextTier,
        dueDate = this.dueDate,
        activeLoanId = this.activeLoanId
    )
}

fun HomeDashboardEntity.toDomain(): HomeDashboardData {
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
        activeLoanId = this.activeLoanId
    )
}

// --- Plafond Mappers ---

fun PlafondResponse.toEntity(): PlafondEntity {
    return PlafondEntity(
        id = this.id,
        plafondAmount = this.plafondAmount.toPlainString(),
        usedAmount = this.usedAmount.toPlainString(),
        availableAmount = this.availableAmount.toPlainString(),
        interestRate = this.interestRate.toPlainString(),
        maxTenorMonths = this.maxTenorMonths,
        status = this.status,
        creditTier = this.creditTier,
        validFrom = this.validFrom,
        validUntil = this.validUntil
    )
}

fun PlafondEntity.toResponse(): PlafondResponse {
    return PlafondResponse(
        id = this.id,
        plafondAmount = BigDecimal(this.plafondAmount),
        usedAmount = BigDecimal(this.usedAmount),
        availableAmount = BigDecimal(this.availableAmount),
        interestRate = BigDecimal(this.interestRate),
        maxTenorMonths = this.maxTenorMonths,
        status = this.status,
        creditTier = this.creditTier,
        validFrom = this.validFrom,
        validUntil = this.validUntil
    )
}