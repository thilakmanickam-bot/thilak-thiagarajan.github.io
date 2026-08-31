package com.astrochart.auth

import java.time.LocalDateTime

/**
 * Pure, side-effect-free reconciliation of local saved charts against the copies
 * held in the cloud (Firestore). Kept free of Room, Firebase and Android so it is
 * unit-testable off-device; [ProfileSync] performs the actual I/O the plan calls for.
 *
 * Charts are matched across devices by their Firestore document id ([remoteId]).
 * Conflicts resolve last-write-wins by [updatedAt].
 */
object ChartMerge {

    /** The minimum a local chart row contributes to the decision. */
    data class LocalKey(val localId: Long, val remoteId: String?, val updatedAt: LocalDateTime)

    /** The minimum a cloud chart contributes to the decision. */
    data class RemoteKey(val remoteId: String, val updatedAt: LocalDateTime)

    /**
     * What to do to make both sides consistent:
     *  - [pushNew]: local ids never pushed (no remoteId) → create in Firestore.
     *  - [pushUpdate]: local ids whose local copy is newer (or whose remote copy
     *    is gone) → overwrite the Firestore doc.
     *  - [pullRemoteIds]: cloud doc ids that are new locally, or newer than the
     *    local copy → write down into Room.
     */
    data class Plan(
        val pushNew: List<Long>,
        val pushUpdate: List<Long>,
        val pullRemoteIds: List<String>
    )

    fun plan(local: List<LocalKey>, remote: List<RemoteKey>): Plan {
        val remoteById = remote.associateBy { it.remoteId }
        val pushNew = mutableListOf<Long>()
        val pushUpdate = mutableListOf<Long>()
        val pullRemoteIds = mutableListOf<String>()
        val matchedRemoteIds = mutableSetOf<String>()

        for (l in local) {
            val rid = l.remoteId
            if (rid == null) {
                pushNew += l.localId
                continue
            }
            val r = remoteById[rid]
            if (r == null) {
                // Was pushed before but the cloud doc is gone; re-push rather than
                // silently drop the chart (delete-propagation is a later feature).
                pushUpdate += l.localId
                continue
            }
            matchedRemoteIds += rid
            when {
                l.updatedAt.isAfter(r.updatedAt) -> pushUpdate += l.localId
                r.updatedAt.isAfter(l.updatedAt) -> pullRemoteIds += rid
                // equal timestamps → already in sync, nothing to do
            }
        }

        // Cloud charts not present locally at all → pull them down.
        for (r in remote) {
            if (r.remoteId !in matchedRemoteIds && local.none { it.remoteId == r.remoteId }) {
                pullRemoteIds += r.remoteId
            }
        }

        return Plan(pushNew, pushUpdate, pullRemoteIds.distinct())
    }
}
