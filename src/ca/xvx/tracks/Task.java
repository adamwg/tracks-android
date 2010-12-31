package ca.xvx.tracks;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task implements Comparable<Task> {
	private int _id;
	private String _description;
	private String _notes;
	private TodoContext _context;
	private Project _project;
	private Date _due;
	private Date _showFrom;
	private boolean _done;

	private static final Map<Integer, Task> TASKS = new HashMap<Integer, Task>();
	
	public Task(String desc, String notes, TodoContext context, Project project, Date due, Date showFrom) {
		_id = -1;
		_description = desc;
		_notes = notes;
		_context = context;
		_project = project;
		_due = due;
		_showFrom = showFrom;
	}

	public Task(int id, String desc, String notes, TodoContext context, Project project, Date due, Date showFrom) throws DuplicateTaskException {
		this(desc, notes, context, project, due, showFrom);

		if(TASKS.containsKey(id)) {
			throw new DuplicateTaskException();
		}
		
		_id = id;
		
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

	public int setId(int id) {
		int tmp = _id;
		_id = id;
		if(tmp < 0) {
			TASKS.put(id, this);
		}
		return tmp;
	}

	public String setDescription(String description) {
		String tmp = _description;
		_description = description;
		return tmp;
	}

	public String setNotes(String notes) {
		String tmp = _notes;
		_notes = notes;
		return tmp;
	}

	public TodoContext setContext(TodoContext context) {
		TodoContext tmp = _context;
		_context = context;
		return tmp;
	}

	public Project setProject(Project project) {
		Project tmp = _project;
		_project = project;
		return tmp;
	}

	public Date setDue(Date due) {
		Date tmp = _due;
		_due = due;
		return tmp;
	}

	public Date setShowFrom(Date showFrom) {
		Date tmp = _showFrom;
		_showFrom = showFrom;
		return tmp;
	}

	public boolean setDone(boolean done) {
		boolean tmp = _done;
		_done = done;
		return tmp;
	}

	public void remove() {
		TASKS.remove(_id);
	}
	
	public static Task getTask(int id) {
		return TASKS.get(id);
	}
	
	public static int getTaskCount() {
		return TASKS.size();
	}

	@Override
	public int compareTo(Task t) {
		if(_due != null) {
			if(t._due != null) {
				return _due.compareTo(t._due);
			} else {
				return -1;
			}
		} else {
			if(t._due != null) {
				return 1;
			} else {
				return t._id - _id;
			}
		}
	}

	public static Collection<Task> getAllTasks() {
		return TASKS.values();
	}
}
