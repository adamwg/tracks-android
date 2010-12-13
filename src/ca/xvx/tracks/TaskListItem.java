package ca.xvx.tracks;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.CheckBox;

public class TaskListItem extends RelativeLayout {
	private TextView _name;
	private TextView _info;
	private CheckBox _done;

	private Task _task;

	public TaskListItem(Context c, Task t) {
		super(c);

		_task = t;

		addView(inflate(c, R.layout.tasklist_item, null));
		_name = (TextView)findViewById(R.id.task_name);
		_info = (TextView)findViewById(R.id.task_info);
		_done = (CheckBox)findViewById(R.id.task_done);

		_name.setText(_task.getDescription());
		String infos = "Context: " + t.getContext().getName();
		if(t.getProject() != null) {
			infos += ", Project: " + t.getProject().getName();
		}
		_info.setText(infos);
	}
}
