package edu.ncsu.interactionmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
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
							Cursor c4 = getCalendarEventsCursor(prefs);
							List<long[]> timePairs = new ArrayList<long[]>();
							while (c4.moveToNext()) {
								long[] timePair = {c4.getLong(c4.getColumnIndex(CalendarContract.Events.DTSTART)), c4.getLong(c4.getColumnIndex(CalendarContract.Events.DTEND))};
								timePairs.add(timePair);
								Log.i("Calendar event:", "ID: "+c4.getLong(c4.getColumnIndex(CalendarContract.Events._ID))+", Start: "+c4.getLong(c4.getColumnIndex(CalendarContract.Events.DTSTART)));
							}
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
	
	private Cursor getCalendarEventsCursor(SharedPreferences prefs) {
		String[] projection = {
			CalendarContract.Events._ID,
			CalendarContract.Events.CALENDAR_ID,
			CalendarContract.Events.DTSTART,
			CalendarContract.Events.DTEND,
		};
		String selection = null;
		Set<String> calendars = prefs.getStringSet(SettingsActivity.CALENDARS_KEY, new HashSet<String>());
		if (calendars.size()!=0) {
			selection = CalendarContract.Events.CALENDAR_ID + " in ";
			String temp = "(";
			for (String calendar : calendars)
				temp += calendar+", ";
			temp = temp.substring(0, temp.length()-2);
			temp += ")";
			selection += temp;
			Calendar min = Calendar.getInstance();
			min.set(Calendar.HOUR_OF_DAY, 17);
			min.set(Calendar.MINUTE, 0);
			Calendar max = Calendar.getInstance();
			max.set(Calendar.HOUR_OF_DAY, 23);
			max.set(Calendar.MINUTE, 59);
			max.set(Calendar.MILLISECOND, 999);
			selection += " AND (" + CalendarContract.Events.DTSTART  + " BETWEEN " + Math.max(System.currentTimeMillis(), min.getTimeInMillis()) + " AND " + max.getTimeInMillis();
			selection += " OR " + CalendarContract.Events.DTEND  + " BETWEEN " + Math.max(System.currentTimeMillis(), min.getTimeInMillis()) + " AND " + max.getTimeInMillis() + ")";
		}
		return context.getContentResolver().query(
				CalendarContract.Events.CONTENT_URI,
				projection,
				selection, null, null);
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
	
	public static long getNextAvailable(ArrayList<long[]> busy) {
		int itemCount = busy.size();
		
		redoIterations:
		while(true) {
			for(int i= 0; i < itemCount; i++) {
				for(int j = 0; j < itemCount; j++) {
					if(i == j) {
						continue;
					}
					
					if(busy.get(i)[0] <= busy.get(j)[0] && 
							busy.get(i)[1] >= busy.get(j)[1]) {
						// i completely contains j
						busy.remove(j);
						
						i -= 1;
						j -= 1;
						itemCount -= 1;
						
						continue redoIterations;
					} else if(busy.get(i)[0] <= busy.get(j)[0] && 
							busy.get(i)[1] >= busy.get(j)[0] &&
							busy.get(i)[1] <= busy.get(j)[1]) {
						// overlap/adjacent, starting with i and ending with j
						busy.get(i)[1] = busy.get(j)[1];
						busy.remove(j);
						
						i -= 1;
						j -= 1;
						itemCount -= 1;
						
						continue redoIterations;
						
					}
				}
			}
			break;
		}
		
		// we now have a list with no overlaps or adjacents
		
		long endTimes[] = new long[itemCount];
		int i = 0;
		for(long[] period : busy) {
			endTimes[i++] = period[1];
		}
		
		Arrays.sort(endTimes);
		return endTimes[0];
		
	}
}