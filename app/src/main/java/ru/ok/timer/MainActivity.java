package ru.ok.timer;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import ru.ok.timer.service.TimerService;
import ru.ok.timer.timer.Timer;

public class MainActivity extends AppCompatActivity {

    private Button startStop;
    private Button reset;
    private EditText timer;
    private boolean activityIsPaused;
    public static SharedPreferences sp;
    public static final String SP_NAME = "timer";
    public static final String STARTTIME_KEY = "startTime";
    public static final String MODE_KEY = "mode";
    public static final String TIMEBUFF_KEY = "timeBuff";
    public static final String INPUTTIME_KEY = "inputTime";
    private NotificationManager notificationManager;
    private boolean handlerStarted;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        sp = getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        initUI();
        initRunnable();
        getExtra();
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void initRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                handlerStarted = true;
                if (Timer.getMode() == Timer.TimerMode.STARTED) {
                    Timer.update();
                    if (!activityIsPaused) {
                        timer.setText(secondsToTime(Timer.getTime(), false));
                    }
                    if (Timer.isEnded()) {
                        stop();
                        reset();
                        stopService(new Intent(MainActivity.this, TimerService.class));
                        notificationManager.cancelAll();
                        timer.setText(secondsToTime(Timer.getInputTime(), false));
                    }
                }
                handler.postDelayed(this, 16);
            }
        };
    }


    private void getExtra() {
        Bundle extras = getIntent().getExtras();
        if (extras != null || sp.getAll().size() != 0) {
            if (Timer.getMode() == Timer.TimerMode.STOPPED) {
                Timer.initTimer(sp);
            }
            if (!Timer.isEnded()) {
                timer.setEnabled(false);
                if (Timer.isStarted()) {
                    Timer.update();
                    timer.setText(secondsToTime(Timer.getTime(), false));
                    if (!handlerStarted)
                        startHandler();
                    reset.setEnabled(false);
                    startStop.setText(getResources().getText(R.string.stop));
                } else if (Timer.getMode() == Timer.TimerMode.PAUSED) {
                    Timer.startTimer(null);
                    Timer.update();
                    Timer.stop();
                    timer.setText(secondsToTime(Timer.getTime(), false));
                } else {
                    reset();
                }
            }
            if (Timer.isEnded()) {
                reset();
            }
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
        if (Timer.isStarted()) {
            stop();
        } else {
            start();
        }
    }

    private void stop() {
        Timer.stop();
        reset.setEnabled(true);
        startStop.setText(getResources().getText(R.string.start));
        timer.setEnabled(false);
    }

    private void start() {
        if (Timer.getMode() == Timer.TimerMode.STOPPED) {
            Timer.startTimer(timer.getText().toString());
        } else if (Timer.getMode() == Timer.TimerMode.PAUSED) {
            Timer.startTimer(secondsToTime(Timer.getInputTime(), false));
        }
        reset.setEnabled(false);
        startStop.setText(getResources().getText(R.string.stop));
        timer.setEnabled(false);
        if (!handlerStarted)
            startHandler();
    }

    private void startHandler() {
        handler.post(runnable);
    }

    private void reset() {
        handler.removeCallbacks(runnable);
        handlerStarted = false;
        Timer.resetTimer();
        timer.setText(secondsToTime(Timer.getInputTime(), false));
        timer.setEnabled(true);
    }

    @SuppressLint("DefaultLocale")
    public static String secondsToTime(long updateTime, boolean forNotify) {
        int seconds = (int) (updateTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        int milliSeconds = (int) (updateTime % 1000);
        if (!forNotify)
            return String.format("%02d:%02d:%03d", minutes, seconds, milliSeconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static long timeToSeconds(String time) {
        String[] buff = time.split(":");
        return Long.parseLong(buff[0]) * 60000 + Long.parseLong(buff[1]) * 1000 + Long.parseLong(buff[2]);
    }

    @Override
    protected void onPause() {
        activityIsPaused = true;
        if (Timer.getMode() != Timer.TimerMode.STOPPED) {
            startService(new Intent(MainActivity.this, TimerService.class));
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        activityIsPaused = false;
        stopService(new Intent(MainActivity.this, TimerService.class));
        notificationManager.cancelAll();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        stopService(new Intent(MainActivity.this, TimerService.class));
        notificationManager.cancelAll();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        timer.setText(secondsToTime(Timer.getTime(), false));
        if (Timer.getMode() == Timer.TimerMode.STOPPED) {
            stop();
            reset();
        } else if (Timer.getMode() == Timer.TimerMode.PAUSED) {
            startStop.setText(getResources().getText(R.string.start));
            reset.setEnabled(true);
        } else if (Timer.getMode() == Timer.TimerMode.STARTED) {
            startStop.setText(getResources().getText(R.string.stop));
            reset.setEnabled(false);
            timer.setEnabled(false);
        }
        super.onNewIntent(intent);
    }
}
