package com.yidont.library.utils


import android.os.Build

fun isHuawei() = Build.MANUFACTURER.contains("huawei", true)
fun isHonor() = Build.MANUFACTURER.contains("honor", true)
fun isXIAOMI() = Build.MANUFACTURER.contains("Xiaomi", true)
fun isOPPO() = Build.MANUFACTURER.contains("oppo", true)
fun isOnePlus() = Build.MANUFACTURER.contains("OnePlus", true)
fun isViVo() = Build.MANUFACTURER.contains("vivo", true)

fun isHarmonyOs(): Boolean {
    return try {
        val buildExClass = Class.forName("com.huawei.system.BuildEx")
        val osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass)!!.toString()
        osBrand.contains("Harmony", true)
    } catch (t: Throwable) {
        false
    }
}