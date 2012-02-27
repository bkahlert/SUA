package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICodeInstanceID;

public interface ICodeInstance {
	public ICode getCode();

	public ICodeInstanceID getId();

	public TimeZoneDate getCreation();
}
