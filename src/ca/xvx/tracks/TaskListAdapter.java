package ca.xvx.tracks;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class TaskListAdapter extends BaseExpandableListAdapter {
	private Vector<TodoContext> _contexts;
	private Map<TodoContext, Vector<Task>> _tasks;

	public TaskListAdapter() {
		super();

		_contexts = new Vector<TodoContext>();
		_tasks = new HashMap<TodoContext, Vector<Task>>();
	}

	@Override
	public void notifyDataSetChanged() {
		_contexts.clear();
		_tasks.clear();
		for(Task t : Task.getAllTasks()) {
			if(_tasks.get(t.getContext()) == null) {
				_contexts.add(t.getContext());
				_tasks.put(t.getContext(), new Vector<Task>());
			}
			_tasks.get(t.getContext()).add(t);
		}

		super.notifyDataSetChanged();
	}

	@Override
	public View getChildView(int group, int position, boolean isLastChild, View convert, ViewGroup parent) {
		TodoContext con = _contexts.get(group);
		if(con == null) {
			return null;
		}
		
		Task t = _tasks.get(con).get(position);
		if(t == null) {
			return null;
		}

		return new TaskListItem(parent.getContext(), t);
	}

	@Override
	public View getGroupView(int group, boolean isExpanded, View convert, ViewGroup parent) {
		TodoContext con = _contexts.get(group);
		if(con == null) {
			return null;
		}

		LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View ret = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
		TextView t = (TextView)ret.findViewById(android.R.id.text1);
		t.setText(con.getName());

		return ret;
	}

	@Override
	public int getChildrenCount(int group) {
		TodoContext con = _contexts.get(group);
		if(con == null) {
			return 0;
		}
		return _tasks.get(con).size();
	}

	@Override
	public int getGroupCount() {
		return _contexts.size();
	}

	@Override
	public Object getChild(int group, int pos) {
		TodoContext con = _contexts.get(group);
		return _tasks.get(con).get(pos);
	}

	@Override
	public long getChildId(int group, int pos) {
		return pos;
	}

	@Override
	public long getGroupId(int group) {
		return group;
	}

	@Override
	public Object getGroup(int group) {
		return _contexts.get(group);
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int g, int p) {
		return true;
	}
}
