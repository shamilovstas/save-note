package com.shamilovstas

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotesApp: Application() {

    override fun onCreate() {
        super.onCreate()

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
    }
}