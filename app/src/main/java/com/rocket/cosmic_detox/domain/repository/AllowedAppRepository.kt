package com.rocket.cosmic_detox.domain.repository

import com.rocket.cosmic_detox.data.model.AllowedApp
import java.lang.Exception

interface AllowedAppRepository {
    fun getAllowedApps(callback: (List<AllowedApp>) -> Unit, failCallback: (Exception?) -> Unit)

    // todo :: 여기에 허용 앱 추가 함수 제작할 것.
}