package com.example.krishisevak

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class KrishiSevakApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: KrishiSevakApplication
            private set

        fun updateLocale(languageTag: String) {
            val locale = Locale.forLanguageTag(languageTag)
            Locale.setDefault(locale)
            val resources = instance.resources
            val config = resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }
            resources.updateConfiguration(config, resources.displayMetrics)
            AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(languageTag))
        }
    }
}
