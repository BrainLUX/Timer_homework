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
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button startStop;
    private Button reset;
    private byte mode = 1;
    private TextView timer;
    private TimerReceiver timerReceiver = new TimerReceiver();
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    int Seconds, Minutes, MilliSeconds;
    private int counter = 0;
    private boolean hidden;
    private SharedPreferences sp;
    public static final String SP_NAME = "timer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        timer = findViewById(R.id.timer);
        initButtons();
        registerBroadcastReceiver();
        getExtra();
        hidden = false;
    }

    private void getExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            StartTime = Long.parseLong(sp.getString("StartTime", "0"));
            mode = Byte.parseByte(sp.getString("mode", "1"));
            TimeBuff = Long.parseLong(sp.getString("TimeBuff", "0"));
//            if(StartTime!=0){
//                timer.setText(secondsToTime(System.currentTimeMillis() - StartTime));
//                startService(new Intent(MainActivity.this, Timer.class));
//                reset.setEnabled(false);
//                mode = -1;
//                startStop.setText("Stop");
//            }
            Log.d("", "reset: " + StartTime);
            if (StartTime != 0) {
                if (mode == -1) {
                    timer.setText(secondsToTime(TimeBuff + System.currentTimeMillis() - StartTime));
                    startService(new Intent(MainActivity.this, Timer.class));
                    reset.setEnabled(false);
                    startStop.setText("Stop");
                } else {
                    timer.setText(secondsToTime(TimeBuff));
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
        sp.edit().putString("mode", String.valueOf(mode)).apply();
        if (mode == -1) {
            start();

        } else {
            stop();

        }
    }

    private void stop() {
        reset.setEnabled(true);
        stopService(new Intent(MainActivity.this, Timer.class));
        TimeBuff += MillisecondTime;
        sp.edit().putString("TimeBuff", String.valueOf(TimeBuff)).apply();
        startStop.setText("Start");
    }

    private void start() {
        reset.setEnabled(false);
        StartTime = System.currentTimeMillis();
        sp.edit().putString("StartTime", String.valueOf(StartTime)).apply();
        startService(new Intent(MainActivity.this, Timer.class));
        startStop.setText("Stop");
    }

    private void reset() {
        MillisecondTime = 0L;
        StartTime = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;
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
                MillisecondTime = System.currentTimeMillis() - StartTime;
                UpdateTime = TimeBuff + MillisecondTime;
                timer.setText(secondsToTime(UpdateTime));
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
        Seconds = (int) (updateTime / 1000);
        Minutes = Seconds / 60;
        Seconds = Seconds % 60;
        MilliSeconds = (int) (updateTime % 1000);
        return String.format("%02d:%02d:%03d", Minutes, Seconds, MilliSeconds);
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
