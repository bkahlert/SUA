package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage;

import java.net.URI;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model.ICode;

public interface ICodeInstance {
	public ICode getCode();

	public URI getId();

	public TimeZoneDate getCreation();
}
