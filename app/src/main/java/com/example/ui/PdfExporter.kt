package com.example.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.GarmentItem
import com.example.data.HistoryLog
import com.example.data.DayHistoryPage
import com.example.data.ConsolidatedEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun exportToPdf(context: Context, items: List<GarmentItem>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        var yPosition = 50f
        val xPosition = 50f
        val lineHeight = 20f
        
        canvas.drawText("Garment Inventory Database", xPosition, yPosition, titlePaint)
        yPosition += lineHeight * 2
        
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        canvas.drawText("Generated at: $dateStr", xPosition, yPosition, paint)
        yPosition += lineHeight * 2

        for ((index, item) in items.withIndex()) {
            if (yPosition > 750f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            val stylePart = if (item.subStyle.isNotBlank()) " (${item.subStyle})" else ""
            canvas.drawText("${index + 1}. ${item.categoryName}$stylePart", xPosition, yPosition, titlePaint)
            yPosition += lineHeight

            canvas.drawText("   - Size Variations: ${item.sizeVariations.joinToString(", ")}", xPosition, yPosition, paint)
            yPosition += lineHeight
            
            if (item.handwrittenSizes.isNotEmpty()) {
                canvas.drawText("   - Handwritten: ${item.handwrittenSizes.joinToString(", ")}", xPosition, yPosition, paint)
                yPosition += lineHeight
            }
            if (item.isPantModel) {
                canvas.drawText("   - Type: Isolated Pant Model", xPosition, yPosition, paint)
                yPosition += lineHeight
            }
            
            if (item.stockPerSize.isNotEmpty()) {
                canvas.drawText("   - Size-Wise Stock:", xPosition, yPosition, paint)
                yPosition += lineHeight
                item.stockPerSize.entries.forEach { (key, value) ->
                    if (yPosition > 750f) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                    }
                    canvas.drawText("      • Size $key: $value units", xPosition, yPosition, paint)
                    yPosition += lineHeight
                }
            }
            
            canvas.drawText("   - Total Stock: ${item.totalStock}", xPosition, yPosition, paint)
            yPosition += lineHeight
            
            if (item.notes.isNotBlank()) {
                canvas.drawText("   - Notes: ${item.notes}", xPosition, yPosition, paint)
                yPosition += lineHeight
            }
            yPosition += lineHeight // extra space
        }
        
        document.finishPage(page)
        
        try {
            val fileName = "GarmentInventory_${dateStr.replace(":", "")}.pdf"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        document.writeTo(outputStream)
                    }
                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: Could not create file", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    document.writeTo(outputStream)
                }
                Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }

    // Bug 2: If nothing was changed today, export the carried-forward stock values instead of generating an empty page
    fun exportHistoryToPdf(
        context: Context,
        historyLogs: List<HistoryLog>,
        rangeLabel: String = "Today",
        currentItems: List<GarmentItem> = emptyList()
    ) {
        val filteredLogs = historyLogs.filter { it.actionType != "DELETE" }
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        var yPosition = 50f
        val xPosition = 50f
        val lineHeight = 20f
        
        canvas.drawText("Garment Inventory - $rangeLabel's History", xPosition, yPosition, titlePaint)
        yPosition += lineHeight * 2
        
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        canvas.drawText("Generated at: $dateStr", xPosition, yPosition, paint)
        yPosition += lineHeight * 2

        if (filteredLogs.isEmpty()) {
            canvas.drawText("No stock changes logged today — carried forward from previous entry:", xPosition, yPosition, titlePaint)
            yPosition += lineHeight * 1.5f

            for ((index, item) in currentItems.withIndex()) {
                if (yPosition > 750f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }
                val name = "${item.categoryName}${if (item.subStyle.isNotBlank()) " (${item.subStyle})" else ""}"
                canvas.drawText("${index + 1}. $name (Carried forward)", xPosition, yPosition, titlePaint)
                yPosition += lineHeight

                item.stockPerSize.forEach { (size, stock) ->
                    if (yPosition > 750f) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                    }
                    canvas.drawText("   • Size $size: $stock units", xPosition, yPosition, paint)
                    yPosition += lineHeight
                }
                canvas.drawText("   • Total Stock: ${item.totalStock}", xPosition, yPosition, paint)
                yPosition += lineHeight * 1.5f
            }
        } else {
            for ((index, log) in filteredLogs.withIndex()) {
                if (yPosition > 750f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                val logDateStr = SimpleDateFormat("HH:mm", Locale.US).format(Date(log.timestamp))
                canvas.drawText("${index + 1}. [$logDateStr] ${log.description}", xPosition, yPosition, titlePaint)
                yPosition += lineHeight

                if (log.size != null && log.stockValue != null) {
                    canvas.drawText("   • Size ${log.size}: ${log.stockValue} units", xPosition, yPosition, paint)
                    yPosition += lineHeight
                }

                if (log.totalStockValue != null) {
                    canvas.drawText("   • Total Stock: ${log.totalStockValue}", xPosition, yPosition, paint)
                    yPosition += lineHeight
                }

                yPosition += lineHeight // extra space
            }
        }
        
        document.finishPage(page)
        
        try {
            val fileName = "GarmentHistory_${rangeLabel}_${dateStr.replace(":", "")}.pdf"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        document.writeTo(outputStream)
                    }
                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: Could not create file", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    document.writeTo(outputStream)
                }
                Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }

    fun exportDayHistoryToPdf(context: Context, dayPage: DayHistoryPage) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        
        var yPosition = 50f
        val xPosition = 50f
        val lineHeight = 20f
        
        val dateHeaderStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(dayPage.dateMs))
        canvas.drawText("Garment Inventory - $dateHeaderStr", xPosition, yPosition, titlePaint)
        yPosition += lineHeight * 2
        
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        canvas.drawText("Generated at: $dateStr", xPosition, yPosition, paint)
        yPosition += lineHeight * 2

        for ((index, entry) in dayPage.entries.withIndex()) {
            if (yPosition > 750f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText("${index + 1}. ${entry.garmentName}", xPosition, yPosition, titlePaint)
            yPosition += lineHeight
            
            entry.sizeToStock.forEach { (size, stock) ->
                if (yPosition > 750f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }
                canvas.drawText("   • Size $size: $stock units", xPosition, yPosition, paint)
                yPosition += lineHeight
            }
            
            val total = entry.sizeToStock.values.sum()
            canvas.drawText("   • Total: $total units", xPosition, yPosition, titlePaint)
            yPosition += lineHeight * 2
        }
        
        document.finishPage(page)
        
        try {
            val fileName = "GarmentHistory_${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(dayPage.dateMs))}_${dateStr.replace(":", "")}.pdf"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        document.writeTo(outputStream)
                    }
                    Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: Could not create file", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    document.writeTo(outputStream)
                }
                Toast.makeText(context, "Saved to Downloads: ${file.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }
}


