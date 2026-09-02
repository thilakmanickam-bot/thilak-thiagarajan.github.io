package com.astrochart.data.db

import android.database.sqlite.SQLiteDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The v2 → v3 migration runs against databases that already hold people's
 * saved charts, so it gets tested rather than trusted.
 *
 * This drives the migration's SQL directly on a hand-built v2 database instead
 * of using Room's `MigrationTestHelper`: that helper reads exported schema
 * JSON, and this database is declared `exportSchema = false`, so no v2 schema
 * file exists to create the old database from.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SavedMatchMigrationTest {

    /** `saved_charts` exactly as it stands at v2, after MIGRATION_1_2. */
    private val createSavedChartsV2 = """
        CREATE TABLE IF NOT EXISTS `saved_charts` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL,
            `birthDateTime` TEXT NOT NULL,
            `latitude` REAL NOT NULL,
            `longitude` REAL NOT NULL,
            `timeZone` TEXT NOT NULL,
            `locationName` TEXT NOT NULL,
            `createdAt` TEXT NOT NULL,
            `chartJson` TEXT NOT NULL,
            `remoteId` TEXT,
            `updatedAt` TEXT NOT NULL DEFAULT '1970-01-01T00:00:00'
        )
    """

    private lateinit var db: SQLiteDatabase

    @Before
    fun openV2DatabaseWithAChartInIt() {
        db = SQLiteDatabase.create(null)
        db.execSQL(createSavedChartsV2)
        db.execSQL(
            """
            INSERT INTO saved_charts
                (name, birthDateTime, latitude, longitude, timeZone, locationName,
                 createdAt, chartJson, remoteId, updatedAt)
            VALUES
                ('Existing Chart', '1988-05-26T23:35:00', 10.0665, 78.7784,
                 'Asia/Kolkata', 'Kāraikkudi', '2026-01-01T00:00:00', '{}',
                 'remote-1', '2026-01-01T00:00:00')
            """
        )
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun migrationIsDeclaredFromVersion2To3() {
        assertEquals(2, AstroChartDatabase.MIGRATION_2_3.startVersion)
        assertEquals(3, AstroChartDatabase.MIGRATION_2_3.endVersion)
    }

    @Test
    fun migrationLeavesExistingSavedChartsUntouched() {
        db.execSQL(AstroChartDatabase.CREATE_SAVED_MATCHES)

        db.rawQuery("SELECT name, remoteId, updatedAt FROM saved_charts", null).use { c ->
            assertEquals("the existing chart must survive the migration", 1, c.count)
            c.moveToFirst()
            assertEquals("Existing Chart", c.getString(0))
            assertEquals("remote-1", c.getString(1))
            assertEquals("2026-01-01T00:00:00", c.getString(2))
        }
    }

    @Test
    fun migrationCreatesSavedMatchesWithTheColumnsRoomExpects() {
        db.execSQL(AstroChartDatabase.CREATE_SAVED_MATCHES)

        // name -> (declared type, notNull, isPrimaryKey). A mismatch with
        // SavedMatchEntity is what makes Room throw "Migration didn't properly
        // handle" on the first open after upgrading.
        val expected = mapOf(
            "id" to Triple("INTEGER", true, true),
            "groomName" to Triple("TEXT", true, false),
            "brideName" to Triple("TEXT", true, false),
            "groomRasi" to Triple("INTEGER", true, false),
            "groomNakshatra" to Triple("INTEGER", true, false),
            "brideRasi" to Triple("INTEGER", true, false),
            "brideNakshatra" to Triple("INTEGER", true, false),
            "total" to Triple("INTEGER", true, false),
            "savedAt" to Triple("TEXT", true, false),
            "groomBirthDateTime" to Triple("TEXT", false, false),
            "groomLatitude" to Triple("REAL", false, false),
            "groomLongitude" to Triple("REAL", false, false),
            "groomTimeZone" to Triple("TEXT", false, false),
            "groomLocationName" to Triple("TEXT", false, false),
            "brideBirthDateTime" to Triple("TEXT", false, false),
            "brideLatitude" to Triple("REAL", false, false),
            "brideLongitude" to Triple("REAL", false, false),
            "brideTimeZone" to Triple("TEXT", false, false),
            "brideLocationName" to Triple("TEXT", false, false)
        )

        val actual = mutableMapOf<String, Triple<String, Boolean, Boolean>>()
        db.rawQuery("PRAGMA table_info(`saved_matches`)", null).use { c ->
            while (c.moveToNext()) {
                actual[c.getString(c.getColumnIndexOrThrow("name"))] = Triple(
                    c.getString(c.getColumnIndexOrThrow("type")),
                    c.getInt(c.getColumnIndexOrThrow("notnull")) == 1,
                    c.getInt(c.getColumnIndexOrThrow("pk")) == 1
                )
            }
        }

        assertEquals(expected, actual)
    }

    @Test
    fun savedMatchesAcceptsARowWithNoBirthDetails() {
        db.execSQL(AstroChartDatabase.CREATE_SAVED_MATCHES)

        // Birth details are optional, so the nullable columns must genuinely
        // accept nothing at all — a stray NOT NULL here would only surface
        // when a user saved a match without them.
        db.execSQL(
            """
            INSERT INTO saved_matches
                (groomName, brideName, groomRasi, groomNakshatra,
                 brideRasi, brideNakshatra, total, savedAt)
            VALUES ('Groom', 'Bride', 5, 12, 5, 12, 26, '2026-09-02T10:00:00')
            """
        )

        db.rawQuery("SELECT total, groomBirthDateTime FROM saved_matches", null).use { c ->
            assertEquals(1, c.count)
            c.moveToFirst()
            assertEquals(26, c.getInt(0))
            assertTrue("birth details should be absent, not blank", c.isNull(1))
        }
    }
}
