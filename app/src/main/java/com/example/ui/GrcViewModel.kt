package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.domain.CompanySize
import com.example.domain.GeneratedPolicy
import com.example.domain.IndustrySector
import com.example.domain.OrganizationProfile
import com.example.domain.PolicyGeneratorEngine
import com.example.domain.PolicySection
import com.example.domain.PolicyType
import com.example.domain.PolicyVersionEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class WorkspaceScreen {
    FORM,
    POLICIES
}

enum class WorkspaceTab {
    CLAUSES,
    CHECKLIST
}

data class GrcUiState(
    val companyName: String = "",
    val selectedSize: CompanySize = CompanySize.SME,
    val selectedIndustry: IndustrySector = IndustrySector.FINTECH,
    val dpoEmail: String = "",
    val selectedPolicyTypes: Set<PolicyType> = setOf(
        PolicyType.DATA_PROTECTION,
        PolicyType.ACCEPTABLE_USE,
        PolicyType.PASSWORD
    ),
    val companyNameError: Boolean = false,
    val policySelectionError: Boolean = false,
    val activeScreen: WorkspaceScreen = WorkspaceScreen.FORM,
    val workspaceTab: WorkspaceTab = WorkspaceTab.CLAUSES,
    val generatedPolicies: List<GeneratedPolicy> = emptyList(),
    val generatedProfile: OrganizationProfile? = null,
    val selectedPolicyType: PolicyType = PolicyType.DATA_PROTECTION,
    val versionHistoryByType: Map<PolicyType, List<PolicyVersionEntry>> = emptyMap(),
    val activeVersionByType: Map<PolicyType, String> = emptyMap(),
    val checkedChecklistIds: Set<String> = emptySet(),
    val clauseSearchQuery: String = "",
    val isExportingPdf: Boolean = false,
    val statusBannerMessage: String? = null
) {
    val activePolicy: GeneratedPolicy?
        get() = generatedPolicies.firstOrNull { it.type == selectedPolicyType }
            ?: generatedPolicies.firstOrNull()

    val activePolicyHistory: List<PolicyVersionEntry>
        get() = activePolicy?.let { versionHistoryByType[it.type].orEmpty() }.orEmpty()

    val activeVersionEntry: PolicyVersionEntry?
        get() {
            val policy = activePolicy ?: return null
            val activeVer = activeVersionByType[policy.type] ?: policy.version
            return versionHistoryByType[policy.type]?.firstOrNull { it.versionLabel == activeVer }
        }
}

class GrcViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GrcUiState())
    val uiState: StateFlow<GrcUiState> = _uiState.asStateFlow()

    fun onCompanyNameChange(value: String) {
        _uiState.update {
            it.copy(
                companyName = value,
                companyNameError = if (value.trim().length >= 2) false else it.companyNameError
            )
        }
    }

    fun onSizeSelected(size: CompanySize) {
        _uiState.update { it.copy(selectedSize = size) }
    }

    fun onIndustrySelected(industry: IndustrySector) {
        _uiState.update { it.copy(selectedIndustry = industry) }
    }

    fun onDpoEmailChange(value: String) {
        _uiState.update { it.copy(dpoEmail = value) }
    }

    fun togglePolicyTypeSelection(type: PolicyType) {
        _uiState.update { current ->
            val updated = if (type in current.selectedPolicyTypes) {
                current.selectedPolicyTypes - type
            } else {
                current.selectedPolicyTypes + type
            }
            current.copy(
                selectedPolicyTypes = updated,
                policySelectionError = updated.isEmpty()
            )
        }
    }

    fun selectAllPolicyTypes() {
        _uiState.update {
            it.copy(
                selectedPolicyTypes = PolicyType.entries.toSet(),
                policySelectionError = false
            )
        }
    }

    fun selectCoreThreePolicies() {
        _uiState.update {
            it.copy(
                selectedPolicyTypes = setOf(
                    PolicyType.DATA_PROTECTION,
                    PolicyType.ACCEPTABLE_USE,
                    PolicyType.PASSWORD
                ),
                policySelectionError = false
            )
        }
    }

    fun applyQuickPreset(name: String, size: CompanySize, industry: IndustrySector, email: String) {
        _uiState.update {
            it.copy(
                companyName = name,
                selectedSize = size,
                selectedIndustry = industry,
                dpoEmail = email,
                companyNameError = false
            )
        }
    }

    fun generatePolicies() {
        val current = _uiState.value
        val trimmedName = current.companyName.trim()
        val hasNameError = trimmedName.length < 2
        val hasSelectionError = current.selectedPolicyTypes.isEmpty()

        if (hasNameError || hasSelectionError) {
            _uiState.update {
                it.copy(
                    companyNameError = hasNameError,
                    policySelectionError = hasSelectionError
                )
            }
            return
        }

        val profile = OrganizationProfile(
            companyName = trimmedName,
            size = current.selectedSize,
            industry = current.selectedIndustry,
            dpoEmail = current.dpoEmail.trim()
        )

        val timestamp = currentTimestamp()
        val updatedHistory = current.versionHistoryByType.toMutableMap()
        val updatedActiveVersions = current.activeVersionByType.toMutableMap()
        val nextVersionMap = mutableMapOf<PolicyType, String>()

        current.selectedPolicyTypes.forEach { type ->
            val existing = updatedHistory[type].orEmpty()
            val nextVer = if (existing.isEmpty()) "v1.0" else "v1.${existing.size}"
            nextVersionMap[type] = nextVer
        }

        val suite = PolicyGeneratorEngine.generateSuite(
            profile = profile,
            selectedTypes = current.selectedPolicyTypes,
            versionMap = nextVersionMap
        )

        suite.forEach { policy ->
            val existing = updatedHistory[policy.type].orEmpty()
            val prevProfile = existing.firstOrNull()?.profileSnapshot
            val changeNote = if (existing.isEmpty()) {
                "Initial generation (${profile.size.label}, ${profile.industry.label})"
            } else {
                PolicyGeneratorEngine.computeChangeSummary(prevProfile, profile)
            }
            val entry = PolicyVersionEntry(
                versionLabel = policy.version,
                timestamp = timestamp,
                changeSummary = changeNote,
                policy = policy,
                profileSnapshot = profile
            )
            // Store newest first for intuitive timeline display
            updatedHistory[policy.type] = listOf(entry) + existing
            updatedActiveVersions[policy.type] = policy.version
        }

        val firstType = suite.firstOrNull()?.type ?: PolicyType.DATA_PROTECTION
        val nextSelectedType = if (current.selectedPolicyType in current.selectedPolicyTypes) {
            current.selectedPolicyType
        } else {
            firstType
        }

        _uiState.update {
            it.copy(
                companyNameError = false,
                policySelectionError = false,
                generatedProfile = profile,
                generatedPolicies = suite,
                selectedPolicyType = nextSelectedType,
                versionHistoryByType = updatedHistory,
                activeVersionByType = updatedActiveVersions,
                clauseSearchQuery = "",
                activeScreen = WorkspaceScreen.POLICIES,
                workspaceTab = WorkspaceTab.CLAUSES,
                statusBannerMessage = if (updatedHistory.values.any { list -> list.size > 1 }) {
                    "Policies updated & new versions logged in version control."
                } else null
            )
        }
    }

    fun createManualRevision(
        type: PolicyType,
        changeNote: String,
        customAddendum: String
    ) {
        val current = _uiState.value
        val activePolicy = current.generatedPolicies.firstOrNull { it.type == type } ?: return
        val profile = current.generatedProfile ?: return

        val existing = current.versionHistoryByType[type].orEmpty()
        val nextVer = "v1.${existing.size}"
        val cleanNote = changeNote.trim().ifEmpty { "Manual governance revision" }
        val cleanAddendum = customAddendum.trim()

        val updatedSections = if (cleanAddendum.isNotEmpty()) {
            val nextNum = "${activePolicy.sections.size + 1}.0"
            activePolicy.sections + PolicySection(
                number = nextNum,
                title = "Organization-Specific Addendum ($nextVer)",
                content = cleanAddendum,
                controlTags = listOf("Custom Governance", "NDPA 2023", "ISO 27001")
            )
        } else {
            activePolicy.sections
        }

        val revisedPolicy = activePolicy.copy(
            version = nextVer,
            sections = updatedSections
        )

        val entry = PolicyVersionEntry(
            versionLabel = nextVer,
            timestamp = currentTimestamp(),
            changeSummary = cleanNote,
            policy = revisedPolicy,
            profileSnapshot = profile
        )

        val updatedHistory = current.versionHistoryByType.toMutableMap().apply {
            put(type, listOf(entry) + existing)
        }
        val updatedActiveVersions = current.activeVersionByType.toMutableMap().apply {
            put(type, nextVer)
        }
        val updatedPolicies = current.generatedPolicies.map {
            if (it.type == type) revisedPolicy else it
        }

        _uiState.update {
            it.copy(
                generatedPolicies = updatedPolicies,
                versionHistoryByType = updatedHistory,
                activeVersionByType = updatedActiveVersions,
                statusBannerMessage = "${type.shortTitle} updated to $nextVer ($cleanNote)."
            )
        }
    }

    fun revertToVersion(type: PolicyType, versionLabel: String) {
        val current = _uiState.value
        val history = current.versionHistoryByType[type].orEmpty()
        val targetEntry = history.firstOrNull { it.versionLabel == versionLabel } ?: return

        val updatedPolicies = current.generatedPolicies.map {
            if (it.type == type) targetEntry.policy else it
        }
        val updatedActiveVersions = current.activeVersionByType.toMutableMap().apply {
            put(type, targetEntry.versionLabel)
        }

        _uiState.update {
            it.copy(
                generatedPolicies = updatedPolicies,
                generatedProfile = targetEntry.profileSnapshot,
                activeVersionByType = updatedActiveVersions,
                statusBannerMessage = "Reverted ${type.shortTitle} to ${targetEntry.versionLabel} (${targetEntry.timestamp})."
            )
        }
    }

    fun selectPolicyType(type: PolicyType) {
        _uiState.update {
            it.copy(
                selectedPolicyType = type,
                clauseSearchQuery = ""
            )
        }
    }

    fun setWorkspaceTab(tab: WorkspaceTab) {
        _uiState.update { it.copy(workspaceTab = tab) }
    }

    fun toggleChecklistItem(type: PolicyType, itemId: String) {
        val key = "${type.name}_$itemId"
        _uiState.update { current ->
            val updated = if (key in current.checkedChecklistIds) {
                current.checkedChecklistIds - key
            } else {
                current.checkedChecklistIds + key
            }
            current.copy(checkedChecklistIds = updated)
        }
    }

    fun onClauseSearchChange(query: String) {
        _uiState.update { it.copy(clauseSearchQuery = query) }
    }

    fun navigateBackToForm() {
        _uiState.update {
            it.copy(
                activeScreen = WorkspaceScreen.FORM,
                statusBannerMessage = null
            )
        }
    }

    fun setExporting(isExporting: Boolean) {
        _uiState.update { it.copy(isExportingPdf = isExporting) }
    }

    fun showStatusMessage(message: String?) {
        _uiState.update { it.copy(statusBannerMessage = message) }
    }

    private fun currentTimestamp(): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH).format(Date())
    }
}
