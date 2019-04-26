package ru.ok.timer.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import ru.ok.timer.MainActivity;
import ru.ok.timer.timer.Timer;
import ru.ok.timer.service.TimerService;

public class BootReceiver extends BroadcastReceiver {

    public static final String ZERO = "0";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        Timer.TimerMode mode = Timer.TimerMode.valueOf(sp.getString(MainActivity.MODE_KEY, String.valueOf(Timer.TimerMode.STOPPED)));
        if (mode != Timer.TimerMode.STOPPED) {
            String action = intent.getAction();
            String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
            if (action != null && action.equalsIgnoreCase(BOOT_ACTION)) {
                context.startService(new Intent(context, TimerService.class));
            }
        }
    }
}
