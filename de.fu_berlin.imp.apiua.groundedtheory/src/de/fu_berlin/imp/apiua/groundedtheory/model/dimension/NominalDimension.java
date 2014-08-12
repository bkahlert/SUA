package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link IDimension} with nominals.
 * 
 * @author bkahlert
 * 
 */
public class NominalDimension implements IDimension {

	private final List<String> possibleValues;

	public NominalDimension(List<String> possibleValues) {
		this.possibleValues = possibleValues != null ? new ArrayList<String>(
				possibleValues) : new ArrayList<String>();
	}

	public NominalDimension(String... possibleValues) {
		this(possibleValues != null ? Arrays.asList(possibleValues) : null);
	}

	@Override
	public boolean isLegal(String value) {
		return value == null || this.possibleValues.contains(value);
	}

	public List<String> getPossibleValues() {
		return new ArrayList<String>(this.possibleValues);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.possibleValues == null) ? 0 : this.possibleValues
						.hashCode());
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
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		NominalDimension other = (NominalDimension) obj;
		if (this.possibleValues == null) {
			if (other.possibleValues != null) {
				return false;
			}
		} else if (!this.possibleValues.equals(other.possibleValues)) {
			return false;
		}
		return true;
	}

}
