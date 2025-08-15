package com.example.krishisevak.data.models

data class GovernmentScheme(
    val id: String,
    val name: String,
    val localNames: Map<String, String>,
    val category: SchemeCategory,
    val type: SchemeType,
    val description: String,
    val benefits: List<SchemeBenefit>,
    val eligibilityCriteria: EligibilityCriteria,
    val applicationProcess: ApplicationProcess,
    val contactInfo: ContactInfo,
    val validityPeriod: ValidityPeriod,
    val status: SchemeStatus = SchemeStatus.ACTIVE
)

data class SchemeBenefit(
    val id: String,
    val name: String,
    val description: String,
    val amount: Double? = null,
    val percentage: Double? = null,
    val maxAmount: Double? = null,
    val type: String // e.g., "Subsidy", "Loan", "Training"
)

data class EligibilityCriteria(
    val id: String,
    val farmerType: List<FarmerType>,
    val landHoldingCriteria: LandHoldingCriteria? = null,
    val ageCriteria: AgeCriteria? = null,
    val incomeCriteria: Double? = null,
    val educationCriteria: String? = null,
    val otherRequirements: List<String> = emptyList()
)

data class LandHoldingCriteria(
    val id: String,
    val minArea: Double? = null,
    val maxArea: Double? = null,
    val ownershipType: List<OwnershipType>,
    val soilType: List<String> = emptyList()
)

data class AgeCriteria(
    val id: String,
    val minAge: Int? = null,
    val maxAge: Int? = null
)

data class ApplicationProcess(
    val id: String,
    val steps: List<ApplicationStep>,
    val requiredDocuments: List<String>,
    val processingTime: String,
    val applicationFee: Double? = null
)

data class ApplicationStep(
    val id: String,
    val stepNumber: Int,
    val description: String,
    val estimatedTime: String? = null,
    val isRequired: Boolean = true
)

data class ContactInfo(
    val id: String,
    val department: String,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val address: String? = null
)

data class ValidityPeriod(
    val id: String,
    val startDate: String,
    val endDate: String? = null,
    val isActive: Boolean = true
)

data class UserEligibility(
    val id: String,
    val userId: String,
    val schemeId: String,
    val isEligible: Boolean,
    val reasons: List<String> = emptyList(),
    val appliedDate: String? = null,
    val status: SchemeStatus = SchemeStatus.PENDING
)

enum class SchemeCategory {
    SEEDS,
    FERTILIZERS,
    EQUIPMENT,
    TRAINING,
    INSURANCE,
    LOANS,
    SUBSIDIES,
    OTHER
}

enum class SchemeType {
    CENTRAL,
    STATE,
    DISTRICT,
    LOCAL
}

enum class FarmerType {
    SMALL,
    MARGINAL,
    MEDIUM,
    LARGE,
    WOMEN_FARMER,
    SC_ST_FARMER,
    OTHER
}

enum class OwnershipType {
    OWNED,
    LEASED,
    SHARED,
    RENTED
}

enum class SchemeStatus {
    ACTIVE,
    INACTIVE,
    UPCOMING,
    EXPIRED,
    PENDING,
    APPROVED,
    REJECTED
}
