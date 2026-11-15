package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.GapAnalysisReport
import com.example.domain.GeneratedPolicy
import com.example.domain.OrganizationProfile
import com.example.domain.PolicyType
import com.example.ui.GrcUiState
import com.example.ui.components.Spatial3DButton
import com.example.ui.theme.Bronze700
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold500
import com.example.ui.theme.Sapphire700

@Composable
fun ExportPreviewScreen(
    uiState: GrcUiState,
    onClose: () -> Unit,
    onSetBundleMode: (Boolean) -> Unit,
    onToggleIncludeChecklists: (Boolean) -> Unit,
    onToggleIncludeGapAnalysis: (Boolean) -> Unit,
    onSavePdf: (List<GeneratedPolicy>, Boolean, GapAnalysisReport?) -> Unit,
    onSharePdf: (List<GeneratedPolicy>, Boolean, GapAnalysisReport?) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val profile = uiState.generatedProfile ?: return
    val activePolicy = uiState.activePolicy ?: return
    val previewPolicies = if (uiState.previewBundleMode) {
        uiState.generatedPolicies
    } else {
        listOf(activePolicy)
    }
    val gapReportToInclude = if (uiState.previewIncludeGapAnalysis) uiState.gapReport else null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
        ) {
            // Preview Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_export_preview_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to workspace"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Export Preview Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${previewPolicies.size} policy doc(s) • A4 Vector Print Sheet",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(64.dp))
            }

            // Export Options Bar
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PreviewScopeChip(
                            label = "Full Suite (${uiState.generatedPolicies.size})",
                            selected = uiState.previewBundleMode,
                            onClick = { onSetBundleMode(true) },
                            modifier = Modifier.weight(1f),
                            testTag = "preview_scope_bundle"
                        )
                        PreviewScopeChip(
                            label = "Active (${activePolicy.type.shortTitle})",
                            selected = !uiState.previewBundleMode,
                            onClick = { onSetBundleMode(false) },
                            modifier = Modifier.weight(1f),
                            testTag = "preview_scope_single"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Switch(
                                checked = uiState.previewIncludeChecklists,
                                onCheckedChange = onToggleIncludeChecklists,
                                modifier = Modifier.testTag("preview_toggle_checklists")
                            )
                            Text(
                                text = "Checklists",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Switch(
                                checked = uiState.previewIncludeGapAnalysis,
                                onCheckedChange = onToggleIncludeGapAnalysis,
                                modifier = Modifier.testTag("preview_toggle_gaps")
                            )
                            Text(
                                text = "Gap Summary",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // A4 Paper Sheet Preview Feed
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = previewPolicies,
                    key = { _, p -> "preview_sheet_${p.id}_${p.version}" }
                ) { index, policy ->
                    A4PolicySheetPreview(
                        sheetIndex = index + 1,
                        profile = profile,
                        policy = policy,
                        includeChecklists = uiState.previewIncludeChecklists
                    )
                }

                if (gapReportToInclude != null) {
                    item(key = "preview_gap_sheet") {
                        A4GapSummarySheetPreview(
                            sheetIndex = previewPolicies.size + 1,
                            profile = profile,
                            report = gapReportToInclude
                        )
                    }
                }
            }

            // Preview Export Action Dock
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spatial3DButton(
                        text = "Save PDF",
                        onClick = {
                            onSavePdf(
                                previewPolicies,
                                uiState.previewIncludeChecklists,
                                gapReportToInclude
                            )
                        },
                        icon = Icons.Filled.PictureAsPdf,
                        containerColor = Emerald700,
                        plinthColor = Emerald900,
                        enabled = !uiState.isExportingPdf,
                        testTag = "preview_save_pdf_button",
                        modifier = Modifier.weight(1f)
                    )
                    Spatial3DButton(
                        text = "Share PDF",
                        onClick = {
                            onSharePdf(
                                previewPolicies,
                                uiState.previewIncludeChecklists,
                                gapReportToInclude
                            )
                        },
                        icon = Icons.Filled.Share,
                        containerColor = Sapphire700,
                        plinthColor = Color(0xFF1E3A8A),
                        enabled = !uiState.isExportingPdf,
                        testTag = "preview_share_pdf_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewScopeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun A4PolicySheetPreview(
    sheetIndex: Int,
    profile: OrganizationProfile,
    policy: GeneratedPolicy,
    includeChecklists: Boolean
) {
    val headerColor = when (policy.type) {
        PolicyType.DATA_PROTECTION -> Color(0xFF063B27)
        PolicyType.ACCEPTABLE_USE -> Sapphire700
        PolicyType.PASSWORD -> Bronze700
        PolicyType.INCIDENT_RESPONSE -> Color(0xFFB91C1C)
        PolicyType.BUSINESS_CONTINUITY -> Color(0xFF0F766E)
    }

    // crisp white A4 paper simulation with 3D drop shadow
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD6E2DC))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // PDF Header Band
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "NAIJAGRC COMPLIANCE SUITE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD1FAE5)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = policy.type.fullTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${profile.companyName}  •  ${policy.documentCode}  •  ${policy.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD1FAE5)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Gold500)
            )

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metadata Box
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF6FAF8),
                    border = BorderStroke(1.dp, Color(0xFFD6E2DC))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "ORGANIZATION: ${profile.companyName} (${profile.size.label})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF0F231B)
                        )
                        Text(
                            text = "INDUSTRY: ${profile.industry.label}  •  EFFECTIVE: ${policy.effectiveDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF52665E)
                        )
                        Text(
                            text = "DPO: ${profile.dpoEmail}  •  NDPA 2023 + ISO/IEC 27001:2022",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald700
                        )
                    }
                }

                Text(
                    text = policy.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0F231B)
                )

                policy.sections.forEach { sec ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFECFDF5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${sec.number}  ${sec.title}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF0F231B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = sec.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1F362C)
                        )
                        Text(
                            text = "Controls: ${sec.controlTags.joinToString(" | ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald700
                        )
                    }
                }

                if (includeChecklists && policy.checklist.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFD6E2DC))
                    Text(
                        text = "ACTIONABLE COMPLIANCE IMPLEMENTATION CHECKLIST",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald700
                    )
                    policy.checklist.forEachIndexed { idx, item ->
                        Text(
                            text = "${idx + 1}. [  ] ${item.actionTitle} (${item.controlCode} • ${item.ownerRole})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF0F231B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFD6E2DC))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NaijaGRC Policy Studio • NDPA 2023 & ISO 27001",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF52665E)
                    )
                    Text(
                        text = "Sheet $sheetIndex",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF52665E)
                    )
                }
            }
        }
    }
}

