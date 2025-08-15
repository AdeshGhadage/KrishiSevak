package com.example.krishisevak.data.local.dao

import androidx.room.*
import com.example.krishisevak.data.local.entities.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    
    @Query("SELECT * FROM crops WHERE userId = :userId")
    fun getCropsByUserId(userId: String): Flow<List<CropEntity>>
    
    @Query("SELECT * FROM crops WHERE id = :cropId")
    suspend fun getCropById(cropId: String): CropEntity?
    
    @Query("SELECT * FROM crops WHERE userId = :userId AND status = :status")
    fun getCropsByStatus(userId: String, status: String): Flow<List<CropEntity>>
    
    @Query("SELECT * FROM crops WHERE userId = :userId AND cropName = :cropName")
    fun getCropsByName(userId: String, cropName: String): Flow<List<CropEntity>>
    
    @Query("SELECT * FROM crops WHERE userId = :userId AND season = :season")
    fun getCropsBySeason(userId: String, season: String): Flow<List<CropEntity>>
    
    @Query("SELECT DISTINCT cropName FROM crops WHERE userId = :userId")
    fun getDistinctCropNames(userId: String): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity)
    
    @Update
    suspend fun updateCrop(crop: CropEntity)
    
    @Delete
    suspend fun deleteCrop(crop: CropEntity)
    
    @Query("UPDATE crops SET status = :status WHERE id = :cropId")
    suspend fun updateCropStatus(cropId: String, status: String)
    
    @Query("UPDATE crops SET expectedHarvestDate = :harvestDate WHERE id = :cropId")
    suspend fun updateHarvestDate(cropId: String, harvestDate: java.time.LocalDateTime?)
    
    @Query("SELECT COUNT(*) FROM crops WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getActiveCropCount(userId: String): Int
    
    @Query("SELECT SUM(area) FROM crops WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getTotalCropArea(userId: String): Double?
}
