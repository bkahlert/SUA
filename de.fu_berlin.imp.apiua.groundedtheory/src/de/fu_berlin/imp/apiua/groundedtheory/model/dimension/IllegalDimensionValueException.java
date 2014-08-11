package de.fu_berlin.imp.apiua.groundedtheory.model.dimension;

public class IllegalDimensionValueException extends DimensionException {
	private static final long serialVersionUID = 1L;

	public IllegalDimensionValueException(IDimension dimension, String value) {
		super("The value " + value + " is incompatible with the dimension "
				+ dimension);
	}
}
