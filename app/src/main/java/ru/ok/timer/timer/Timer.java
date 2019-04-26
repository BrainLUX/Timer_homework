package ru.ok.timer.timer;

import android.content.SharedPreferences;


import ru.ok.timer.MainActivity;
import ru.ok.timer.reciever.BootReceiver;

public class Timer {
    private static long millisecondTime;
    private static long startTime;
    private static long timeBuff;
    private static long updateTime;
    private static long inputTime;
    private static TimerMode mode = Timer.TimerMode.STOPPED;

    public static void stop() {
        timeBuff += millisecondTime;
        mode = TimerMode.PAUSED;
        MainActivity.sp.edit().putString(MainActivity.TIMEBUFF_KEY, String.valueOf(Timer.getTimeBuff())).apply();
        MainActivity.sp.edit().putString(MainActivity.MODE_KEY, String.valueOf(Timer.getMode())).apply();
    }

    private static long getTimeBuff() {
        return timeBuff;
    }

    public static void startTimer(String inputTime) {
        if (inputTime != null) {
            Timer.inputTime = MainActivity.timeToSeconds(inputTime);
        }
        startTime = System.currentTimeMillis();
        mode = TimerMode.STARTED;
        MainActivity.sp.edit().putString(MainActivity.STARTTIME_KEY, String.valueOf(Timer.getStartTime())).apply();
        MainActivity.sp.edit().putString(MainActivity.INPUTTIME_KEY, String.valueOf(Timer.getInputTime())).apply();
        MainActivity.sp.edit().putString(MainActivity.MODE_KEY, String.valueOf(Timer.getMode())).apply();
    }

    public static long getInputTime() {
        return inputTime;
    }

    private static long getStartTime() {
        return startTime;
    }

    public static void update() {
        millisecondTime = System.currentTimeMillis() - startTime;
        updateTime = timeBuff + millisecondTime;
    }

    public static long getTime() {
        return inputTime - updateTime;
    }

    public static boolean isEnded() {
        return inputTime - updateTime <= 0;
    }

    public static void resetTimer() {
        millisecondTime = 0L;
        startTime = 0L;
        timeBuff = 0L;
        updateTime = 0L;
        mode = TimerMode.STOPPED;
        MainActivity.sp.edit().clear().apply();
        MainActivity.sp.edit().putString(MainActivity.MODE_KEY, String.valueOf(Timer.getMode())).apply();
        MainActivity.sp.edit().putString(MainActivity.INPUTTIME_KEY, String.valueOf(Timer.getInputTime())).apply();
    }

    public static void initTimer(SharedPreferences sp) {
        startTime = Long.parseLong(sp.getString(MainActivity.STARTTIME_KEY, BootReceiver.ZERO));
        mode = TimerMode.valueOf(sp.getString(MainActivity.MODE_KEY, String.valueOf(TimerMode.STOPPED)));
        timeBuff = Long.parseLong(sp.getString(MainActivity.TIMEBUFF_KEY, BootReceiver.ZERO));
        inputTime = Long.parseLong(sp.getString(MainActivity.INPUTTIME_KEY, BootReceiver.ZERO));
    }

    public static boolean isStarted() {
        return getMode() == TimerMode.STARTED;
    }

    public enum TimerMode {
        STARTED,
        PAUSED,
        STOPPED
    }

    public static TimerMode getMode() {
        return mode;
    }
}
