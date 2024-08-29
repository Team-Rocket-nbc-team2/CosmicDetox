package com.rocket.cosmic_detox.presentation.model

import com.rocket.cosmic_detox.R

object AppManager {

    private val appList: List<App> = getDummyData()

    private fun getDummyData(): List<App> {
        return listOf(
            App(
                packageId = "1",
                appName = "Alice",
                appIcon = null,
                limitedTime = 1000
            ),
            App(
                packageId = "2",
                appName = "Bob",
                appIcon = null,
                limitedTime = 2000
            ),
            App(
                packageId = "3",
                appName = "Charlie",
                appIcon = null,
                limitedTime = 3000
            ),
            App(
                packageId = "4",
                appName = "David",
                appIcon = null,
                limitedTime = 4000
            ),
            App(
                packageId = "5",
                appName = "Eve",
                appIcon = null,
                limitedTime = 5000
            ),
            App(
                packageId = "6",
                appName = "Frank",
                appIcon = null,
                limitedTime = 6000
            ),
            App(
                packageId = "7",
                appName = "Grace",
                appIcon = null,
                limitedTime = 7000
            ),
            App(
                packageId = "8",
                appName = "Hank",
                appIcon = null,
                limitedTime = 8000
            ),
            App(
                packageId = "9",
                appName = "Ivy",
                appIcon = null,
                limitedTime = 9000
            ),
            App(
                packageId = "10",
                appName = "Jack",
                appIcon = null,
                limitedTime = 10000
            )
        )
    }

    fun getAppList(): List<App> {
        return appList
    }
}