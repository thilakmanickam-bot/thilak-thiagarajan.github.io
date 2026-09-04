package com.astrochart.data.db

import androidx.room.Room
import com.astrochart.data.db.entities.SavedMatchEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDateTime

/**
 * Exercises the entity and DAO through real Room-generated code, which is what
 * actually proves [SavedMatchEntity]'s columns and the DAO's queries agree —
 * the migration test only checks the raw SQL.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SavedMatchDaoTest {

    private lateinit var db: AstroChartDatabase

    private val savedAt = LocalDateTime.of(2026, 9, 2, 10, 0)

    private fun match(
        groom: String = "Groom",
        bride: String = "Bride",
        total: Int = 26,
        at: LocalDateTime = savedAt
    ) = SavedMatchEntity(
        groomName = groom,
        brideName = bride,
        groomRasi = 5,
        groomNakshatra = 12,
        brideRasi = 5,
        brideNakshatra = 12,
        total = total,
        savedAt = at
    )

    @Before
    fun createDatabase() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AstroChartDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun close() {
        db.close()
    }

    @Test
    fun savedMatchRoundTripsWithoutBirthDetails() = runBlocking {
        val id = db.savedMatchDao().insert(match())

        val loaded = db.savedMatchDao().getById(id)

        assertEquals("Groom", loaded?.groomName)
        assertEquals("Bride", loaded?.brideName)
        assertEquals(26, loaded?.total)
        assertEquals(savedAt, loaded?.savedAt)
        assertNull(loaded?.groomBirthDateTime)
        assertFalse(
            "with no birth details stored, no chart can be drawn",
            loaded?.groomHasBirthDetails ?: true
        )
    }

    @Test
    fun savedMatchRoundTripsWithBirthDetailsForOnePersonOnly() = runBlocking {
        // The asymmetric case the results screen has to handle: one chart is
        // drawable, the other is not.
        val id = db.savedMatchDao().insert(
            match().copy(
                groomBirthDateTime = LocalDateTime.of(1988, 5, 26, 23, 35),
                groomLatitude = 10.0665,
                groomLongitude = 78.7784,
                groomTimeZone = "Asia/Kolkata",
                groomLocationName = "Kāraikkudi"
            )
        )

        val loaded = db.savedMatchDao().getById(id)!!

        assertEquals(LocalDateTime.of(1988, 5, 26, 23, 35), loaded.groomBirthDateTime)
        assertEquals(10.0665, loaded.groomLatitude!!, 1e-9)
        assertEquals("Kāraikkudi", loaded.groomLocationName)
        assertTrue(loaded.groomHasBirthDetails)
        assertFalse(loaded.brideHasBirthDetails)
    }

    @Test
    fun observeAllReturnsNewestFirst() = runBlocking {
        db.savedMatchDao().insert(match(groom = "Older", at = savedAt.minusDays(1)))
        db.savedMatchDao().insert(match(groom = "Newer", at = savedAt))

        val all = db.savedMatchDao().observeAll().first()

        assertEquals(listOf("Newer", "Older"), all.map { it.groomName })
    }

    @Test
    fun deleteByIdRemovesOnlyThatMatch() = runBlocking {
        val doomed = db.savedMatchDao().insert(match(groom = "Doomed"))
        db.savedMatchDao().insert(match(groom = "Kept"))

        db.savedMatchDao().deleteById(doomed)

        val all = db.savedMatchDao().observeAll().first()
        assertEquals(listOf("Kept"), all.map { it.groomName })
        assertNull(db.savedMatchDao().getById(doomed))
    }

    @Test
    fun savedChartsStillWorkAlongsideMatches() = runBlocking {
        // The matches table is additive; the existing chart storage must be
        // unaffected by sharing the database with it.
        db.savedMatchDao().insert(match())

        assertNull(db.savedChartDao().getLatestChart())
        assertEquals(1, db.savedMatchDao().observeAll().first().size)
    }
}
