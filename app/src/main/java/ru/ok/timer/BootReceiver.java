package ru.ok.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equalsIgnoreCase(BOOT_ACTION)) {
            Intent activivtyIntent = new Intent(context, MainActivity.class);
            activivtyIntent.putExtra("reboot", 1);
            activivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activivtyIntent);
        }
    }
}
