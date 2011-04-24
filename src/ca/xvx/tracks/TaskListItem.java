package ca.xvx.tracks;

import ca.xvx.tracks.preferences.PreferenceConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class TaskListItem extends RelativeLayout {
	private TextView _name;
	private TextView _info;
	private CheckBox _done;
	private LinearLayout _deferBox;
	private Button _defer1;
	private Button _defer7;

	private Task _task;
	private TracksAction _doneAction;
	private Handler _changeHandler;
	private Handler _notifyHandler;

	private SharedPreferences _prefs;
	private boolean _defer;
	
	// Within one day is amber.
	private static final long AMBER_TIME = 24*3600*1000;
	// Within one week is orange.
	private static final long ORANGE_TIME = 7*24*3600*1000;

	public TaskListItem(Context c, Task t, Handler n) {
		super(c);
		Calendar now = Calendar.getInstance();
		Calendar cmp = Calendar.getInstance();
		long nowm, cmpm;

		_prefs = PreferenceManager.getDefaultSharedPreferences(c);
		_defer = _prefs.getBoolean(PreferenceConstants.DEFER, true);
		_changeHandler = TracksCommunicator.getHandler();
		_notifyHandler = n;

		if(t == null) {
			return;
		}
		_task = t;

		addView(inflate(c, R.layout.tasklist_item, null));
		_name = (TextView)findViewById(R.id.TLI_name);
		_info = (TextView)findViewById(R.id.TLI_info);
		_done = (CheckBox)findViewById(R.id.TLI_done);
		_deferBox = (LinearLayout)findViewById(R.id.TLI_defer_box);
		_defer1 = (Button)findViewById(R.id.TLI_defer_1);
		_defer7 = (Button)findViewById(R.id.TLI_defer_7);

		String name = _task.getDescription();
		if(_task.getProject() != null) {
			name += "  [" + t.getProject().getName() + "]";
		}
		_name.setText(name);
		
		String infos = "";
		if(t.getDue() != null) {
			infos += "Due: " + DateFormat.getDateFormat(c).format(t.getDue());

			// Set the text color according to due date
			cmp.setTime(t.getDue());
			cmp.set(Calendar.HOUR_OF_DAY, 0);
			cmp.set(Calendar.MINUTE, 0);
			cmp.set(Calendar.SECOND, 0);
			cmp.set(Calendar.MILLISECOND, 0);

			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MILLISECOND, 0);

			nowm = now.getTimeInMillis();
			cmpm = cmp.getTimeInMillis();
			if(cmpm - nowm < 0) {
				_info.setTextColor(getResources().getColor(R.color.red));
			} else if(cmpm - nowm <= AMBER_TIME) {
				_info.setTextColor(getResources().getColor(R.color.amber));
			} else if(cmpm - nowm <= ORANGE_TIME) {
				_info.setTextColor(getResources().getColor(R.color.orange));
			} else {
				_info.setTextColor(getResources().getColor(R.color.green));
			}
		}
		_info.setText(infos);

		_done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton b, boolean checked) {
					_task.setDone(checked);
					if(checked) {
						_doneAction = new TracksAction(TracksAction.ActionType.COMPLETE_TASK,
													   _task, _notifyHandler);
						Message m = _changeHandler.obtainMessage(0, _doneAction);
						_changeHandler.sendMessageDelayed(m, 500);
					} else {
						_changeHandler.removeMessages(0, _doneAction);
					}
				}
			});

		if(_defer) {
			_deferBox.setVisibility(View.VISIBLE);

			_defer1.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Calendar newdue = Calendar.getInstance();
						Date olddue = _task.getDue();
						if(olddue != null) {
							newdue.setTime(_task.getDue());
						}
						newdue.set(Calendar.HOUR_OF_DAY, 0);
						newdue.set(Calendar.MINUTE, 0);
						newdue.set(Calendar.SECOND, 0);
						newdue.set(Calendar.MILLISECOND, 0);
						newdue.add(Calendar.DAY_OF_YEAR, 1);

						_task.setDue(newdue.getTime());

						Message m = _changeHandler.obtainMessage(0, new TracksAction(TracksAction.ActionType.UPDATE_TASK,
																					 _task, _notifyHandler));
						_changeHandler.sendMessage(m);
					}
				});

			_defer7.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Calendar newdue = Calendar.getInstance();
						Date olddue = _task.getDue();
						if(olddue != null) {
							newdue.setTime(_task.getDue());
						}
						newdue.set(Calendar.HOUR_OF_DAY, 0);
						newdue.set(Calendar.MINUTE, 0);
						newdue.set(Calendar.SECOND, 0);
						newdue.set(Calendar.MILLISECOND, 0);
						newdue.add(Calendar.DAY_OF_YEAR, 7);

						_task.setDue(newdue.getTime());

						Message m = _changeHandler.obtainMessage(0, new TracksAction(TracksAction.ActionType.UPDATE_TASK,
																					 _task, _notifyHandler));
						_changeHandler.sendMessage(m);
					}
				});
		} else {
			_deferBox.setVisibility(View.GONE);
		}
	}

	protected Task getTask() {
		return _task;
	}
}
