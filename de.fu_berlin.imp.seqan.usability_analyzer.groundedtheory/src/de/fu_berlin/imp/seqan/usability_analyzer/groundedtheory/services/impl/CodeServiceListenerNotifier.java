package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.CodeServiceListener;

public class CodeServiceListenerNotifier {
	private List<CodeServiceListener> codeServiceListeners = new ArrayList<CodeServiceListener>();

	void addCodeServiceListener(CodeServiceListener codeServiceListener) {
		codeServiceListeners.add(codeServiceListener);
	}

	void removeCodeServiceListener(CodeServiceListener codeServiceListener) {
		codeServiceListeners.remove(codeServiceListeners);
	}

	void codeCreated(ICode code) {
		for (CodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeAdded(code);
		}
	}

	void codeAssigned(ICode code, List<ICodeable> codeables) {
		for (CodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeAssigned(code, codeables);
		}
	}

	void codeRemoved(ICode code, List<ICodeable> codeables) {
		for (CodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeRemoved(code, codeables);
		}
	}

	void codeDeleted(ICode code) {
		for (CodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeDeleted(code);
		}
	}
}