@Composable
private fun A4GapSummarySheetPreview(
    sheetIndex: Int,
    profile: OrganizationProfile,
    report: GapAnalysisReport
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD6E2DC))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "COMPLIANCE READINESS & GAP ANALYSIS SUMMARY",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF063B27)
            )
            Text(
                text = "${profile.companyName} • Readiness: ${report.overallReadinessPercent}% • Policy Coverage: ${report.policyCoverageCount}/${report.totalPolicyTemplates} • Controls: ${report.completedControls}/${report.totalControlsInGenerated}",
                style = MaterialTheme.typography.labelSmall,
                color = Emerald700
            )
            Text(
                text = "Open Gaps: ${report.criticalCount} Critical • ${report.highCount} High • ${report.mediumCount} Medium",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF52665E)
            )
            HorizontalDivider(color = Color(0xFFD6E2DC))
            report.gaps.take(10).forEachIndexed { idx, gap ->
                Text(
                    text = "${idx + 1}. [${gap.severity.label.uppercase()}] ${gap.title} (${gap.controlReference})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF0F231B)
                )
            }
            HorizontalDivider(color = Color(0xFFD6E2DC))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Executive Gap Appendix",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF52665E)
                )
                Text(
                    text = "Sheet $sheetIndex",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF52665E)
                )
            }
        }
    }
}
