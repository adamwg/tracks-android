package ca.xvx.tracks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		try {
			addPreferencesFromResource(R.xml.preferences);
		} catch(ClassCastException e) {
			// Blow away prefs
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor ed = prefs.edit();
			ed.clear();
			ed.commit();

			PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
			ed = prefs.edit();
			ed.commit();

			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
