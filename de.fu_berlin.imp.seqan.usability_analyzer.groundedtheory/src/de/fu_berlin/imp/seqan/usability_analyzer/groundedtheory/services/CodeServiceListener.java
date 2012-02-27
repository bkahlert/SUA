package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public interface CodeServiceListener {
	public void codeAdded(ICode code);

	public void codeAssigned(ICode code, List<ICodeable> codeables);

	public void codeRemoved(ICode code, List<ICodeable> codeables);

	public void codeDeleted(ICode code);
}
