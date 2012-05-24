package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;

public class CodeServiceListenerNotifier {
	private List<ICodeServiceListener> iCodeServiceListeners = new ArrayList<ICodeServiceListener>();

	void addCodeServiceListener(ICodeServiceListener iCodeServiceListener) {
		iCodeServiceListeners.add(iCodeServiceListener);
	}

	void removeCodeServiceListener(ICodeServiceListener iCodeServiceListener) {
		iCodeServiceListeners.remove(iCodeServiceListeners);
	}

	void codeCreated(ICode code) {
		for (ICodeServiceListener iCodeServiceListener : iCodeServiceListeners) {
			iCodeServiceListener.codeAdded(code);
		}
	}

	void codeAssigned(ICode code, List<ICodeable> codeables) {
		for (ICodeServiceListener iCodeServiceListener : iCodeServiceListeners) {
			iCodeServiceListener.codeAssigned(code, codeables);
		}
	}

	void codeRemoved(ICode code, List<ICodeable> codeables) {
		for (ICodeServiceListener iCodeServiceListener : iCodeServiceListeners) {
			iCodeServiceListener.codeRemoved(code, codeables);
		}
	}

	void codeDeleted(ICode code) {
		for (ICodeServiceListener iCodeServiceListener : iCodeServiceListeners) {
			iCodeServiceListener.codeDeleted(code);
		}
	}
}
