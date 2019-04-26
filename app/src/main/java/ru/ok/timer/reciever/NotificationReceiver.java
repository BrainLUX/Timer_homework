package ru.ok.timer.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.ok.timer.timer.Timer;
import ru.ok.timer.service.TimerService;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case TimerService.STOP_ACTION:
                    Timer.stop();
                    break;
                case TimerService.RESET_ACTION:
                    Timer.stop();
                    Timer.resetTimer();
                    break;
                case TimerService.START_ACTION:
                    Timer.startTimer(null);
                    break;
            }
        }
    }
}
