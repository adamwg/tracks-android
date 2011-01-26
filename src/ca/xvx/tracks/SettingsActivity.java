package ca.xvx.tracks;

import ca.xvx.tracks.preferences.PreferenceConstants;

import android.util.Log;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {
	private static final String TAG = "SettingsActivity";
	
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor ed = prefs.edit();
		if(!prefs.getBoolean(PreferenceConstants.RUN, false)) {
			prefs.edit().putBoolean(PreferenceConstants.RUN, true).commit();
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setCancelable(false)
				.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface d, int id) {
							d.cancel();
						}
					})
				.setMessage(R.string.MSG_welcome)
				.show();
		}
		
		try {
			addPreferencesFromResource(R.xml.preferences);
		} catch(ClassCastException e) {
			// Blow away prefs
			Log.i(TAG, "Clearing preferences because we couldn't load them");
			ed.clear();
			ed.putBoolean(PreferenceConstants.RUN, true);
			ed.commit();

			PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
			ed.commit();

			addPreferencesFromResource(R.xml.preferences);
		}

		CheckBoxPreference https = (CheckBoxPreference)findPreference(PreferenceConstants.HTTPS);
		final EditTextPreference port = (EditTextPreference)findPreference(PreferenceConstants.PORT);
		https.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object newValue) {
					boolean yes = (Boolean)newValue;
					if(yes) {
						if(prefs.getString(PreferenceConstants.PORT, "80").equals("80")) {
							port.setText("443");
						}
					} else {
						if(prefs.getString(PreferenceConstants.PORT, "443").equals("443")) {
							port.setText("80");
						}
					}

					return true;
				}
			});
	}
}
