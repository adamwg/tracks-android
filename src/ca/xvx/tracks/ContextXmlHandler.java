package ca.xvx.tracks;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ContextXmlHandler extends DefaultHandler {
	private static final String TAG = "ContextXmlHandler";
	
	private int _id;
	private String _name;
	private boolean _hide;
	private int _position;

	private final StringBuffer _text;

	public ContextXmlHandler() {
		super();
		TodoContext.clear();
		_text = new StringBuffer();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(qName.equals("context")) {
			_id = -1;
			_name = null;
			_hide = false;
			_position = -1;
		}
		_text.setLength(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("context")) {
			try {
				new TodoContext(_id, _name, _position, _hide);
			} catch(DuplicateContextException e) {
				Log.w(TAG, "Tried to add the same context twice, id: " + String.valueOf(_id), e);
			}
		} else if(qName.equals("id")) {
			_id = Integer.valueOf(_text.toString());
		} else if(qName.equals("name")) {
			_name = _text.toString();
		} else if(qName.equals("hide")) {
			_hide = _text.toString().equals("hide") ? true : false;
		} else if(qName.equals("position")) {
			_position = Integer.valueOf(_text.toString());
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		_text.append(ch, start, length);
	}
}

