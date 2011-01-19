package ca.xvx.tracks;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ProjectXmlHandler extends DefaultHandler {
	private static final String TAG = "ProjectXmlHandler";
	
	private int _id;
	private String _name;
	private String _description;
	private int _position;
	private Project.ProjectState _state;
	private TodoContext _defaultContext;

	private final StringBuffer _text;

	public ProjectXmlHandler() {
		super();
		Project.clear();
		_text = new StringBuffer();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(qName.equals("project")) {
			_id = -1;
			_name = null;
			_state = Project.ProjectState.ACTIVE;
			_position = -1;
			_defaultContext = null;
		}
		_text.setLength(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("project")) {
			try {
				new Project(_id, _name, _description, _position, _state, _defaultContext);
			} catch(DuplicateProjectException e) {
				Log.w(TAG, "Tried to add the same project twice, id: " + String.valueOf(_id), e);
			}
		} else if(qName.equals("id")) {
			_id = Integer.valueOf(_text.toString());
		} else if(qName.equals("name")) {
			_name = _text.toString();
		} else if(qName.equals("description")) {
			_description = _text.toString();
		} else if(qName.equals("state")) {
			String s = _text.toString();
			if(s.equals("active")) {
				_state = Project.ProjectState.ACTIVE;
			} else if(s.equals("completed")) {
				_state = Project.ProjectState.COMPLETED;
			} else if(s.equals("hidden")) {
				_state = Project.ProjectState.HIDDEN;
			}
		} else if(qName.equals("position")) {
			_position = Integer.valueOf(_text.toString());
		} else if(qName.equals("default-context-id")) {
			try {
				_defaultContext = TodoContext.getContext(Integer.valueOf(_text.toString()));
			} catch(NumberFormatException e) {
				Log.w(TAG, "Unexpected number format: " + _text.toString(), e);
				_defaultContext = null;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		_text.append(ch, start, length);
	}
}

