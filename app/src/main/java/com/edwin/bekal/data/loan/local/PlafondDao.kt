package com.edwin.bekal.data.loan.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.edwin.bekal.core.database.entity.PlafondEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlafondDao {

    //NOTE: commented cause we not yet impl paging
    //@Query("SELECT * FROM TRX_PLAFONDS WHERE customer_id = :customerId LIMIT :limit")
    @Query("SELECT * FROM TRX_PLAFONDS WHERE customer_id = :customerId")
    fun observeList(customerId: Long): Flow<List<PlafondEntity>>

    @Query("SELECT COUNT(*) FROM TRX_PLAFONDS WHERE customer_id = :customerId")
    suspend fun count(customerId: Long): Int

    @Upsert
    suspend fun upsertAll(items: List<PlafondEntity>)

    @Query("DELETE FROM TRX_PLAFONDS WHERE customer_id = :customerId")
    suspend fun clear(customerId: Long)

    @Transaction
    suspend fun replaceAll(customerId: Long, items: List<PlafondEntity>) {
        clear(customerId)
        upsertAll(items)
    }
}