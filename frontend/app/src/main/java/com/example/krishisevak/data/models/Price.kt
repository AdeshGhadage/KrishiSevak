package com.example.krishisevak.data.models

data class CropPrice(
    val id: String,
    val cropName: String,
    val localNames: Map<String, String>,
    val category: CropCategory,
    val quality: CropQuality,
    val marketType: MarketType,
    val priceInfo: PriceInfo,
    val marketInfo: MarketInfo,
    val lastUpdated: String
)

data class FertilizerPrice(
    val id: String,
    val fertilizerName: String,
    val localNames: Map<String, String>,
    val type: FertilizerType,
    val brand: String? = null,
    val priceInfo: PriceInfo,
    val marketInfo: MarketInfo,
    val lastUpdated: String
)

data class PriceInfo(
    val id: String,
    val currentPrice: Double,
    val previousPrice: Double? = null,
    val priceChange: Double? = null,
    val priceChangePercentage: Double? = null,
    val unit: String,
    val currency: String = "INR"
)

data class MarketInfo(
    val id: String,
    val marketName: String,
    val location: String,
    val district: String,
    val state: String,
    val marketType: MarketType,
    val contactInfo: String? = null
)

data class PriceAlert(
    val id: String,
    val userId: String,
    val itemType: ItemType,
    val itemId: String,
    val targetPrice: Double,
    val alertType: String, // "ABOVE", "BELOW"
    val isActive: Boolean = true,
    val createdAt: String
)

enum class CropCategory {
    GRAINS,
    PULSES,
    VEGETABLES,
    FRUITS,
    SPICES,
    CASH_CROPS,
    MEDICINAL,
    OTHER
}

enum class CropQuality {
    GRADE_A,
    GRADE_B,
    GRADE_C,
    ORGANIC,
    CONVENTIONAL
}

enum class MarketType {
    MANDI,
    LOCAL_MARKET,
    WHOLESALE_MARKET,
    RETAIL_MARKET,
    ONLINE_MARKET
}

enum class ItemType {
    CROP,
    FERTILIZER,
    SEED,
    EQUIPMENT
}

enum class FertilizerType {
    NITROGEN,
    PHOSPHORUS,
    POTASSIUM,
    COMPOUND,
    ORGANIC,
    MICRONUTRIENT
}
