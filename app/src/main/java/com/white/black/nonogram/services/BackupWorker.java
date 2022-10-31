package com.white.black.nonogram.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.white.black.nonogram.R;
import com.white.black.nonogram.activities.MenuActivity;

public class BackupWorker extends Worker {

    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        nextAlarm();
        displayNotification();

        return Result.success();
    }

    private void displayNotification() {
        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel("notif",
                "notif", NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);

        // create and display a notification
        Intent intent_main = new Intent(getApplicationContext(), MenuActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(getApplicationContext(), 0, intent_main, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "notif")
                .setContentTitle(getApplicationContext().getString(R.string.new_puzzles))
                .setContentText(getApplicationContext().getString(R.string.click_to_play))
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.icon_white_512)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.puzzle_green_512))
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOngoing(false)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    private void nextAlarm() {
        Intent _intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, _intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        final long TIME_TO_WAIT_MILLISECONDS = 1000 * 3600 * 6; // 6 hours
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TIME_TO_WAIT_MILLISECONDS, pendingIntent);
    }
}