package ru.ok.timer;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class Timer extends Service {
    private boolean run;
    private Intent intent;


    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        run = true;
        someTask();
        this.intent = new Intent("increaseTimer");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        run = false;
    }

    public IBinder onBind(Intent intent) {
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
