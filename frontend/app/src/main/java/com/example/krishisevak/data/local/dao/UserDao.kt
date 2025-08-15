package com.example.krishisevak.data.local.dao

import androidx.room.*
import com.example.krishisevak.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Int): UserEntity?
    
    @Query("SELECT * FROM users ORDER BY lastActive DESC")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Query("UPDATE users SET lastActive = :timestamp WHERE id = :userId")
    suspend fun updateLastActive(userId: Int, timestamp: java.time.LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)
    
    @Query("SELECT * FROM users WHERE isOnboardingComplete = 1 LIMIT 1")
    suspend fun getOnboardedUser(): UserEntity?
    
    @Query("UPDATE users SET isOnboardingComplete = 1 WHERE id = :userId")
    suspend fun markOnboardingComplete(userId: Int)
}