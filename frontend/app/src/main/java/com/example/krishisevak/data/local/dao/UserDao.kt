package com.example.krishisevak.data.local.dao

import androidx.room.*
import com.example.krishisevak.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE aadhaarNumber = :aadhaarNumber")
    suspend fun getUserByAadhaar(aadhaarNumber: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("UPDATE users SET lastActive = :lastActive WHERE id = :userId")
    suspend fun updateLastActive(userId: String, lastActive: java.time.LocalDateTime)
    
    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, isActive: Boolean)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT * FROM users WHERE village = :village AND district = :district")
    fun getUsersByLocation(village: String, district: String): Flow<List<UserEntity>>
}
