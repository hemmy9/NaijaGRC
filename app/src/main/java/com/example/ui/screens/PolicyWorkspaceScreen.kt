package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.ChecklistItem
import com.example.domain.GeneratedPolicy
import com.example.domain.OrganizationProfile
import com.example.domain.PolicySection
import com.example.domain.PolicyType
import com.example.domain.PolicyVersionEntry
import com.example.ui.GrcUiState
import com.example.ui.WorkspaceTab
import com.example.ui.components.Spatial3DButton
import com.example.ui.components.Spatial3DCard
import com.example.ui.components.Spatial3DPolicyDeck
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Sapphire700

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PolicyWorkspaceScreen(
    uiState: GrcUiState,
    onBackToForm: () -> Unit,
    onSelectPolicy: (PolicyType) -> Unit,
    onSelectTab: (WorkspaceTab) -> Unit,
    onToggleChecklistItem: (PolicyType, String) -> Unit,
    onSearchChange: (String) -> Unit,
    onCopyPolicy: (GeneratedPolicy, OrganizationProfile) -> Unit,
    onCreateRevision: (PolicyType, String, String) -> Unit,
    onRevertVersion: (PolicyType, String) -> Unit,
    onExportActivePdf: (GeneratedPolicy) -> Unit,
    onExportBundlePdf: () -> Unit,
    onShareBundlePdf: () -> Unit,
    onDismissStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToForm() }

    val activePolicy = uiState.activePolicy ?: return
    val profile = uiState.generatedProfile ?: return
    var showMatrix by remember { mutableStateOf(true) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showRevisionDialog by remember { mutableStateOf(false) }

    val availableTypes = remember(uiState.generatedPolicies) {
        uiState.generatedPolicies.map { it.type }
    }

    val historyEntries = uiState.activePolicyHistory
    val activeVersionEntry = uiState.activeVersionEntry

    val checklistItems = activePolicy.checklist
    val completedCount = remember(checklistItems, uiState.checkedChecklistIds, activePolicy.type) {
        checklistItems.count { "${activePolicy.type.name}_${it.id}" in uiState.checkedChecklistIds }
    }

    val filteredSections = remember(activePolicy, uiState.clauseSearchQuery) {
        val q = uiState.clauseSearchQuery.trim().lowercase()
        if (q.isEmpty()) {
            activePolicy.sections
        } else {
            activePolicy.sections.filter { sec ->
                sec.title.lowercase().contains(q) ||
                    sec.content.lowercase().contains(q) ||
                    sec.controlTags.any { it.lowercase().contains(q) }
            }
        }
    }

    if (showHistoryDialog) {
        VersionHistoryDialog(
            policyTitle = activePolicy.type.fullTitle,
            activeVersionLabel = activePolicy.version,
            entries = historyEntries,
            onRevert = { versionLabel ->
                onRevertVersion(activePolicy.type, versionLabel)
                showHistoryDialog = false
            },
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (showRevisionDialog) {
        CreateRevisionDialog(
            policyTitle = activePolicy.type.shortTitle,
            nextVersionLabel = "v1.${historyEntries.size}",
            onConfirm = { changeNote, addendum ->
                onCreateRevision(activePolicy.type, changeNote, addendum)
                showRevisionDialog = false
            },
            onDismiss = { showRevisionDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
        ) {
            // Top Navigation & Company Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onBackToForm,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("back_to_form_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_edit_profile)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.btn_edit_profile))
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "${profile.companyName} • ${profile.industry.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = { onCopyPolicy(activePolicy, profile) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("copy_policy_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = stringResource(R.string.btn_copy_policy),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Status / Feedback Banner
            AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.statusBannerMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        TextButton(onClick = onDismissStatus) {
                            Text("OK")
                        }
                    }
                }
            }

            if (uiState.isExportingPdf) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // 3D Interactive Policy Deck Switcher (1 to 5 selected policies)
            Spatial3DPolicyDeck(
                availableTypes = availableTypes,
                selectedType = activePolicy.type,
                versionByType = uiState.activeVersionByType,
                onSelectPolicy = onSelectPolicy,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // 3D Segmented Mode Switcher: Policy Clauses vs Compliance Checklist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WorkspaceModePill(
                    title = "Policy Clauses (${activePolicy.sections.size})",
                    selected = uiState.workspaceTab == WorkspaceTab.CLAUSES,
                    onClick = { onSelectTab(WorkspaceTab.CLAUSES) },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_policy_clauses"
                )
                WorkspaceModePill(
                    title = "Action Checklist ($completedCount/${checklistItems.size})",
                    selected = uiState.workspaceTab == WorkspaceTab.CHECKLIST,
                    onClick = { onSelectTab(WorkspaceTab.CHECKLIST) },
                    modifier = Modifier.weight(1f),
                    testTag = "tab_action_checklist"
                )
            }

            // Scrollable Active Policy Content or Checklist
            AnimatedContent(
                targetState = Pair(activePolicy, uiState.workspaceTab),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
                label = "active_policy_content"
            ) { (policy, currentTab) ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Document Header + Version Control Card
                    item(key = "doc_header_${policy.id}_${policy.version}") {
                        Spatial3DCard(
                            depth = 6.dp,
                            cornerRadius = 22.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${policy.documentCode} • ${policy.version}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "Effective: ${policy.effectiveDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = policy.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = policy.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Version Control Bar
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "VERSION CONTROL • ${policy.version.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (activeVersionEntry != null) {
                                                Text(
                                                    text = "${activeVersionEntry.timestamp} — ${activeVersionEntry.changeSummary}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showHistoryDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .testTag("version_history_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.History,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.btn_version_history, historyEntries.size),
                                                maxLines = 1
                                            )
                                        }

                                        Button(
                                            onClick = { showRevisionDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .testTag("new_revision_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.EditNote,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.btn_new_revision),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NDPA 2023 & ISO 27001 Alignment",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { showMatrix = !showMatrix },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (showMatrix) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Toggle compliance matrix"
                                    )
                                }
                            }

                            AnimatedVisibility(visible = showMatrix) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    policy.ndpaReferences.forEach { ref ->
                                        ControlTagPill(
                                            text = "${ref.framework} ${ref.code}",
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    policy.iso27001Controls.forEach { ref ->
                                        ControlTagPill(
                                            text = "ISO ${ref.code}",
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            textColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (currentTab == WorkspaceTab.CLAUSES) {
                        item(key = "clause_search") {
                            OutlinedTextField(
                                value = uiState.clauseSearchQuery,
                                onValueChange = onSearchChange,
                                placeholder = { Text("Filter clauses or controls (e.g. 72 hours, RTO, MFA)") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("clause_search_input")
                            )
                        }

                        items(
                            items = filteredSections,
                            key = { "${policy.id}_${policy.version}_${it.number}" }
                        ) { section ->
                            PolicyClauseCard(section = section)
                        }

                        if (filteredSections.isEmpty()) {
                            item(key = "empty_search") {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No policy clauses match '${uiState.clauseSearchQuery}'. Clear filter to view all clauses.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Actionable Compliance Checklist View
                        item(key = "checklist_progress_${policy.id}") {
                            val total = policy.checklist.size.coerceAtLeast(1)
                            val progress = completedCount.toFloat() / total.toFloat()
                            val pct = (progress * 100).toInt()
                            Spatial3DCard(
                                depth = 5.dp,
                                cornerRadius = 18.dp,
                                surfaceColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Implementation Readiness",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$completedCount / ${policy.checklist.size} Done ($pct%)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }

                        items(
                            items = policy.checklist,
                            key = { "${policy.id}_chk_${it.id}" }
                        ) { item ->
                            val isChecked = "${policy.type.name}_${item.id}" in uiState.checkedChecklistIds
                            ChecklistActionCard(
                                item = item,
                                isChecked = isChecked,
                                onToggle = { onToggleChecklistItem(policy.type, item.id) }
                            )
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // 3D PDF Export Dock
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spatial3DButton(
                        text = stringResource(R.string.btn_export_bundle_pdf, uiState.generatedPolicies.size),
                        onClick = onExportBundlePdf,
                        icon = Icons.Filled.PictureAsPdf,
                        containerColor = Emerald700,
                        plinthColor = Emerald900,
                        enabled = !uiState.isExportingPdf,
                        testTag = "export_bundle_pdf_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Spatial3DButton(
                            text = stringResource(R.string.btn_export_active_pdf),
                            onClick = { onExportActivePdf(activePolicy) },
                            icon = Icons.Filled.PictureAsPdf,
                            containerColor = Sapphire700,
                            plinthColor = Color(0xFF1E3A8A),
                            enabled = !uiState.isExportingPdf,
                            testTag = "export_active_pdf_button",
                            modifier = Modifier.weight(1f)
                        )
                        Spatial3DButton(
                            text = stringResource(R.string.btn_share_pdf),
                            onClick = onShareBundlePdf,
                            icon = Icons.Filled.Share,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            plinthColor = Color(0xFF78350F),
                            enabled = !uiState.isExportingPdf,
                            testTag = "share_pdf_button",
                            modifier = Modifier.weight(0.78f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceModePill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChecklistActionCard(
    item: ChecklistItem,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Spatial3DCard(
        depth = 4.dp,
        cornerRadius = 18.dp,
        surfaceColor = if (isChecked) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clickable(onClick = onToggle)
                .testTag("checklist_item_${item.id}")
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = if (isChecked) "Completed" else "Mark completed",
                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.actionTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.guidance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ControlTagPill(
                        text = item.controlCode,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    ControlTagPill(
                        text = "Owner: ${item.ownerRole}",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ControlTagPill(
                        text = item.timeframe,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionHistoryDialog(
    policyTitle: String,
    activeVersionLabel: String,
    entries: List<PolicyVersionEntry>,
    onRevert: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Version History",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = policyTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entries.forEach { entry ->
                    val isCurrent = entry.versionLabel == activeVersionLabel
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${entry.versionLabel} ${if (isCurrent) "(Active)" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = entry.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = entry.changeSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${entry.profileSnapshot.companyName} • ${entry.profileSnapshot.size.label} • ${entry.profileSnapshot.industry.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!isCurrent) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { onRevert(entry.versionLabel) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("revert_version_${entry.versionLabel}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Restore,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Revert to ${entry.versionLabel}")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun CreateRevisionDialog(
    policyTitle: String,
    nextVersionLabel: String,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var changeNote by remember { mutableStateOf("") }
    var customAddendum by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log $nextVersionLabel — $policyTitle",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Record a change description and optional custom governance clause for audit traceability.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = changeNote,
                    onValueChange = { changeNote = it },
                    label = { Text("Description of changes") },
                    placeholder = { Text("e.g. Updated MFA timeout for CBN audit") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("revision_note_input")
                )
                OutlinedTextField(
                    value = customAddendum,
                    onValueChange = { customAddendum = it },
                    label = { Text("Custom Addendum Clause (Optional)") },
                    placeholder = { Text("e.g. All third-party API gateways require mTLS certificates.") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("revision_addendum_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(changeNote, customAddendum) },
                modifier = Modifier.testTag("confirm_revision_button")
            ) {
                Text("Save $nextVersionLabel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PolicyClauseCard(section: PolicySection) {
    Spatial3DCard(
        depth = 4.dp,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = section.number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = section.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            section.controlTags.forEach { tag ->
                ControlTagPill(
                    text = tag,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ControlTagPill(
    text: String,
    containerColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(0.8.dp, textColor.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
