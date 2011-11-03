package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

public class DataSourceInvalidException extends Exception {

	private static final long serialVersionUID = 2785689409057390667L;

	public DataSourceInvalidException(String message) {
		super(message);
	}

	public DataSourceInvalidException(String message, Throwable innerException) {
		super(message, innerException);
	}
}
