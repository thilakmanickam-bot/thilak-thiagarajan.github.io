package com.astrochart.auth

import android.content.Context
import com.astrochart.data.db.entities.SavedChartEntity
import com.astrochart.data.repository.ChartRepository
import com.astrochart.ui.i18n.PrimaryProfile
import com.astrochart.ui.i18n.PrimaryProfileStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

/**
 * Two-way sync of the signed-in user's data with Cloud Firestore.
 *
 *   users/{uid}                      → profile fields (name, rasi, nakshatra)
 *   users/{uid}/charts/{remoteId}    → one doc per saved chart
 *
 * The reconciliation decisions come from the pure [ChartMerge]; this class only
 * performs the reads/writes it prescribes. Runs only when
 * [com.astrochart.Features.AUTH_ENABLED] is true and a user is signed in.
 */
object ProfileSync {

    private val db get() = FirebaseFirestore.getInstance()
    private fun userDoc(uid: String) = db.collection("users").document(uid)
    private fun chartsCol(uid: String) = userDoc(uid).collection("charts")

    /** Pull-then-push the profile and all saved charts for the current user. */
    suspend fun syncAll(context: Context, repo: ChartRepository) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        syncProfile(context, uid)
        syncCharts(repo, uid)
    }

    // ----- Profile ----------------------------------------------------------

    private suspend fun syncProfile(context: Context, uid: String) {
        val doc = userDoc(uid).get().await()
        val remote = doc.toPrimaryProfile()
        val local = PrimaryProfileStore.load(context)
        when {
            remote != null -> PrimaryProfileStore.save(context, remote)   // cloud copy wins
            local != null -> pushProfile(uid, local)                      // seed the cloud
        }
    }

    /** Push the local profile up (called on sign-in and when the profile changes). */
    suspend fun pushProfile(uid: String, profile: PrimaryProfile) {
        userDoc(uid).set(
            mapOf(
                "name" to profile.name,
                "rasi" to profile.rasi,
                "nakshatra" to profile.nakshatra
            ),
            SetOptions.merge()
        ).await()
    }

    private fun DocumentSnapshot.toPrimaryProfile(): PrimaryProfile? {
        val rasi = getLong("rasi")?.toInt() ?: return null
        val nak = getLong("nakshatra")?.toInt() ?: return null
        if (rasi !in 0..11 || nak !in 0..26) return null
        return PrimaryProfile(getString("name") ?: "", rasi, nak)
    }

    // ----- Charts -----------------------------------------------------------

    private suspend fun syncCharts(repo: ChartRepository, uid: String) {
        val localList = repo.allChartsOnce()
        val remoteDocs = chartsCol(uid).get().await().documents

        val localKeys = localList.map {
            ChartMerge.LocalKey(it.id, it.remoteId, it.updatedAt)
        }
        val remoteKeys = remoteDocs.mapNotNull { d ->
            val ts = d.getString("updatedAt") ?: return@mapNotNull null
            runCatching { ChartMerge.RemoteKey(d.id, LocalDateTime.parse(ts)) }.getOrNull()
        }
        val plan = ChartMerge.plan(localKeys, remoteKeys)

        for (id in plan.pushNew) {
            val e = localList.first { it.id == id }
            val ref = chartsCol(uid).document()
            ref.set(chartToMap(e)).await()
            repo.stampRemoteId(id, ref.id)
        }
        for (id in plan.pushUpdate) {
            val e = localList.first { it.id == id }
            val rid = e.remoteId ?: chartsCol(uid).document().id
            chartsCol(uid).document(rid).set(chartToMap(e)).await()
            if (e.remoteId == null) repo.stampRemoteId(id, rid)
        }
        val remoteById = remoteDocs.associateBy { it.id }
        for (rid in plan.pullRemoteIds) {
            val d = remoteById[rid] ?: continue
            val existingId = repo.chartByRemoteId(rid)?.id ?: 0L
            val entity = d.toChartEntity(existingId) ?: continue
            repo.upsertLocal(entity)
        }
    }

    private fun chartToMap(e: SavedChartEntity): Map<String, Any> = mapOf(
        "name" to e.name,
        "birthDateTime" to e.birthDateTime.toString(),
        "latitude" to e.latitude,
        "longitude" to e.longitude,
        "timeZone" to e.timeZone,
        "locationName" to e.locationName,
        "createdAt" to e.createdAt.toString(),
        "chartJson" to e.chartJson,
        "updatedAt" to e.updatedAt.toString()
    )

    private fun DocumentSnapshot.toChartEntity(localId: Long): SavedChartEntity? = runCatching {
        SavedChartEntity(
            id = localId,
            name = getString("name") ?: "",
            birthDateTime = LocalDateTime.parse(getString("birthDateTime")),
            latitude = getDouble("latitude") ?: 0.0,
            longitude = getDouble("longitude") ?: 0.0,
            timeZone = getString("timeZone") ?: "UTC",
            locationName = getString("locationName") ?: "",
            createdAt = LocalDateTime.parse(getString("createdAt")),
            chartJson = getString("chartJson") ?: "",
            remoteId = id,
            updatedAt = LocalDateTime.parse(getString("updatedAt"))
        )
    }.getOrNull()
}
