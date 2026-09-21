package com.edwin.bekal.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edwin.bekal.core.database.dao.HomeDashboardDao
import com.edwin.bekal.core.database.dao.PlafondDao
import com.edwin.bekal.core.database.entity.HomeDashboardEntity
import com.edwin.bekal.core.database.entity.PlafondEntity


@Database(
    entities = [HomeDashboardEntity::class, PlafondEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun homeDashboardDao(): HomeDashboardDao
    abstract fun plafondDao(): PlafondDao
}