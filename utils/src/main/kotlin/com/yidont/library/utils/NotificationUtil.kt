package com.yidont.library.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
    sound: String = "",
    importance: Int? = null
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        channelId,
        name,
        importance ?: NotificationManager.IMPORTANCE_HIGH
    ).apply {
        enableVibration(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        if (sound.isNotEmpty()) {
            val uri = "android.resource://${context.packageName}/raw/$sound"
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            setSound(Uri.parse(uri), attributes)
        }
    }
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
}

fun areNotificationsEnabled(context: Context): Boolean {
    return NotificationManagerCompat.from(context).areNotificationsEnabled()
}

fun areNotificationsChannelEnabled(
    context: Context, channelId: String
): Boolean {
    if (!areNotificationsEnabled(context)) return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val importance = manager.getNotificationChannel(channelId).importance
        if (importance < NotificationManager.IMPORTANCE_LOW) {
            return false
        }
    }
    return true
}

fun startNotificationSetting(context: Context) {
    val intent = Intent().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("app_package", context.packageName)
            putExtra("app_uid", context.applicationInfo.uid)
        }
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

fun notifyBase(
    context: Context,
    channelId: String,
    notifyId: Int,
    title: String? = null,
    text: String? = null
) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    val pendingIntent = PendingIntent.getActivity(context, 1, intent, flag)

    val style = NotificationCompat.BigTextStyle()
    style.bigText(text)

    val notification = buildNotificationBase(context, channelId)
        .setContentIntent(pendingIntent)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(style)
        .build()

    val manager = context.getSystemService(NotificationManager::class.java)
    manager.notify(notifyId, notification)
}

fun buildNotificationBase(context: Context, channelId: String) =
    NotificationCompat.Builder(context, channelId)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        .setAutoCancel(true)
        .setLargeIcon(null)
        .setSmallIcon(R.drawable.notification_small)