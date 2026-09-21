package com.edwin.bekal.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plafonds")
data class PlafondEntity(
    @PrimaryKey val id: String,
    val plafondAmount: String, // Stored as String to preserve BigDecimal precision
    val usedAmount: String,
    val availableAmount: String,
    val interestRate: String,
    val maxTenorMonths: Int,
    val status: String,
    val creditTier: String?,
    val validFrom: String?,
    val validUntil: String?
)