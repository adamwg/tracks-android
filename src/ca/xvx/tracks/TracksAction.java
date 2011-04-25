package ca.xvx.tracks;

import android.os.Handler;

public class TracksAction {
	public enum ActionType { FETCH_TASKS, COMPLETE_TASK, UPDATE_TASK, DELETE_TASK, UPDATE_CONTEXT, UPDATE_PROJECT };

	ActionType type;
	Object target;
	Handler notify;

	public TracksAction(ActionType t, Object o, Handler n) {
		type = t;
		target = o;
		notify = n;
	}
}
