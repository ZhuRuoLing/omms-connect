package icu.takeneko.omms.connect

import android.app.Application
import com.google.android.material.color.DynamicColors

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        const val TAG = "OMMS_CONNECT"
    }
}