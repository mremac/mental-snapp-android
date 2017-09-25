package com.SMobiLogger;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public class DBUpdater extends Service {

	public int REQUESTCODE_SERVICE = 1;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		// TODO Need to check why onStart() is getting invoked multiple times

		// To maintain in SharedPref that Service has been started
		Editor editor = DatabaseHelper.getPreferences(this).edit();
		editor.putBoolean(DatabaseHelper.IS_SERVICE_STARTED, true).commit();

		SLogManager sLogManager = new SLogManager(this);
		sLogManager.refreshLogs();

		Intent serviceIntent = new Intent(this, DBUpdater.class);
		PendingIntent servicePIntent = PendingIntent.getService(this,
				REQUESTCODE_SERVICE, serviceIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		// To calculate next update time i.e., mid-night of next day
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.roll(Calendar.DATE, 1);
		Date now = calendar.getTime();
		// Log.i("Now", now.toString());
		Date nextUpdateDate = new Date(now.getYear(), now.getMonth(),
				now.getDate());
		// Log.i("nextUpdateDate", nextUpdateDate.toString());

		alarm.set(AlarmManager.RTC_WAKEUP, nextUpdateDate.getTime(),
				servicePIntent);
	}

	@Override
	public void onDestroy() {
		// To maintain in SharedPref that Service has been stopped
		Editor editor = DatabaseHelper.getPreferences(this).edit();
		editor.putBoolean(DatabaseHelper.IS_SERVICE_STARTED, false).commit();
		super.onDestroy();
	}
}