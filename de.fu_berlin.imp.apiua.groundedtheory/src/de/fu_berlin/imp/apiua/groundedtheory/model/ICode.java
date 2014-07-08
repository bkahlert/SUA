package de.fu_berlin.imp.apiua.groundedtheory.model;

import com.bkahlert.nebula.utils.colors.RGB;

import de.fu_berlin.imp.apiua.core.model.ILocatable;
import de.fu_berlin.imp.apiua.core.model.TimeZoneDate;

public interface ICode extends ILocatable {
	public long getId();

	public String getCaption();

	public void setCaption(String newCaption);

	public TimeZoneDate getCreation();

	public RGB getColor();

	public void setColor(RGB rgb);
}
