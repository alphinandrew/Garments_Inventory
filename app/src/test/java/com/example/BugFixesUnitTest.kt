package com.example

import com.example.data.ConsolidatedEntry
import com.example.data.DayHistoryPage
import com.example.data.GarmentItem
import com.example.data.HistoryGrouping
import com.example.data.HistoryLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class BugFixesUnitTest {

    @Test
    fun testAlarmScheduler_calculateNextTriggerTime_futureToday() {
        val cal = Calendar.getInstance()
        val nowMs = cal.timeInMillis

        // Set target 2 hours in the future
        val targetHour = (cal.get(Calendar.HOUR_OF_DAY) + 2) % 24
        val targetMinute = 30
        val targetMs = AlarmScheduler.calculateNextTriggerTime(targetHour, targetMinute, nowMs)

        assertTrue("Trigger time should be in the future", targetMs > nowMs)
    }

    @Test
    fun testAlarmScheduler_calculateNextTriggerTime_pastAdvancesToTomorrow() {
        val cal = Calendar.getInstance()
        val nowMs = cal.timeInMillis

        // Set target 2 hours in the past
        val pastHour = (cal.get(Calendar.HOUR_OF_DAY) - 2 + 24) % 24
        val targetMs = AlarmScheduler.calculateNextTriggerTime(pastHour, 0, nowMs)

        assertTrue("Past time should advance to tomorrow", targetMs > nowMs)
    }

    @Test
    fun testHistoryGrouping_resolveFinalValuePerDay_forwardFillsGapDays() {
        val todayMidnight = HistoryGrouping.getMidnightMs(System.currentTimeMillis())
        val threeDaysAgo = todayMidnight - (3 * 86400000L)
        val oneDayAgo = todayMidnight - 86400000L

        val logs = listOf(
            HistoryLog(
                id = 1,
                timestamp = threeDaysAgo + 3600000L, // 1am 3 days ago
                description = "Initial stock",
                garmentId = 1L,
                size = "M",
                stockValue = 20,
                totalStockValue = 20,
                actionType = "CREATE"
            ),
            HistoryLog(
                id = 2,
                timestamp = oneDayAgo + 3600000L, // 1am yesterday
                description = "Stock update",
                garmentId = 1L,
                size = "M",
                stockValue = 15,
                totalStockValue = 15,
                actionType = "STOCK_UPDATE"
            )
        )

        val result = HistoryGrouping.resolveFinalValuePerDay(
            logs = logs,
            isTotal = false,
            size = "M",
            includeToday = true
        )

        // Must include: Day -3 (20), Day -2 (20 - carried forward), Day -1 (15), Day 0 / today (15 - carried forward)
        assertEquals("Should forward fill all 4 days from Day -3 to Today", 4, result.size)
        assertEquals("Day -3 stock", 20, result[0].second)
        assertEquals("Day -2 stock (forward-filled)", 20, result[1].second)
        assertEquals("Day -1 stock", 15, result[2].second)
        assertEquals("Day 0 / Today stock (forward-filled)", 15, result[3].second)
    }

    @Test
    fun testHistoryGrouping_resolveFinalValuePerDay_fallbackStockWhenNoLogs() {
        val todayMidnight = HistoryGrouping.getMidnightMs(System.currentTimeMillis())
        val result = HistoryGrouping.resolveFinalValuePerDay(
            logs = emptyList(),
            isTotal = true,
            size = null,
            includeToday = true,
            fallbackStock = 42
        )

        assertEquals("Fallback stock should produce 1 point for today", 1, result.size)
        assertEquals("Timestamp should be today midnight", todayMidnight, result[0].first)
        assertEquals("Stock should match fallback", 42, result[0].second)
    }

    @Test
    fun testHistoryGrouping_computeConsolidatedDayPages_alwaysIncludesToday() {
        val garments = listOf(
            GarmentItem(
                id = 1L,
                categoryName = "Boutique Dress",
                subStyle = "Linen Shift",
                sizeVariations = listOf("S", "M"),
                sizeTypeCategory = "Standard Alpha",
                stockPerSize = mapOf("S" to 10, "M" to 15),
                totalStock = 25
            )
        )

        val pages = HistoryGrouping.computeConsolidatedDayPages(garments, emptyList())

        assertFalse("Pages should not be empty", pages.isEmpty())
        val todayPage = pages.first()
        assertTrue("Today's page should be marked carried forward when no logs exist", todayPage.isCarriedForward)
        assertEquals("Should contain 1 garment entry", 1, todayPage.entries.size)
        assertEquals("Boutique Dress - Linen Shift", todayPage.entries[0].garmentName)
        assertEquals(10, todayPage.entries[0].sizeToStock["S"])
        assertEquals(15, todayPage.entries[0].sizeToStock["M"])
    }
}
