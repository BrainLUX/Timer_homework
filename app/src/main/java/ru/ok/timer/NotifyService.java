package ru.ok.timer;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class NotifyService extends Service {
    private Intent intent;
    private boolean run;

    public NotifyService() {
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = new Intent("createNotification");
        run = true;
        doWork();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        run = false;
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    void doWork() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (run) {
                    sendBroadcast(intent);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }
}
