package com.edwin.bekal.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edwin.bekal.data.loan.local.PlafondDao
import com.edwin.bekal.core.database.entity.PlafondEntity

@Database(
    entities = [PlafondEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BekalDatabase : RoomDatabase() {

    abstract fun plafondDao(): PlafondDao

    companion object {
        const val NAME = "bekal.db"
    }
}