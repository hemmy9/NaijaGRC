package com.example.domain

enum class CompanySize(
    val label: String,
    val headcountRange: String,
    val ndpcTier: String,
    val dpoRequirement: String,
    val minPasswordLength: Int,
    val privilegedPasswordLength: Int,
    val accessReviewCadence: String
) {
    MICRO(
        label = "Micro / Startup",
        headcountRange = "1–10 Staff",
        ndpcTier = "Standard Data Controller (NDPA Sec. 24)",
        dpoRequirement = "Designated Privacy Lead or Outsourced DPO",
        minPasswordLength = 12,
        privilegedPasswordLength = 16,
        accessReviewCadence = "Semi-annually"
    ),
    SME(
        label = "SME Growth",
        headcountRange = "11–50 Staff",
        ndpcTier = "Ordinary Data Controller (NDPC Registration Recommended)",
        dpoRequirement = "Designated Internal or Shared Data Protection Officer",
        minPasswordLength = 12,
        privilegedPasswordLength = 16,
        accessReviewCadence = "Quarterly"
    ),
    MID_MARKET(
        label = "Mid-Market",
        headcountRange = "51–250 Staff",
        ndpcTier = "Data Controller of Major Importance – Ordinary/Extra-High",
        dpoRequirement = "Dedicated Data Protection Officer (NDPA Sec. 32)",
        minPasswordLength = 14,
        privilegedPasswordLength = 18,
        accessReviewCadence = "Quarterly with automated de-provisioning"
    ),
    ENTERPRISE(
        label = "Enterprise",
        headcountRange = "250+ Staff",
        ndpcTier = "Data Controller of Major Importance – Major High Tier (NDPA Sec. 44)",
        dpoRequirement = "Dedicated Certified DPO + Annual NDPC Compliance Audit Return (CAR)",
        minPasswordLength = 14,
        privilegedPasswordLength = 20,
        accessReviewCadence = "Monthly for privileged accounts, quarterly for standard users"
    )
}

enum class IndustrySector(
    val label: String,
    val regulatoryOverlay: String,
    val regulatedDataCategories: String,
    val sectorSpecificClause: String
) {
    FINTECH(
        label = "Fintech & Banking",
        regulatoryOverlay = "CBN Risk-Based Cybersecurity Framework & PCI-DSS v4.0",
        regulatedDataCategories = "BVN, NIN, bank account numbers, KYC biometrics, and payment card tokens",
        sectorSpecificClause = "Financial identity credentials (BVN/NIN) and transaction logs must be encrypted at rest (AES-256) and in transit (TLS 1.3), with zero plaintext storage of PAN/CVV and real-time fraud telemetry."
    ),
    HEALTHCARE(
        label = "Healthcare & Diagnostics",
        regulatoryOverlay = "NDPA Sec. 30 Sensitive Personal Data & National Health Act",
        regulatedDataCategories = "Electronic Medical Records (EMR), diagnostic lab results, NHIA/HMO IDs, and genetic/biometric health data",
        sectorSpecificClause = "Health records constitute Sensitive Personal Data under NDPA Sec. 30 requiring explicit consent or vital-interest justification, strict role-based clinical segregation, and tamper-evident audit trails."
    ),
    ECOMMERCE(
        label = "E-Commerce & Retail",
        regulatoryOverlay = "NDPA Sec. 25–26 Consent & FCCPC Consumer Data Rules",
        regulatedDataCategories = "Customer names, residential delivery addresses, phone numbers, order histories, and checkout tokens",
        sectorSpecificClause = "Marketing communications require verifiable opt-in consent with one-click withdrawal, and third-party logistics/payment partners must execute NDPA-compliant Data Processing Agreements (DPAs)."
    ),
    TELECOM(
        label = "Telecoms & ISP",
        regulatoryOverlay = "NCC Guidelines & NDPA Sec. 44 Critical Communications",
        regulatedDataCategories = "SIM/NIN linkage records, CDR metadata, subscriber geolocation, and IP assignment logs",
        sectorSpecificClause = "Subscriber NIN verification databases and lawful-interception interfaces must be air-gapped or segmented via Zero-Trust micro-perimeters with immutable dual-custody access logging."
    ),
    SAAS_TECH(
        label = "SaaS & Technology",
        regulatoryOverlay = "ISO/IEC 27001:2022 Annex A.8 Secure SDLC & Cloud Controls",
        regulatedDataCategories = "Tenant user profiles, API keys, SSO assertions, telemetry logs, and customer-uploaded datasets",
        sectorSpecificClause = "Production personal data must never be seeded into staging or development environments without cryptographic anonymization, and tenant isolation must be enforced at the database layer."
    ),
    EDUCATION(
        label = "Education & EdTech",
        regulatoryOverlay = "NDPA Sec. 31 Protection of Children & Minors",
        regulatedDataCategories = "Student academic transcripts, guardian contact records, age verification data, and assessment portfolios",
        sectorSpecificClause = "Where learners are under 18 years of age, verifiable parental or legal guardian consent must be obtained prior to processing under NDPA Sec. 31, and student profiling for advertising is strictly prohibited."
    ),
    ENERGY(
        label = "Energy, Oil & Gas",
        regulatoryOverlay = "NCDMB / NUPRC Critical Infrastructure & OT/IT Security",
        regulatedDataCategories = "Contractor biometric site-access logs, HSE medical clearances, and SCADA/OT operator credentials",
        sectorSpecificClause = "Operational Technology (OT) and Industrial Control Systems (ICS) must remain strictly segmented from corporate IT networks via DMZ jump hosts with hardware-token MFA."
    ),
    PUBLIC_LEGAL(
        label = "Legal, Consulting & Public",
        regulatoryOverlay = "NDPA Sec. 24 Public Interest & Legal Privilege Standards",
        regulatedDataCategories = "Case files, statutory filings, client KYC dossiers, and privileged correspondence",
        sectorSpecificClause = "Client-privileged documents and statutory datasets must carry explicit classification labels (Confidential/Restricted) and remain stored within approved sovereign or adequacy-certified cloud regions."
    )
}

