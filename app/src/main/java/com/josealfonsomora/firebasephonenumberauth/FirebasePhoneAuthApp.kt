package com.josealfonsomora.firebasephonenumberauth

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FirebasePhoneAuthApp: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
