package de.fu_berlin.imp.seqan.usability_analyzer.groundedtheory.storage.exceptions;

import java.io.IOException;

public class CodeStoreWriteException extends IOException {

	private static final long serialVersionUID = -4142078302194129801L;

	public CodeStoreWriteException(Throwable innerThrowable) {
		super(innerThrowable);
	}

	public CodeStoreWriteException(String description) {
		super(description);
	}
}
