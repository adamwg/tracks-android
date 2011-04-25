package ca.xvx.tracks;

import android.util.Log;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class ContextEditorActivity extends Activity {
	private static final String TAG = "ContextEditorActivity";
	
	private EditText _name;
	private CheckBox _hide;

	private TodoContext _context;
	private Handler _commHandler;

	public static final int SAVED = 0;
	public static final int CANCELED = 1;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.contexteditor_activity);

		Button saveButt;
		Button cancelButt;

		final java.text.DateFormat dform = DateFormat.getDateFormat(this);

		Intent intent = getIntent();
		_name = (EditText)findViewById(R.id.CEA_name);
		_hide = (CheckBox)findViewById(R.id.CEA_hide);
		saveButt = (Button)findViewById(R.id.CEA_save);
		cancelButt = (Button)findViewById(R.id.CEA_cancel);

		_commHandler = TracksCommunicator.getHandler();
		int cno = intent.getIntExtra("context", -1);
		if(cno >= 0) {
			_context = TodoContext.getContext(cno);
			
			_name.setText(_context.getName());
			_hide.setChecked(_context.isHidden());
		}

		saveButt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v(TAG, "Edit saved");
					save();
				}
			});

		cancelButt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v(TAG, "Edit cancelled");
					setResult(CANCELED);
					finish();
				}
			});
	}

	private void save() {
		final String oldName;
		final boolean oldHide;
		final Context context = this;

		Log.d(TAG, "Saving context!");
		
		// Must have a name
		if(_name.length() <= 0) {
			Log.w(TAG, "Attempted to save with no name");
			Toast.makeText(context, R.string.ERR_savecontext_baddata, Toast.LENGTH_LONG).show();
			return;
		}

		if(_context == null) {
			Log.d(TAG, "Creating a new context");
			_context = new TodoContext(_name.getText().toString(), 0, _hide.isChecked());
			oldName = _context.getName();
			oldHide = _context.isHidden();
		} else {
			Log.d(TAG, "Updating an existing context");
			oldName = _context.setName(_name.getText().toString());
			oldHide = _context.setHidden(_hide.isChecked());
		}

		final ProgressDialog p = ProgressDialog.show(context, "", getString(R.string.MSG_saving), true);
		TracksAction a = new TracksAction(TracksAction.ActionType.UPDATE_CONTEXT, _context, new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch(msg.what) {
					case TracksCommunicator.SUCCESS_CODE:
						Log.d(TAG, "Saved successfully");
						p.dismiss();
						setResult(SAVED);
						finish();
						break;
					case TracksCommunicator.UPDATE_FAIL_CODE:
						Log.w(TAG, "Save failed");
						p.dismiss();
						Toast.makeText(context, R.string.ERR_savecontext_general, Toast.LENGTH_LONG).show();
						// Reset task data to stay synced with server.
						_context.setName(oldName);
						_context.setHidden(oldHide);
						break;
					}
				}
			});
		_commHandler.obtainMessage(0, a).sendToTarget();
	}
}
