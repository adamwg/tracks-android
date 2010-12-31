package ca.xvx.tracks;

import ca.xvx.tracks.util.HttpConnection;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Semaphore;
import org.apache.http.HttpResponse;
import org.xml.sax.SAXException;

public class TracksCommunicator extends HandlerThread {
	private static Handler _handler;
	private static SharedPreferences _prefs;
	private static Semaphore _ready;

	public static final int SUCCESS_CODE = 0;
	public static final int FETCH_CODE = 1;
	public static final int PARSE_CODE = 2;
	public static final int PARSE_FAIL_CODE = 3;
	public static final int FETCH_FAIL_CODE = 4;
	public static final int PREFS_FAIL_CODE = 5;
	public static final int UPDATE_FAIL_CODE = 6;

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

	public static Handler getHandler() {
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
			Message.obtain(replyTo, PREFS_FAIL_CODE).sendToTarget();
			return;
		}
		
		HttpResponse r;
		InputStream[] ret = new InputStream[3];

		Message.obtain(replyTo, FETCH_CODE).sendToTarget();

		try {
			r = HttpConnection.get(new URI("http", null, server, 80, "/contexts.xml", null, null),
								   username, password);
			ret[0] = r.getEntity().getContent();
			
			r = HttpConnection.get(new URI("http", null, server, 80, "/projects.xml", null, null),
								   username, password);
			ret[1] = r.getEntity().getContent();
			
			r = HttpConnection.get(new URI("http", null, server, 80, "/todos.xml", null, null),
								   username, password);
			ret[2] = r.getEntity().getContent();
		} catch(Exception e) {
			Log.w("TC", e);
			Message.obtain(replyTo, FETCH_FAIL_CODE).sendToTarget();
			return;
		}

		Message.obtain(replyTo, PARSE_CODE).sendToTarget();
		
		try {
			Xml.parse(ret[0], Xml.Encoding.UTF_8, new ContextXmlHandler());
			Xml.parse(ret[1], Xml.Encoding.UTF_8, new ProjectXmlHandler());
			Xml.parse(ret[2], Xml.Encoding.UTF_8, new TaskXmlHandler());
		} catch(IOException e) {
			Message.obtain(replyTo, FETCH_FAIL_CODE).sendToTarget();
			return;
		} catch(SAXException e) {
			Message.obtain(replyTo, PARSE_FAIL_CODE).sendToTarget();
			return;
		}
		
		Message.obtain(replyTo, SUCCESS_CODE).sendToTarget();
	}

	private void completeTask(TracksAction act) {
		final String server = _prefs.getString(PreferenceConstants.SERVER, null);
		final String username = _prefs.getString(PreferenceConstants.USERNAME, null);
		final String password = _prefs.getString(PreferenceConstants.PASSWORD, null);

		Task t = (Task)act.target;
		HttpResponse r;

		try {
			r = HttpConnection.put(new URI("http", server, "/todos/" +
										   String.valueOf(t.getId()) + "/toggle_check.xml", null),
								   username,
								   password,
								   null);
		} catch(Exception e) {
			return;
		}
		
		t.remove();
		act.notify.sendEmptyMessage(0);
	}

	private void updateTask(TracksAction act) {
		final String server = _prefs.getString(PreferenceConstants.SERVER, null);
		final String username = _prefs.getString(PreferenceConstants.USERNAME, null);
		final String password = _prefs.getString(PreferenceConstants.PASSWORD, null);

		Task t = (Task)act.target;

		StringBuilder xml = new StringBuilder("<todo>");
		xml.append("<description>"); xml.append(t.getDescription()); xml.append("</description>");
		xml.append("<notes>"); xml.append(t.getNotes() == null ? "" : t.getNotes()); xml.append("</notes>");
		xml.append("<context-id type=\"integer\">");
		xml.append(String.valueOf(t.getContext().getId())); xml.append("</context-id>");
		
		xml.append("<project-id type=\"integer\"");
		if(t.getProject() == null) {
			xml.append(" nil=\"true\"></project-id>");
		} else {
			xml.append(">"); xml.append(String.valueOf(t.getProject().getId())); xml.append("</project-id>");
		}
		
		xml.append("<due type=\"datetime\"");
		if(t.getDue() == null) {
			xml.append(" nil=\"true\"></due>");
		} else {
			xml.append(">"); xml.append(t.getDue()); xml.append("</due>");
		}
		
		xml.append("<show-from type=\"datetime\"");
		if(t.getShowFrom() == null) {
			xml.append(" nil=\"true\"></show-from>");
		} else {
			xml.append(">"); xml.append(t.getShowFrom()); xml.append("</show-from>");
		}

		xml.append("</todo>");

		Log.i("TC", xml.toString());
		
		try {
			HttpResponse r;
			int resp;

			if(t.getId() < 0) {
				r = HttpConnection.post(new URI("http", server, "/todos.xml", null), username, password,
										xml.toString());
			} else {
				r = HttpConnection.put(new URI("http", server,
											   "/todos/" + String.valueOf(t.getId()) + ".xml", null),
									   username, password, xml.toString());
			}

			resp = r.getStatusLine().getStatusCode();

			if(resp == 200) {
				act.notify.sendEmptyMessage(SUCCESS_CODE);
			} else if(resp == 201) {
				String got = r.getFirstHeader("Location").getValue();
				got = got.substring(got.lastIndexOf('/') + 1, got.lastIndexOf('.'));
				int tno = Integer.parseInt(got);
				t.setId(tno);
				act.notify.sendEmptyMessage(SUCCESS_CODE);
			} else {
				act.notify.sendEmptyMessage(UPDATE_FAIL_CODE);
				Log.i("TC", "Unexpected resp: " + String.valueOf(resp));
			}
		} catch(Exception e) {
			Log.w("TC", e);
			act.notify.sendEmptyMessage(UPDATE_FAIL_CODE);
		}
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

			case UPDATE_TASK:
				updateTask(act);
				break;
			}
		}
	}
}