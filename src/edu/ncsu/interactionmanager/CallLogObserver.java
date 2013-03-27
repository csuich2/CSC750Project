package edu.ncsu.interactionmanager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.widget.Toast;

public class CallLogObserver extends ContentObserver {
	
	Context context;
	long lastUpdate;

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
		
		// Only let us update every second
		if (lastUpdate + 1000 > System.currentTimeMillis())
			return;
		
		// Get the latest call
		Cursor c = getCallLogCursor();
		// If we found the latest call
		if (c.moveToFirst()) {
			// Populate a helper class from the cursor
			Call call = callFromCursor(c);
			// If this call is a missed call
			if (call.type == CallLog.Calls.MISSED_TYPE) {
				// Lookup the contact id for the missed call
				Cursor c2 = getPhoneLookupCursor(call);
				// If we found a contact for the missed call
				if (c2.moveToFirst()) {
					// Get the id for the contact
					long id = c2.getLong(c2.getColumnIndex(PhoneLookup._ID));
					// Lookup the group id for the caller
					Cursor c3 = getGroupIdCursor(id);
					// If we found a group id
					if (c3.moveToFirst()) {
						// Get the group id
						long groupId = c3.getLong(c3.getColumnIndex(ContactsContract.Groups._ID));
						// Get the list of important groups
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
						Set<String>set = prefs.getStringSet(SettingsActivity.GROUPS_KEY, new HashSet<String>());
						// If the caller's group 
						if (set.contains(""+groupId)) {
							Toast.makeText(context, "Last caller is in important group '"+groupId+"'", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(context, "Last caller is NOT in an important group", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(context, "Unable to find group id off last caller", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(context, "Unable to determine if last caller is in a group", Toast.LENGTH_SHORT).show();
				}
			}
			
			Log.i("Last call info", buildCallLogString(c));
		} else {
			Toast.makeText(context, "Unable to get last call log item", Toast.LENGTH_SHORT).show();
		}
		
		lastUpdate = System.currentTimeMillis();
	}
	
	private Cursor getCallLogCursor() {
		String[] fields = {
				CallLog.Calls.NUMBER,
				CallLog.Calls.CACHED_NAME,
				CallLog.Calls.TYPE,
				CallLog.Calls.NEW,
		};
		String order = CallLog.Calls.DATE + " DESC";
		
		return context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				fields,
				null,
				null,
				order);
	}
	
	private Cursor getPhoneLookupCursor(Call call) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(call.number));
		return context.getContentResolver().query(
				uri,
				new String[]{PhoneLookup._ID},
				null, null, null);
	}
	
	private Cursor getGroupIdCursor(long contactId) {
		return context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI,
				new String[]{ContactsContract.Data.CONTACT_ID , ContactsContract.Groups._ID},
				ContactsContract.Data.CONTACT_ID+"="+contactId,
				null, null);
	}
	
	private Call callFromCursor(Cursor c) {
		Call call = new Call();
		call.number = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
		call.name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));
		call.type = c.getInt(c.getColumnIndex(CallLog.Calls.TYPE));
		call.isNew = c.getString(c.getColumnIndex(CallLog.Calls.NEW));
		return call;
	}
	
	private String buildCallLogString(Cursor c) {
		return	"Number: "+c.getString(c.getColumnIndex(CallLog.Calls.NUMBER))+", "+
				"Name: "+c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME))+", "+
				"Type: "+c.getString(c.getColumnIndex(CallLog.Calls.TYPE))+", "+
				"New: "+c.getString(c.getColumnIndex(CallLog.Calls.NEW));
	}
	
	private class Call {
		String number;
		String name;
		int type;
		String isNew;
	}
}