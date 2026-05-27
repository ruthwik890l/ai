package com.example.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.data.model.StartupAnalysis
import com.example.data.model.StartupReport
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    fun exportReportToPdf(context: Context, report: StartupReport, analysis: StartupAnalysis): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page dimensions
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = android.graphics.Color.rgb(33, 33, 33)
                textSize = 24f
                isFakeBoldText = true
            }
            val subtitlePaint = Paint().apply {
                color = android.graphics.Color.rgb(100, 100, 100)
                textSize = 11f
            }
            val headerPaint = Paint().apply {
                color = android.graphics.Color.rgb(10, 80, 180)
                textSize = 14f
                isFakeBoldText = true
            }
            val textPaint = Paint().apply {
                color = android.graphics.Color.rgb(50, 50, 50)
                textSize = 10f
            }

            var y = 60f
            canvas.drawText("AI VALUE VALIDATION SHEET", 50f, y, titlePaint)
            y += 24f
            canvas.drawText("Generated Synthetically by AI Startup Validator", 50f, y, subtitlePaint)
            y += 35f

            canvas.drawText("1. BASIC PROFILE", 50f, y, headerPaint)
            y += 20f
            canvas.drawText("Startup Name: ${report.startupName}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("One-Sentence Idea: ${report.idea}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Core Problem Target: ${report.problem}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Vertical Industry Focus: ${report.industry}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Development Stage: ${report.stage}", 50f, y, textPaint)
            y += 35f

            canvas.drawText("2. STRATEGIC ANALYSIS VERDICT", 50f, y, headerPaint)
            y += 20f
            canvas.drawText("Validation Score: ${report.validationScore}/100", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Market Potential: ${analysis.marketPotential}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Level of Local Competition: ${analysis.competitionLevel}", 50f, y, textPaint)
            y += 35f

            canvas.drawText("3. MARKET OPPORTUNITY (TAM / SAM / SOM)", 50f, y, headerPaint)
            y += 20f
            canvas.drawText("TAM (Total Addressable Market): ${analysis.marketOpportunity.tam}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("SAM (Serviceable Addressable Market): ${analysis.marketOpportunity.sam}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("SOM (Serviceable Obtainable Market): ${analysis.marketOpportunity.som}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Growth Trend & Potential: ${analysis.marketOpportunity.growthPotential}", 50f, y, textPaint)
            y += 35f

            canvas.drawText("4. SWAT BRIEF", 50f, y, headerPaint)
            y += 20f
            canvas.drawText("Strengths: ${analysis.swot.strengths.joinToString(", ")}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Weaknesses: ${analysis.swot.weaknesses.joinToString(", ")}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Opportunities: ${analysis.swot.opportunities.joinToString(", ")}", 50f, y, textPaint)
            y += 16f
            canvas.drawText("Threats: ${analysis.swot.threats.joinToString(", ")}", 50f, y, textPaint)
            y += 40f

            canvas.drawText("* End of Venture Validation Sheet. Saved to Documents/Startup_Validator_*", 50f, y, subtitlePaint)

            pdfDocument.finishPage(page)

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(dir, "Startup_Validator_${report.startupName.replace(" ", "_")}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
