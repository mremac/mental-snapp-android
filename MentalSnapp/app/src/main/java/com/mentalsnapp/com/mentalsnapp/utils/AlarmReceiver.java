package com.mentalsnapp.com.mentalsnapp.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import com.mentalsnapp.com.mentalsnapp.R;
import com.mentalsnapp.com.mentalsnapp.activities.AlertDialogActivity;
import com.mentalsnapp.com.mentalsnapp.activities.BaseActivity;
import com.mentalsnapp.com.mentalsnapp.activities.MainActivity;
import com.mentalsnapp.com.mentalsnapp.activities.SplashActivity;

import java.util.Random;

/**
 * Created by gchandra on 7/2/17.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static int schedule_recording = 150;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (SharedPref.isLogin(context)) {
            if (BaseActivity.getsActivityCount() != 0) {
                Intent alarmIntent = new Intent(context, AlertDialogActivity.class);
                alarmIntent.putExtra(Constants.CATEGORY_NAME, intent.getStringExtra(Constants.CATEGORY_NAME));
                alarmIntent.putExtra(Constants.SUB_CATEGORY_ID, intent.getLongExtra(Constants.SUB_CATEGORY_ID, 0));
                alarmIntent.putExtra(Constants.SCHEDULE_DESCRIPTION, intent.getStringExtra(Constants.SCHEDULE_DESCRIPTION));
                alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(alarmIntent);
            } else {
                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent1 = new Intent(context, SplashActivity.class);
                intent1.putExtra(Constants.SCHEDULE_RECORDING, schedule_recording);
                intent1.putExtra(Constants.CATEGORY_NAME, intent.getStringExtra(Constants.CATEGORY_NAME));
                intent1.putExtra(Constants.SUB_CATEGORY_ID, intent.getLongExtra(Constants.SUB_CATEGORY_ID, 0));
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(intent1);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
                if (Build.VERSION.SDK_INT >= 21) {
                    notificationBuilder.setSmallIcon(R.drawable.notification);
                } else {
                    notificationBuilder.setSmallIcon(R.mipmap.app_icon);
                }
                notificationBuilder.setContentTitle(context.getResources().getString(R.string.app_name));
                notificationBuilder.setContentText(context.getResources().getString(R.string.schedule_message) +
                        intent.getStringExtra(Constants.SCHEDULE_DESCRIPTION));
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setContentIntent(pendingIntent);
                notificationBuilder.setShowWhen(true);
                notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
                notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
                Random r = new Random();
                int i1 = r.nextInt(1000 - 1) + 1;
                mNotificationManager.notify(i1, notificationBuilder.build());
            }
        }
    }
}