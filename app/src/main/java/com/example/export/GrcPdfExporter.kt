package com.example.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.domain.GeneratedPolicy
import com.example.domain.OrganizationProfile
import com.example.domain.PolicyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Locale

object GrcPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_X = 44f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2f)
    private const val FOOTER_Y = PAGE_HEIGHT - 30f
    private const val MAX_CONTENT_Y = PAGE_HEIGHT - 56f

    private val emeraldDark = Color.rgb(6, 59, 39)
    private val emeraldAccent = Color.rgb(11, 110, 79)
    private val emeraldLight = Color.rgb(236, 253, 245)
    private val goldAccent = Color.rgb(217, 119, 6)
    private val inkDark = Color.rgb(15, 35, 27)
    private val inkMuted = Color.rgb(82, 102, 94)
    private val borderLight = Color.rgb(214, 226, 220)
    private val sapphireDark = Color.rgb(29, 78, 216)
    private val bronzeDark = Color.rgb(180, 83, 9)
    private val crimsonDark = Color.rgb(185, 28, 28)
    private val tealDark = Color.rgb(15, 118, 110)

    fun buildSuggestedFileName(
        companyName: String,
        policies: List<GeneratedPolicy>
    ): String {
        val slug = companyName.trim()
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "Organization" }
        return if (policies.size == 1) {
            val typeSlug = policies.first().type.shortTitle.replace(Regex("[^A-Za-z0-9]+"), "_")
            "${slug}_${typeSlug}_NDPA_ISO27001.pdf"
        } else {
            "${slug}_NaijaGRC_Compliance_Suite.pdf"
        }
    }

    suspend fun writePoliciesToUri(
        context: Context,
        uri: Uri,
        profile: OrganizationProfile,
        policies: List<GeneratedPolicy>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            resolver.openOutputStream(uri)?.use { stream ->
                renderPdfToStream(profile, policies, stream)
            } ?: error("Unable to open destination file stream.")
        }
    }

    suspend fun createShareablePdfUri(
        context: Context,
        profile: OrganizationProfile,
        policies: List<GeneratedPolicy>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val fileName = buildSuggestedFileName(profile.companyName, policies)
            val file = File(pdfDir, fileName)
            FileOutputStream(file).use { stream ->
                renderPdfToStream(profile, policies, stream)
            }
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
            )
        }
    }

    private fun renderPdfToStream(
        profile: OrganizationProfile,
        policies: List<GeneratedPolicy>,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        try {
            val state = PdfRenderState(document, profile)
            policies.forEachIndexed { index, policy ->
                if (index > 0) {
                    state.startNewPage(policy)
                } else {
                    state.startFirstPage(policy)
                }
                state.drawPolicyContent(policy)
            }
            state.finishCurrentPage()
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    private class PdfRenderState(
        private val document: PdfDocument,
        private val profile: OrganizationProfile
    ) {
        private var currentPage: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var pageNumber = 0
        private var cursorY = 0f

        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(209, 250, 229)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkDark
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkDark
            textSize = 9.8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        private val monoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = emeraldAccent
            textSize = 8.4f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        private val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = inkMuted
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = borderLight
        }

        fun startFirstPage(policy: GeneratedPolicy) {
            beginPage(policy, isCoverHeader = true)
        }

        fun startNewPage(policy: GeneratedPolicy) {
            finishCurrentPage()
            beginPage(policy, isCoverHeader = true)
        }

        fun finishCurrentPage() {
            val page = currentPage ?: return
            val c = canvas ?: return
            drawFooter(c)
            document.finishPage(page)
            currentPage = null
            canvas = null
        }

        private fun beginPage(policy: GeneratedPolicy, isCoverHeader: Boolean) {
            pageNumber += 1
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = document.startPage(pageInfo)
            currentPage = page
            val c = page.canvas
            canvas = c

            val accentColor = when (policy.type) {
                PolicyType.DATA_PROTECTION -> emeraldDark
                PolicyType.ACCEPTABLE_USE -> sapphireDark
                PolicyType.PASSWORD -> bronzeDark
                PolicyType.INCIDENT_RESPONSE -> crimsonDark
                PolicyType.BUSINESS_CONTINUITY -> tealDark
            }

            if (isCoverHeader) {
                fillPaint.color = accentColor
                c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 96f, fillPaint)

                fillPaint.color = goldAccent
                c.drawRect(0f, 96f, PAGE_WIDTH.toFloat(), 99f, fillPaint)

                c.drawText("NAIJAGRC COMPLIANCE SUITE", MARGIN_X, 28f, subtitlePaint)
                c.drawText(policy.type.fullTitle, MARGIN_X, 54f, titlePaint)
                c.drawText(
                    "${profile.companyName}  •  ${policy.documentCode}  •  ${policy.version}",
                    MARGIN_X,
                    76f,
                    subtitlePaint
                )
                cursorY = 118f
                drawMetadataCard(c, policy)
            } else {
                fillPaint.color = emeraldLight
                c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 40f, fillPaint)
                c.drawText(
                    "${profile.companyName} — ${policy.type.fullTitle} (${policy.documentCode})",
                    MARGIN_X,
                    25f,
                    mutedPaint
                )
                cursorY = 60f
            }
        }

        private fun drawMetadataCard(c: Canvas, policy: GeneratedPolicy) {
            val cardRect = RectF(MARGIN_X, cursorY, PAGE_WIDTH - MARGIN_X, cursorY + 76f)
            fillPaint.color = Color.rgb(246, 250, 248)
            c.drawRoundRect(cardRect, 8f, 8f, fillPaint)
            c.drawRoundRect(cardRect, 8f, 8f, strokePaint)

            val col2X = MARGIN_X + CONTENT_WIDTH * 0.52f
            c.drawText("ORGANIZATION: ${profile.companyName}", MARGIN_X + 12f, cursorY + 20f, headingPaint.apply { textSize = 9.5f })
            c.drawText("EFFECTIVE DATE: ${policy.effectiveDate}", col2X, cursorY + 20f, mutedPaint)
            c.drawText("SIZE TIER: ${profile.size.label} (${profile.size.headcountRange})", MARGIN_X + 12f, cursorY + 38f, mutedPaint)
            c.drawText("INDUSTRY: ${profile.industry.label}", col2X, cursorY + 38f, mutedPaint)
            c.drawText("DPO CONTACT: ${profile.dpoEmail}", MARGIN_X + 12f, cursorY + 56f, mutedPaint)
            c.drawText("FRAMEWORKS: NDPA 2023 + ISO/IEC 27001:2022", col2X, cursorY + 56f, monoPaint)

            headingPaint.textSize = 12.5f
            cursorY += 92f
        }

        fun drawPolicyContent(policy: GeneratedPolicy) {
            // Executive Summary
            ensureSpace(policy, 56f)
            drawWrappedText(policy, policy.summary, bodyPaint, MARGIN_X, CONTENT_WIDTH, 14f)
            cursorY += 10f

            // Compliance Frameworks Strip
            ensureSpace(policy, 70f)
            canvas?.drawText("REGULATORY & CONTROL MAPPING", MARGIN_X, cursorY, monoPaint)
            cursorY += 14f
            val allRefs = policy.ndpaReferences + policy.iso27001Controls
            allRefs.forEach { ref ->
                ensureSpace(policy, 18f)
                val line = "• [${ref.framework} ${ref.code}] ${ref.title}"
                drawWrappedText(policy, line, mutedPaint, MARGIN_X + 6f, CONTENT_WIDTH - 6f, 12.5f)
            }
            cursorY += 12f

            // Policy Clauses
            policy.sections.forEach { section ->
                ensureSpace(policy, 72f)
                canvas?.let { c ->
                    fillPaint.color = emeraldLight
                    val headerRect = RectF(MARGIN_X, cursorY - 12f, PAGE_WIDTH - MARGIN_X, cursorY + 8f)
                    c.drawRoundRect(headerRect, 4f, 4f, fillPaint)
                    c.drawText("${section.number}  ${section.title}", MARGIN_X + 8f, cursorY + 2f, headingPaint)
                }
                cursorY += 18f
                drawWrappedText(policy, section.content, bodyPaint, MARGIN_X, CONTENT_WIDTH, 14f)
                cursorY += 4f
                ensureSpace(policy, 18f)
                canvas?.drawText(
                    "Controls: ${section.controlTags.joinToString("  |  ")}",
                    MARGIN_X,
                    cursorY,
                    monoPaint
                )
                cursorY += 20f
            }

            // Actionable Compliance Implementation Checklist
            if (policy.checklist.isNotEmpty()) {
                ensureSpace(policy, 52f)
                canvas?.drawText("ACTIONABLE COMPLIANCE IMPLEMENTATION CHECKLIST", MARGIN_X, cursorY, monoPaint)
                cursorY += 16f
                policy.checklist.forEachIndexed { idx, item ->
                    ensureSpace(policy, 42f)
                    val headerLine = "${idx + 1}. [  ] ${item.actionTitle}  (${item.controlCode} • ${item.ownerRole} • ${item.timeframe})"
                    drawWrappedText(policy, headerLine, headingPaint.apply { textSize = 9.5f }, MARGIN_X, CONTENT_WIDTH, 13f)
                    drawWrappedText(policy, item.guidance, mutedPaint, MARGIN_X + 14f, CONTENT_WIDTH - 14f, 12f)
                    cursorY += 6f
                }
                headingPaint.textSize = 12.5f
                cursorY += 6f
            }

            // Governance Sign-Off Box
            ensureSpace(policy, 72f)
            canvas?.let { c ->
                val signRect = RectF(MARGIN_X, cursorY, PAGE_WIDTH - MARGIN_X, cursorY + 58f)
                fillPaint.color = Color.rgb(250, 252, 251)
                c.drawRoundRect(signRect, 6f, 6f, fillPaint)
                c.drawRoundRect(signRect, 6f, 6f, strokePaint)
                c.drawText(
                    "GOVERNANCE APPROVAL & ATTESTATION",
                    MARGIN_X + 12f,
                    cursorY + 18f,
                    monoPaint
                )
                c.drawText(
                    "Approved for ${profile.companyName}  •  DPO / CISO: ${profile.dpoEmail}",
                    MARGIN_X + 12f,
                    cursorY + 34f,
                    mutedPaint
                )
                c.drawText(
                    "Review Cycle: Annual / Upon Material Change (NDPA Sec. 44 & ISO 27001 Cl. 5.2)",
                    MARGIN_X + 12f,
                    cursorY + 48f,
                    mutedPaint
                )
                cursorY += 72f
            }
        }

        private fun ensureSpace(policy: GeneratedPolicy, neededHeight: Float) {
            if (cursorY + neededHeight > MAX_CONTENT_Y) {
                finishCurrentPage()
                beginPage(policy, isCoverHeader = false)
            }
        }

        private fun drawWrappedText(
            policy: GeneratedPolicy,
            text: String,
            paint: Paint,
            x: Float,
            maxWidth: Float,
            lineHeight: Float
        ) {
            val words = text.split(Regex("\\s+"))
            val lineBuilder = StringBuilder()
            for (word in words) {
                val candidate = if (lineBuilder.isEmpty()) word else "$lineBuilder $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    lineBuilder.clear()
                    lineBuilder.append(candidate)
                } else {
                    if (cursorY + lineHeight > MAX_CONTENT_Y) {
                        finishCurrentPage()
                        beginPage(policy, isCoverHeader = false)
                    }
                    canvas?.drawText(lineBuilder.toString(), x, cursorY, paint)
                    cursorY += lineHeight
                    lineBuilder.clear()
                    lineBuilder.append(word)
                }
            }
            if (lineBuilder.isNotEmpty()) {
                if (cursorY + lineHeight > MAX_CONTENT_Y) {
                    finishCurrentPage()
                    beginPage(policy, isCoverHeader = false)
                }
                canvas?.drawText(lineBuilder.toString(), x, cursorY, paint)
                cursorY += lineHeight
            }
        }

        private fun drawFooter(c: Canvas) {
            c.drawLine(MARGIN_X, FOOTER_Y - 12f, PAGE_WIDTH - MARGIN_X, FOOTER_Y - 12f, strokePaint)
            c.drawText(
                "Generated by NaijaGRC Policy Studio  •  Nigeria NDPA 2023 & ISO/IEC 27001:2022",
                MARGIN_X,
                FOOTER_Y,
                mutedPaint
            )
            val pageText = String.format(Locale.ENGLISH, "Page %d", pageNumber)
            val textWidth = mutedPaint.measureText(pageText)
            c.drawText(pageText, PAGE_WIDTH - MARGIN_X - textWidth, FOOTER_Y, mutedPaint)
        }
    }
}
