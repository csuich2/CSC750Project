package edu.ncsu.interactionmanager;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Notifier extends Activity {

	public static void notifyNow(Context context, Call callInfo) {
		final Call call = callInfo;
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle("Missed call reminder.");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(call.time);
		builder.setContentTitle(context.getString(R.string.app_name));
		builder.setContentText("Missed call from "+call.name);
		builder.setTicker("You missed a call from "+call.name+" at "+cal.getTime().toString());
		builder.setOngoing(true);
		builder.setWhen(cal.getTimeInMillis());
		builder.setSmallIcon(android.R.drawable.stat_notify_missed_call);

		String uri = "tel:"+call.number;
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse(uri));
		builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

		Uri soundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
		if (soundUri!=null) {
			builder.setDefaults(Notification.DEFAULT_ALL);
		} else {
			builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
		}

		NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(call.number, (int)call.time, builder.build());
	}

	public static void notifyLater(Context context, long timeMillis, Call callInfo) {
		Intent notifyIntent = new Intent();
		notifyIntent.setClass(context, Notifier.class);
		notifyIntent.putExtra("time", callInfo.time);
		notifyIntent.putExtra("number", callInfo.number);
		notifyIntent.putExtra("name", callInfo.name);
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent op = PendingIntent.getActivity(context, 0, notifyIntent, 0);
		Log.i("Notifier", "current time:          "+System.currentTimeMillis());
		Log.i("Notifier", "wakeup time:           "+timeMillis);
		Calendar utcWakeup = Calendar.getInstance();
		utcWakeup.setTimeInMillis(timeMillis);
		utcWakeup.setTimeZone(TimeZone.getTimeZone("UTC"));
		Log.i("Notifier", "wakeup time adjusted:  "+utcWakeup.getTimeInMillis());
		am.set(AlarmManager.RTC_WAKEUP, utcWakeup.getTimeInMillis(), op);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Notifer", "in onCreate");
		Call call = new Call();
		call.time = getIntent().getExtras().getLong("time");
		call.number = getIntent().getExtras().getString("number");
		call.name = getIntent().getExtras().getString("name");
		notifyNow(this, call);
		finish();
	}
}
