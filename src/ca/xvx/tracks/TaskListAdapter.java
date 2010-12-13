package ca.xvx.tracks;

import java.util.Vector;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TaskListAdapter extends BaseAdapter {
	private Vector<Task> _tasks;

	public TaskListAdapter() {
		super();
		
		_tasks = new Vector<Task>();
	}

	@Override
	public void notifyDataSetChanged() {
		_tasks.clear();
		for(Task t : Task.getAllTasks()) {
			_tasks.add(t);
		}

		super.notifyDataSetChanged();
	}
	
	public View getView(int position, View convert, ViewGroup parent) {
		Task t = _tasks.get(position);

		if(t == null) {
			return null;
		}

		return new TaskListItem(parent.getContext(), t);
	}

	public int getCount() {
		return _tasks.size();
	}

	public Object getItem(int pos) {
		return _tasks.get(pos);
	}

	public long getItemId(int pos) {
		return pos;
	}
}
