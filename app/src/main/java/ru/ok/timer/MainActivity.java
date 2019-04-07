package ru.ok.timer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button startStop;
    private Button reset;
    private byte mode = 1;
    private TextView timer;
    private TimerReceiver timerReceiver;
    private long millisecondTime;
    private long startTime;
    private long timeBuff;
    private long updateTime;
    private int seconds;
    private int minutes;
    private int milliSeconds;
    private int counter = 0;
    private boolean hidden;
    private SharedPreferences sp;
    private final String SP_NAME = "timer";
    private final String STARTTIME_KEY = "startTime";
    private final String MODE_KEY = "mode";
    private final String TIMEBUFF_KEY = "timeBuff";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        timer = findViewById(R.id.timer);
        timerReceiver = new TimerReceiver();
        initButtons();
        registerBroadcastReceiver();
        getExtra();
        hidden = false;
    }

    private void getExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            startTime = Long.parseLong(sp.getString(STARTTIME_KEY, "0"));
            mode = Byte.parseByte(sp.getString(MODE_KEY, "1"));
            timeBuff = Long.parseLong(sp.getString(TIMEBUFF_KEY, "0"));
            if (startTime != 0) {
                if (mode == -1) {
                    timer.setText(secondsToTime(timeBuff + System.currentTimeMillis() - startTime));
                    startService(new Intent(MainActivity.this, Timer.class));
                    reset.setEnabled(false);
                    startStop.setText("Stop");
                } else {
                    timer.setText(secondsToTime(timeBuff));
                }
            }
        } else {
            sp.edit().clear().apply();
        }
    }

    private void initButtons() {
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
        mode *= -1;
        sp.edit().putString(MODE_KEY, String.valueOf(mode)).apply();
        if (mode == -1) {
            start();
        } else {
            stop();

        }
    }

    private void stop() {
        reset.setEnabled(true);
        stopService(new Intent(MainActivity.this, Timer.class));
        timeBuff += millisecondTime;
        sp.edit().putString(TIMEBUFF_KEY, String.valueOf(timeBuff)).apply();
        startStop.setText("Start");
    }

    private void start() {
        reset.setEnabled(false);
        startTime = System.currentTimeMillis();
        sp.edit().putString(STARTTIME_KEY, String.valueOf(startTime)).apply();
        startService(new Intent(MainActivity.this, Timer.class));
        startStop.setText("Stop");
    }

    private void reset() {
        millisecondTime = 0L;
        startTime = 0L;
        timeBuff = 0L;
        updateTime = 0L;
        seconds = 0;
        minutes = 0;
        milliSeconds = 0;
        sp.edit().clear().apply();
        timer.setText("00:00:000");
    }


    public void registerBroadcastReceiver() {
        this.registerReceiver(timerReceiver, new IntentFilter(
                "increaseTimer"));
    }

    public void unregisterBroadcastReceiver() {
        this.unregisterReceiver(timerReceiver);
    }

    private class TimerReceiver extends BroadcastReceiver {

        public TimerReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mode == -1) {
                millisecondTime = System.currentTimeMillis() - startTime;
                updateTime = timeBuff + millisecondTime;
                timer.setText(secondsToTime(updateTime));
                counter++;
                if (counter % 100 == 0 && hidden) {
                    createNotification();
                }
            }
        }
    }


    private void createNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        String notifyText = timer.getText().toString();
        notifyText = notifyText.substring(0, notifyText.length() - 4);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(mode == -1 ? "Таймер запущен" : "Таймер на паузе")
                        .setContentText(notifyText)
                        .setContentIntent(resultPendingIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


    @SuppressLint("DefaultLocale")
    private String secondsToTime(long updateTime) {
        seconds = (int) (updateTime / 1000);
        minutes = seconds / 60;
        seconds = seconds % 60;
        milliSeconds = (int) (updateTime % 1000);
        return String.format("%02d:%02d:%03d", minutes, seconds, milliSeconds);
    }

    @Override
    protected void onPause() {
        hidden = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        hidden = false;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        getIntent().putExtra("refresh", 1);
        super.onDestroy();
    }

}
