package com.rocket.cosmic_detox.presentation.model

object AppManager {

    private val appList: List<App> = getDummyData()

    private fun getDummyData(): List<App> {
        return listOf(
            App(
                packageId = "1",
                appName = "Alice",
                appIcon = 0,
                limitedTime = 1000
            ),
            App(
                packageId = "2",
                appName = "Bob",
                appIcon = 0,
                limitedTime = 2000
            ),
            App(
                packageId = "3",
                appName = "Charlie",
                appIcon = 0,
                limitedTime = 3000
            ),
            App(
                packageId = "4",
                appName = "David",
                appIcon = 0,
                limitedTime = 4000
            ),
            App(
                packageId = "5",
                appName = "Eve",
                appIcon = 0,
                limitedTime = 5000
            ),
            App(
                packageId = "6",
                appName = "Frank",
                appIcon = 0,
                limitedTime = 6000
            ),
            App(
                packageId = "7",
                appName = "Grace",
                appIcon = 0,
                limitedTime = 7000
            ),
            App(
                packageId = "8",
                appName = "Hank",
                appIcon = 0,
                limitedTime = 8000
            ),
            App(
                packageId = "9",
                appName = "Ivy",
                appIcon = 0,
                limitedTime = 9000
            ),
            App(
                packageId = "10",
                appName = "Jack",
                appIcon = 0,
                limitedTime = 10000
            )
        )
    }

    fun getAppList(): List<App> {
        return appList
    }
}