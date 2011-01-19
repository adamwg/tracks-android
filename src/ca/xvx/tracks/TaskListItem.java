package ca.xvx.tracks;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

public class TaskListItem extends RelativeLayout {
	private TextView _name;
	private TextView _info;
	private CheckBox _done;

	private Task _task;
	private TracksAction _doneAction;
	private Handler _changeHandler;
	private Handler _notifyHandler;

	// Within one day is amber.
	private static final long AMBER_TIME = 24*3600*1000;
	// Within one week is orange.
	private static final long ORANGE_TIME = 7*24*3600*1000;

	public TaskListItem(Context c, Task t, Handler n) {
		super(c);
		Calendar now = Calendar.getInstance();
		Calendar cmp = Calendar.getInstance();
		long nowm, cmpm;
		
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
	}

	protected Task getTask() {
		return _task;
	}
}
