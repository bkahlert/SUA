package de.fu_berlin.imp.seqan.usability_analyzer.core.services.impl;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSession;
import de.fu_berlin.imp.seqan.usability_analyzer.core.services.IWorkSessionListener;

public class WorkSessionListenerNotifier {
	private List<IWorkSessionListener> workSessionListeners = new ArrayList<IWorkSessionListener>();

	void addWorkSessionListener(IWorkSessionListener workSessionListener) {
		workSessionListeners.add(workSessionListener);
	}

	void removeWorkSessionListener(IWorkSessionListener codeServiceListener) {
		workSessionListeners.remove(workSessionListeners);
	}

	void workSessionStarted(IWorkSession workSession) {
		for (IWorkSessionListener codeServiceListener : workSessionListeners) {
			codeServiceListener.IWorkSessionStarted(workSession);
		}
	}
}
