package com.healthcare.yash.preeti.other

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HealthCareApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }
}