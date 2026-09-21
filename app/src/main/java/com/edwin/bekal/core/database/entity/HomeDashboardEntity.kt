package com.edwin.bekal.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_dashboard")
data class HomeDashboardEntity(
    @PrimaryKey val id: Int = 1, // Single row cache
    val loanStatus: String?,
    val creditTier: String?,
    val maxLimit: Double?,
    val availableLimit: Double?,
    val activeBillAmount: Double?,
    val successfulLoansCount: Int?,
    val requiredForNextTier: Int?,
    val dueDate: String?,
    val activeLoanId: String?
)