package com.example.krishisevak.data.local.dao

import androidx.room.*
import com.example.krishisevak.data.local.entities.LandHoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LandHoldingDao {
    
    @Query("SELECT * FROM land_holdings WHERE userId = :userId")
    fun getLandHoldingsByUserId(userId: String): Flow<List<LandHoldingEntity>>
    
    @Query("SELECT * FROM land_holdings WHERE id = :landHoldingId")
    suspend fun getLandHoldingById(landHoldingId: String): LandHoldingEntity?
    
    @Query("SELECT * FROM land_holdings WHERE userId = :userId AND currentCrop = :cropName")
    fun getLandHoldingsByCrop(userId: String, cropName: String): Flow<List<LandHoldingEntity>>
    
    @Query("SELECT SUM(area) FROM land_holdings WHERE userId = :userId")
    suspend fun getTotalLandArea(userId: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandHolding(landHolding: LandHoldingEntity)
    
    @Update
    suspend fun updateLandHolding(landHolding: LandHoldingEntity)
    
    @Delete
    suspend fun deleteLandHolding(landHolding: LandHoldingEntity)
    
    @Query("UPDATE land_holdings SET currentCrop = :cropName WHERE id = :landHoldingId")
    suspend fun updateCurrentCrop(landHoldingId: String, cropName: String?)
    
    @Query("UPDATE land_holdings SET area = :area WHERE id = :landHoldingId")
    suspend fun updateLandArea(landHoldingId: String, area: Double)
    
    @Query("SELECT * FROM land_holdings WHERE userId = :userId AND soilType = :soilType")
    fun getLandHoldingsBySoilType(userId: String, soilType: String): Flow<List<LandHoldingEntity>>
}
