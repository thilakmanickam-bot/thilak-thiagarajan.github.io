package com.astrochart.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.astrochart.data.db.dao.SavedChartDao
import com.astrochart.data.db.dao.SavedMatchDao
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.data.db.entities.SavedMatchEntity

@Database(
    entities = [SavedChartEntity::class, SavedMatchEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class AstroChartDatabase : RoomDatabase() {
    abstract fun savedChartDao(): SavedChartDao
    abstract fun savedMatchDao(): SavedMatchDao

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

        /**
         * The `saved_matches` table, as SQL rather than inline in [MIGRATION_2_3]
         * so a test can execute the identical statement. Column types and
         * nullability must agree with [SavedMatchEntity] or Room rejects the
         * database at open time with "Migration didn't properly handle".
         *
         * Non-null `LocalDateTime` maps to `TEXT NOT NULL` even though
         * [DateTimeConverters] takes and returns a nullable String — the
         * shipped v1→v2 migration above relies on exactly that for `updatedAt`.
         */
        internal const val CREATE_SAVED_MATCHES = """
            CREATE TABLE IF NOT EXISTS `saved_matches` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `groomName` TEXT NOT NULL,
                `brideName` TEXT NOT NULL,
                `groomRasi` INTEGER NOT NULL,
                `groomNakshatra` INTEGER NOT NULL,
                `brideRasi` INTEGER NOT NULL,
                `brideNakshatra` INTEGER NOT NULL,
                `total` INTEGER NOT NULL,
                `savedAt` TEXT NOT NULL,
                `groomBirthDateTime` TEXT,
                `groomLatitude` REAL,
                `groomLongitude` REAL,
                `groomTimeZone` TEXT,
                `groomLocationName` TEXT,
                `brideBirthDateTime` TEXT,
                `brideLatitude` REAL,
                `brideLongitude` REAL,
                `brideTimeZone` TEXT,
                `brideLocationName` TEXT
            )
        """

        /**
         * v2 → v3: adds `saved_matches`. Purely additive — it creates a new
         * table and does not touch `saved_charts`, so existing saved charts
         * (and their sync state) are carried across untouched.
         *
         * `internal` so [com.astrochart.data.db.SavedMatchMigrationTest] can
         * run it against a hand-built v2 database.
         */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATE_SAVED_MATCHES)
            }
        }

        fun getInstance(context: Context): AstroChartDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AstroChartDatabase::class.java,
                    "astro_chart_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
        }
    }
}
