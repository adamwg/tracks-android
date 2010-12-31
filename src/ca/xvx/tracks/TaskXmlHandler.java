package ca.xvx.tracks;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TaskXmlHandler extends DefaultHandler {
	private static final String TAG = "TaskXmlHandler";
	
	private int _id;
	private String _description;
	private String _notes;
	private TodoContext _context;
	private Project _project;
	private Date _due;
	private Date _showFrom;

	private final StringBuffer _text;
	private static final DateFormat DATEFORM = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	public TaskXmlHandler() {
		super();
		_text = new StringBuffer();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(qName.equals("todo")) {
			_id = -1;
			_description = null;
			_notes = null;
			_context = null;
			_project = null;
			_due = null;
			_showFrom = null;
		}
		_text.setLength(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("todo")) {
			try {
				new Task(_id, _description, _notes, _context, _project, _due, _showFrom);
			} catch(DuplicateTaskException e) {
				Log.w(TAG, "Tried to add the same task twice, id: " + String.valueOf(_id), e);
			}
		} else if(qName.equals("id")) {
			_id = Integer.valueOf(_text.toString());
		} else if(qName.equals("description")) {
			_description = _text.toString();
		} else if(qName.equals("notes")) {
			_notes = _text.toString();
		} else if(qName.equals("context-id")) {
			_context = TodoContext.getContext(Integer.parseInt(_text.toString()));
		} else if(qName.equals("project-id")) {
			if(_text.length() > 0) {
				_project = Project.getProject(Integer.parseInt(_text.toString()));
			}
		} else if(qName.equals("due")) {
			try {
				_due = DATEFORM.parse(_text.toString());
			} catch(ParseException e) {
				Log.w(TAG, "Unexpected date format: " + _text.toString(), e);
			}
		} else if(qName.equals("show-from")) {
			try {
				_showFrom = DATEFORM.parse(_text.toString());
			} catch(ParseException e) {
				Log.w(TAG, "Unexpected date format: " + _text.toString(), e);
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		_text.append(ch, start, length);
	}
}

