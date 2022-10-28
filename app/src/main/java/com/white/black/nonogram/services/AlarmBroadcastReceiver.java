package com.white.black.nonogram.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(BackupWorker.class).addTag("BACKUP_WORKER_TAG").build();
            WorkManager.getInstance(context).enqueue(request);
        } else {
            context.startForegroundService(serviceIntent);
        }
    }
}
