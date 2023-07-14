package com.yidont.library.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

private fun devicesMap() = mapOf(
    "HUAWEI" to arrayOf(
        "com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity",
        "com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity",
        "com.huawei.systemmanager/.optimize.process.ProtectActivity",
        "com.huawei.systemmanager/.optimize.bootstart.BootStartActivity",
        "com.huawei.systemmanager"
    ),
    "Xiaomi" to arrayOf(
        "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity",
        "com.miui.securitycenter"
    ),
    "vivo" to arrayOf(
        "com.iqoo.secure/.ui.phoneoptimize.BgStartUpManager",
        "com.iqoo.secure/.safeguard.PurviewTabActivity",
        "com.vivo.permissionmanager/.activity.BgStartUpManagerActivity",
        "com.iqoo.secure",
        "com.vivo.permissionmanager"
    ),
    "OPPO" to arrayOf(
        "com.coloros.safecenter/.startupapp.StartupAppListActivity",
        "com.coloros.safecenter/.permission.startup.StartupAppListActivity",
        "com.oppo.safe/.permission.startup.StartupAppListActivity",
        "com.coloros.oppoguardelf/com.coloros.powermanager.fuelgaue.PowerUsageModelActivity",
        "com.coloros.safecenter/com.coloros.privacypermissionsentry.PermissionTopActivity",
        "com.coloros.safecenter",
        "com.oppo.safe",
        "com.coloros.oppoguardelf"
    ),
    "Meizu" to arrayOf(
        "com.meizu.safe/.permission.SmartBGActivity",
        "com.meizu.safe/.permission.PermissionMainActivity",
        "com.meizu.safe"
    ),
    "samsung" to arrayOf(
        "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity",
        "com.samsung.android.sm_cn/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
        "com.samsung.android.sm_cn/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
        "com.samsung.android.sm_cn/.ui.ram.RamActivity",
        "com.samsung.android.sm_cn/.app.dashboard.SmartManagerDashBoardActivity",
        "com.samsung.android.sm/com.samsung.android.sm.ui.ram.AutoRunActivity",
        "com.samsung.android.sm/com.samsung.android.sm.ui.appmanagement.AppManagementActivity",
        "com.samsung.android.sm/com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
        "com.samsung.android.sm/.ui.ram.RamActivity",
        "com.samsung.android.sm/.app.dashboard.SmartManagerDashBoardActivity",
        "com.samsung.android.lool/com.samsung.android.sm.ui.battery.BatteryActivity",
        "com.samsung.android.sm_cn",
        "com.samsung.android.sm"
    ),
    "oneplus" to arrayOf(
        "com.oneplus.security/.chainlaunch.view.ChainLaunchAppListActivity",
        "com.oneplus.security"
    ),
    "zte" to arrayOf(
        "com.zte.heartyservice/.autorun.AppAutoRunManager",
        "com.zte.heartyservice"
    ),
    "letv" to arrayOf(
        "com.letv.android.letvsafe/.AutobootManageActivity",
        "com.letv.android.letvsafe/.BackgroundAppManageActivity",
        "com.letv.android.letvsafe"
    ),
    // 金立
    "F" to arrayOf("com.gionee.softmanager/.MainActivity", "com.gionee.softmanager"),
    "smartisanos" to arrayOf(
        "com.smartisanos.security/.invokeHistory.InvokeHistoryActivity",
        "com.smartisanos.security"
    ),
    "360" to arrayOf(
        "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
        "com.yulong.android.coolsafe"
    ),
    "ulong" to arrayOf(
        "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity",
        "com.yulong.android.coolsafe"
    ),
    "coolpad" to arrayOf(
        "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain",
        "com.yulong.android.security"
    ),
    "YuLong" to arrayOf(
        "com.yulong.android.softmanager/.SpeedupActivity",
        "com.yulong.android.security/com.yulong.android.seccenter.tabbarmain",
        "com.yulong.android.security"
    ),
    "lenovo" to arrayOf(
        "com.lenovo.security/.purebackground.PureBackgroundActivity",
        "com.lenovo.security"
    ),
    "htc" to arrayOf(
        "com.htc.pitroad/.landingpage.activity.LandingPageActivity",
        "com.htc.pitroad"
    ),
    "asus" to arrayOf("com.asus.mobilemanager/.MainActivity", "com.asus.mobilemanager"),
)

fun autoStartSetting(context: Context) {
    var start = false
    out@ for ((key, list) in devicesMap()) {
        if (Build.MANUFACTURER.equals(key, true)) {
            for (act in list) {
                try {
                    val intent = if (act.contains("/")) {
                        Intent().apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            component = ComponentName.unflattenFromString(act)
                        }
                    } else {
                        context.packageManager.getLaunchIntentForPackage(act)
                    }
                    context.startActivity(intent)
                    start = true
                    break@out
                } catch (e: Exception) {
//                    logE("启动失败", e)
                }
            }
        }
    }
    if (!start) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}