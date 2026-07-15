package com.astrochart.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.astrochart.data.db.dao.SavedChartDao
import com.astrochart.data.db.entities.SavedChartEntity

@Database(
    entities = [SavedChartEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AstroChartDatabase : RoomDatabase() {
    abstract fun savedChartDao(): SavedChartDao

    companion object {
        @Volatile
        private var instance: AstroChartDatabase? = null

        fun getInstance(context: Context): AstroChartDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AstroChartDatabase::class.java,
                    "astro_chart_database"
                ).build().also { instance = it }
            }
        }
    }
}
