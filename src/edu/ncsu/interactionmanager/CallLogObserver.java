package edu.ncsu.interactionmanager;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

public class CallLogObserver extends ContentObserver {
	
	Context context;

	public CallLogObserver(Handler handler, Context context) {
		super(handler);
		
		this.context = context;
	}
	
	/* Some code derived from:
	 * http://www.dcpagesapps.com/developer-resources/android/25-android-tutorial-call-logs
	 */

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		
		String[] fields = {
				CallLog.Calls.NUMBER,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls.CACHED_NUMBER_TYPE,
				CallLog.Calls.TYPE,
				CallLog.Calls.NEW
		};
		String order = CallLog.Calls.DATE + " DESC";
		
		Cursor c = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				fields,
				null,
				null,
				order);
		
		c.moveToFirst();
		
		Log.i("Last call info", buildCallLogString(c));
	}
	
	private String buildCallLogString(Cursor c) {
		return	"Number: "+c.getString(c.getColumnIndex(CallLog.Calls.NUMBER))+", "+
				"Name: "+c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME))+", "+
				"Cached Type: "+c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE))+", "+
				"Type: "+c.getString(c.getColumnIndex(CallLog.Calls.TYPE))+", "+
				"New: "+c.getString(c.getColumnIndex(CallLog.Calls.NEW));
	}
}