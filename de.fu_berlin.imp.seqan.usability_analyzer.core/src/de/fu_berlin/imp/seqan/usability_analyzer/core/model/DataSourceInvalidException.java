package de.fu_berlin.imp.seqan.usability_analyzer.core.model;

import java.io.UnsupportedEncodingException;

public class DataSourceInvalidException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataSourceInvalidException(String string) {
		super(string);
	}

	public DataSourceInvalidException(String string,
			UnsupportedEncodingException e) {
		super(string, e);
	}

}
