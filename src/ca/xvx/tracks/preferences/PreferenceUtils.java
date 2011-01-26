package ca.xvx.tracks.preferences;

import android.content.SharedPreferences;

import java.net.URI;
import java.net.URISyntaxException;

public class PreferenceUtils {
	public static URI getUri(SharedPreferences prefs, String file) throws URISyntaxException {
		String server = prefs.getString(PreferenceConstants.SERVER, null);
		final boolean https = prefs.getBoolean(PreferenceConstants.HTTPS, false);
		final boolean badcert = prefs.getBoolean(PreferenceConstants.BADCERT, false);
		final int port;
		if(https) {
			port = Integer.parseInt(prefs.getString(PreferenceConstants.PORT, "443"));
		} else {
			port = Integer.parseInt(prefs.getString(PreferenceConstants.PORT, "80"));
		}
		final String protocol = https ? "https" : "http";
		final String[] spl = server.split("/", 2);
		final String path;
		if(spl.length > 1) {
			server = spl[0];
			path = "/" + spl[1] + "/";
		} else {
			path = "/";
		}

		return new URI(protocol, null, server, port, path + file, null, null);
	}
}