enum class PolicyType(
    val shortTitle: String,
    val fullTitle: String,
    val codePrefix: String,
    val primaryFocus: String
) {
    DATA_PROTECTION(
        shortTitle = "Data Protection",
        fullTitle = "Data Protection & Privacy Policy",
        codePrefix = "POL-DPA",
        primaryFocus = "NDPA 2023 Lawfulness, DSRs & Breach SLA"
    ),
    ACCEPTABLE_USE(
        shortTitle = "Acceptable Use",
        fullTitle = "Acceptable Use Policy (AUP)",
        codePrefix = "POL-AUP",
        primaryFocus = "ISO 27001 Asset, Endpoint & AI Hygiene"
    ),
    PASSWORD(
        shortTitle = "Password & Auth",
        fullTitle = "Password & Authentication Policy",
        codePrefix = "POL-PWD",
        primaryFocus = "ISO A.5.17 / A.8.5 MFA & Credential Depth"
    ),
    INCIDENT_RESPONSE(
        shortTitle = "Incident Response",
        fullTitle = "Incident Response & Breach Policy",
        codePrefix = "POL-IRP",
        primaryFocus = "NDPA Sec. 40 (72h) & ISO A.5.24–A.5.28"
    ),
    BUSINESS_CONTINUITY(
        shortTitle = "Business Continuity",
        fullTitle = "Business Continuity & Resilience Policy",
        codePrefix = "POL-BCP",
        primaryFocus = "ISO A.5.29 / A.5.30 ICT Readiness & NDPA Availability"
    )
}

data class ComplianceControlRef(
    val framework: String,
    val code: String,
    val title: String
)

data class PolicySection(
    val number: String,
    val title: String,
    val content: String,
    val controlTags: List<String>
)

data class ChecklistItem(
    val id: String,
    val actionTitle: String,
    val guidance: String,
    val ownerRole: String,
    val controlCode: String,
    val timeframe: String
)

data class GeneratedPolicy(
    val id: String,
    val type: PolicyType,
    val title: String,
    val documentCode: String,
    val version: String,
    val effectiveDate: String,
    val summary: String,
    val ndpaReferences: List<ComplianceControlRef>,
    val iso27001Controls: List<ComplianceControlRef>,
    val sections: List<PolicySection>,
    val checklist: List<ChecklistItem>
)

data class OrganizationProfile(
    val companyName: String,
    val size: CompanySize,
    val industry: IndustrySector,
    val dpoEmail: String
)

data class PolicyVersionEntry(
    val versionLabel: String,
    val timestamp: String,
    val changeSummary: String,
    val policy: GeneratedPolicy,
    val profileSnapshot: OrganizationProfile
)
