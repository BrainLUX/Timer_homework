package ru.ok.timer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import ru.ok.timer.App;
import ru.ok.timer.MainActivity;
import ru.ok.timer.R;
import ru.ok.timer.timer.Timer;
import ru.ok.timer.reciever.NotificationReceiver;

public class TimerService extends Service {
    private NotificationManager notificationManager;
    private static final int FOREGROUND_ID = 1;
    public static final String STOP_ACTION = "stop";
    public static final String START_ACTION = "start";
    public static final String RESET_ACTION = "reset";
    private PendingIntent resultPendingIntent;
    private PendingIntent resultStopIntent;
    private PendingIntent resultStartIntent;
    private PendingIntent resultResetIntent;
    private Handler handler;
    private Runnable runnable;

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        initResources();
        setIntents();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (Timer.isStarted()) {
                    Timer.update();
                }
                if (Timer.isEnded()) {
                    Timer.resetTimer();
                }
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(TimerService.this, App.TIMER_ID)
                                .setSmallIcon(R.drawable.ic_notification_timer)
                                .setContentTitle(Timer.isStarted() ? getResources().getString(R.string.timer_started) : getResources().getString(R.string.timer_paused))
                                .setContentText(MainActivity.secondsToTime(Timer.getTime(), true))
                                .addAction(R.drawable.ic_notification_timer, Timer.isStarted() ? getResources().getString(R.string.stop) : getResources().getString(R.string.start),
                                        Timer.isStarted() ? resultStopIntent : resultStartIntent)
                                .addAction(R.drawable.ic_notification_timer, getResources().getString(R.string.reset), resultResetIntent)
                                .setContentIntent(resultPendingIntent);
                Notification notification = builder.build();
                startForeground(FOREGROUND_ID, notification);
                handler.postDelayed(this, 1000);
            }
        };
        if (!Timer.isEnded()) {
            doWork();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initResources() {
        if (MainActivity.sp == null) {
            MainActivity.sp = getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        }
        if (MainActivity.sp.getAll().size() != 0 && Timer.getMode() == Timer.TimerMode.STOPPED) {
            Timer.initTimer(MainActivity.sp);
            if (Timer.getMode() == Timer.TimerMode.PAUSED) {
                Timer.startTimer(null);
                Timer.update();
                Timer.stop();
            } else if (Timer.getMode() == Timer.TimerMode.STARTED) {
                Timer.update();
                Timer.stop();
                Timer.startTimer(null);
            }
        }
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setIntents() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stopIntent = new Intent(this, NotificationReceiver.class);
        stopIntent.setAction(TimerService.STOP_ACTION);
        resultStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent startIntent = new Intent(this, NotificationReceiver.class);
        startIntent.setAction(TimerService.START_ACTION);
        resultStartIntent = PendingIntent.getBroadcast(this, 0, startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resetIntent = new Intent(this, NotificationReceiver.class);
        resetIntent.setAction(TimerService.RESET_ACTION);
        resultResetIntent = PendingIntent.getBroadcast(this, 0, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    void doWork() {
        handler.post(runnable);
    }
}
