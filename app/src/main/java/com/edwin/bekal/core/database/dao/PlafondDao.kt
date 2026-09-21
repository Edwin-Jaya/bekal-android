package com.edwin.bekal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.edwin.bekal.core.database.entity.PlafondEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlafondDao {
    @Query("SELECT * FROM plafonds LIMIT 1")
    fun getPlafond(): Flow<PlafondEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlafond(entity: PlafondEntity)

    @Query("DELETE FROM plafonds")
    suspend fun clearPlafonds()
}