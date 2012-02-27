package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public class CodeServiceAdapter implements CodeServiceListener {

	@Override
	public void codeAdded(ICode code) {
	}

	@Override
	public void codeAssigned(ICode code, List<ICodeable> codeables) {
	}

	@Override
	public void codeRemoved(ICode code, List<ICodeable> codeables) {
	}

	@Override
	public void codeDeleted(ICode code) {
	}

}
