package com.myapplication

import KoinHelper
import android.app.Application

class SandeerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        KoinHelper().startKoin()
    }
}