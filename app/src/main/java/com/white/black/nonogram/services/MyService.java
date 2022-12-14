package com.white.black.nonogram.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.white.black.nonogram.R;
import com.white.black.nonogram.activities.MenuActivity;

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nextAlarm();

        String id = "default_channel_id";
        String title = getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(id);
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(id, title, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // create and display a notification
        Intent intent_main = new Intent(this, MenuActivity.class);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(this, 0, intent_main, PendingIntent.FLAG_IMMUTABLE);
        Notification notificationPopup = new NotificationCompat.Builder(this, id)
                .setContentTitle(getString(R.string.new_puzzles))
                .setContentText(getString(R.string.click_to_play))
                .setContentIntent(pendingIntentMain)
                .setSmallIcon(R.drawable.icon_white_512)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.puzzle_green_512))
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOngoing(false)
                .setAutoCancel(true)
                .build();


        notificationManager.notify(334455 /* made up number */, notificationPopup);

        startForeground(334455 /* made up number */, notificationPopup);
        stopForeground(false);

        return super.onStartCommand(intent, flags, startId);
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
