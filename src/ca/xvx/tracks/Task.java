package ca.xvx.tracks;

import android.util.Log;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task {
	private int _id;
	private String _description;
	private String _notes;
	private TodoContext _context;
	private Project _project;
	private Date _due;
	private Date _showFrom;
	private boolean _done;
	
	private static final Map<Integer, Task> TASKS = new HashMap<Integer, Task>();
	
	public Task(int id, String desc, String notes, TodoContext context, Project project, Date due, Date showFrom) throws DuplicateTaskException {
		if(TASKS.containsKey(id)) {
			throw new DuplicateTaskException();
		}
		
		_id = id;
		_description = desc;
		_notes = notes;
		_context = context;
		_project = project;
		_due = due;
		_showFrom = showFrom;

		TASKS.put(id, this);
	}
	
	public int getId() {
		return _id;
	}

	public String getDescription() {
		return _description;
	}

	public String getNotes() {
		return _notes;
	}

	public TodoContext getContext() {
		return _context;
	}

	public Project getProject() {
		return _project;
	}

	public Date getDue() {
		return _due;
	}

	public Date getShowFrom() {
		return _showFrom;
	}

	public boolean getDone() {
		return _done;
	}

	public void setId(int id) {
		_id = id;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void setNotes(String notes) {
		_notes = notes;
	}

	public void setContext(TodoContext context) {
		_context = context;
	}

	public void setProject(Project project) {
		_project = project;
	}

	public void setDue(Date due) {
		_due = due;
	}

	public void setShowFrom(Date showFrom) {
		_showFrom = showFrom;
	}

	public void setDone(boolean done) {
		_done = done;
	}

	
	
	public static Task getTask(int id) {
		return TASKS.get(id);
	}
	
	public static int getTaskCount() {
		return TASKS.size();
	}

	public static Collection<Task> getAllTasks() {
		Log.i("Task", "There are " + TASKS.size() + " tasks");
		return TASKS.values();
	}
}
