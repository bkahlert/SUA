package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.util.List;

import com.bkahlert.devel.nebula.colors.RGB;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.TimeZoneDate;
import de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.ui.Utils;

public class Code implements ICode {

	/**
	 * Calculates a unique {@link IIdentifier} based on given
	 * {@link IIdentifier}s
	 * 
	 * @param existingIds
	 * @return
	 */
	public static long calculateId(List<Long> existingIds) {
		long id = Long.MIN_VALUE;
		for (Long existingId : existingIds) {
			id = Math.max(id, existingId + 1);
		}
		return id;
	}

	private final long id;
	private String caption;
	private RGB color;
	private TimeZoneDate creation;

	public Code(long id, String caption, RGB color, TimeZoneDate creation) {
		this.id = id;
		this.caption = caption;
		this.color = color;
		this.creation = creation;
	}

	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public String getCaption() {
		return this.caption;
	}

	@Override
	public void setCaption(String newCaption) {
		this.caption = newCaption;
	}

	@Override
	public RGB getColor() {
		return this.color != null ? this.color : Utils.getFancyCodeColor();
	}

	@Override
	public void setColor(RGB rgb) {
		this.color = rgb;
	}

	@Override
	public TimeZoneDate getCreation() {
		return this.creation;
	}

	@Override
	public String toString() {
		return this.caption;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.id ^ (this.id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ICode)) {
			return false;
		}
		ICode other = (ICode) obj;
		if (this.id != other.getId()) {
			return false;
		}
		return true;
	}

}
