package ca.xvx.tracks;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Calendar;
import java.util.Date;

public class TaskEditorActivity extends Activity {
	private EditText _description;
	private EditText _notes;
	private Spinner _project;
	private Spinner _context;
	private Date _due;
	private Date _showfrom;
	private Button _dueButt;
	private Button _showButt;

	private Task _task;

	public static final int SAVED = 0;
	public static final int CANCELED = 1;

	private static final int SHOW_FROM = 0;
	private static final int DUE = 1;
	
	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.taskeditor_activity);

		Button saveButt;
		Button cancelButt;

		final java.text.DateFormat dform = DateFormat.getDateFormat(this);

		Intent intent = getIntent();
		_description = (EditText)findViewById(R.id.TEA_description);
		_notes = (EditText)findViewById(R.id.TEA_notes);
		_project = (Spinner)findViewById(R.id.TEA_project);
		_context = (Spinner)findViewById(R.id.TEA_context);

		_dueButt = (Button)findViewById(R.id.TEA_due_date);
		_showButt = (Button)findViewById(R.id.TEA_show_from);

		saveButt = (Button)findViewById(R.id.TEA_save);
		cancelButt = (Button)findViewById(R.id.TEA_cancel);

		int tno = intent.getIntExtra("task", -1);
		if(tno >= 0) {
			_task = Task.getTask(tno);
			
			_description.setText(_task.getDescription());
			_notes.setText(_task.getNotes());
			_due = _task.getDue();
			_showfrom = _task.getShowFrom();
			if(_due != null) {
				_dueButt.setText(dform.format(_due));
			}
			if(_showfrom != null) {
				_showButt.setText(dform.format(_showfrom));
			}
		}

		saveButt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setResult(SAVED);
					finish();
				}
			});

		cancelButt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setResult(CANCELED);
					finish();
				}
			});

		_dueButt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(DUE);
				}
			});
		
		_showButt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(SHOW_FROM);
				}
			});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Calendar initial = Calendar.getInstance();
		Date st;
		final int did = id;
		
		switch(id) {
		case SHOW_FROM:
			st = _showfrom == null ? null : _showfrom;
			break;
		case DUE:
			st = _due == null ? null : _due;
			break;
		default:
			return null;
		}

		if(st != null) {
			initial.setTime(st);
		}
			
		final java.text.DateFormat dform = DateFormat.getDateFormat(this);
		return new DatePickerDialog(this,
									new DatePickerDialog.OnDateSetListener() {
										@Override
										public void onDateSet(DatePicker v, int year, int month, int day) {
											Calendar c = Calendar.getInstance();
											c.set(year, month, day);
											if(did == SHOW_FROM) {
												_showfrom = c.getTime();
												_showButt.setText(dform.format(_showfrom));
											} else if(did == DUE) {
												_due = c.getTime();
												_dueButt.setText(dform.format(_due));
											}
										}
									},
									initial.get(Calendar.YEAR),
									initial.get(Calendar.MONTH),
									initial.get(Calendar.DAY_OF_MONTH));
	}
}
