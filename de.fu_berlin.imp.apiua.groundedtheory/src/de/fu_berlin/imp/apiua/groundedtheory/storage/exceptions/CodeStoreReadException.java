package de.fu_berlin.imp.apiua.groundedtheory.storage.exceptions;

import java.io.IOException;

public class CodeStoreReadException extends IOException {

	private static final long serialVersionUID = -4142078302194129801L;

	public CodeStoreReadException(Throwable innerThrowable) {
		super(innerThrowable);
	}
}
