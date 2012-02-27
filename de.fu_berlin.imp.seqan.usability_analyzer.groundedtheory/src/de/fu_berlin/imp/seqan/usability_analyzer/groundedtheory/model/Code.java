package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.model;

import java.util.List;

import de.fu_berlin.imp.seqan.usability_analyzer.core.model.ID;

public class Code implements ICode {

	/**
	 * Calculates a unique {@link ID} based on given {@link ID}s
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
	private final String caption;

	public Code(long id, String caption) {
		this.id = id;
		this.caption = caption;
	}

	public long getId() {
		return id;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public String toString() {
		return caption;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ICode))
			return false;
		ICode other = (ICode) obj;
		if (id != other.getId())
			return false;
		return true;
	}

}
