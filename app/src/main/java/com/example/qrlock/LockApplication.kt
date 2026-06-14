package com.example.qrlock

import android.app.Application
import com.example.qrlock.data.LockRepository

class LockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LockRepository.init(this)
    }
}
