package com.example.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PolicyGeneratorEngine {

    fun generateSuite(
        profile: OrganizationProfile,
        selectedTypes: Set<PolicyType> = PolicyType.entries.toSet(),
        versionMap: Map<PolicyType, String> = emptyMap()
    ): List<GeneratedPolicy> {
        val cleanName = profile.companyName.trim()
        val effectiveDate = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Date())
        val contactEmail = profile.dpoEmail.trim().ifEmpty {
            val slug = cleanName.lowercase(Locale.ENGLISH)
                .replace(Regex("[^a-z0-9]+"), "")
                .take(18)
                .ifEmpty { "company" }
            "dpo@$slug.ng"
        }
        val normalizedProfile = profile.copy(companyName = cleanName, dpoEmail = contactEmail)

        return PolicyType.entries
            .filter { it in selectedTypes }
            .map { type ->
                val ver = versionMap[type] ?: "v1.0"
                when (type) {
                    PolicyType.DATA_PROTECTION -> buildDataProtectionPolicy(normalizedProfile, effectiveDate, ver)
                    PolicyType.ACCEPTABLE_USE -> buildAcceptableUsePolicy(normalizedProfile, effectiveDate, ver)
                    PolicyType.PASSWORD -> buildPasswordPolicy(normalizedProfile, effectiveDate, ver)
                    PolicyType.INCIDENT_RESPONSE -> buildIncidentResponsePolicy(normalizedProfile, effectiveDate, ver)
                    PolicyType.BUSINESS_CONTINUITY -> buildBusinessContinuityPolicy(normalizedProfile, effectiveDate, ver)
                }
            }
    }

    fun computeChangeSummary(
        previous: OrganizationProfile?,
        current: OrganizationProfile
    ): String {
        if (previous == null) return "Initial policy generation"
        val diffs = mutableListOf<String>()
        if (!previous.companyName.equals(current.companyName, ignoreCase = false)) {
            diffs.add("Company name updated to ${current.companyName}")
        }
        if (previous.size != current.size) {
            diffs.add("Size tier changed (${previous.size.label} → ${current.size.label})")
        }
        if (previous.industry != current.industry) {
            diffs.add("Industry changed (${previous.industry.label} → ${current.industry.label})")
        }
        if (previous.dpoEmail != current.dpoEmail && current.dpoEmail.isNotBlank()) {
            diffs.add("DPO contact updated")
        }
        return if (diffs.isEmpty()) {
            "Policy regenerated from organization profile"
        } else {
            diffs.joinToString("; ")
        }
    }

    private fun buildDataProtectionPolicy(
        profile: OrganizationProfile,
        effectiveDate: String,
        version: String
    ): GeneratedPolicy {
        val co = profile.companyName
        val size = profile.size
        val ind = profile.industry

        val ndpaRefs = listOf(
            ComplianceControlRef("NDPA 2023", "Sec. 24–28", "Principles of Personal Data Processing & Lawful Basis"),
            ComplianceControlRef("NDPA 2023", "Sec. 30–31", "Sensitive Personal Data & Children's Consent"),
            ComplianceControlRef("NDPA 2023", "Sec. 32", "Designation of Data Protection Officer (DPO)"),
            ComplianceControlRef("NDPA 2023", "Sec. 34–38", "Rights of Data Subjects (Access, Erasure, Portability)"),
            ComplianceControlRef("NDPA 2023", "Sec. 39–40", "Data Security & 72-Hour NDPC Breach Notification"),
            ComplianceControlRef("NDPA 2023", "Sec. 41–44", "Cross-Border Transfers & Major Importance Registration")
        )

        val isoRefs = listOf(
            ComplianceControlRef("ISO 27001:2022", "A.5.31", "Legal, Statutory, Regulatory & Contractual Requirements"),
            ComplianceControlRef("ISO 27001:2022", "A.5.34", "Privacy and Protection of Personally Identifiable Information (PII)"),
            ComplianceControlRef("ISO 27001:2022", "A.5.36", "Compliance with Policies, Rules & Standards"),
            ComplianceControlRef("ISO 27001:2022", "A.5.24", "Information Security Incident Management Planning"),
            ComplianceControlRef("ISO 27001:2022", "A.8.11", "Data Masking & Pseudonymization Controls")
        )

        val sections = listOf(
            PolicySection(
                number = "1.0",
                title = "Purpose, Scope & Governance Tier",
                content = "$co establishes this Data Protection & Privacy Policy to safeguard all personal and sensitive personal data processed across its ${ind.label} operations in strict compliance with the Nigeria Data Protection Act (NDPA) 2023 and ISO/IEC 27001:2022 Annex A.5.34. This policy binds all employees, directors, contractors, and third-party processors operating on behalf of $co (${size.headcountRange}). Under the NDPC framework, $co is classified as: ${size.ndpcTier}.",
                controlTags = listOf("NDPA Sec. 24", "ISO A.5.1", "ISO A.5.34")
            ),
            PolicySection(
                number = "2.0",
                title = "Lawful Processing Principles & Sector Controls",
                content = "All processing of ${ind.regulatedDataCategories} by $co must satisfy at least one lawful basis under NDPA Sec. 25 (consent, contractual necessity, legal obligation, vital interest, public interest, or legitimate interest). Data collected for a specified ${ind.label} purpose shall remain adequate, accurate, minimized, and retained strictly for defined statutory schedules. Regulatory Overlay (${ind.regulatoryOverlay}): ${ind.sectorSpecificClause}",
                controlTags = listOf("NDPA Sec. 24–26", "NDPA Sec. 30–31", "ISO A.5.31")
            ),
            PolicySection(
                number = "3.0",
                title = "Data Subject Rights & 30-Day Fulfillment SLA",
                content = "$co guarantees Data Subjects their statutory rights under NDPA Sections 34–38: right of access, rectification, erasure ('right to be forgotten'), restriction of processing, data portability, objection to automated profiling, and withdrawal of consent. All verified requests submitted to ${profile.dpoEmail} must be acknowledged within 48 hours and fulfilled without undue delay, and no later than 30 calendar days.",
                controlTags = listOf("NDPA Sec. 34–38", "ISO A.5.34")
            ),
            PolicySection(
                number = "4.0",
                title = "Data Protection Officer & NDPC Compliance Audits",
                content = "Governance Ownership: ${size.dpoRequirement}. The designated DPO (${profile.dpoEmail}) oversees Data Protection Impact Assessments (DPIAs) under NDPA Sec. 28 prior to high-risk processing, maintains the Record of Processing Activities (RoPA), and coordinates ${if (size == CompanySize.ENTERPRISE || size == CompanySize.MID_MARKET) "mandatory annual Compliance Audit Returns (CAR) with the Nigeria Data Protection Commission (NDPC) via a licensed DPCO" else "periodic internal privacy reviews and NDPC registration checks as thresholds scale"}.",
                controlTags = listOf("NDPA Sec. 28, 32, 44", "ISO A.5.36")
            ),
            PolicySection(
                number = "5.0",
                title = "Cross-Border Transfers & Third-Party Processors",
                content = "$co shall not transfer personal data outside the Federal Republic of Nigeria unless the recipient jurisdiction offers an adequate level of protection recognized by the NDPC (NDPA Sec. 41) or Binding Corporate Rules / Standard Contractual Clauses (NDPA Sec. 42) are executed. All vendors processing data for $co must sign a written Data Processing Agreement (DPA) enforcing ISO/IEC 27001 security parity.",
                controlTags = listOf("NDPA Sec. 29, 41–43", "ISO A.5.19")
            ),
            PolicySection(
                number = "6.0",
                title = "72-Hour Personal Data Breach Notification",
                content = "In accordance with NDPA Sec. 40 and ISO A.5.24, any suspected or confirmed personal data breach must be escalated immediately to the Security Lead and DPO (${profile.dpoEmail}). Where a breach is likely to result in a risk to the rights and freedoms of individuals, $co shall notify the NDPC within 72 hours of becoming aware, and notify affected Data Subjects without undue delay when high risk exists.",
                controlTags = listOf("NDPA Sec. 39–40", "ISO A.5.24", "ISO A.5.26")
            )
        )

        val checklist = listOf(
            ChecklistItem(
                id = "dpa_1",
                actionTitle = "Build Record of Processing Activities (RoPA)",
                guidance = "Inventory all ${ind.regulatedDataCategories} collected by $co, mapping lawful bases under NDPA Sec. 25.",
                ownerRole = "DPO / Ops",
                controlCode = "NDPA Sec. 24–25",
                timeframe = "Week 1–2"
            ),
            ChecklistItem(
                id = "dpa_2",
                actionTitle = "Designate DPO & Verify NDPC Registration",
                guidance = "Confirm ${size.dpoRequirement} (${profile.dpoEmail}) and verify NDPC DCMI registration/CAR status.",
                ownerRole = "Executive / Legal",
                controlCode = "NDPA Sec. 32 & 44",
                timeframe = "Week 2"
            ),
            ChecklistItem(
                id = "dpa_3",
                actionTitle = "Publish Layered Privacy Notice & Consent Flows",
                guidance = "Update onboarding/web forms with plain-language NDPA Sec. 26–27 notices and granular opt-in capture.",
                ownerRole = "Product / Legal",
                controlCode = "NDPA Sec. 26–27",
                timeframe = "Week 3"
            ),
            ChecklistItem(
                id = "dpa_4",
                actionTitle = "Execute Vendor DPAs & Cross-Border Clauses",
                guidance = "Sign NDPA-compliant Data Processing Agreements and Standard Contractual Clauses for offshore cloud hosts.",
                ownerRole = "Procurement / Legal",
                controlCode = "NDPA Sec. 29 & 41",
                timeframe = "Day 30"
            ),
            ChecklistItem(
                id = "dpa_5",
                actionTitle = "Operationalize 30-Day DSR & 72h Breach Workflow",
                guidance = "Test DSAR intake mailbox (${profile.dpoEmail}) and dry-run NDPC 72-hour breach escalation.",
                ownerRole = "DPO / SecOps",
                controlCode = "NDPA Sec. 34–40",
                timeframe = "Day 45"
            )
        )

        return GeneratedPolicy(
            id = "policy_data_protection",
            type = PolicyType.DATA_PROTECTION,
            title = "${co} — ${PolicyType.DATA_PROTECTION.fullTitle}",
            documentCode = "${PolicyType.DATA_PROTECTION.codePrefix}-2023-01",
            version = version,
            effectiveDate = effectiveDate,
            summary = "Tailored for ${co} (${size.label}, ${ind.label}) covering NDPA 2023 lawful processing, DSR 30-day SLAs, 72-hour NDPC breach notification, cross-border safeguards, and ISO 27001 Annex A.5.34 privacy controls.",
            ndpaReferences = ndpaRefs,
            iso27001Controls = isoRefs,
            sections = sections,
            checklist = checklist
        )
    }

    private fun buildAcceptableUsePolicy(
        profile: OrganizationProfile,
        effectiveDate: String,
        version: String
    ): GeneratedPolicy {
        val co = profile.companyName
        val size = profile.size
        val ind = profile.industry

        val ndpaRefs = listOf(
            ComplianceControlRef("NDPA 2023", "Sec. 24(1)(f)", "Integrity & Confidentiality Against Unauthorized Processing"),
            ComplianceControlRef("NDPA 2023", "Sec. 39", "Organizational & Technical Measures for Data Controllers"),
            ComplianceControlRef("Nigeria Cybercrimes Act", "Sec. 6 / 14", "Prohibition of Unauthorized System Access & Fraud")
        )

        val isoRefs = listOf(
            ComplianceControlRef("ISO 27001:2022", "A.5.10", "Acceptable Use of Information and Associated Assets"),
            ComplianceControlRef("ISO 27001:2022", "A.5.12", "Classification of Information"),
            ComplianceControlRef("ISO 27001:2022", "A.6.2", "Terms and Conditions of Employment"),
            ComplianceControlRef("ISO 27001:2022", "A.6.7", "Remote Working"),
            ComplianceControlRef("ISO 27001:2022", "A.7.7", "Clear Desk and Clear Screen"),
            ComplianceControlRef("ISO 27001:2022", "A.8.1 / A.8.7", "User Endpoint Devices & Protection Against Malware")
        )

        val sections = listOf(
            PolicySection(
                number = "1.0",
                title = "Purpose & Asset Stewardship",
                content = "This Acceptable Use Policy defines mandatory behavioral and technical rules for using $co's laptops, mobile devices, cloud workspaces, corporate email, networks, and ${ind.label} platforms in alignment with ISO/IEC 27001:2022 Annex A.5.10 and NDPA Sec. 39. All personnel within $co (${size.headcountRange}) are custodians of corporate and customer data and must protect assets against unauthorized disclosure or tampering.",
                controlTags = listOf("ISO A.5.10", "ISO A.5.9", "NDPA Sec. 39")
            ),
            PolicySection(
                number = "2.0",
                title = "Authorized Use & Shadow IT / AI Restrictions",
                content = "Corporate systems are provisioned strictly for authorized $co business duties; incidental personal use must never degrade performance, introduce malware, or violate Nigerian law. Personnel are strictly prohibited from installing unapproved software ('Shadow IT'), forwarding corporate records to personal webmail, or pasting ${ind.regulatedDataCategories} into unapproved public generative AI tools or external file-sharing sites.",
                controlTags = listOf("ISO A.5.10", "ISO A.8.19", "NDPA Sec. 24(1)(f)")
            ),
            PolicySection(
                number = "3.0",
                title = "Endpoint Hygiene, Remote Work & Clear Screen",
                content = "In compliance with ISO A.6.7, A.7.7, and A.8.1, all endpoints accessing $co environments must run managed Endpoint Detection & Response (EDR), full-disk encryption (BitLocker/FileVault), and automatic screen locking after 5 minutes of inactivity. Remote connections from public or untrusted Wi-Fi must traverse the approved corporate VPN. Personnel must never leave physical documents or unlocked screens unattended.",
                controlTags = listOf("ISO A.6.7", "ISO A.7.7", "ISO A.8.1", "ISO A.8.7")
            ),
            PolicySection(
                number = "4.0",
                title = "Information Classification & Sector Handling",
                content = "All $co information must be handled according to four tiers under ISO A.5.12: Public, Internal, Confidential, and Restricted. ${ind.sectorSpecificClause} Removable USB media is disabled by default unless formally approved with hardware encryption by Information Security.",
                controlTags = listOf("ISO A.5.12", "ISO A.8.10", "NDPA Sec. 39")
            ),
            PolicySection(
                number = "5.0",
                title = "Prohibited Conduct & Phishing Escalation",
                content = "Users must never bypass security controls, conduct unauthorized port scanning, share authentication tokens, or engage in copyright infringement or harassment. Suspected phishing emails, social engineering calls, or lost/stolen devices must be reported immediately to IT Security and ${profile.dpoEmail}.",
                controlTags = listOf("ISO A.6.8", "ISO A.8.23", "Cybercrimes Act")
            ),
            PolicySection(
                number = "6.0",
                title = "Monitoring, Privacy Balance & Disciplinary Action",
                content = "Subject to NDPA Sec. 24 proportionality, $co logs network, authentication, and DLP events (ISO A.8.15) to detect anomalies and demonstrate ISO 27001 audit compliance. Access privileges are reviewed ${size.accessReviewCadence}. Violations of this policy trigger disciplinary action under ISO A.6.4, up to termination and statutory reporting.",
                controlTags = listOf("ISO A.8.15", "ISO A.6.4", "NDPA Sec. 24")
            )
        )

        val checklist = listOf(
            ChecklistItem(
                id = "aup_1",
                actionTitle = "Collect Signed Staff & Contractor AUP Attestations",
                guidance = "Distribute AUP to all ${size.headcountRange} personnel and log signed acknowledgments in HR records.",
                ownerRole = "HR / Compliance",
                controlCode = "ISO A.6.2",
                timeframe = "Week 1"
            ),
            ChecklistItem(
                id = "aup_2",
                actionTitle = "Enforce Full-Disk Encryption, EDR & 5-Min Lock",
                guidance = "Push MDM/GPO baselines requiring BitLocker/FileVault, managed EDR, and idle screen locks on all endpoints.",
                ownerRole = "IT Admin",
                controlCode = "ISO A.8.1 & A.7.7",
                timeframe = "Week 2"
            ),
            ChecklistItem(
                id = "aup_3",
                actionTitle = "Block Shadow IT & Unapproved AI Data Pastes",
                guidance = "Configure DNS/SWG filtering and DLP alerts to prevent ${ind.regulatedDataCategories} leaks to public AI tools.",
                ownerRole = "IT Security",
                controlCode = "ISO A.5.10 & A.8.23",
                timeframe = "Week 3"
            ),
            ChecklistItem(
                id = "aup_4",
                actionTitle = "Enforce Corporate VPN & Disable USB Mass Storage",
                guidance = "Require VPN for remote access and restrict removable USB drives by default across corporate laptops.",
                ownerRole = "Network / SecOps",
                controlCode = "ISO A.6.7 & A.8.1",
                timeframe = "Day 30"
            ),
            ChecklistItem(
                id = "aup_5",
                actionTitle = "Deploy Data Classification Labels & Phishing Drills",
                guidance = "Enable sensitivity labels (Public/Internal/Confidential/Restricted) and run quarterly phishing simulations.",
                ownerRole = "Security Awareness",
                controlCode = "ISO A.5.12 & A.6.3",
                timeframe = "Day 45"
            )
        )

        return GeneratedPolicy(
            id = "policy_acceptable_use",
            type = PolicyType.ACCEPTABLE_USE,
            title = "${co} — ${PolicyType.ACCEPTABLE_USE.fullTitle}",
            documentCode = "${PolicyType.ACCEPTABLE_USE.codePrefix}-2023-02",
            version = version,
            effectiveDate = effectiveDate,
            summary = "Establishes safe endpoint, remote-work, information classification, and anti-Shadow-IT rules for ${co} (${size.label}) aligned with ISO/IEC 27001:2022 Annex A.5.10, A.6.7, A.8.1, and NDPA Sec. 39.",
            ndpaReferences = ndpaRefs,
            iso27001Controls = isoRefs,
            sections = sections,
            checklist = checklist
        )
    }

    private fun buildPasswordPolicy(
        profile: OrganizationProfile,
        effectiveDate: String,
        version: String
    ): GeneratedPolicy {
        val co = profile.companyName
        val size = profile.size
        val ind = profile.industry

        val ndpaRefs = listOf(
            ComplianceControlRef("NDPA 2023", "Sec. 39(1)", "Appropriate Technical Measures to Protect Personal Data"),
            ComplianceControlRef("NDPA 2023", "Sec. 24(1)(f)", "Protection Against Unauthorized or Unlawful Access")
        )

        val isoRefs = listOf(
            ComplianceControlRef("ISO 27001:2022", "A.5.15", "Access Control & Least Privilege"),
            ComplianceControlRef("ISO 27001:2022", "A.5.17", "Authentication Information Management"),
            ComplianceControlRef("ISO 27001:2022", "A.5.18", "Access Rights Provisioning & Review"),
            ComplianceControlRef("ISO 27001:2022", "A.8.2", "Privileged Access Rights"),
            ComplianceControlRef("ISO 27001:2022", "A.8.5", "Secure Authentication Technologies")
        )

        val sections = listOf(
            PolicySection(
                number = "1.0",
                title = "Purpose & Authentication Scope",
                content = "This Password & Authentication Policy enforces cryptographic strength, Multi-Factor Authentication (MFA), and credential lifecycle governance across all $co (${size.headcountRange}) user accounts, administrative consoles, cloud tenants, and ${ind.label} databases, fulfilling ISO/IEC 27001:2022 Annex A.5.17 & A.8.5 and NDPA Sec. 39 technical safeguards.",
                controlTags = listOf("ISO A.5.17", "ISO A.8.5", "NDPA Sec. 39")
            ),
            PolicySection(
                number = "2.0",
                title = "Password Length, Passphrases & Blocklist Rules",
                content = "Standard corporate accounts at $co must use passphrases of at least ${size.minPasswordLength} characters; privileged, root, database, and financial admin accounts require at least ${size.privilegedPasswordLength} characters. Consistent with modern ISO A.5.17 and NIST SP 800-63B guidance, $co prioritizes length and uniqueness over arbitrary periodic rotation unless compromise is suspected. Passwords must be screened against breached-credential dictionaries, company names ('$co'), and predictable patterns.",
                controlTags = listOf("ISO A.5.17", "NIST 800-63B")
            ),
            PolicySection(
                number = "3.0",
                title = "Multi-Factor Authentication (MFA) Mandate",
                content = "Phishing-resistant MFA (FIDO2 hardware security keys, passkeys, or TOTP authenticator apps) is mandatory for all remote access (VPN), corporate SSO/email, cloud infrastructure, and any repository storing ${ind.regulatedDataCategories}. Legacy SMS/voice OTP is prohibited for administrative or ${ind.label} production portals due to SIM-swap exposure.",
                controlTags = listOf("ISO A.8.5", "NDPA Sec. 39")
            ),
            PolicySection(
                number = "4.0",
                title = "Privileged Access, Service Accounts & API Secrets",
                content = "Under ISO A.8.2 and A.5.15, shared administrative accounts are strictly prohibited. Administrators must operate standard non-privileged accounts for daily tasks and elevate via Just-in-Time (JIT) or Privileged Access Management (PAM) vaults. API keys, OAuth client secrets, and database connection strings must reside in a centralized secrets manager (never hardcoded in source control) and rotate every 90 days.",
                controlTags = listOf("ISO A.8.2", "ISO A.5.15", "ISO A.8.25")
            ),
            PolicySection(
                number = "5.0",
                title = "Password Storage, Vaults & Lockout Thresholds",
                content = "Users must store unique credentials exclusively within the $co-approved enterprise password manager—never in browsers without master protection, spreadsheets, or chat messages. Systems must enforce adaptive rate-limiting and temporary lockout after 5 consecutive failed logins, and store all authentication verifiers using salted Argon2id, bcrypt, or PBKDF2 hashing.",
                controlTags = listOf("ISO A.5.17", "ISO A.8.5", "ISO A.8.24")
            ),
            PolicySection(
                number = "6.0",
                title = "Compromise Response & Access Lifecycle Reviews",
                content = "If any credential is exposed via phishing, device theft, or external breach alerts, the user must revoke sessions and rotate credentials within 15 minutes while notifying IT Security (${profile.dpoEmail}). Access rights and orphaned service accounts are audited ${size.accessReviewCadence}, with immediate revocation upon employee or contractor offboarding under ISO A.5.18.",
                controlTags = listOf("ISO A.5.18", "ISO A.5.24", "NDPA Sec. 40")
            )
        )

        val checklist = listOf(
            ChecklistItem(
                id = "pwd_1",
                actionTitle = "Enforce ${size.minPasswordLength}+ / ${size.privilegedPasswordLength}+ Char Passphrases",
                guidance = "Configure IdP/Active Directory minimum length (${size.minPasswordLength} standard, ${size.privilegedPasswordLength} privileged) and breached-password blocklists.",
                ownerRole = "IAM / IT Admin",
                controlCode = "ISO A.5.17",
                timeframe = "Week 1"
            ),
            ChecklistItem(
                id = "pwd_2",
                actionTitle = "Enforce Phishing-Resistant MFA (FIDO2 / TOTP)",
                guidance = "Mandate hardware keys or authenticator apps on SSO, VPN, email, and ${ind.label} databases; disable SMS OTP for admins.",
                ownerRole = "IT Security",
                controlCode = "ISO A.8.5 & NDPA Sec. 39",
                timeframe = "Week 2"
            ),
            ChecklistItem(
                id = "pwd_3",
                actionTitle = "Provision Enterprise Password Manager & Secrets Vault",
                guidance = "Roll out centralized password vaulting for staff and rotate all API keys/service secrets on a 90-day cycle.",
                ownerRole = "DevSecOps / IT",
                controlCode = "ISO A.8.2 & A.8.25",
                timeframe = "Week 3"
            ),
            ChecklistItem(
                id = "pwd_4",
                actionTitle = "Eliminate Shared Admin Accounts & Enforce Lockout",
                guidance = "Replace generic admin logins with named JIT elevation and configure 5-attempt rate-limiting + Argon2id hashing.",
                ownerRole = "Platform Engineering",
                controlCode = "ISO A.5.15 & A.8.24",
                timeframe = "Day 30"
            ),
            ChecklistItem(
                id = "pwd_5",
                actionTitle = "Schedule ${size.accessReviewCadence} Access & Offboarding Audits",
                guidance = "Audit privileged and orphaned accounts ${size.accessReviewCadence.lowercase(Locale.ENGLISH)} and verify immediate revocation at exit.",
                ownerRole = "IT / Compliance",
                controlCode = "ISO A.5.18",
                timeframe = "Ongoing"
            )
        )

        return GeneratedPolicy(
            id = "policy_password",
            type = PolicyType.PASSWORD,
            title = "${co} — ${PolicyType.PASSWORD.fullTitle}",
            documentCode = "${PolicyType.PASSWORD.codePrefix}-2023-03",
            version = version,
            effectiveDate = effectiveDate,
            summary = "Mandates ${size.minPasswordLength}+ char passphrases (${size.privilegedPasswordLength}+ privileged), phishing-resistant MFA, secrets vaulting, and ${size.accessReviewCadence.lowercase(Locale.ENGLISH)} access reviews for ${co} per ISO A.5.17, A.8.5, and NDPA Sec. 39.",
            ndpaReferences = ndpaRefs,
            iso27001Controls = isoRefs,
            sections = sections,
            checklist = checklist
        )
    }

    private fun buildIncidentResponsePolicy(
        profile: OrganizationProfile,
        effectiveDate: String,
        version: String
    ): GeneratedPolicy {
        val co = profile.companyName
        val size = profile.size
        val ind = profile.industry

        val ndpaRefs = listOf(
            ComplianceControlRef("NDPA 2023", "Sec. 40(1)–(4)", "72-Hour NDPC Breach Notification & High-Risk Data Subject Alert"),
            ComplianceControlRef("NDPA 2023", "Sec. 40(5)–(7)", "Mandatory Breach Register & Post-Incident Recordkeeping"),
            ComplianceControlRef("NDPA 2023", "Sec. 39", "Technical & Organizational Breach Containment Measures"),
            ComplianceControlRef("Nigeria Cybercrimes Act", "Sec. 21", "Mandatory Reporting of Cyber Threats to ngCERT / Authorities")
        )

        val isoRefs = listOf(
            ComplianceControlRef("ISO 27001:2022", "A.5.24", "Information Security Incident Management Planning & Preparation"),
            ComplianceControlRef("ISO 27001:2022", "A.5.25", "Assessment and Decision on Information Security Events"),
            ComplianceControlRef("ISO 27001:2022", "A.5.26", "Response to Information Security Incidents"),
            ComplianceControlRef("ISO 27001:2022", "A.5.27", "Learning from Information Security Incidents"),
            ComplianceControlRef("ISO 27001:2022", "A.5.28", "Collection of Forensic Evidence"),
            ComplianceControlRef("ISO 27001:2022", "A.6.8", "Information Security Event Reporting")
        )

        val escalationSla = when (size) {
            CompanySize.MICRO, CompanySize.SME -> "within 30 minutes of detection to the Incident Lead & DPO"
            CompanySize.MID_MARKET -> "within 15 minutes to the CSIRT Lead, IT Security & DPO"
            CompanySize.ENTERPRISE -> "immediately (under 15 minutes) to the 24/7 SOC / CSIRT Commander & Certified DPO"
        }

        val sections = listOf(
            PolicySection(
                number = "1.0",
                title = "Purpose, Scope & CSIRT Governance",
                content = "$co establishes this Incident Response & Personal Data Breach Policy to detect, triage, contain, and recover from cybersecurity and privacy incidents affecting ${ind.label} operations. Aligned with ISO/IEC 27001:2022 Annex A.5.24–A.5.28 and Nigeria Data Protection Act (NDPA) 2023 Sec. 40, this policy applies to all endpoints, cloud workloads, APIs, third-party processors, and personnel across $co (${size.headcountRange}).",
                controlTags = listOf("ISO A.5.24", "NDPA Sec. 39–40")
            ),
            PolicySection(
                number = "2.0",
                title = "Detection, Triage & Severity Classification",
                content = "All employees and automated telemetry must report suspected security events $escalationSla (${profile.dpoEmail}) per ISO A.6.8. Under ISO A.5.25, events are triaged into Severity 1 (Critical Personal Data Breach / Ransomware), Severity 2 (High Impact / Privileged Compromise), Severity 3 (Contained Malware / Policy Violation), and Severity 4 (Low Informational). Any incident exposing ${ind.regulatedDataCategories} defaults to Severity 1.",
                controlTags = listOf("ISO A.5.25", "ISO A.6.8")
            ),
            PolicySection(
                number = "3.0",
                title = "Containment, Eradication & Forensic Evidence",
                content = "Upon confirmation under ISO A.5.26, the response team isolates compromised hosts, revokes active tokens/credentials, and preserves volatile memory, immutable cloud logs, and disk snapshots with chain-of-custody hashes under ISO A.5.28. Sector Constraint (${ind.regulatoryOverlay}): ${ind.sectorSpecificClause}",
                controlTags = listOf("ISO A.5.26", "ISO A.5.28", "NDPA Sec. 39")
            ),
            PolicySection(
                number = "4.0",
                title = "72-Hour NDPC & Statutory Breach Notification",
                content = "Pursuant to NDPA Sec. 40(1)–(4), where a personal data breach poses a risk to the rights and freedoms of individuals, the DPO (${profile.dpoEmail}) must notify the Nigeria Data Protection Commission (NDPC) within 72 hours of becoming aware, detailing the nature of the breach, approximate volume of affected records, DPO contact, likely consequences, and mitigation steps. Where high risk exists, affected Data Subjects must be notified without undue delay in plain language, alongside sector regulators (${ind.regulatoryOverlay}) and ngCERT where applicable.",
                controlTags = listOf("NDPA Sec. 40", "Cybercrimes Act Sec. 21")
            ),
            PolicySection(
                number = "5.0",
                title = "Breach Register, Post-Incident Review & Drills",
                content = "In compliance with NDPA Sec. 40(5) and ISO A.5.27, $co maintains a tamper-evident Breach & Incident Register logging all facts, effects, and remedial actions even when NDPC notification is not triggered. A formal Root Cause Analysis (RCA) is completed within 10 business days of closure, and tabletop breach-response drills are conducted ${if (size == CompanySize.ENTERPRISE || size == CompanySize.MID_MARKET) "semi-annually" else "annually"}.",
                controlTags = listOf("NDPA Sec. 40(5)", "ISO A.5.27")
            )
        )

        val checklist = listOf(
            ChecklistItem(
                id = "irp_1",
                actionTitle = "Appoint CSIRT Roster & 24/7 Escalation Hotline",
                guidance = "Publish internal incident reporting channels routing directly to Security Lead & DPO (${profile.dpoEmail}).",
                ownerRole = "CISO / DPO",
                controlCode = "ISO A.5.24 & A.6.8",
                timeframe = "Week 1"
            ),
            ChecklistItem(
                id = "irp_2",
                actionTitle = "Pre-Stage NDPC 72-Hour Breach Notification Pack",
                guidance = "Prepare NDPC portal templates, high-risk Data Subject alert drafts, and ngCERT / ${ind.label} regulator contacts.",
                ownerRole = "DPO / Legal",
                controlCode = "NDPA Sec. 40(1)–(4)",
                timeframe = "Week 2"
            ),
            ChecklistItem(
                id = "irp_3",
                actionTitle = "Configure SIEM/EDR Alerts & Forensic Chain-of-Custody",
                guidance = "Tune Severity-1 detections for ${ind.regulatedDataCategories} exfiltration and verify immutable log retention.",
                ownerRole = "SecOps",
                controlCode = "ISO A.5.25 & A.5.28",
                timeframe = "Week 3"
            ),
            ChecklistItem(
                id = "irp_4",
                actionTitle = "Maintain Tamper-Evident Breach & Incident Register",
                guidance = "Establish an auditable log capturing incident timelines, root causes, and NDPA Sec. 40(5) materiality decisions.",
                ownerRole = "DPO / GRC",
                controlCode = "NDPA Sec. 40(5)",
                timeframe = "Day 30"
            ),
            ChecklistItem(
                id = "irp_5",
                actionTitle = "Conduct Tabletop Breach & Ransomware Simulation",
                guidance = "Run a live 72-hour NDPC reporting & containment drill with leadership and record lessons learned.",
                ownerRole = "CSIRT / Executive",
                controlCode = "ISO A.5.27",
                timeframe = "Day 60"
            )
        )

        return GeneratedPolicy(
            id = "policy_incident_response",
            type = PolicyType.INCIDENT_RESPONSE,
            title = "${co} — ${PolicyType.INCIDENT_RESPONSE.fullTitle}",
            documentCode = "${PolicyType.INCIDENT_RESPONSE.codePrefix}-2023-04",
            version = version,
            effectiveDate = effectiveDate,
            summary = "Defines rapid triage, forensic evidence preservation, and mandatory 72-hour NDPC breach reporting for ${co} (${size.label}, ${ind.label}) aligned to NDPA 2023 Sec. 40 and ISO/IEC 27001:2022 Annex A.5.24–A.5.28.",
            ndpaReferences = ndpaRefs,
            iso27001Controls = isoRefs,
            sections = sections,
            checklist = checklist
        )
    }

    private fun buildBusinessContinuityPolicy(
        profile: OrganizationProfile,
        effectiveDate: String,
        version: String
    ): GeneratedPolicy {
        val co = profile.companyName
        val size = profile.size
        val ind = profile.industry

        val (rtoTarget, rpoTarget) = when (size) {
            CompanySize.MICRO -> "24 hours" to "24 hours"
            CompanySize.SME -> "8 hours" to "4 hours"
            CompanySize.MID_MARKET -> "4 hours" to "1 hour"
            CompanySize.ENTERPRISE -> "1 hour" to "15 minutes"
        }

        val ndpaRefs = listOf(
            ComplianceControlRef("NDPA 2023", "Sec. 24(1)(f)", "Availability, Integrity & Resilience of Personal Data"),
            ComplianceControlRef("NDPA 2023", "Sec. 39(2)", "Ability to Restore Availability & Access After Physical or Technical Incidents")
        )

        val isoRefs = listOf(
            ComplianceControlRef("ISO 27001:2022", "A.5.29", "Information Security During Disruption"),
            ComplianceControlRef("ISO 27001:2022", "A.5.30", "ICT Readiness for Business Continuity"),
            ComplianceControlRef("ISO 27001:2022", "A.8.13", "Information Backup & Restoration Testing"),
            ComplianceControlRef("ISO 27001:2022", "A.8.14", "Redundancy of Information Processing Facilities")
        )

        val sections = listOf(
            PolicySection(
                number = "1.0",
                title = "Purpose, Resilience Mandate & Scope",
                content = "$co establishes this Business Continuity & ICT Resilience Policy to guarantee uninterrupted delivery of critical ${ind.label} services and continuous protection of personal data during cyberattacks, grid/telecom outages, cloud region failures, or natural disasters. Aligned with ISO/IEC 27001:2022 Annex A.5.29–A.5.30 and NDPA 2023 Sec. 24(1)(f) & Sec. 39, this policy governs all production workloads, backup vaults, and critical personnel of $co (${size.headcountRange}).",
                controlTags = listOf("ISO A.5.29", "ISO A.5.30", "NDPA Sec. 39")
            ),
            PolicySection(
                number = "2.0",
                title = "Business Impact Analysis (BIA), RTO & RPO Tiers",
                content = "Based on $co's operational tier (${size.label}), Tier-1 Mission-Critical ${ind.label} systems processing ${ind.regulatedDataCategories} must achieve a Recovery Time Objective (RTO) of $rtoTarget and a Recovery Point Objective (RPO) of $rpoTarget. Business Impact Analyses (BIAs) are reviewed annually and after any major architectural or regulatory change.",
                controlTags = listOf("ISO A.5.30", "ISO 22301 Alignment")
            ),
            PolicySection(
                number = "3.0",
                title = "Immutable Backups & Geographic Redundancy",
                content = "Under ISO A.8.13 and A.8.14, $co enforces the 3-2-1-1 backup standard: at least 3 copies across 2 media types, 1 off-site/cross-region replica, and 1 immutable/air-gapped copy protected against ransomware deletion. All backups containing personal data are encrypted using AES-256 with separate KMS custody and comply with NDPA Sec. 41–43 cross-border residency rules. Automated restore verification drills run ${if (size == CompanySize.ENTERPRISE || size == CompanySize.MID_MARKET) "monthly" else "quarterly"}.",
                controlTags = listOf("ISO A.8.13", "ISO A.8.14", "NDPA Sec. 41–43")
            ),
            PolicySection(
                number = "4.0",
                title = "Security Parity During Disruption & Failover",
                content = "Pursuant to ISO A.5.29, emergency failover modes, manual workarounds, or disaster-recovery (DR) environments must never bypass authentication, MFA, RBAC, or encryption controls. Sector Continuity Requirement (${ind.regulatoryOverlay}): ${ind.sectorSpecificClause}",
                controlTags = listOf("ISO A.5.29", "NDPA Sec. 24(1)(f)")
            ),
            PolicySection(
                number = "5.0",
                title = "Crisis Governance, Communication & Annual Testing",
                content = "The Crisis Management Lead alongside the DPO (${profile.dpoEmail}) authorizes DR invocation and coordinates stakeholder, customer, and regulator communications. Full failover exercises and post-test remediation reports are documented annually to satisfy ISO 27001 certification audits and NDPA Sec. 44 compliance reviews.",
                controlTags = listOf("ISO A.5.30", "NDPA Sec. 44")
            )
        )

        val checklist = listOf(
            ChecklistItem(
                id = "bcp_1",
                actionTitle = "Complete Business Impact Analysis (RTO $rtoTarget / RPO $rpoTarget)",
                guidance = "Rank Tier-1 ${ind.label} applications processing ${ind.regulatedDataCategories} and sign off recovery SLAs.",
                ownerRole = "BC Lead / Ops",
                controlCode = "ISO A.5.30",
                timeframe = "Week 2"
            ),
            ChecklistItem(
                id = "bcp_2",
                actionTitle = "Deploy 3-2-1-1 Immutable AES-256 Backups",
                guidance = "Configure off-site/cross-region replicas plus immutable air-gapped backups in NDPA-compliant regions.",
                ownerRole = "Cloud / Infrastructure",
                controlCode = "ISO A.8.13 & NDPA Sec. 41",
                timeframe = "Week 3"
            ),
            ChecklistItem(
                id = "bcp_3",
                actionTitle = "Validate DR Failover MFA & Encryption Parity",
                guidance = "Verify disaster-recovery environments enforce identical RBAC, MFA, and TLS/AES protections as primary systems.",
                ownerRole = "SecOps / Infra",
                controlCode = "ISO A.5.29 & A.8.14",
                timeframe = "Day 30"
            ),
            ChecklistItem(
                id = "bcp_4",
                actionTitle = "Document Crisis Call Tree & Out-of-Band Comms",
                guidance = "Maintain offline emergency contacts for executive crisis leads, DPO (${profile.dpoEmail}), and critical vendors.",
                ownerRole = "Crisis Lead / DPO",
                controlCode = "ISO A.5.30",
                timeframe = "Day 30"
            ),
            ChecklistItem(
                id = "bcp_5",
                actionTitle = "Run ${if (size == CompanySize.ENTERPRISE || size == CompanySize.MID_MARKET) "Monthly" else "Quarterly"} Backup Restore & Annual Failover",
                guidance = "Verify backup integrity via timed restore drills and archive test evidence for ISO 27001 & NDPC audits.",
                ownerRole = "Infra / Audit",
                controlCode = "ISO A.8.13 & NDPA Sec. 39",
                timeframe = "Ongoing"
            )
        )

        return GeneratedPolicy(
            id = "policy_business_continuity",
            type = PolicyType.BUSINESS_CONTINUITY,
            title = "${co} — ${PolicyType.BUSINESS_CONTINUITY.fullTitle}",
            documentCode = "${PolicyType.BUSINESS_CONTINUITY.codePrefix}-2023-05",
            version = version,
            effectiveDate = effectiveDate,
            summary = "Establishes Tier-1 RTO ($rtoTarget) / RPO ($rpoTarget) targets, immutable backups, and zero-compromise failover security for ${co} (${size.label}, ${ind.label}) aligned to ISO A.5.29–A.5.30, A.8.13–A.8.14, and NDPA Sec. 39.",
            ndpaReferences = ndpaRefs,
            iso27001Controls = isoRefs,
            sections = sections,
            checklist = checklist
        )
    }

    fun formatPolicyPlainText(policy: GeneratedPolicy, profile: OrganizationProfile): String {
        return buildString {
            appendLine("====================================================")
            appendLine(policy.title.uppercase(Locale.ENGLISH))
            appendLine("====================================================")
            appendLine("Document ID: ${policy.documentCode} | Version: ${policy.version}")
            appendLine("Effective Date: ${policy.effectiveDate}")
            appendLine("Organization: ${profile.companyName} (${profile.size.label} - ${profile.size.headcountRange})")
            appendLine("Industry Sector: ${profile.industry.label}")
            appendLine("DPO / Security Contact: ${profile.dpoEmail}")
            appendLine()
            appendLine("EXECUTIVE SUMMARY")
            appendLine(policy.summary)
            appendLine()
            appendLine("COMPLIANCE ALIGNMENT")
            policy.ndpaReferences.forEach { appendLine(" - [${it.framework} ${it.code}] ${it.title}") }
            policy.iso27001Controls.forEach { appendLine(" - [${it.framework} ${it.code}] ${it.title}") }
            appendLine()
            policy.sections.forEach { sec ->
                appendLine("${sec.number} ${sec.title}")
                appendLine(sec.content)
                appendLine("Mapped Controls: ${sec.controlTags.joinToString(", ")}")
                appendLine()
            }
            appendLine("IMPLEMENTATION COMPLIANCE CHECKLIST")
            policy.checklist.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. [ ] ${item.actionTitle} (${item.controlCode} | Owner: ${item.ownerRole} | ${item.timeframe})")
                appendLine("       ${item.guidance}")
            }
            appendLine()
            appendLine("Approved by Executive Management & Data Protection Officer — ${profile.companyName}")
        }
    }

    fun analyzeGaps(
        profile: OrganizationProfile,
        generatedPolicies: List<GeneratedPolicy>,
        checkedChecklistIds: Set<String>
    ): GapAnalysisReport {
        val generatedMap = generatedPolicies.associateBy { it.type }
        val gaps = mutableListOf<PolicyGapItem>()

        // 1. Missing Policy Domain Gaps
        PolicyType.entries.forEach { type ->
            if (type !in generatedMap) {
                val (severity, ref, desc, fix) = when (type) {
                    PolicyType.DATA_PROTECTION -> Quad(
                        GapSeverity.CRITICAL,
                        "NDPA Sec. 24–44 & ISO A.5.34",
                        "${profile.companyName} lacks a formal Data Protection & Privacy Policy governing ${profile.industry.regulatedDataCategories}.",
                        "Generate the Data Protection Policy to establish lawful processing bases, 30-day DSR workflows, and DPO governance."
                    )
                    PolicyType.INCIDENT_RESPONSE -> Quad(
                        GapSeverity.CRITICAL,
                        "NDPA Sec. 40 (72h) & ISO A.5.24–A.5.28",
                        "No Incident Response & Breach Policy exists to enforce the statutory 72-hour NDPC breach notification window.",
                        "Generate the Incident Response Policy to define CSIRT escalation, forensic custody, and NDPC reporting."
                    )
                    PolicyType.PASSWORD -> Quad(
                        GapSeverity.HIGH,
                        "ISO A.5.17, A.8.5 & NDPA Sec. 39",
                        "Missing authentication standards exposes ${profile.industry.label} portals to credential stuffing and SIM-swap MFA bypass.",
                        "Generate the Password & Auth Policy to mandate ${profile.size.minPasswordLength}+ char passphrases and FIDO2/TOTP MFA."
                    )
                    PolicyType.ACCEPTABLE_USE -> Quad(
                        GapSeverity.HIGH,
                        "ISO A.5.10, A.6.7, A.8.1",
                        "No Acceptable Use Policy governs remote endpoints, Shadow IT, or pasting personal data into unapproved AI tools.",
                        "Generate the Acceptable Use Policy to enforce endpoint encryption, VPN, and staff attestation rules."
                    )
                    PolicyType.BUSINESS_CONTINUITY -> Quad(
                        if (profile.size == CompanySize.ENTERPRISE || profile.size == CompanySize.MID_MARKET) GapSeverity.HIGH else GapSeverity.MEDIUM,
                        "ISO A.5.29–A.5.30 & NDPA Sec. 39",
                        "Without a Business Continuity Policy, outage recovery and immutable ransomware backup SLAs are undefined.",
                        "Generate the Business Continuity Policy to formalize RTO/RPO targets and 3-2-1-1 backup testing."
                    )
                }
                gaps.add(
                    PolicyGapItem(
                        id = "gap_missing_${type.name}",
                        title = "Missing ${type.fullTitle}",
                        description = desc,
                        remediation = fix,
                        severity = severity,
                        category = GapCategory.MISSING_POLICY,
                        policyType = type,
                        controlReference = ref
                    )
                )
            }
        }

        // 2. Per-Policy Implementation Progress & Open Control Gaps
        val progressMetrics = mutableListOf<PolicyProgressMetric>()
        var totalControls = 0
        var completedControls = 0

        generatedPolicies.forEach { policy ->
            val total = policy.checklist.size
            val done = policy.checklist.count { "${policy.type.name}_${it.id}" in checkedChecklistIds }
            totalControls += total
            completedControls += done
            progressMetrics.add(
                PolicyProgressMetric(
                    policyType = policy.type,
                    completedSteps = done,
                    totalSteps = total
                )
            )

            policy.checklist.forEach { item ->
                val isDone = "${policy.type.name}_${item.id}" in checkedChecklistIds
                if (!isDone) {
                    val severity = when {
                        item.controlCode.contains("Sec. 40") ||
                            item.controlCode.contains("A.8.5") ||
                            item.controlCode.contains("Sec. 32") ||
                            item.controlCode.contains("A.8.13") -> GapSeverity.CRITICAL
                        item.timeframe.contains("Week 1") ||
                            item.timeframe.contains("Week 2") ||
                            item.controlCode.contains("NDPA") -> GapSeverity.HIGH
                        else -> GapSeverity.MEDIUM
                    }
                    gaps.add(
                        PolicyGapItem(
                            id = "gap_ctrl_${policy.type.name}_${item.id}",
                            title = item.actionTitle,
                            description = item.guidance,
                            remediation = "Assign to ${item.ownerRole} • Target: ${item.timeframe}",
                            severity = severity,
                            category = GapCategory.OPEN_CONTROL,
                            policyType = policy.type,
                            controlReference = item.controlCode,
                            checklistItemId = item.id
                        )
                    )
                }
            }
        }

        // Weighted readiness: 40% policy domain coverage (out of 5) + 60% checklist control completion
        val coverageRatio = generatedPolicies.size.toFloat() / PolicyType.entries.size.toFloat()
        val checklistRatio = if (totalControls == 0) 0f else completedControls.toFloat() / totalControls.toFloat()
        val readinessPct = ((coverageRatio * 40f) + (checklistRatio * 60f)).toInt().coerceIn(0, 100)

        val sortedGaps = gaps.sortedWith(compareBy<PolicyGapItem> { it.severity.ordinal }.thenBy { it.category.ordinal })

        return GapAnalysisReport(
            overallReadinessPercent = readinessPct,
            policyCoverageCount = generatedPolicies.size,
            totalPolicyTemplates = PolicyType.entries.size,
            completedControls = completedControls,
            totalControlsInGenerated = totalControls,
            perPolicyProgress = progressMetrics,
            gaps = sortedGaps
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
