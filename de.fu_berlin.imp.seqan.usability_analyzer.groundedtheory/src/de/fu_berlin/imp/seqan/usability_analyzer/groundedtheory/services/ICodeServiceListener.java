package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeable;

public interface ICodeServiceListener {
	public void codeAdded(ICode code);

	public void codeAssigned(ICode code, List<ICodeable> codeables);

	public void codeRenamed(ICode code, String oldCaption, String newCaption);

	public void codeRemoved(ICode code, List<ICodeable> codeables);

	public void codeMoved(ICode code, ICode oldParentCode, ICode newParentCode);

	public void codeDeleted(ICode code);

	public void memoModified(ICode code);

	public void memoModified(ICodeable codeable);
}
