package edu.ncsu.interactionmanager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Notifier extends Activity {
	
	public static void notifyNow() {
		Log.v("interactionmanager", "notification!");
	}
	
	public static void notifyLater(Context context, long timeMillis) {
		Intent notifyIntent = new Intent();
		notifyIntent.setClass(context, Notifier.class);
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent op = PendingIntent.getActivity(context, 0, notifyIntent, 0);
		am.set(AlarmManager.RTC_WAKEUP, timeMillis, op);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("interactionmanager", "notification oncreate!");
		super.onCreate(savedInstanceState);
		notifyNow();
		finish();
	}

}
