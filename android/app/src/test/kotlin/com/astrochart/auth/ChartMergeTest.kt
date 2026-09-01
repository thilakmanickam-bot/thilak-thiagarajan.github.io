package com.astrochart.auth

import com.astrochart.auth.ChartMerge.LocalKey
import com.astrochart.auth.ChartMerge.RemoteKey
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartMergeTest {

    private val t0 = LocalDateTime.of(2026, 1, 1, 0, 0)
    private val t1 = LocalDateTime.of(2026, 6, 1, 0, 0)
    private val t2 = LocalDateTime.of(2026, 9, 1, 0, 0)

    @Test
    fun localNeverPushed_isPushedNew() {
        val plan = ChartMerge.plan(
            local = listOf(LocalKey(localId = 5, remoteId = null, updatedAt = t1)),
            remote = emptyList()
        )
        assertEquals(listOf(5L), plan.pushNew)
        assertTrue(plan.pushUpdate.isEmpty())
        assertTrue(plan.pullRemoteIds.isEmpty())
    }

    @Test
    fun remoteOnly_isPulledDown() {
        val plan = ChartMerge.plan(
            local = emptyList(),
            remote = listOf(RemoteKey(remoteId = "abc", updatedAt = t1))
        )
        assertEquals(listOf("abc"), plan.pullRemoteIds)
        assertTrue(plan.pushNew.isEmpty())
        assertTrue(plan.pushUpdate.isEmpty())
    }

    @Test
    fun conflict_newerLocal_pushesUpdate() {
        val plan = ChartMerge.plan(
            local = listOf(LocalKey(localId = 1, remoteId = "abc", updatedAt = t2)),
            remote = listOf(RemoteKey(remoteId = "abc", updatedAt = t1))
        )
        assertEquals(listOf(1L), plan.pushUpdate)
        assertTrue(plan.pullRemoteIds.isEmpty())
    }

    @Test
    fun conflict_newerRemote_pullsDown() {
        val plan = ChartMerge.plan(
            local = listOf(LocalKey(localId = 1, remoteId = "abc", updatedAt = t1)),
            remote = listOf(RemoteKey(remoteId = "abc", updatedAt = t2))
        )
        assertEquals(listOf("abc"), plan.pullRemoteIds)
        assertTrue(plan.pushUpdate.isEmpty())
    }

    @Test
    fun equalTimestamps_doNothing() {
        val plan = ChartMerge.plan(
            local = listOf(LocalKey(localId = 1, remoteId = "abc", updatedAt = t1)),
            remote = listOf(RemoteKey(remoteId = "abc", updatedAt = t1))
        )
        assertTrue(plan.pushNew.isEmpty())
        assertTrue(plan.pushUpdate.isEmpty())
        assertTrue(plan.pullRemoteIds.isEmpty())
    }

    @Test
    fun matchedButRemoteGone_isRepushed() {
        val plan = ChartMerge.plan(
            local = listOf(LocalKey(localId = 7, remoteId = "stale", updatedAt = t0)),
            remote = emptyList()
        )
        assertEquals(listOf(7L), plan.pushUpdate)
    }

    @Test
    fun mixedSets_reconcileIndependently() {
        val plan = ChartMerge.plan(
            local = listOf(
                LocalKey(localId = 1, remoteId = null, updatedAt = t1),   // new → push
                LocalKey(localId = 2, remoteId = "keep", updatedAt = t1)  // in sync
            ),
            remote = listOf(
                RemoteKey(remoteId = "keep", updatedAt = t1),             // in sync
                RemoteKey(remoteId = "fresh", updatedAt = t2)             // new → pull
            )
        )
        assertEquals(listOf(1L), plan.pushNew)
        assertEquals(listOf("fresh"), plan.pullRemoteIds)
        assertTrue(plan.pushUpdate.isEmpty())
    }
}
