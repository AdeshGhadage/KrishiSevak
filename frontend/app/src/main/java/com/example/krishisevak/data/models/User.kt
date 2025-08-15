package com.example.krishisevak.data.models

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val profile: FarmerProfile,
    val landHoldings: List<LandHolding> = emptyList(),
    val bankAccounts: List<BankAccount> = emptyList(),
    val preferences: UserPreferences,
    val createdAt: String,
    val lastActive: String
)

data class FarmerProfile(
    val id: String,
    val age: Int,
    val gender: Gender,
    val educationLevel: EducationLevel,
    val primaryOccupation: String,
    val secondaryOccupation: String? = null,
    val experience: Int, // years of farming experience
    val familySize: Int,
    val incomeSource: List<IncomeSource>
)

data class LandHolding(
    val id: String,
    val area: Double, // in acres
    val soilType: String,
    val irrigationType: IrrigationType,
    val ownershipType: String,
    val location: Address,
    val crops: List<String> = emptyList()
)

data class BankAccount(
    val id: String,
    val accountNumber: String,
    val bankName: String,
    val branchName: String,
    val accountType: AccountType,
    val ifscCode: String,
    val isActive: Boolean = true
)

data class Address(
    val id: String,
    val village: String,
    val district: String,
    val state: String,
    val pincode: String,
    val coordinates: Pair<Double, Double>? = null // latitude, longitude
)

data class UserPreferences(
    val id: String,
    val language: String = "en",
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val theme: AppTheme = AppTheme.SYSTEM,
    val notifications: NotificationPreferences,
    val privacy: PrivacySettings,
    val accessibility: AccessibilitySettings
)

data class NotificationPreferences(
    val id: String,
    val weatherAlerts: Boolean = true,
    val priceAlerts: Boolean = true,
    val schemeUpdates: Boolean = true,
    val diseaseAlerts: Boolean = true,
    val marketUpdates: Boolean = true
)

data class PrivacySettings(
    val id: String,
    val shareLocation: Boolean = false,
    val shareCropData: Boolean = true,
    val sharePersonalInfo: Boolean = false,
    val analyticsEnabled: Boolean = true
)

data class AccessibilitySettings(
    val id: String,
    val fontSize: String = "MEDIUM",
    val highContrast: Boolean = false,
    val screenReader: Boolean = false,
    val voiceCommands: Boolean = true
)

data class AppSettings(
    val id: String,
    val autoSync: Boolean = true,
    val cacheSize: Int = 100, // MB
    val dataUsage: String = "BALANCED" // LOW, BALANCED, HIGH
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}

enum class EducationLevel {
    ILLITERATE,
    PRIMARY,
    SECONDARY,
    HIGHER_SECONDARY,
    GRADUATE,
    POST_GRADUATE,
    OTHER
}

enum class IrrigationType {
    CANAL,
    WELL,
    TUBE_WELL,
    RAIN_FED,
    SPRINKLER,
    DRIP,
    OTHER
}

enum class IncomeSource {
    FARMING,
    DAIRY,
    POULTRY,
    FISHERY,
    WAGE_LABOR,
    BUSINESS,
    OTHER
}

enum class AccountType {
    SAVINGS,
    CURRENT,
    FIXED_DEPOSIT,
    RECURRING_DEPOSIT
}

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}
