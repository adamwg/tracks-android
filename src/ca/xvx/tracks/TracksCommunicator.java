package ca.xvx.tracks;

import android.content.SharedPreferences;
import android.util.Xml;
import java.io.InputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.Semaphore;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import org.xml.sax.SAXException;

public class TracksCommunicator extends HandlerThread {
	private Handler _handler;
	private SharedPreferences _prefs;
	private Semaphore _ready;

	public TracksCommunicator(SharedPreferences prefs) {
		super("Tracks Communicator");
		_prefs = prefs;
		_ready = new Semaphore(1);
		_ready.acquireUninterruptibly();
	}

	@Override
	protected void onLooperPrepared() {
		_handler = new CommHandler();
		_ready.release();
	}

	public Handler getHandler() {
		if(_handler == null) {
			_ready.acquireUninterruptibly();
			_ready.release();
		}
		
		return _handler;
	}

	private void fetchTasks(TracksAction act) {
		final String server = _prefs.getString(PreferenceConstants.SERVER, null);
		final String username = _prefs.getString(PreferenceConstants.USERNAME, null);
		final String password = _prefs.getString(PreferenceConstants.PASSWORD, null);

		Handler replyTo = act.notify;
		
		if(server == null || username == null || password == null) {
			Message.obtain(replyTo, 1, "Please Check Your Preferences").sendToTarget();
			return;
		}
		
		HttpURLConnection h;
		InputStream[] ret = new InputStream[3];

		Message.obtain(replyTo, 0, "Fetching Data").sendToTarget();

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
			Message.obtain(replyTo, 1, "Could not Connect to Server").sendToTarget();
			return;
		}

		Message.obtain(replyTo, 0, "Parsing Data").sendToTarget();
		
		try {
			Xml.parse(ret[0], Xml.Encoding.UTF_8, new ContextXmlHandler());
			Xml.parse(ret[1], Xml.Encoding.UTF_8, new ProjectXmlHandler());
			Xml.parse(ret[2], Xml.Encoding.UTF_8, new TaskXmlHandler());
		} catch(IOException e) {
			Message.obtain(replyTo, 1, "Failed to Get XML").sendToTarget();
			return;
		} catch(SAXException e) {
			Message.obtain(replyTo, 1, "Failed to Parse Data").sendToTarget();
			return;
		}
		
		Message.obtain(replyTo, 2).sendToTarget();
	}

	private void completeTask(TracksAction act) {
		final String server = _prefs.getString(PreferenceConstants.SERVER, null);
		final String username = _prefs.getString(PreferenceConstants.USERNAME, null);
		final String password = _prefs.getString(PreferenceConstants.PASSWORD, null);

		Task t = (Task)act.target;

		Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});

		try {
			HttpURLConnection h;
			h = (HttpURLConnection)(new URL("http://" + server + "/todos/" +
											String.valueOf(t.getId()) + "/toggle_check.xml").openConnection());
			h.setRequestMethod("PUT");
			h.getResponseCode();
		} catch(Exception e) {
			return;
		}
		
		t.remove();
		act.notify.sendEmptyMessage(0);
	}
	
	private class CommHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			TracksAction act = (TracksAction)msg.obj;
			
			switch(act.type) {
			case FETCH_TASKS:
				fetchTasks(act);
				break;
				
			case COMPLETE_TASK:
				completeTask(act);
				break;
			}
		}
	}
}