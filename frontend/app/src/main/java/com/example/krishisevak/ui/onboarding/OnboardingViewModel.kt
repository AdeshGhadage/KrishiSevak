package com.example.krishisevak.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.krishisevak.data.local.KrishiSevakDatabase
import com.example.krishisevak.data.local.entities.UserEntity
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = KrishiSevakDatabase.getDatabase(application)
    private val userDao = database.userDao()
    
    fun saveUserData(user: UserEntity) {
        viewModelScope.launch {
            try {
                userDao.insertUser(user)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle database errors gracefully
            }
        }
    }
    
    suspend fun isUserOnboarded(): Boolean {
        return try {
            val user = userDao.getUser(1)
            user?.isOnboardingComplete == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
