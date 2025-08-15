package com.example.krishisevak.data.local.dao

import androidx.room.*
import com.example.krishisevak.data.local.entities.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getUserPreferences(userId: String): UserPreferencesEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferencesEntity)
    
    @Update
    suspend fun updateUserPreferences(preferences: UserPreferencesEntity)
    
    @Delete
    suspend fun deleteUserPreferences(preferences: UserPreferencesEntity)
    
    @Query("UPDATE user_preferences SET priceAlerts = :enabled WHERE userId = :userId")
    suspend fun updatePriceAlerts(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET weatherUpdates = :enabled WHERE userId = :userId")
    suspend fun updateWeatherUpdates(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET schemeUpdates = :enabled WHERE userId = :userId")
    suspend fun updateSchemeUpdates(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET diseaseAlerts = :enabled WHERE userId = :userId")
    suspend fun updateDiseaseAlerts(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET irrigationReminders = :enabled WHERE userId = :userId")
    suspend fun updateIrrigationReminders(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET pushNotifications = :enabled WHERE userId = :userId")
    suspend fun updatePushNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET smsNotifications = :enabled WHERE userId = :userId")
    suspend fun updateSmsNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET emailNotifications = :enabled WHERE userId = :userId")
    suspend fun updateEmailNotifications(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET voiceNavigation = :enabled WHERE userId = :userId")
    suspend fun updateVoiceNavigation(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET largeText = :enabled WHERE userId = :userId")
    suspend fun updateLargeText(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET highContrast = :enabled WHERE userId = :userId")
    suspend fun updateHighContrast(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET autoBackup = :enabled WHERE userId = :userId")
    suspend fun updateAutoBackup(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET offlineMode = :enabled WHERE userId = :userId")
    suspend fun updateOfflineMode(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET dataSync = :enabled WHERE userId = :userId")
    suspend fun updateDataSync(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_preferences SET cacheSize = :size WHERE userId = :userId")
    suspend fun updateCacheSize(userId: String, size: Int)
}
