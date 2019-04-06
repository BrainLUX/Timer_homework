package ru.ok.timer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Timer extends Service {
    private static final String TAG = "Timer";
    private boolean run;
    private Intent intent;


    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        run = true;
        someTask();
        this.intent = new Intent("increaseTimer");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        run = false;
        Log.d(TAG, "onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    void someTask() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (run) {
                    sendBroadcast(intent);
                }
                handler.postDelayed(this, 0);
            }
        });
    }


}
