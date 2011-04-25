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
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class ProjectEditorActivity extends Activity {
	private static final String TAG = "ProjectEditorActivity";
	
	private EditText _name;
	private EditText _description;

	private Project _project;
	private Handler _commHandler;

	public static final int SAVED = 0;
	public static final int CANCELED = 1;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.projecteditor_activity);

		Button saveButt;
		Button cancelButt;

		final java.text.DateFormat dform = DateFormat.getDateFormat(this);

		Intent intent = getIntent();
		_name = (EditText)findViewById(R.id.PEA_name);
		_description = (EditText)findViewById(R.id.PEA_description);
		saveButt = (Button)findViewById(R.id.PEA_save);
		cancelButt = (Button)findViewById(R.id.PEA_cancel);

		_commHandler = TracksCommunicator.getHandler();
		int pno = intent.getIntExtra("project", -1);
		if(pno >= 0) {
			_project = Project.getProject(pno);
			
			_name.setText(_project.getName());
			_description.setText(_project.getDescription());
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
		final String oldDescription;
		final Context context = this;

		Log.d(TAG, "Saving project!");
		
		// Must have a name
		if(_name.length() <= 0) {
			Log.w(TAG, "Attempted to save with no name");
			Toast.makeText(context, R.string.ERR_saveproject_baddata, Toast.LENGTH_LONG).show();
			return;
		}

		if(_project == null) {
			Log.d(TAG, "Creating a new project");
			_project = new Project(_name.getText().toString(), _description.getText().toString(), 0,
								   Project.ProjectState.ACTIVE, null);
			oldName = _project.getName();
			oldDescription = _project.getDescription();
		} else {
			Log.d(TAG, "Updating an existing project");
			oldName = _project.setName(_name.getText().toString());
			oldDescription = _project.setDescription(_description.getText().toString());
		}

		final ProgressDialog p = ProgressDialog.show(context, "", getString(R.string.MSG_saving), true);
		TracksAction a = new TracksAction(TracksAction.ActionType.UPDATE_PROJECT, _project, new Handler() {
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
						Toast.makeText(context, R.string.ERR_saveproject_general, Toast.LENGTH_LONG).show();
						// Reset task data to stay synced with server.
						_project.setName(oldName);
						_project.setDescription(oldDescription);
						break;
					}
				}
			});
		_commHandler.obtainMessage(0, a).sendToTarget();
	}
}
