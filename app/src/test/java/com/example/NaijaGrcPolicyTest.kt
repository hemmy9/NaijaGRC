package com.example

import com.example.domain.CompanySize
import com.example.domain.IndustrySector
import com.example.domain.OrganizationProfile
import com.example.domain.PolicyGeneratorEngine
import com.example.domain.PolicyType
import com.example.export.GrcPdfExporter
import com.example.ui.GrcViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NaijaGrcPolicyTest {

    @Test
    fun `generates all five policies with checklists aligned to NDPA 2023 and ISO 27001`() {
        val profile = OrganizationProfile(
            companyName = "ZaraPay Nigeria Ltd",
            size = CompanySize.ENTERPRISE,
            industry = IndustrySector.FINTECH,
            dpoEmail = "dpo@zarapay.ng"
        )

        val policies = PolicyGeneratorEngine.generateSuite(profile, PolicyType.entries.toSet())

        assertEquals(5, policies.size)
        assertEquals(PolicyType.entries, policies.map { it.type })

        policies.forEach { policy ->
            assertTrue(policy.title.contains("ZaraPay Nigeria Ltd"))
            assertTrue(policy.ndpaReferences.isNotEmpty())
            assertTrue(policy.iso27001Controls.isNotEmpty())
            assertTrue(policy.sections.size >= 5)
            assertTrue(policy.checklist.size >= 5)
        }

        val irp = policies.first { it.type == PolicyType.INCIDENT_RESPONSE }
        assertTrue(irp.sections.any { it.content.contains("72 hours") })
        assertTrue(irp.checklist.any { it.controlCode.contains("NDPA Sec. 40") })

        val bcp = policies.first { it.type == PolicyType.BUSINESS_CONTINUITY }
        assertTrue(bcp.sections.any { it.content.contains("1 hour") && it.content.contains("15 minutes") })
        assertTrue(bcp.checklist.any { it.controlCode.contains("ISO A.8.13") })
    }

    @Test
    fun `viewModel supports selective generation, version logging, manual revision and revert`() {
        val vm = GrcViewModel()
        vm.onCompanyNameChange("NaijaHealth Ltd")
        vm.selectCoreThreePolicies()
        vm.togglePolicyTypeSelection(PolicyType.PASSWORD) // now Data Protection + Acceptable Use
        vm.togglePolicyTypeSelection(PolicyType.INCIDENT_RESPONSE) // now 3 selected
        vm.generatePolicies()

        val v1State = vm.uiState.value
        assertEquals(3, v1State.generatedPolicies.size)
        assertEquals(
            setOf(PolicyType.DATA_PROTECTION, PolicyType.ACCEPTABLE_USE, PolicyType.INCIDENT_RESPONSE),
            v1State.generatedPolicies.map { it.type }.toSet()
        )
        assertEquals("v1.0", v1State.activePolicy?.version)

        // Manual revision adds v1.1 with timestamp & change summary
        vm.createManualRevision(
            type = PolicyType.DATA_PROTECTION,
            changeNote = "Added CBN residency clause",
            customAddendum = "All KYC biometric logs must remain hosted in Nigeria."
        )
        val v2State = vm.uiState.value
        assertEquals("v1.1", v2State.activePolicy?.version)
        assertEquals(2, v2State.activePolicyHistory.size)
        assertEquals("Added CBN residency clause", v2State.activePolicyHistory.first().changeSummary)
        assertTrue(v2State.activePolicy?.sections?.last()?.content?.contains("mTLS") == false)
        assertTrue(v2State.activePolicy?.sections?.last()?.content?.contains("Nigeria") == true)

        // Revert back to v1.0
        vm.revertToVersion(PolicyType.DATA_PROTECTION, "v1.0")
        val revertedState = vm.uiState.value
        assertEquals("v1.0", revertedState.activePolicy?.version)
        assertEquals(6, revertedState.activePolicy?.sections?.size)
        assertEquals(2, revertedState.activePolicyHistory.size)
    }

    @Test
    fun `builds clean PDF filenames and formatted policy text with checklists`() {
        val profile = OrganizationProfile(
            companyName = "Lagos Health Hub",
            size = CompanySize.MID_MARKET,
            industry = IndustrySector.HEALTHCARE,
            dpoEmail = "privacy@lagoshealth.ng"
        )
        val policies = PolicyGeneratorEngine.generateSuite(profile, PolicyType.entries.toSet())

        val bundleName = GrcPdfExporter.buildSuggestedFileName(profile.companyName, policies)
        assertEquals("Lagos_Health_Hub_NaijaGRC_Compliance_Suite.pdf", bundleName)

        val singleName = GrcPdfExporter.buildSuggestedFileName(
            profile.companyName,
            listOf(policies.first { it.type == PolicyType.INCIDENT_RESPONSE })
        )
        assertEquals("Lagos_Health_Hub_Incident_Response_NDPA_ISO27001.pdf", singleName)

        val plainText = PolicyGeneratorEngine.formatPolicyPlainText(policies.first(), profile)
        assertTrue(plainText.contains("IMPLEMENTATION COMPLIANCE CHECKLIST"))
        assertTrue(plainText.contains("NDPA 2023"))
        assertTrue(plainText.contains("ISO 27001:2022"))
    }

    @Test
    fun `computes policy progress and gap analysis with missing-policy remediation`() {
        val vm = GrcViewModel()
        vm.onCompanyNameChange("PayNaija Ltd")
        vm.selectCoreThreePolicies() // 3 of 5 selected -> Incident Response & Business Continuity missing
        vm.generatePolicies()

        val initialGap = vm.uiState.value.gapReport!!
        assertEquals(3, initialGap.policyCoverageCount)
        assertEquals(5, initialGap.totalPolicyTemplates)
        assertEquals(0, initialGap.completedControls)
        assertTrue(initialGap.criticalCount >= 1)

        // Toggle 2 checklist items and verify readiness increases
        vm.toggleChecklistItem(PolicyType.DATA_PROTECTION, "dpa_1")
        vm.toggleChecklistItem(PolicyType.DATA_PROTECTION, "dpa_2")
        val updatedGap = vm.uiState.value.gapReport!!
        assertEquals(2, updatedGap.completedControls)
        assertTrue(updatedGap.overallReadinessPercent > initialGap.overallReadinessPercent)

        // Remediate missing Incident Response policy directly from Gap Analysis
        vm.addMissingPolicyFromGap(PolicyType.INCIDENT_RESPONSE)
        assertEquals(4, vm.uiState.value.generatedPolicies.size)
        assertEquals(4, vm.uiState.value.gapReport!!.policyCoverageCount)

        // Toggle Export Preview Mode
        vm.openExportPreview(bundleMode = true)
        assertTrue(vm.uiState.value.showExportPreview)
        assertTrue(vm.uiState.value.previewBundleMode)
        vm.closeExportPreview()
        assertTrue(!vm.uiState.value.showExportPreview)
    }
}
