package com.edwin.bekal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "TRX_PLAFONDS")
data class PlafondEntity(
    @PrimaryKey
    val id: Long,

    @ColumnInfo(name = "customer_id")
    val customerId: Long,

    @ColumnInfo(name = "plafond_amount")
    val plafondAmount: BigDecimal,

    @ColumnInfo(name = "used_amount", defaultValue = "0")
    val usedAmount: BigDecimal = BigDecimal.ZERO,

    @ColumnInfo(name = "interest_rate")
    val interestRate: BigDecimal,

    @ColumnInfo(name = "max_tenor_months")
    val maxTenorMonths: Int,

    @ColumnInfo(name = "status", defaultValue = "'active'")
    val status: String = "active",

    @ColumnInfo(name = "valid_from")
    val validFrom: LocalDate? = null,

    @ColumnInfo(name = "valid_until")
    val validUntil: LocalDate? = null,

    @ColumnInfo(name = "created_by_id")
    val createdById: Long? = null,

    @ColumnInfo(name = "created_by_name")
    val createdByName: String? = null,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long = System.currentTimeMillis()
)