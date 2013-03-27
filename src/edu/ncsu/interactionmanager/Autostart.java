package edu.ncsu.interactionmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* Some code derived from:
 * http://stackoverflow.com/questions/7690350/android-start-service-on-boot/7690600#7690600
 */

public class Autostart extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    context.startService(new Intent(context, MyService.class));
	}

}
