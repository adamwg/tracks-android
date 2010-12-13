package ca.xvx.tracks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Project {
	public enum ProjectState { ACTIVE, HIDDEN, COMPLETED };
	
	private int _id;
	private String _name;
	private String _description;
	private int _position;
	private ProjectState _state;
	private TodoContext _defaultContext;
	
	// Singleton list of projects
	private static final Map<Integer, Project> PROJECTS = new HashMap<Integer, Project>();
	
	public Project(int id, String name, String description, int position, ProjectState state, TodoContext defaultContext) throws DuplicateProjectException {
		if(PROJECTS.containsKey(id)) {
			throw new DuplicateProjectException();
		}
		
		_id = id;
		_name = name;
		_position = position;
		_state = state;
		_defaultContext = defaultContext;
		
		PROJECTS.put(id, this);
	}
	
	public int get_id() {
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
		_id = id;
	}
	public void setName(String name) {
		_name = name;
	}
	public void setDescription(String description) {
		_description = description;
	}
	public void setPosition(int position) {
		_position = position;
	}
	public void setState(ProjectState state) {
		_state = state;
	}
	public void setDefaultContext(TodoContext defaultContext) {
		_defaultContext = defaultContext;
	}

	public static Project getProject(int id) {
		return PROJECTS.get(id);
	}

	public static Collection<Project> getAllProjects() {
		return PROJECTS.values();
	}
}
