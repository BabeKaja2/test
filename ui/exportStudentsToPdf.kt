package com.babetech.ucb_admin_access.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.babetech.ucb_admin_access.viewmodel.StudentCached
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants for PDF layout
private const val PAGE_WIDTH = 595 // A4 width in points
private const val PAGE_HEIGHT = 842 // A4 height in points
private const val MARGIN_TOP = 60f
private const val MARGIN_LEFT = 40f
private const val MARGIN_RIGHT = 40f
private const val MARGIN_BOTTOM = 60f
private const val HEADER_HEIGHT = 80f // Space for title and date
private const val TABLE_HEADER_HEIGHT = 30f
private const val ROW_HEIGHT = 25f
private const val FONT_SIZE_TITLE = 24f
private const val FONT_SIZE_HEADER = 10f // Reduced font size for headers
private const val FONT_SIZE_BODY = 9f // Reduced font size for body text
private const val LINE_THICKNESS = 1f

fun exportStudentsToPdf(
    context: Context,
    students: List<StudentCached>
) {
    val document = PdfDocument()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    // Define paints for different text styles and lines
    val titlePaint = Paint().apply {
        textSize = FONT_SIZE_TITLE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }

    val headerPaint = Paint().apply {
        textSize = FONT_SIZE_HEADER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.BLACK
        textAlign = Paint.Align.CENTER // Centered for headers
    }

    val bodyPaint = Paint().apply {
        textSize = FONT_SIZE_BODY
        color = Color.BLACK
        textAlign = Paint.Align.CENTER // Centered for body text
    }

    val linePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = LINE_THICKNESS
    }

    val tableHeaders = listOf(
        "Matricule", "Nom Complet", "Filière", "Orientation", "Dernière MAJ"
    )

    // Calculate column widths dynamically based on page width and margins
    val availableWidth = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    val colWidths = floatArrayOf(
        availableWidth * 0.15f, // Matricule
        availableWidth * 0.30f, // Nom Complet
        availableWidth * 0.20f, // Filière
        availableWidth * 0.20f, // Orientation
        availableWidth * 0.15f  // Dernière MAJ
    )

    var pageNumber = 1
    var currentY = MARGIN_TOP + HEADER_HEIGHT // Starting Y for content after header

    fun startNewPage(doc: PdfDocument): Pair<PdfDocument.Page, Canvas> {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        // Draw document title
        canvas.drawText("Rapport des Étudiants UCB", PAGE_WIDTH / 2f, MARGIN_TOP, titlePaint)
        // Draw date
        canvas.drawText("Date: $currentDate", PAGE_WIDTH - MARGIN_RIGHT, MARGIN_TOP + 20f, bodyPaint.apply { textAlign = Paint.Align.RIGHT })
        // Draw page number
        canvas.drawText("Page $pageNumber", PAGE_WIDTH / 2f, PAGE_HEIGHT - MARGIN_BOTTOM / 2, bodyPaint.apply { textAlign = Paint.Align.CENTER })

        currentY = MARGIN_TOP + HEADER_HEIGHT

        // Draw table headers
        var xOffset = MARGIN_LEFT
        canvas.drawLine(MARGIN_LEFT, currentY, PAGE_WIDTH - MARGIN_RIGHT, currentY, linePaint) // Top border of header row
        tableHeaders.forEachIndexed { index, header ->
            // Draw header text centered within its column
            canvas.drawText(header, xOffset + colWidths[index] / 2f, currentY + TABLE_HEADER_HEIGHT / 2 + FONT_SIZE_HEADER / 2 - 2f, headerPaint)
            xOffset += colWidths[index]
        }
        canvas.drawLine(MARGIN_LEFT, currentY + TABLE_HEADER_HEIGHT, PAGE_WIDTH - MARGIN_RIGHT, currentY + TABLE_HEADER_HEIGHT, linePaint) // Bottom border of header row
        currentY += TABLE_HEADER_HEIGHT

        return Pair(page, canvas)
    }

    var (page, canvas) = startNewPage(document)

    students.forEach { student ->
        // Check if new page is needed for the next row
        if (currentY + ROW_HEIGHT > PAGE_HEIGHT - MARGIN_BOTTOM) {
            document.finishPage(page)
            pageNumber++
            val newPage = startNewPage(document)
            page = newPage.first
            canvas = newPage.second
        }

        var xOffset = MARGIN_LEFT
        val rowY = currentY + ROW_HEIGHT / 2 + FONT_SIZE_BODY / 2 - 2f // Center text vertically in row

        // Draw student data for each column, centered
        canvas.drawText(student.matricule, xOffset + colWidths[0] / 2f, rowY, bodyPaint)
        xOffset += colWidths[0]
        canvas.drawText(student.fullname, xOffset + colWidths[1] / 2f, rowY, bodyPaint)
        xOffset += colWidths[1]
        canvas.drawText(student.schoolFilieres ?: "-", xOffset + colWidths[2] / 2f, rowY, bodyPaint)
        xOffset += colWidths[2]
        canvas.drawText(student.schoolOrientations ?: "-", xOffset + colWidths[3] / 2f, rowY, bodyPaint)
        xOffset += colWidths[3]
        val lastFetchedText = student.lastFetched
            ?.let { dateFormat.format(Date(it)) }
            ?: "-"
        canvas.drawText(lastFetchedText, xOffset + colWidths[4] / 2f, rowY, bodyPaint)

        // Draw horizontal line after each row
        canvas.drawLine(MARGIN_LEFT, currentY + ROW_HEIGHT, PAGE_WIDTH - MARGIN_RIGHT, currentY + ROW_HEIGHT, linePaint)
        currentY += ROW_HEIGHT
    }

    document.finishPage(page)

    // Save the PDF file
    val docsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "")
    if (!docsDir.exists()) docsDir.mkdirs()
    val file = File(docsDir, "rapport_etudiants.pdf")
    FileOutputStream(file).use { out ->
        document.writeTo(out)
    }
    document.close()
}

