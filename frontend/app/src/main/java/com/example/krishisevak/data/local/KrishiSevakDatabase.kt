package com.example.krishisevak.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.krishisevak.data.local.converters.Converters
import com.example.krishisevak.data.local.entities.*
import com.example.krishisevak.data.local.dao.*

@Database(
    entities = [
        UserEntity::class,
        LandHoldingEntity::class,
        CropEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KrishiSevakDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun landHoldingDao(): LandHoldingDao
    abstract fun cropDao(): CropDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: KrishiSevakDatabase? = null

        fun getDatabase(context: Context): KrishiSevakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KrishiSevakDatabase::class.java,
                    "krishisevak_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
