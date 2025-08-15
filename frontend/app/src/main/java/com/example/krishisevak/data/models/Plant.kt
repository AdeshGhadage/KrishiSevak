package com.example.krishisevak.data.models

data class Plant(
    val id: String,
    val name: String,
    val localNames: Map<String, String>,
    val scientificName: String,
    val category: PlantCategory,
    val waterRequirement: WaterRequirement,
    val soilType: List<SoilType>,
    val growthDuration: String, // e.g., "90-120 days"
    val season: List<String>, // e.g., ["Kharif", "Rabi"]
    val diseases: List<PlantDisease> = emptyList()
)

data class PlantDisease(
    val id: String,
    val name: String,
    val localNames: Map<String, String>,
    val scientificName: String? = null,
    val symptoms: List<String>,
    val affectedParts: List<PlantPart>,
    val severity: DiseaseSeverity,
    val treatments: List<Treatment> = emptyList(),
    val prevention: List<String> = emptyList(),
    val images: List<String> = emptyList() // URLs or file paths
)

data class Treatment(
    val id: String,
    val name: String,
    val type: TreatmentType,
    val description: String,
    val dosage: String? = null,
    val applicationMethod: String,
    val frequency: String? = null,
    val cost: Double? = null,
    val effectiveness: String // "HIGH", "MEDIUM", "LOW"
)

enum class PlantCategory {
    GRAINS,
    PULSES,
    VEGETABLES,
    FRUITS,
    SPICES,
    CASH_CROPS,
    MEDICINAL,
    FORAGE,
    OTHER
}

enum class WaterRequirement {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}

enum class SoilType {
    CLAY,
    LOAMY,
    SANDY,
    SILTY,
    PEATY,
    CHALKY,
    BLACK_SOIL,
    RED_SOIL,
    ALLUVIAL
}

enum class DiseaseSeverity {
    MILD,
    MODERATE,
    SEVERE,
    CRITICAL
}

enum class PlantPart {
    LEAVES,
    STEM,
    ROOTS,
    FLOWERS,
    FRUITS,
    SEEDS,
    WHOLE_PLANT
}

enum class TreatmentType {
    ORGANIC,
    CHEMICAL,
    BIOLOGICAL,
    CULTURAL,
    PHYSICAL
}
