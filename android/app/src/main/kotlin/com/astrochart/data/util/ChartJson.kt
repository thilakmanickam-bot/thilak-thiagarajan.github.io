package com.astrochart.data.util

import com.astrochart.core.models.NatalChart
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * JSON (de)serialization for [NatalChart]. Gson handles the plain data classes
 * and maps out of the box; the only fields needing custom adapters are the
 * java.time types on BirthData ([LocalDateTime] and [ZoneId]).
 */
object ChartJson {

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter().nullSafe())
        .registerTypeHierarchyAdapter(ZoneId::class.java, ZoneIdAdapter().nullSafe())
        .create()

    fun toJson(chart: NatalChart): String = gson.toJson(chart)

    /** Returns null if the JSON is empty or cannot be parsed back into a chart. */
    fun fromJson(json: String): NatalChart? {
        if (json.isBlank()) return null
        return try {
            gson.fromJson(json, NatalChart::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        override fun write(out: JsonWriter, value: LocalDateTime) {
            out.value(value.format(formatter))
        }
        override fun read(reader: JsonReader): LocalDateTime =
            LocalDateTime.parse(reader.nextString(), formatter)
    }

    private class ZoneIdAdapter : TypeAdapter<ZoneId>() {
        override fun write(out: JsonWriter, value: ZoneId) {
            out.value(value.id)
        }
        override fun read(reader: JsonReader): ZoneId =
            ZoneId.of(reader.nextString())
    }
}
