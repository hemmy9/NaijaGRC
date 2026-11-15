package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.CompanySize
import com.example.domain.IndustrySector
import com.example.domain.PolicyType
import com.example.ui.GrcUiState
import com.example.ui.components.Spatial3DButton
import com.example.ui.components.Spatial3DCard
import com.example.ui.components.Spatial3DChip
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Gold400

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorFormScreen(
    uiState: GrcUiState,
    onCompanyNameChange: (String) -> Unit,
    onSizeSelected: (CompanySize) -> Unit,
    onIndustrySelected: (IndustrySector) -> Unit,
    onDpoEmailChange: (String) -> Unit,
    onTogglePolicyType: (PolicyType) -> Unit,
    onSelectAllPolicies: () -> Unit,
    onSelectCorePolicies: () -> Unit,
    onApplyPreset: (String, CompanySize, IndustrySector, String) -> Unit,
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 3D Hero Studio Card
            Spatial3DCard(
                depth = 8.dp,
                cornerRadius = 26.dp,
                surfaceColor = Emerald950,
                bevelColor = Emerald700,
                glowColor = Emerald700,
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_grc),
                        contentDescription = stringResource(R.string.hero_banner_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Emerald950.copy(alpha = 0.25f),
                                        Emerald950.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FrameworkBadgePill(text = "NDPA 2023", accent = Gold400)
                            FrameworkBadgePill(text = "ISO/IEC 27001", accent = Color(0xFF6EE7B7))
                            FrameworkBadgePill(text = "5 TEMPLATES", accent = Color.White)
                        }

                        Column {
                            Text(
                                text = "NaijaGRC Policy Studio",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select and generate board-ready policies with compliance checklists, version control & instant PDF export.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.86f)
                            )
                        }
                    }
                }
            }

            // Quick-Fill Sample Presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "QUICK PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetPill(
                        label = "Lagos Fintech (SME)",
                        onClick = {
                            onApplyPreset(
                                "ZaraPay Financial Technologies Ltd",
                                CompanySize.SME,
                                IndustrySector.FINTECH,
                                "dpo@zarapay.ng"
                            )
                        },
                        testTag = "preset_fintech"
                    )
                    PresetPill(
                        label = "Abuja Medical (Mid-Market)",
                        onClick = {
                            onApplyPreset(
                                "Apex Care Diagnostics Ltd",
                                CompanySize.MID_MARKET,
                                IndustrySector.HEALTHCARE,
                                "privacy@apexcare.ng"
                            )
                        },
                        testTag = "preset_health"
                    )
                    PresetPill(
                        label = "Enterprise Telco",
                        onClick = {
                            onApplyPreset(
                                "Sahel Fiber Networks Plc",
                                CompanySize.ENTERPRISE,
                                IndustrySector.TELECOM,
                                "grc@sahelfiber.ng"
                            )
                        },
                        testTag = "preset_telco"
                    )
                }
            }

            // Organization Profile & Policy Selection 3D Form Card
            Spatial3DCard(
                depth = 7.dp,
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.section_org_profile),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tailors NDPC tier classification, ISO controls & checklists",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Company Name Input
                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = onCompanyNameChange,
                    label = { Text(stringResource(R.string.label_company_name)) },
                    placeholder = { Text(stringResource(R.string.placeholder_company_name)) },
                    singleLine = true,
                    isError = uiState.companyNameError,
                    supportingText = if (uiState.companyNameError) {
                        { Text(stringResource(R.string.error_company_name)) }
                    } else null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("company_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Company Size 3D Selector Grid
                Text(
                    text = stringResource(R.string.label_company_size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val sizes = CompanySize.entries
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sizes.chunked(2).forEach { rowSizes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowSizes.forEach { size ->
                                Spatial3DChip(
                                    title = size.label,
                                    subtitle = size.headcountRange,
                                    selected = uiState.selectedSize == size,
                                    onClick = { onSizeSelected(size) },
                                    modifier = Modifier.weight(1f),
                                    testTag = "size_chip_${size.name.lowercase()}"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "NDPC Tier: ${uiState.selectedSize.ndpcTier}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Industry Sector 3D Selector Grid
                Text(
                    text = stringResource(R.string.label_industry),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val industries = IndustrySector.entries
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    industries.chunked(2).forEach { rowIndustries ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowIndustries.forEach { sector ->
                                Spatial3DChip(
                                    title = sector.label,
                                    subtitle = sector.regulatoryOverlay.substringBefore(" &").take(26),
                                    selected = uiState.selectedIndustry == sector,
                                    onClick = { onIndustrySelected(sector) },
                                    modifier = Modifier.weight(1f),
                                    testTag = "industry_chip_${sector.name.lowercase()}"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Overlay: ${uiState.selectedIndustry.regulatoryOverlay}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Policy Templates Multi-Select Grid (All 5 Templates)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_select_policies),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PresetPill(
                            label = "Core 3",
                            onClick = onSelectCorePolicies,
                            testTag = "select_core_policies"
                        )
                        PresetPill(
                            label = "Select All (5)",
                            onClick = onSelectAllPolicies,
                            testTag = "select_all_policies"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                val allTemplates = PolicyType.entries
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allTemplates.chunked(2).forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTypes.forEach { policyType ->
                                Spatial3DChip(
                                    title = policyType.shortTitle,
                                    subtitle = policyType.primaryFocus,
                                    selected = policyType in uiState.selectedPolicyTypes,
                                    onClick = { onTogglePolicyType(policyType) },
                                    modifier = Modifier.weight(1f),
                                    testTag = "template_chip_${policyType.name.lowercase()}"
                                )
                            }
                            if (rowTypes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (uiState.policySelectionError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.error_select_policies),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Optional DPO Email
                OutlinedTextField(
                    value = uiState.dpoEmail,
                    onValueChange = onDpoEmailChange,
                    label = { Text(stringResource(R.string.label_dpo_email)) },
                    placeholder = { Text(stringResource(R.string.placeholder_dpo_email)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onGenerateClick()
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dpo_email_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                val count = uiState.selectedPolicyTypes.size
                val ctaLabel = if (count == 1) {
                    stringResource(R.string.btn_generate_single_policy)
                } else {
                    stringResource(R.string.btn_generate_policies, count)
                }

                Spatial3DButton(
                    text = ctaLabel,
                    onClick = {
                        focusManager.clearFocus()
                        onGenerateClick()
                    },
                    icon = Icons.Filled.AutoAwesome,
                    containerColor = MaterialTheme.colorScheme.primary,
                    plinthColor = Emerald900,
                    testTag = "generate_policies_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FrameworkBadgePill(
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.75f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PresetPill(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}
