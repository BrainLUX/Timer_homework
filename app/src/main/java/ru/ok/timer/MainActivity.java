package ru.ok.timer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class MainActivity extends AppCompatActivity {

    private Button startStop;
    private Button reset;
    private byte mode;
    private EditText timer;
    private TimerReceiver timerReceiver;
    private NotifyReceiver notifyReceiver;
    private long millisecondTime;
    private long startTime;
    private long timeBuff;
    private long updateTime;
    private long inputTime;
    private int seconds;
    private int minutes;
    private int milliSeconds;
    private int counter;
    private boolean hidden;
    private SharedPreferences sp;
    private final String SP_NAME = "timer";
    private final String STARTTIME_KEY = "startTime";
    private final String MODE_KEY = "mode";
    private final String TIMEBUFF_KEY = "timeBuff";
    private final String INPUTTIME_KEY = "inputTime";
    private final String STOP_ACTION = "stop";
    private final String START_ACTION = "start";
    private final String RESET_ACTION = "reset";
    private PendingIntent resultPendingIntent;
    private PendingIntent resultStopIntent;
    private PendingIntent resultStartIntent;
    private PendingIntent resultResetIntent;
    private final String TIMER_ID = "timer";
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        timerReceiver = new TimerReceiver();
        notifyReceiver = new NotifyReceiver();
        initUI();
        registerBroadcastReceiver();
        getExtra();
        setIntents();
        createNotificationChannel();
    }

    private void setIntents() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(STOP_ACTION);
        resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stopIntent = new Intent(this, NotificationReceiver.class);
        stopIntent.setAction(STOP_ACTION);
        resultStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent,
                0);
        Intent startIntent = new Intent(this, NotificationReceiver.class);
        startIntent.setAction(START_ACTION);
        resultStartIntent = PendingIntent.getBroadcast(this, 0, startIntent,
                0);
        Intent resetIntent = new Intent(this, NotificationReceiver.class);
        resetIntent.setAction(RESET_ACTION);
        resultResetIntent = PendingIntent.getBroadcast(this, 0, resetIntent,
                0);
    }

    private void getExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startTime = Long.parseLong(sp.getString(STARTTIME_KEY, "0"));
            mode = Byte.parseByte(sp.getString(MODE_KEY, "0"));
            timeBuff = Long.parseLong(sp.getString(TIMEBUFF_KEY, "0"));
            inputTime = Long.parseLong(sp.getString(INPUTTIME_KEY, "0"));
            if (mode != 0) {
                timer.setEnabled(false);
            }
            if (startTime != 0) {
                if (mode == 1) {
                    timer.setText(secondsToTime(timeBuff + System.currentTimeMillis() - startTime));
                    startService(new Intent(MainActivity.this, Timer.class));
                    reset.setEnabled(false);
                    startStop.setText("Stop");
                } else {
                    timer.setText(secondsToTime(inputTime - timeBuff));
                }
            }
        } else {
            sp.edit().clear().apply();
        }
    }

    private void initUI() {
        timer = findViewById(R.id.timer);
        startStop = findViewById(R.id.btn_start_stop);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode();
            }
        });
        reset = findViewById(R.id.btn_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
    }

    private void switchMode() {
        if (mode == -1 || mode == 0) {
            start();
        } else {
            stop();
        }
    }

    private void stop() {
        mode = -1;
        reset.setEnabled(true);
        stopService(new Intent(MainActivity.this, Timer.class));
        timeBuff += millisecondTime;
        sp.edit().putString(TIMEBUFF_KEY, String.valueOf(timeBuff)).apply();
        startStop.setText("Start");
        timer.setEnabled(false);
        sp.edit().putString(MODE_KEY, String.valueOf(mode)).apply();
    }

    private void start() {
        if (mode == 0) {
            inputTime = timeToSeconds(timer.getText().toString());
        }
        mode = 1;
        reset.setEnabled(false);
        startTime = System.currentTimeMillis();
        sp.edit().putString(STARTTIME_KEY, String.valueOf(startTime)).apply();
        sp.edit().putString(INPUTTIME_KEY, String.valueOf(inputTime)).apply();
        startService(new Intent(MainActivity.this, Timer.class));
        startStop.setText("Stop");
        timer.setEnabled(false);
        sp.edit().putString(MODE_KEY, String.valueOf(mode)).apply();
    }

    private void reset() {
        millisecondTime = 0L;
        startTime = 0L;
        timeBuff = 0L;
        updateTime = 0L;
        seconds = 0;
        minutes = 0;
        milliSeconds = 0;
        mode = 0;
        sp.edit().clear().apply();
        timer.setText(secondsToTime(inputTime));
        timer.setEnabled(true);
        sp.edit().putString(MODE_KEY, String.valueOf(mode)).apply();
    }


    public void registerBroadcastReceiver() {
        this.registerReceiver(timerReceiver, new IntentFilter(
                "increaseTimer"));
        this.registerReceiver(notifyReceiver, new IntentFilter(
                "notify"));
    }

    public void unregisterBroadcastReceiver() {
        this.unregisterReceiver(timerReceiver);
        this.unregisterReceiver(notifyReceiver);
    }

    private class TimerReceiver extends BroadcastReceiver {

        public TimerReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mode == 1) {
                millisecondTime = System.currentTimeMillis() - startTime;
                updateTime = timeBuff + millisecondTime;
                timer.setText(secondsToTime(inputTime - updateTime));
                counter++;
                if (counter % 100 == 0 && hidden) {
                    createNotification(false);
                }
                if (inputTime - updateTime <= 0) {
                    mode = 1;
                    if (hidden) {
                        createNotification(true);
                    }
                    stop();
                    reset();
                    timer.setText(secondsToTime(inputTime));
                }
            }
        }
    }

    private class NotifyReceiver extends BroadcastReceiver {
        public NotifyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                switch (extras.getString("action")) {
                    case STOP_ACTION:
                        stop();
                        createNotification(true);
                        break;
                    case START_ACTION:
                        start();
                        break;
                    case RESET_ACTION:
                        stop();
                        reset();
                        startActivity(new Intent(context, MainActivity.class));
                        break;
                }
            }
        }
    }

    private void createNotification(boolean paused) {
        String notifyText = timer.getText().toString();
        notifyText = notifyText.substring(0, notifyText.length() - 4);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, TIMER_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(!paused ? "Таймер запущен" : "Таймер на паузе")
                        .setContentText(notifyText)
                        .addAction(R.mipmap.ic_launcher, !paused ? "Stop" : "Start", !paused ? resultStopIntent : resultStartIntent)
                        .addAction(R.mipmap.ic_launcher, "Reset", resultResetIntent)
                        .setContentIntent(resultPendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

    private void createNotificationChannel() {
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("", "createNotificationChannel: ");
            CharSequence name = "timer";
            String description = "time";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(TIMER_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @SuppressLint("DefaultLocale")
    private String secondsToTime(long updateTime) {
        seconds = (int) (updateTime / 1000);
        minutes = seconds / 60;
        seconds = seconds % 60;
        milliSeconds = (int) (updateTime % 1000);
        return String.format("%02d:%02d:%03d", minutes, seconds, milliSeconds);
    }

    private long timeToSeconds(String time) {
        String[] buff = time.split(":");
        return Long.parseLong(buff[0]) * 60000 + Long.parseLong(buff[1]) * 1000 + Long.parseLong(buff[2]);
    }

    @Override
    protected void onPause() {
        hidden = true;
        if (mode == -1) {
            createNotification(true);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        hidden = false;
        notificationManager.cancelAll();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        getIntent().putExtra("refresh", 1);
        notificationManager.cancelAll();
        super.onDestroy();
    }

}
