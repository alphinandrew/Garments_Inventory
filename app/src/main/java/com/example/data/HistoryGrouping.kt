package com.example.data

import androidx.compose.runtime.Immutable
import java.util.Calendar

// Bug 3: Mark data classes used in UI lists as @Immutable so Compose can skip unnecessary recompositions.
@Immutable
data class DayHistoryPage(
    val dateMs: Long,
    val entries: List<ConsolidatedEntry>,
    val isCarriedForward: Boolean = false
)

@Immutable
data class ConsolidatedEntry(
    val garmentName: String,
    val sizeToStock: Map<String, Int>,
    val isCarriedForward: Boolean = false
)

object HistoryGrouping {
    fun getMidnightMs(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Bug 2: Originally, resolveFinalValuePerDay only returned days with explicit logs,
    // which caused trend line charts to skip straight to the next logged day rather than
    // reflecting that stock stayed the same during days with no activity.
    // Forward-filling day-by-day correctly produces flat continuity between logged days up to today.
    fun resolveFinalValuePerDay(
        logs: List<HistoryLog>,
        isTotal: Boolean,
        size: String?,
        includeToday: Boolean = true,
        fallbackStock: Int? = null
    ): List<Pair<Long, Int>> {
        val nonDeleteLogs = logs.filter { it.actionType != "DELETE" }
        val grouped = nonDeleteLogs.groupBy { getMidnightMs(it.timestamp) }
        val rawPoints = mutableListOf<Pair<Long, Int>>()
        grouped.forEach { (dateMs, dayLogs) ->
            val filteredDayLogs = dayLogs.filter { log ->
                if (isTotal) log.totalStockValue != null else log.size == size && log.stockValue != null
            }
            filteredDayLogs.maxByOrNull { it.timestamp }?.let { lastLog ->
                val stock = if (isTotal) lastLog.totalStockValue!! else lastLog.stockValue!!
                rawPoints.add(Pair(dateMs, stock))
            }
        }
        val todayMidnight = getMidnightMs(System.currentTimeMillis())
        if (rawPoints.isEmpty()) {
            return if (fallbackStock != null) {
                listOf(Pair(todayMidnight, fallbackStock))
            } else {
                emptyList()
            }
        }

        val sortedRaw = rawPoints.sortedBy { it.first }
        val rawMap = sortedRaw.toMap()

        val startMs = sortedRaw.first().first
        val endMs = if (includeToday) maxOf(sortedRaw.last().first, todayMidnight) else sortedRaw.last().first

        val filledResult = mutableListOf<Pair<Long, Int>>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = startMs
        var lastKnownStock = sortedRaw.first().second

        while (cal.timeInMillis <= endMs) {
            val currMs = cal.timeInMillis
            if (rawMap.containsKey(currMs)) {
                lastKnownStock = rawMap[currMs]!!
            }
            filledResult.add(Pair(currMs, lastKnownStock))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return filledResult
    }

    // Used by HistoryScreen & PDF
    fun resolveFinalValuesForGarmentDay(logs: List<HistoryLog>): Map<String, Int> {
        val sizeToStock = mutableMapOf<String, Int>()
        // logs are already for a specific day and garment
        logs.sortedBy { it.timestamp }.forEach { log ->
            if (log.size != null && log.stockValue != null) {
                sizeToStock[log.size] = log.stockValue
            }
        }
        return sizeToStock
    }

    // Bug 2: Synthesize carried-forward history for today and any intermediate days with zero logs,
    // without polluting the database with artificial rows.
    fun computeConsolidatedDayPages(
        garments: List<GarmentItem>,
        allLogs: List<HistoryLog>
    ): List<DayHistoryPage> {
        val todayMidnight = getMidnightMs(System.currentTimeMillis())
        val nonDeleteLogs = allLogs.filter {
            it.actionType != "DELETE" && it.garmentId != null && it.size != null && it.stockValue != null
        }
        val groupedByDay = nonDeleteLogs.groupBy { getMidnightMs(it.timestamp) }

        val earliestMs = if (groupedByDay.isNotEmpty()) groupedByDay.keys.minOrNull() ?: todayMidnight else todayMidnight
        val allDayMsList = mutableSetOf<Long>()
        val cal = Calendar.getInstance()
        cal.timeInMillis = earliestMs
        while (cal.timeInMillis <= todayMidnight) {
            allDayMsList.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        allDayMsList.add(todayMidnight)

        val runningGarmentState = mutableMapOf<Long, MutableMap<String, Int>>()
        garments.forEach { g ->
            runningGarmentState[g.id] = g.stockPerSize.toMutableMap()
        }

        val pages = mutableListOf<DayHistoryPage>()

        for (dayMs in allDayMsList.sorted()) {
            val dayLogs = groupedByDay[dayMs]
            val hasActualLogs = !dayLogs.isNullOrEmpty()

            if (hasActualLogs) {
                val groupedByGarment = dayLogs!!.groupBy { it.garmentId!! }
                groupedByGarment.forEach { (garmentId, logs) ->
                    val garmentMap = runningGarmentState.getOrPut(garmentId) { mutableMapOf() }
                    logs.sortedBy { it.timestamp }.forEach { log ->
                        if (log.size != null && log.stockValue != null) {
                            garmentMap[log.size] = log.stockValue
                        }
                    }
                }
            }

            val entries = mutableListOf<ConsolidatedEntry>()
            garments.forEach { garment ->
                val sizeMap = runningGarmentState[garment.id] ?: garment.stockPerSize
                if (sizeMap.isNotEmpty()) {
                    val garmentName = if (garment.subStyle.isNotBlank()) {
                        "${garment.categoryName} - ${garment.subStyle}"
                    } else {
                        garment.categoryName
                    }
                    entries.add(
                        ConsolidatedEntry(
                            garmentName = garmentName,
                            sizeToStock = sizeMap.toMap(),
                            isCarriedForward = !hasActualLogs
                        )
                    )
                }
            }

            pages.add(
                DayHistoryPage(
                    dateMs = dayMs,
                    entries = entries.sortedBy { it.garmentName },
                    isCarriedForward = !hasActualLogs
                )
            )
        }

        return pages.sortedByDescending { it.dateMs }
    }
}
