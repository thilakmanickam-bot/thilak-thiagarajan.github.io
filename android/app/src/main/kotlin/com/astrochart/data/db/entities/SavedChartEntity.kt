package com.astrochart.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "saved_charts")
data class SavedChartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val birthDateTime: LocalDateTime,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    val locationName: String,
    val createdAt: LocalDateTime,
    val chartJson: String
)
