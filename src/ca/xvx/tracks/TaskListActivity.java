package ca.xvx.tracks;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class TaskListActivity extends ExpandableListActivity {
	private TaskListAdapter _tla;
	private SharedPreferences _prefs;

	private static final int INIT_SETTINGS = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_tla = new TaskListAdapter();
		setListAdapter(_tla);
		_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!_prefs.getBoolean(PreferenceConstants.RUN, false)) {
			_prefs.edit().putBoolean(PreferenceConstants.RUN, true).commit();
			startActivityForResult(new Intent(this, SettingsActivity.class), INIT_SETTINGS);
		} else {
			refreshList();
		}
	}

	private void refreshList() {
		String server = _prefs.getString(PreferenceConstants.SERVER, null);
		String username = _prefs.getString(PreferenceConstants.USERNAME, null);
		String password = _prefs.getString(PreferenceConstants.PASSWORD, null);

		Context context = getExpandableListView().getContext();

		if(server == null || username == null || password == null) {
			Toast.makeText(context, R.string.err_badprefs, Toast.LENGTH_LONG).show();
		}
		
		try {
			new TracksFetcher(context, _tla).execute(server, username, password);
		} catch(Exception e) { }

		registerForContextMenu(getExpandableListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)menuInfo;
		if(info.targetView instanceof TaskListItem) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.task_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
		int cid = getExpandableListView().getPackedPositionChild(info.packedPosition);
		int gid = getExpandableListView().getPackedPositionGroup(info.packedPosition);
		Task t = (Task)_tla.getChild(gid, cid);
		String desc = t.getDescription();
		Context context = getExpandableListView().getContext();
		
		switch(item.getItemId()) {
		case R.id.edit_task:
			Toast.makeText(context, "I would edit " + desc, Toast.LENGTH_LONG).show();
			return true;
		case R.id.delete_task:
			Toast.makeText(context, "I would delete " + desc, Toast.LENGTH_LONG).show();
			return true;
		case R.id.done_task:
			Toast.makeText(context, "I would complete " + desc, Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == INIT_SETTINGS) {
			refreshList();
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView l, View view, int group, int position, long id) {
		Task t = (Task)_tla.getChild(group, position);
		Toast.makeText(l.getContext(), t.getDescription(), Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.settings:
			startActivityForResult(new Intent(this, SettingsActivity.class), INIT_SETTINGS);
			return true;
		case R.id.refresh:
			refreshList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
