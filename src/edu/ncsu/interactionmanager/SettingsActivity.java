package edu.ncsu.interactionmanager;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.ContactsContract;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	
	static MultiSelectListPreference selectGroupsList, selectCalendarsList;
	
	final static String GROUPS_KEY = "ImportantGroups";
	final static String CALENDARS_KEY = "SchedulingCalendars";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPreferenceFragment()).commit();
	}

	public static class MyPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.pref_general);
		}
		
		@Override
		public void onResume() {
			super.onResume();
			
			// Load the list of contact groups
			String[] projection = {
					ContactsContract.Groups._ID,
					ContactsContract.Groups.TITLE
			};
			Cursor c = getActivity().getContentResolver().query(
					ContactsContract.Groups.CONTENT_URI,
					projection,
					null, null, ContactsContract.Groups.TITLE + " ASC");
			CharSequence[] entries = new CharSequence[c.getCount()];
			CharSequence[] entryValues = new CharSequence[c.getCount()];
			int i=0;
			while (c.moveToNext()) {
				entries[i] = c.getString(c.getColumnIndex(ContactsContract.Groups.TITLE));
				entryValues[i] = c.getString(c.getColumnIndex(ContactsContract.Groups._ID));
				i++;
			}
			c.close();
			
			selectGroupsList = (MultiSelectListPreference)findPreference(GROUPS_KEY);
			if (entries.length!=0) {
				selectGroupsList.setEnabled(true);
				selectGroupsList.setSummary(R.string.select_groups);
				selectGroupsList.setEntries(entries);
				selectGroupsList.setEntryValues(entryValues);
			} else {
				selectGroupsList.setEnabled(false);
				selectGroupsList.setSummary("You have no contact groups to choose from.");
			}
			
			// Load the list of calendars
			String[] projection2 = {
					Calendars._ID,
					Calendars.NAME,
					Calendars.ACCOUNT_NAME,
			};
			c = getActivity().getContentResolver().query(
					Calendars.CONTENT_URI,
					projection2,
					null, null, null);
			entries = new CharSequence[c.getCount()];
			entryValues = new CharSequence[c.getCount()];
			i=0;
			while (c.moveToNext()) {
				entries[i] = c.getString(c.getColumnIndex(Calendars.NAME));
				if (entries[i]==null)
					entries[i] = c.getString(c.getColumnIndex(Calendars.ACCOUNT_NAME));
				entryValues[i] = c.getString(c.getColumnIndex(Calendars._ID));
				i++;
			}
			c.close();
			
			selectCalendarsList = (MultiSelectListPreference)findPreference("SchedulingCalendars");
			if (entries.length!=0) {
				selectCalendarsList.setEnabled(true);
				selectCalendarsList.setSummary(R.string.select_calendars);
				selectCalendarsList.setEntries(entries);
				selectCalendarsList.setEntryValues(entryValues);
			} else {
				selectCalendarsList.setEnabled(false);
				selectCalendarsList.setSummary("You have no calendars to choose from.");
			}
		}
	}
}
