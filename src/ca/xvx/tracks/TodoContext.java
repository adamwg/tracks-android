package ca.xvx.tracks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TodoContext {
	private int _id;
	private String _name;
	private int _position;
	private boolean _hide;
	
	// Singleton list of contexts
	private static final Map<Integer, TodoContext> CONTEXTS = new HashMap<Integer, TodoContext>();
	
	public TodoContext(int id, String name, int position, boolean hide) throws DuplicateContextException {
		if(CONTEXTS.containsKey(id)) {
			throw new DuplicateContextException();
		}
		
		_id = id;
		_name = name;
		_position = position;
		_hide = hide;
		
		CONTEXTS.put(id, this);
	}
	
	public int getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getPosition() {
		return _position;
	}
	
	public void setId(int id) {
		_id = id;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setPosition(int pos) {
		_position = pos;
	}

	public boolean isHidden() {
		return _hide;
	}

	public void setHidden(boolean hide) {
		_hide = hide;
	}
	
	// Singleton behavior
	public static TodoContext getContext(int id) {
		return CONTEXTS.get(id);
	}

	public static Collection<TodoContext> getAllContexts() {
		return CONTEXTS.values();
	}
}
