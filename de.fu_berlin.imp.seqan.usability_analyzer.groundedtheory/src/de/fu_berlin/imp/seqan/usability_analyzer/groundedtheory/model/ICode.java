package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;

public interface ICode {
	public long getId();

	public String getCaption();

	public void setCaption(String newCaption);

	public TimeZoneDate getCreation();

	public RGB getColor();

	public void setColor(RGB rgb);
}
