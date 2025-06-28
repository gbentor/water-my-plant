package com.example.watermyplant

import android.app.Application
import com.example.watermyplant.data.TokenManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WaterMyPlantApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            tokenManager.initialize()
        }
    }
} 