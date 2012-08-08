package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.services;

import java.io.IOException;

public class CodeServiceException extends IOException {

	private static final long serialVersionUID = -4141078302194129801L;

	public CodeServiceException(Throwable innerThrowable) {
		super(innerThrowable);
	}

	public CodeServiceException(String string) {
		super(string);
	}
}
