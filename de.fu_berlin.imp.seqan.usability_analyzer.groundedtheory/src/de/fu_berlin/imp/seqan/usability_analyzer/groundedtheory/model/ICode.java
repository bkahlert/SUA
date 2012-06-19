package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;

public interface ICode {
	public long getId();

	public String getCaption();

	public void setCaption(String newCaption);

	public TimeZoneDate getCreation();
}
