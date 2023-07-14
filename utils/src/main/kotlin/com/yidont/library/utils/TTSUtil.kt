@file:JvmName("TTSUtilKt")

package com.yidont.library.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*


object TTSUtil : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val ttsListener = TTSListener()
    private var reinitialize: Pair<Boolean, TTSBean?> = false to null

    private val speakList = mutableListOf<TTSBean>()

    private var defVolume: Int? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.CHINESE) ?: -2
            if (result >= 0) {
                tts?.setOnUtteranceProgressListener(ttsListener)
                setResetSpeak()
            } else {
                val context = appContext ?: return
                createNotificationChannel(context, "tts", "TTS引擎异常提醒")
                if (isAppInstalled(context, "com.iflytek.speechcloud")) {
                    settingsNotice()
                } else {
                    downloadApkNotice("当前TTS引擎不支持中文，请更换或下载其他引擎。\n点击下载TTS引擎，安装完成之后需要在手机设置里搜索TTS(文字转语音)更换【系统语音】为首选引擎，设置完成之后重启App即可正常使用")
                }
            }
        } else {
            val context = appContext ?: return
            createNotificationChannel(context, "tts", "TTS引擎异常提醒")
            downloadApkNotice("系统TTS引擎异常，请更换或下载其他引擎。\n点击下载TTS引擎，安装完成之后需要在手机设置里搜索TTS(文字转语音)更换【系统语音】为首选引擎，设置完成之后重启App即可正常使用")
        }
    }

    private var appContext: Context? = null

    fun addSpeak(context: Context?, bean: TTSBean?) {
        appContext = context?.applicationContext
        bean?.text ?: return
        if (bean.text.isBlank()) return
        var queueMode = TextToSpeech.QUEUE_ADD
        if (bean.flush) {
            speakList.clear()
            queueMode = TextToSpeech.QUEUE_FLUSH
        }
        speakList.add(bean)
        if (speakList.size == 1 || tts?.isSpeaking != true) {
            speak(bean, queueMode)
        }
    }

    private fun speak(bean: TTSBean, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        val msg = bean.text ?: return
        if ((tts?.isLanguageAvailable(Locale.CHINESE) ?: -2) >= 0) {
            tts?.speak(msg, queueMode, null, System.currentTimeMillis().toString())
        } else {
            // tts被系统回收，需要重新初始化
            reinitialize = true to bean
            tts?.shutdown()
            tts = null
            appContext ?: return
            tts = TextToSpeech(appContext, this)
        }
    }

    private fun setResetSpeak() {
        if (reinitialize.first) {
            val msg = reinitialize.second?.text
            reinitialize = false to null
            tts?.speak(msg, TextToSpeech.QUEUE_ADD, null, System.currentTimeMillis().toString())
        }
    }

    private fun downloadApkNotice(text: String) {
        val intent = try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ttsAppUri())).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            PendingIntent.getActivity(appContext, 3, intent, PendingIntent.FLAG_IMMUTABLE)
        } catch (e: Exception) {
            Log.e("zwonb", "downloadApkNotice获取intent出错", e)
            null
        }
        val context = appContext ?: return
        val style = NotificationCompat.BigTextStyle()
        style.bigText(text)

        val notification = buildNotificationBase(context, "tts")
            .setContentIntent(intent)
            .setContentTitle("缺少文字转语音引擎")
            .setContentText(text)
            .setStyle(style)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun settingsNotice() {
        val intent = try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            PendingIntent.getActivity(appContext, 3, intent, PendingIntent.FLAG_IMMUTABLE)
        } catch (e: Exception) {
            Log.e("zwonb", "settingsNotice获取intent出错", e)
            null
        }
        val context = appContext ?: return
        val style = NotificationCompat.BigTextStyle()
        style.bigText("当前TTS引擎不支持中文，请在手机设置里搜索TTS(文字转语音)更换【系统语音】为首选引擎，设置完成之后重启App即可正常使用")

        val notification = buildNotificationBase(context, "tts")
            .setContentIntent(intent)
            .setContentTitle("切换文字转语音引擎")
            .setStyle(style)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun setVolume(volume: Int?) {
        volume ?: return
        val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND)
        } catch (e: Exception) {
            Log.e("zwonb", "设置音量出错", e)
        }
    }

    /**
     * [volumePercent] 0..1
     */
    private fun setVolume(volumePercent: Float) {
        val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVolume = (maxVolume.toFloat() * volumePercent).toInt()
            Log.i("zwonb", "设的音量：$newVolume")
            am.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND)
        } catch (e: Exception) {
            Log.e("zwonb", "设置音量出错", e)
        }
    }

    private fun getCurrentVolume(): Int? {
        val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return null
        return try {
            am.getStreamVolume(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            null
        }
    }

    private fun startSetVolume() {
        defVolume = getCurrentVolume()
        speakList.firstOrNull()?.let {
            if (it.percent >= 0) {
                setVolume(it.percent)
            }
        }
    }

    private fun finishSetDefVolume() {
        setVolume(defVolume)
        try {
            speakList.removeAt(0)
        } finally {
            val next = speakList.firstOrNull()
            if (next != null) speak(next)
        }
    }

    internal class TTSListener : UtteranceProgressListener() {

        override fun onStart(utteranceId: String?) {
            startSetVolume()
        }

        override fun onDone(utteranceId: String?) {
            finishSetDefVolume()
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            finishSetDefVolume()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            finishSetDefVolume()
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
        }
    }

}

// 科大讯飞语音引擎3.0
private fun ttsAppUri() =
    "https://zuyu.imgoss.dingkeyun.cn/cos/0/file/20230412/6abc6d66e04e3de0.apk"

/**
 * percent 0..1
 */
class TTSBean(val text: String?, val percent: Float = -1f, val flush: Boolean = false)

/**
 * 涉及到多进程，需要跨进程通讯
 */
class TTSMessengerService : Service() {

    companion object {
        const val TEXT = "text"
        const val PERCENT = "percent"
        const val FLUSH = "flush"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Messenger(MessengerHandler(this)).binder
    }

    class MessengerHandler(private val context: Context) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val text = msg.data.getString(TEXT) ?: return
            val percent = msg.data.getFloat(PERCENT, -1f)
            val flush = msg.data.getBoolean(FLUSH, false)
            val bean = TTSBean(text, percent, flush)
            TTSUtil.addSpeak(context, bean)
        }
    }
}