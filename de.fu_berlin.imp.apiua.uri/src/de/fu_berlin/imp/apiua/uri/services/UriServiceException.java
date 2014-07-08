package de.fu_berlin.imp.apiua.uri.services;

import java.io.IOException;

public class UriServiceException extends IOException {

	private static final long serialVersionUID = -4141078302194129801L;

	public UriServiceException(Throwable innerThrowable) {
		super(innerThrowable);
	}

	public UriServiceException(String string) {
		super(string);
	}
}
