package com.astrochart.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.astrochart.data.db.dao.SavedChartDao
import com.astrochart.data.db.entities.SavedChartEntity

@Database(
    entities = [SavedChartEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AstroChartDatabase : RoomDatabase() {
    abstract fun savedChartDao(): SavedChartDao

    companion object {
        @Volatile
        private var instance: AstroChartDatabase? = null

        /**
         * v1 → v2: adds [SavedChartEntity.remoteId] and [SavedChartEntity.updatedAt]
         * for cloud sync. Existing rows get a null remoteId (not yet synced) and an
         * epoch updatedAt so the cloud copy wins on first reconcile.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_charts ADD COLUMN remoteId TEXT")
                db.execSQL(
                    "ALTER TABLE saved_charts ADD COLUMN updatedAt TEXT NOT NULL DEFAULT '1970-01-01T00:00:00'"
                )
            }
        }

        fun getInstance(context: Context): AstroChartDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AstroChartDatabase::class.java,
                    "astro_chart_database"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
        }
    }
}
