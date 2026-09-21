package com.edwin.bekal.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.edwin.bekal.core.database.entity.HomeDashboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDashboardDao {
    @Query("SELECT * FROM home_dashboard WHERE id = 1")
    fun getHomeDashboard(): Flow<HomeDashboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeDashboard(entity: HomeDashboardEntity)

    @Query("DELETE FROM home_dashboard")
    suspend fun clearHomeDashboard()
}