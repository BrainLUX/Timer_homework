package ru.ok.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent broadIntent = new Intent("notify");
        broadIntent.putExtra("action", intent.getAction());
        context.sendBroadcast(broadIntent);
    }
}
