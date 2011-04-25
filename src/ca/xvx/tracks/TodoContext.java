package ca.xvx.tracks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TodoContext implements Comparable<TodoContext> {
	private int _id;
	private String _name;
	private int _position;
	private boolean _hide;
	
	// Singleton list of contexts
	private static final Map<Integer, TodoContext> CONTEXTS;
	
	static {
		CONTEXTS = new HashMap<Integer, TodoContext>();
	}

	public TodoContext(String name, int position, boolean hide) {
		_id = -1;
		_name = name;
		_position = position;
		_hide = hide;
	}

	public TodoContext(int id, String name, int position, boolean hide) throws DuplicateContextException {
		this(name, position, hide);
		
		if(CONTEXTS.containsKey(id)) {
			throw new DuplicateContextException();
		}

		_id = id;
		
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
	
	public String setName(String name) {
		String on = _name;
		_name = name;
		return on;
	}
	
	public int setPosition(int pos) {
		int op = _position;
		_position = pos;
		return op;
	}

	public boolean isHidden() {
		return _hide;
	}

	public boolean setHidden(boolean hide) {
		boolean oh = _hide;
		_hide = hide;
		return oh;
	}

	@Override
	public String toString() {
		return _name;
	}

	@Override
	public int compareTo(TodoContext c) {
		return _position - c._position;
	}

	// Singleton behavior
	public static TodoContext getContext(int id) {
		return CONTEXTS.get(id);
	}

	public static Collection<TodoContext> getAllContexts() {
		return CONTEXTS.values();
	}

	protected static void clear() {
		CONTEXTS.clear();
	}
}
