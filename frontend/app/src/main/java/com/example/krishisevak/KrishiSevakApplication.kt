package com.example.krishisevak

import android.app.Application

class KrishiSevakApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: KrishiSevakApplication
            private set
    }
}
