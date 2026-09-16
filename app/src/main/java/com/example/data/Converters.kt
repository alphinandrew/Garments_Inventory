package com.example.data

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val jsonArray = JSONArray()
        value.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // fallback for comma-separated
            return value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        return list
    }

    @TypeConverter
    fun fromStockMap(value: Map<String, Int>?): String {
        if (value == null) return "{}"
        val json = JSONObject()
        value.forEach { (k, v) -> json.put(k, v) }
        return json.toString()
    }

    @TypeConverter
    fun toStockMap(value: String?): Map<String, Int> {
        if (value.isNullOrBlank()) return emptyMap()
        val map = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(value)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getInt(key)
            }
        } catch (e: Exception) {
            // ignore
        }
        return map
    }
}
