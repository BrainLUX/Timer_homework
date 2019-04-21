package ru.ok.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);
        String mode = sp.getString(MainActivity.MODE_KEY, "0");
        if (mode != null && !mode.equals("0")) {
            String action = intent.getAction();
            String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
            if (action != null && action.equalsIgnoreCase(BOOT_ACTION)) {
                Intent activivtyIntent = new Intent(context, MainActivity.class);
                activivtyIntent.putExtra("reboot", 1);
                activivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activivtyIntent);
            }
        }
    }
}
