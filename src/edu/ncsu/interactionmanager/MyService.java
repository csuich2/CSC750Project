package edu.ncsu.interactionmanager;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

/* Some code derived from:
 * http://stackoverflow.com/questions/7690350/android-start-service-on-boot/7690600#7690600
 */

public class MyService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyService", "service starting...");
		
		getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, new CallLogObserver(new Handler(), this));

		return super.onStartCommand(intent, flags, startId);
	}
}
