package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.impl;

import java.util.ArrayList;
import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services.ICodeServiceListener2;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.ICodeInstance;

public class CodeServiceListenerNotifier {
	private List<ICodeServiceListener> codeServiceListeners = new ArrayList<ICodeServiceListener>();

	void addCodeServiceListener(ICodeServiceListener codeServiceListener) {
		codeServiceListeners.add(codeServiceListener);
	}

	void removeCodeServiceListener(ICodeServiceListener iCodeServiceListener) {
		codeServiceListeners.remove(codeServiceListeners);
	}

	void codeCreated(ICode code) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeAdded(code);
		}
	}

	void codeAssigned(ICode code, List<ICodeable> codeables) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeAssigned(code, codeables);
		}
	}

	public void codeRenamed(ICode code, String oldCaption, String newCaption) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeRenamed(code, oldCaption, newCaption);
		}
	}

	void codeRemoved(ICode code, List<ICodeable> codeables) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeRemoved(code, codeables);
		}
	}

	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeMoved(code, oldParentCode, newParentCode);
		}
	}

	void codeDeleted(ICode code) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.codeDeleted(code);
		}
	}

	void memoModified(ICode code, String html) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.memoModified(code);
		}
	}

	void memoModified(ICodeInstance codeInstance, String html) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			if (codeServiceListener instanceof ICodeServiceListener2)
				((ICodeServiceListener2) codeServiceListener)
						.memoModified(codeInstance);
		}
	}

	void memoModified(ICodeable codeable, String html) {
		for (ICodeServiceListener codeServiceListener : codeServiceListeners) {
			codeServiceListener.memoModified(codeable);
		}
	}

}
