package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.util.Set;

public interface ICode {
	public long getId();

	public String getCaption();

	public void setCaption(String newCaption);

	public Set<ICode> getChildCodes();

	public void addChildCode(ICode code);

	public void removeChildCode(ICode code);
}
