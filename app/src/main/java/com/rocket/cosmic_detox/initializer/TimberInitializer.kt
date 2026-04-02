package com.rocket.cosmic_detox.initializer

import android.content.Context
import androidx.startup.Initializer
import com.rocket.cosmic_detox.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    val fileName = element.fileName ?: "Unknown"
                    return "${BuildConfig.APPLICATION_ID}://$fileName:${element.lineNumber}#${element.methodName}"
                }
            })
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
