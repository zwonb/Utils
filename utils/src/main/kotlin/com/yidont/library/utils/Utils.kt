package com.yidont.library.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

fun startAppSetting(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.parse("package:${context.packageName}")
    if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
    }
}

fun startSystemSetting(context: Context) {
    val intent = Intent(Settings.ACTION_SETTINGS)
    if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
    }
}

fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(LocationManager::class.java)
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    || lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    } catch (e: Exception) {
        false
    }
}

fun startLocationSetting(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        startSystemSetting(context)
    }
}

fun startWifiSetting(context: Context) {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
    }
}

fun batteryOptimization(context: Context) {
    if (isIgnoringBatteryOptimizations(context)) return
    requestIgnoreBatteryOptimizations(context)
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pm = context.getSystemService(PowerManager::class.java)
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

@SuppressLint("BatteryLife")
fun requestIgnoreBatteryOptimizations(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            if (context !is Activity) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("zwonb", "requestIgnoreBatteryOptimizations error", e)
    }
}

fun isAppInstalled(context: Context?, packageName: String) = try {
    val pm = context?.packageManager
    @Suppress("DEPRECATION")
    pm?.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES) ?: false
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
}

@SuppressLint("HardwareIds")
fun androidIdShort(context: Context): String {
    return try {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            .takeLast(16)
    } catch (e: Exception) {
        UUID.randomUUID().toString().replace("-", "").takeLast(16)
    }
}

fun File.fileToUri(context: Context): Uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
    Uri.fromFile(this)
} else {
    val authority = "${context.packageName}.dc.fileprovider"
    FileProvider.getUriForFile(context, authority, this)
}

fun saveFileToDownloadDir(context: Context, file: File) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val uri = ContentValues().run {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(context, Uri.fromFile(file)))
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, this)
            //当相同路径下的文件，在文件管理器中被手动删除时，就会插入失败
        } ?: throw NullPointerException("插入数据失败")
        resolver.openOutputStream(uri)?.use {
            file.inputStream().copyTo(it)
            it.flush()
        }
    } else {
        val directory =
            "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOWNLOADS}"
        val target = File("$directory/${file.name}")
        file.copyTo(target)
    }
}

fun getMimeType(context: Context, uri: Uri): String? {
    val contentResolver: ContentResolver = context.contentResolver
    return contentResolver.getType(uri)
}

fun setWifiEnabled(context: Context, enabled: Boolean) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } else {
        val wifiManager = context.getSystemService(WifiManager::class.java)
        @Suppress("DEPRECATION")
        wifiManager.isWifiEnabled = enabled
    }
}

fun numberAdd0(number: Int) = if (number < 10) "0$number" else number.toString()