package com.rocket.cosmic_detox.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext

class GetPermissionUseCase {
    fun execute(@ApplicationContext context: Context) {
        val intent = Intent()
        val packageManager = context.packageManager

        // 사용량 통계 권한 설정 화면 이동 Intent
        val usageStatsIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        if (usageStatsIntent.resolveActivity(packageManager) != null) {
            intent.action = usageStatsIntent.action
        }

        // 오버레이 권한 설정 화면 이동 Intent
        val overlayPermissionIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
        if (overlayPermissionIntent.resolveActivity(packageManager) != null) {
            intent.action = overlayPermissionIntent.action
        }

        context.startActivity(intent)
    }
}