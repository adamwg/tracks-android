package ca.xvx.tracks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import java.io.InputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import org.xml.sax.SAXException;

public class TracksFetcher extends AsyncTask<String, Void, InputStream[]> {
	private ProgressDialog _p;
	private TaskListAdapter _target;
	private Context _context;

	public TracksFetcher(Context c, TaskListAdapter target) {
		super();

		_context = c;
		_target = target;
	}
	
	@Override
	protected void onPreExecute() {
		_p = ProgressDialog.show(_context, "", "Fetching Data", true);
	}

	@Override
	protected InputStream[] doInBackground(String... v) {
		HttpURLConnection h;
		InputStream[] ret = new InputStream[3];
		String server = v[0];
		final String username = v[1];
		final String password = v[2];
		
		Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});

		try {
			h = (HttpURLConnection)(new URL("http://" + server + "/contexts.xml").openConnection());
			ret[0] = h.getInputStream();
			h = (HttpURLConnection)(new URL("http://" + server + "/projects.xml").openConnection());
			ret[1] = h.getInputStream();
			h = (HttpURLConnection)(new URL("http://" + server + "/todos.xml").openConnection());
			ret[2] = h.getInputStream();
		} catch(Exception e) {
			Toast.makeText(_context, "Failed to Connect to Server", Toast.LENGTH_LONG).show();
			return null;
		}

		return ret;
	}

	@Override
	protected void onPostExecute(InputStream results[]) {
		if(results == null) {
			_p.dismiss();
			Toast.makeText(_context, "Failed to Fetch Data", Toast.LENGTH_LONG).show();
			return;
		}

		_p.setMessage("Parsing Data");
		
		try {
			Xml.parse(results[0], Xml.Encoding.UTF_8, new ContextXmlHandler());
			Xml.parse(results[1], Xml.Encoding.UTF_8, new ProjectXmlHandler());
			Xml.parse(results[2], Xml.Encoding.UTF_8, new TaskXmlHandler());
		} catch(IOException e) {
			Toast.makeText(_context, "Failed to Get XML", Toast.LENGTH_LONG).show();
		} catch(SAXException e) {
			Toast.makeText(_context, "Failed to Parse Data", Toast.LENGTH_LONG).show();
			Log.e("Tracks", "Parse Error", e);
		}

		_target.notifyDataSetChanged();
		_p.dismiss();
	}
}