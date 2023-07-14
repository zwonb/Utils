@file:JvmName("KeepAliveUtil")

package com.yidont.library.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.*

object KeepAliveUtil {

    // 是否可以用唤醒锁
    private fun isWakeLockWork() = isXIAOMI() || isViVo()

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null

    //    private var wifiLock: WifiManager.WifiLock? = null
    private var player: MediaPlayer? = null

    fun init(appContext: Context) {
        release()
        Log.d("zwonb", "KeepAlive init")
//        initWifiLock(appContext)
        if (isWakeLockWork() /*&& isIgnoringBatteryOptimizations(appContext)*/) {
            initWakeLock(appContext)
        } else {
            initPlayer(appContext)
        }
//        testAlive()
    }

    fun release() {
        Log.d("zwonb", "KeepAlive release")
        if (scope.isActive) scope.cancel()
//        releaseWifiLock()
//        if (isWakeLockWork()) {
        releaseWakeLock()
//        } else {
        releasePlay()
//        }
    }

    @SuppressLint("WakelockTimeout")
    private fun initWakeLock(appContext: Context) {
        val pm = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FootBath:Push")
        wakeLock?.apply {
            setReferenceCounted(false)
            acquire()
        }
    }

//    private fun initWifiLock(appContext: Context) {
//        releaseWifiLock()
//        val wifiManager =
//            appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            WifiManager.WIFI_MODE_FULL_HIGH_PERF
//        } else {
//            @Suppress("DEPRECATION")
//            WifiManager.WIFI_MODE_FULL
//        }
//        wifiLock = wifiManager.createWifiLock(mode, "FootBath:Player")
//        wifiLock?.apply {
//            setReferenceCounted(false)
//            acquire()
//        }
//    }

    private fun initPlayer(appContext: Context) {
        scope.launch(Dispatchers.IO) {
            player = if (player == null) MediaPlayer.create(appContext, R.raw.notice_0) else player
            player?.apply {
                setVolume(0.01f, 0f)
                setWakeMode(appContext, PowerManager.PARTIAL_WAKE_LOCK)
                isLooping = true
                startPlay()
            }
        }
    }

    private fun startPlay() {
        player?.apply {
            try {
                if (!isPlaying) start()
            } catch (e: Exception) {
                Log.e("zwonb", "startPlay出错", e)
            }
        }
    }

    private fun releasePlay() {
        try {
            player?.stop()
            player?.reset()
        } finally {
            player?.release()
            player = null
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.apply {
            if (isHeld) release()
            wakeLock = null
        }
    }

//    private fun releaseWifiLock() {
//        wifiLock?.apply {
//            if (isHeld) release()
//            wifiLock = null
//        }
//    }


    private fun testAlive() {
        scope.launch {
            while (isActive) {
//                writeLog()
//                TTSUtil.speak("我还活着")
                delay(1_000)
            }
        }
    }

}

//fun writeLog(text: String = "") {
//    val file = File(appContext.getExternalFilesDir(null), "log.txt")
//    FileOutputStream(file, true).buffered().use {
//        val time = SimpleDateFormat("HH:mm:ss").format(Date())
//        val str = "$time $text\n"
//        try {
//            it.write(str.toByteArray(Charsets.UTF_8))
//            it.flush()
//        } catch (_: Throwable) {
//        }
//    }
//}

