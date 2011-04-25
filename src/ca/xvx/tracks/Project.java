package ca.xvx.tracks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Project {
	public enum ProjectState { ACTIVE, HIDDEN, COMPLETED };
	
	private int _id;
	private String _name;
	private String _description;
	private int _position;
	private ProjectState _state;
	private TodoContext _defaultContext;
	
	// Singleton list of projects
	private static final Map<Integer, Project> PROJECTS;
	static {
		PROJECTS = new HashMap<Integer, Project>();
	}

	private Project() {
		_id = -1;
		_name = "<none>";
		_state = ProjectState.ACTIVE;
	}

	public Project(String name, String description, int position, ProjectState state, TodoContext defaultContext) {
		_id = -1;
		_name = name;
		_position = position;
		_state = state;
		_defaultContext = defaultContext;
	}
	
	public Project(int id, String name, String description, int position, ProjectState state, TodoContext defaultContext) throws DuplicateProjectException {
		this(name, description, position, state, defaultContext);
		
		if(PROJECTS.containsKey(id)) {
			throw new DuplicateProjectException();
		}
		
		_id = id;

		PROJECTS.put(id, this);
	}
	
	public int getId() {
		return _id;
	}
	public String getName() {
		return _name;
	}
	public String getDescription() {
		return _description;
	}
	public int getPosition() {
		return _position;
	}
	public ProjectState getState() {
		return _state;
	}
	public TodoContext getDefaultContext() {
		return _defaultContext;
	}
	public void setId(int id) {
		int oid = _id;
		
		_id = id;

		if(oid < 0) {
			if(!PROJECTS.containsKey(id)) {
				PROJECTS.put(id, this);
			}
		}
	}
	public String setName(String name) {
		String on = _name;
		_name = name;
		return on;
	}
	public String setDescription(String description) {
		String od = _description;
		_description = description;
		return od;
	}
	public int setPosition(int position) {
		int op = _position;
		_position = position;
		return op;
	}
	public ProjectState setState(ProjectState state) {
		ProjectState ops = _state;
		_state = state;
		return ops;
	}
	public TodoContext setDefaultContext(TodoContext defaultContext) {
		TodoContext old = _defaultContext;
		_defaultContext = defaultContext;
		return old;
	}

	@Override
	public String toString() {
		return _name;
	}

	public static Project getProject(int id) {
		return PROJECTS.get(id);
	}

	public static Collection<Project> getAllProjects() {
		return PROJECTS.values();
	}

	public static Collection<Project> getActiveProjects() {
		Collection<Project> ret = PROJECTS.values();
		Collection<Project> rem = new Vector<Project>();

		for(Project p : ret) {
			if(p.getState() != ProjectState.ACTIVE) {
				rem.add(p);
			}
		}

		ret.removeAll(rem);

		return ret;
	}

	protected static void clear() {
		PROJECTS.clear();
		PROJECTS.put(-1, new Project());
	}
}
